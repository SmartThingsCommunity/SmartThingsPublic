/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.api.internal.file;

import org.gradle.api.internal.cache.StringInterner;
import org.gradle.api.internal.file.collections.DefaultDirectoryFileTreeFactory;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.provider.PropertyHost;
import org.gradle.api.internal.resources.ApiTextResourceAdapter;
import org.gradle.api.internal.resources.DefaultResourceHandler;
import org.gradle.api.internal.tasks.DefaultTaskDependencyFactory;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.api.tasks.util.internal.PatternSets;
import org.gradle.internal.Factory;
import org.gradle.internal.concurrent.DefaultExecutorFactory;
import org.gradle.internal.file.Deleter;
import org.gradle.internal.file.PathToFileResolver;
import org.gradle.internal.file.impl.DefaultDeleter;
import org.gradle.internal.fingerprint.GenericFileTreeSnapshotter;
import org.gradle.internal.fingerprint.impl.DefaultFileCollectionSnapshotter;
import org.gradle.internal.fingerprint.impl.DefaultGenericFileTreeSnapshotter;
import org.gradle.internal.hash.DefaultFileHasher;
import org.gradle.internal.hash.DefaultStreamHasher;
import org.gradle.internal.nativeintegration.filesystem.FileSystem;
import org.gradle.internal.resource.local.FileResourceConnector;
import org.gradle.internal.resource.local.FileResourceRepository;
import org.gradle.internal.time.Time;
import org.gradle.internal.vfs.VirtualFileSystem;
import org.gradle.internal.vfs.impl.DefaultVirtualFileSystem;
import org.gradle.process.internal.DefaultExecActionFactory;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.ExecFactory;
import org.gradle.process.internal.ExecHandleFactory;
import org.gradle.process.internal.JavaExecHandleFactory;
import org.gradle.testfixtures.internal.NativeServicesTestFixture;
import org.gradle.util.TestUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static org.gradle.internal.snapshot.CaseSensitivity.CASE_INSENSITIVE;
import static org.gradle.internal.snapshot.CaseSensitivity.CASE_SENSITIVE;
import static org.gradle.util.TestUtil.objectFactory;

public class TestFiles {
    private static final FileSystem FILE_SYSTEM = NativeServicesTestFixture.getInstance().get(FileSystem.class);
    private static final DefaultFileLookup FILE_LOOKUP = new DefaultFileLookup();
    private static final DefaultExecActionFactory EXEC_FACTORY = DefaultExecActionFactory.of(resolver(), fileCollectionFactory(), new DefaultExecutorFactory());

    public static FileCollectionInternal empty() {
        return fileCollectionFactory().empty();
    }

    public static FileCollectionInternal fixed(File... files) {
        return fileCollectionFactory().fixed(files);
    }

    public static FileCollectionInternal fixed(List<File> files) {
        return fileCollectionFactory().fixed(files);
    }

    public static FileLookup fileLookup() {
        return FILE_LOOKUP;
    }

    public static FileSystem fileSystem() {
        return FILE_SYSTEM;
    }

    public static FileResourceRepository fileRepository() {
        return new FileResourceConnector(FILE_SYSTEM);
    }

    /**
     * Returns a resolver with no base directory.
     */
    public static FileResolver resolver() {
        return FILE_LOOKUP.getFileResolver();
    }

    /**
     * Returns a resolver with no base directory.
     */
    public static PathToFileResolver pathToFileResolver() {
        return FILE_LOOKUP.getPathToFileResolver();
    }

    /**
     * Returns a resolver with the given base directory.
     */
    public static FileResolver resolver(File baseDir) {
        return FILE_LOOKUP.getFileResolver(baseDir);
    }

    /**
     * Returns a resolver with the given base directory.
     */
    public static PathToFileResolver pathToFileResolver(File baseDir) {
        return FILE_LOOKUP.getPathToFileResolver(baseDir);
    }

    public static DirectoryFileTreeFactory directoryFileTreeFactory() {
        return new DefaultDirectoryFileTreeFactory(getPatternSetFactory(), fileSystem());
    }

    public static Deleter deleter() {
        return new DefaultDeleter(Time.clock()::getCurrentTime, fileSystem()::isSymlink, false);
    }

    public static FilePropertyFactory filePropertyFactory() {
        return new DefaultFilePropertyFactory(PropertyHost.NO_OP, resolver(), fileCollectionFactory());
    }

