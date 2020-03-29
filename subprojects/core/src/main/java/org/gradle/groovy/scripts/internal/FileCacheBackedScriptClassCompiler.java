/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.groovy.scripts.internal;

import com.google.common.io.Files;
import groovy.lang.Script;
import org.codehaus.groovy.ast.ClassNode;
import org.gradle.api.Action;
import org.gradle.api.internal.initialization.ClassLoaderScope;
import org.gradle.api.internal.initialization.loadercache.ClassLoaderCache;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.PersistentCache;
import org.gradle.groovy.scripts.ScriptSource;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.classanalysis.AsmConstants;
import org.gradle.internal.hash.ClassLoaderHierarchyHasher;
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.hash.HashUtil;
import org.gradle.internal.hash.Hashing;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.model.dsl.internal.transform.RuleVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * A {@link ScriptClassCompiler} which compiles scripts to a cache directory, and loads them from there.
 */
public class FileCacheBackedScriptClassCompiler implements ScriptClassCompiler, Closeable {
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final ScriptCompilationHandler scriptCompilationHandler;
    private final ProgressLoggerFactory progressLoggerFactory;
    private final CacheRepository cacheRepository;
    private final ClassLoaderCache classLoaderCache;
    private final ClassLoaderHierarchyHasher classLoaderHierarchyHasher;

    public FileCacheBackedScriptClassCompiler(CacheRepository cacheRepository, ScriptCompilationHandler scriptCompilationHandler,
                                              ProgressLoggerFactory progressLoggerFactory, ClassLoaderCache classLoaderCache,
                                              ClassLoaderHierarchyHasher classLoaderHierarchyHasher) {
        this.cacheRepository = cacheRepository;
        this.scriptCompilationHandler = scriptCompilationHandler;
        this.progressLoggerFactory = progressLoggerFactory;
        this.classLoaderCache = classLoaderCache;
        this.classLoaderHierarchyHasher = classLoaderHierarchyHasher;
    }

    @Override
    public <T extends Script, M> CompiledScript<T, M> compile(final ScriptSource source,
                                                              final ClassLoaderScope targetScope,
                                                              final CompileOperation<M> operation,
                                                              final Class<T> scriptBaseClass,
                                                              final Action<? super ClassNode> verifier) {
        assert source.getResource().isContentCached();
        if (source.getResource().getHasEmptyContent()) {
            return emptyCompiledScript(operation);
        }

        ClassLoader classLoader = targetScope.getExportClassLoader();
        HashCode sourceHashCode = source.getResource().getContentHash();
        final String sourceHash = HashUtil.compactStringFor(sourceHashCode.toByteArray());
        final String dslId = operation.getId();
        HashCode classLoaderHash = classLoaderHierarchyHasher.getClassLoaderHash(classLoader);
        if (classLoaderHash == null) {
            throw new IllegalArgumentException("Unknown classloader: " + classLoader);
        }
        final String classpathHash = dslId + classLoaderHash;
        final RemappingScriptSource remapped = new RemappingScriptSource(source);

        // Caching involves 2 distinct caches, so that 2 scripts with the same (hash, classpath) do not get compiled twice
        // 1. First, we look for a cache script which (path, hash) matches. This cache is invalidated when the compile classpath of the script changes
        // 2. Then we look into the 2d cache for a "generic script" with the same hash, that will be remapped to the script class name
        // Both caches can be closed directly after use because:
        // For 1, if the script changes or its compile classpath changes, a different directory will be used
        // For 2, if the script changes, a different cache is used. If the classpath changes, the cache is invalidated, but classes are remapped to 1. anyway so never directly used
        PersistentCache remappedClassesCache = cacheRepository.cache("scripts-remapped/" + source.getClassName() + "/" + sourceHash + "/" + classpathHash)
            .withDisplayName(dslId + " remapped class cache for " + sourceHash)
            .withInitializer(new ProgressReportingInitializer(progressLoggerFactory, new RemapBuildScriptsAction<M, T>(remapped, classpathHash, sourceHash, dslId, classLoader, operation, verifier, scriptBaseClass),
                "Compiling script into cache",
                "Compiling " + source.getFileName() + " into local compilation cache"))
            .open();
        try {
            File remappedClassesDir = classesDir(remappedClassesCache);
            File remappedMetadataDir = metadataDir(remappedClassesCache);

            return scriptCompilationHandler.loadFromDir(source, sourceHashCode, targetScope, remappedClassesDir, remappedMetadataDir, operation, scriptBaseClass);
        } finally {
            remappedClassesCache.close();
        }
    }

    private <T extends Script, M> CompiledScript<T, M> emptyCompiledScript(CompileOperation<M> operation) {
        return new EmptyCompiledScript<T, M>(operation);
    }

    @Override
    public void close() {
    }

    private File classesDir(PersistentCache cache) {
        return new File(cache.getBaseDir(), "classes");
    }

    private File metadataDir(PersistentCache cache) {
        return new File(cache.getBaseDir(), "metadata");
    }

    private class CompileToCrossBuildCacheAction implements Action<PersistentCache> {
        private final Action<? super ClassNode> verifier;
        private final Class<? extends Script> scriptBaseClass;
        private final ClassLoader classLoader;
        private final CompileOperation<?> transformer;
        private final ScriptSource source;

        public <T extends Script> CompileToCrossBuildCacheAction(ScriptSource source, ClassLoader classLoader, CompileOperation<?> transformer,
                                                                 Action<? super ClassNode> verifier, Class<T> scriptBaseClass) {
            this.source = source;
            this.classLoader = classLoader;
            this.transformer = transformer;
            this.verifier = verifier;
            this.scriptBaseClass = scriptBaseClass;
        }

        @Override
        public void execute(PersistentCache cache) {
            File classesDir = classesDir(cache);
            File metadataDir = metadataDir(cache);
            scriptCompilationHandler.compileToDir(source, classLoader, classesDir, metadataDir, transformer, scriptBaseClass, verifier);
        }
    }

    static class ProgressReportingInitializer implements Action<PersistentCache> {
        private ProgressLoggerFactory progressLoggerFactory;
        private Action<? super PersistentCache> delegate;
        private final String shortDescription;
        private final String longDescription;

        public ProgressReportingInitializer(ProgressLoggerFactory progressLoggerFactory,
                                            Action<PersistentCache> delegate,
                                            String shortDescription,
                                            String longDescription) {
            this.progressLoggerFactory = progressLoggerFactory;
            this.delegate = delegate;
            this.shortDescription = shortDescription;
            this.longDescription = longDescription;
        }

        @Override
        public void execute(PersistentCache cache) {
            ProgressLogger op = progressLoggerFactory.newOperation(FileCacheBackedScriptClassCompiler.class)
                .start(shortDescription, longDescription);
            try {
                delegate.execute(cache);
            } finally {
                op.completed();
            }
        }
    }

    private static class EmptyCompiledScript<T extends Script, M> implements CompiledScript<T, M> {
        private final M data;

        public EmptyCompiledScript(CompileOperation<M> operation) {
            this.data = operation.getExtractedData();
        }

        @Override
        public boolean getRunDoesSomething() {
            return false;
        }

        @Override
        public boolean getHasMethods() {
            return false;
        }

        @Override
        public void onReuse() {
            // Ignore
        }

        @Override
        public Class<? extends T> loadClass() {
            throw new UnsupportedOperationException("Cannot load a script that does nothing.");
        }

        @Override
        public M getData() {
            return data;
        }
    }

    private static class BuildScriptRemapper extends ClassVisitor implements Opcodes {
        private static final String SCRIPT_ORIGIN = "org/gradle/internal/scripts/ScriptOrigin";
        private final ScriptSource scriptSource;
        private final String originalClassName;
        private final String contentHash;