    public static FilePropertyFactory filePropertyFactory(File baseDir) {
        return new DefaultFilePropertyFactory(PropertyHost.NO_OP, resolver(baseDir), fileCollectionFactory(baseDir));
    }

    public static FileFactory fileFactory() {
        return new DefaultFilePropertyFactory(PropertyHost.NO_OP, resolver(), fileCollectionFactory());
    }

    public static FileOperations fileOperations(File basedDir) {
        return fileOperations(basedDir, null);
    }

    public static FileOperations fileOperations(File basedDir, @Nullable TemporaryFileProvider temporaryFileProvider) {
        FileResolver fileResolver = resolver(basedDir);
        FileSystem fileSystem = fileSystem();

        DefaultResourceHandler.Factory resourceHandlerFactory = DefaultResourceHandler.Factory.from(
            fileResolver,
            fileSystem,
            temporaryFileProvider,
            textResourceAdapterFactory(temporaryFileProvider)
        );

        return new DefaultFileOperations(
            fileResolver,
            temporaryFileProvider,
            TestUtil.instantiatorFactory().inject(),
            directoryFileTreeFactory(),
            streamHasher(),
            fileHasher(),
            resourceHandlerFactory,
            fileCollectionFactory(basedDir),
            fileSystem,
            getPatternSetFactory(),
            deleter()
        );
    }

    public static ApiTextResourceAdapter.Factory textResourceAdapterFactory(@Nullable TemporaryFileProvider temporaryFileProvider) {
        return new ApiTextResourceAdapter.Factory(
            __ -> {
                throw new IllegalStateException("Can't create TextUriResourceLoader");
            },
            temporaryFileProvider
        );
    }

    public static DefaultStreamHasher streamHasher() {
        return new DefaultStreamHasher();
    }

    public static DefaultFileHasher fileHasher() {
        return new DefaultFileHasher(streamHasher());
    }

    public static GenericFileTreeSnapshotter genericFileTreeSnapshotter() {
        return new DefaultGenericFileTreeSnapshotter(fileHasher(), new StringInterner());
    }

    public static DefaultFileCollectionSnapshotter fileCollectionSnapshotter() {
        return new DefaultFileCollectionSnapshotter(virtualFileSystem(), genericFileTreeSnapshotter(), fileSystem());
    }

    public static VirtualFileSystem virtualFileSystem() {
        return new DefaultVirtualFileSystem(fileHasher(), new StringInterner(), fileSystem(), fileSystem().isCaseSensitive() ? CASE_SENSITIVE : CASE_INSENSITIVE);
    }

    public static FileCollectionFactory fileCollectionFactory() {
        return new DefaultFileCollectionFactory(pathToFileResolver(), DefaultTaskDependencyFactory.withNoAssociatedProject(), directoryFileTreeFactory(), getPatternSetFactory(), PropertyHost.NO_OP, fileSystem());
    }

    public static FileCollectionFactory fileCollectionFactory(File baseDir) {
        return new DefaultFileCollectionFactory(pathToFileResolver(baseDir), DefaultTaskDependencyFactory.withNoAssociatedProject(), directoryFileTreeFactory(), getPatternSetFactory(), PropertyHost.NO_OP, fileSystem());
    }

    public static FileCollectionFactory fileCollectionFactory(File baseDir, TaskDependencyFactory taskDependencyFactory) {
        return new DefaultFileCollectionFactory(pathToFileResolver(baseDir), taskDependencyFactory, directoryFileTreeFactory(), getPatternSetFactory(), PropertyHost.NO_OP, fileSystem());
    }

    public static ExecFactory execFactory() {
        return EXEC_FACTORY;
    }

    public static ExecFactory execFactory(File baseDir) {
        return execFactory().forContext(resolver(baseDir), fileCollectionFactory(baseDir), TestUtil.instantiatorFactory().inject(), objectFactory());
    }

    public static ExecActionFactory execActionFactory() {
        return execFactory();
    }

    public static ExecHandleFactory execHandleFactory() {
        return execFactory();
    }

    public static ExecHandleFactory execHandleFactory(File baseDir) {
        return execFactory(baseDir);
    }

    public static JavaExecHandleFactory javaExecHandleFactory(File baseDir) {
        return execFactory(baseDir);
    }

    public static Factory<PatternSet> getPatternSetFactory() {
        return PatternSets.getNonCachingPatternSetFactory();
    }

    public static String systemSpecificAbsolutePath(String path) {
        return new File(path).getAbsolutePath();
    }
}