        public BuildScriptRemapper(ClassVisitor cv, ScriptSource source, String originalClassName, String contentHash) {
            super(AsmConstants.ASM_LEVEL, cv);
            this.scriptSource = source;
            this.originalClassName = originalClassName;
            this.contentHash = contentHash;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            String owner = remap(name);
            boolean shouldAddScriptOrigin = shouldAddScriptOrigin(access);
            cv.visit(version, access, owner, remap(signature), remap(superName), remapAndAddInterfaces(interfaces, shouldAddScriptOrigin));
            if (shouldAddScriptOrigin) {
                addOriginalClassName(cv, owner, originalClassName);
                addContentHash(cv, owner, contentHash);
            }
        }

        private static boolean shouldAddScriptOrigin(int access) {
            return ((access & ACC_INTERFACE) == 0) && ((access & ACC_ANNOTATION) == 0);
        }

        private static void addOriginalClassName(ClassVisitor cv, String owner, String originalClassName) {
            cv.visitField(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC | ACC_FINAL, "__originalClassName", Type.getDescriptor(String.class), "", originalClassName);
            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "getOriginalClassName", Type.getMethodDescriptor(Type.getType(String.class)), null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, owner, "__originalClassName", Type.getDescriptor(String.class));
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        private static void addContentHash(ClassVisitor cv, String owner, String contentHash) {
            cv.visitField(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC | ACC_FINAL, "__signature", Type.getDescriptor(String.class), "", contentHash);
            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "getContentHash", Type.getMethodDescriptor(Type.getType(String.class)), null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, owner, "__signature", Type.getDescriptor(String.class));
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        @Override
        public void visitSource(String source, String debug) {
            cv.visitSource(scriptSource.getFileName(), debug);
        }

        private String[] remapAndAddInterfaces(String[] interfaces, boolean shouldAddScriptOrigin) {
            if (!shouldAddScriptOrigin) {
                return remap(interfaces);
            }
            if (interfaces == null) {
                return new String[]{SCRIPT_ORIGIN};
            }
            String[] remapped = new String[interfaces.length + 1];
            for (int i = 0; i < interfaces.length; i++) {
                remapped[i] = remap(interfaces[i]);
            }
            remapped[remapped.length - 1] = SCRIPT_ORIGIN;
            return remapped;
        }

        private String[] remap(String[] names) {
            if (names == null) {
                return null;
            }
            String[] remapped = new String[names.length];
            for (int i = 0; i < names.length; i++) {
                remapped[i] = remap(names[i]);
            }
            return remapped;
        }

        private String remap(String name) {
            if (name == null) {
                return null;
            }
            if (RuleVisitor.SOURCE_URI_TOKEN.equals(name)) {
                URI uri = scriptSource.getResource().getLocation().getURI();
                return uri == null ? null : uri.toString();
            }
            if (RuleVisitor.SOURCE_DESC_TOKEN.equals(name)) {
                return scriptSource.getDisplayName();
            }
            return name.replaceAll(RemappingScriptSource.MAPPED_SCRIPT, scriptSource.getClassName());
        }

        private Object remap(Object o) {
            if (o instanceof Type) {
                return Type.getType(remap(((Type) o).getDescriptor()));
            }
            if (o instanceof String) {
                return remap((String) o);
            }
            return o;
        }

        private Object[] remap(int count, Object[] original) {
            if (count == 0) {
                return EMPTY_OBJECT_ARRAY;
            }
            Object[] remapped = new Object[count];
            for (int idx = 0; idx < count; idx++) {
                remapped[idx] = remap(original[idx]);
            }
            return remapped;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, remap(desc), remap(signature), remap(exceptions));
            if (mv != null && (access & ACC_ABSTRACT) == 0) {
                mv = new MethodRenamer(mv);
            }
            return mv;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return super.visitField(access, name, remap(desc), remap(signature), remap(value));
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(remap(name), remap(outerName), remap(innerName), access);
        }

        @Override
        public void visitOuterClass(String owner, String name, String desc) {
            super.visitOuterClass(remap(owner), remap(name), remap(desc));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return super.visitAnnotation(remap(desc), visible);
        }

        class MethodRenamer extends MethodVisitor {

            public MethodRenamer(final MethodVisitor mv) {
                super(AsmConstants.ASM_LEVEL, mv);
            }

            @Override
            public void visitTypeInsn(int i, String name) {
                mv.visitTypeInsn(i, remap(name));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                mv.visitFieldInsn(opcode, remap(owner), name, remap(desc));
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean intf) {
                mv.visitMethodInsn(opcode, remap(owner), name, remap(desc), intf);
            }

            @Override
            public void visitLdcInsn(Object cst) {
                super.visitLdcInsn(remap(cst));
            }

            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                super.visitLocalVariable(name, remap(desc), remap(signature), start, end, index);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return super.visitAnnotation(remap(desc), visible);
            }

            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                super.visitFrame(type, nLocal, remap(nLocal, local), nStack, remap(nStack, stack));
            }
        }
    }

    private class RemapBuildScriptsAction<M, T extends Script> implements Action<PersistentCache> {
        private final String classpathHash;
        private final String sourceHash;
        private final String dslId;
        private final ScriptSource source;
        private final RemappingScriptSource remapped;
        private final ClassLoader classLoader;
        private final CompileOperation<M> operation;
        private final Action<? super ClassNode> verifier;
        private final Class<T> scriptBaseClass;

        public RemapBuildScriptsAction(RemappingScriptSource remapped, String classpathHash, String sourceHash, String dslId, ClassLoader classLoader, CompileOperation<M> operation, Action<? super ClassNode> verifier, Class<T> scriptBaseClass) {
            this.classpathHash = classpathHash;
            this.sourceHash = sourceHash;
            this.dslId = dslId;
            this.remapped = remapped;
            this.source = remapped.getSource();
            this.classLoader = classLoader;
            this.operation = operation;
            this.verifier = verifier;
            this.scriptBaseClass = scriptBaseClass;
        }

        @Override
        public void execute(final PersistentCache remappedClassesCache) {
            final PersistentCache cache = cacheRepository.cache("scripts/" + sourceHash + "/" + dslId + "/" + classpathHash)
                .withDisplayName(dslId + " generic class cache for " + source.getDisplayName())
                .withInitializer(new ProgressReportingInitializer(
                    progressLoggerFactory,
                    new CompileToCrossBuildCacheAction(remapped, classLoader, operation, verifier, scriptBaseClass),
                    "Compiling script into cache",
                    "Compiling " + source.getDisplayName() + " to cross build script cache"))
                .open();
            try {
                final File genericClassesDir = classesDir(cache);
                final File metadataDir = metadataDir(cache);
                remapClasses(genericClassesDir, classesDir(remappedClassesCache), remapped);
                copyMetadata(metadataDir, metadataDir(remappedClassesCache));
            } finally {
                cache.close();
            }
        }

        private void remapClasses(File scriptCacheDir, File relocalizedDir, RemappingScriptSource source) {
            ScriptSource origin = source.getSource();
            String className = origin.getClassName();
            if (!relocalizedDir.exists()) {
                relocalizedDir.mkdir();
            }
            File[] files = scriptCacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String renamed = file.getName();
                    if (renamed.startsWith(RemappingScriptSource.MAPPED_SCRIPT)) {
                        renamed = className + renamed.substring(RemappingScriptSource.MAPPED_SCRIPT.length());
                    }
                    ClassWriter cv = new ClassWriter(0);
                    try {
                        byte[] contents = Files.toByteArray(file);
                        ClassReader cr = new ClassReader(contents);
                        String originalClassName = cr.getClassName();
                        String contentHash = Hashing.hashBytes(contents).toString();
                        BuildScriptRemapper remapper = new BuildScriptRemapper(cv, origin, originalClassName, contentHash);
                        cr.accept(remapper, 0);
                        Files.write(cv.toByteArray(), new File(relocalizedDir, renamed));
                    } catch (IOException ex) {
                        throw UncheckedException.throwAsUncheckedException(ex);
                    }
                }
            }
        }

        private void copyMetadata(File source, File dest) {
            if (dest.mkdir()) {
                for (File src : source.listFiles()) {
                    try {
                        Files.copy(src, new File(dest, src.getName()));
                    } catch (IOException ex) {
                        throw UncheckedException.throwAsUncheckedException(ex);
                    }
                }
            }
        }
    }
}
