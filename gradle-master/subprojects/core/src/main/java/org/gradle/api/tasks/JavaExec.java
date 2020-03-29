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

package org.gradle.api.tasks;

import org.apache.tools.ant.types.Commandline;
import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.jpms.ModularClasspathHandling;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.options.Option;
import org.gradle.internal.jpms.DefaultModularClasspathHandling;
import org.gradle.internal.jpms.JavaModuleDetector;
import org.gradle.internal.jvm.inspection.JvmVersionDetector;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaDebugOptions;
import org.gradle.process.JavaExecSpec;
import org.gradle.process.JavaForkOptions;
import org.gradle.process.ProcessForkOptions;
import org.gradle.process.internal.DslExecActionFactory;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.JavaExecAction;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Executes a Java application in a child process.
 * <p>
 * Similar to {@link org.gradle.api.tasks.Exec}, but starts a JVM with the given classpath and application class.
 * </p>
 * <pre class='autoTested'>
 * apply plugin: 'java'
 *
 * task runApp(type: JavaExec) {
 *   classpath = sourceSets.main.runtimeClasspath
 *
 *   main = 'package.Main'
 *
 *   // arguments to pass to the application
 *   args 'appArg1'
 * }
 *
 * // Using and creating an Executable Jar
 * jar {
 *   manifest {
 *     attributes('Main-Class': 'package.Main')
 *   }
 * }
 *
 * task runExecutableJar(type: JavaExec) {
 *   // Executable jars can have only _one_ jar on the classpath.
 *   classpath = files(tasks.jar)
 *
 *   // 'main' does not need to be specified
 *
 *   // arguments to pass to the application
 *   args 'appArg1'
 * }
 *
 * </pre>
 * <p>
 * The process can be started in debug mode (see {@link #getDebug()}) in an ad-hoc manner by supplying the `--debug-jvm` switch when invoking the build.
 * <pre>
 * gradle someJavaExecTask --debug-jvm
 * </pre>
 * <p>
 * Also, debug configuration can be explicitly set in {@link #debugOptions(Action)}:
 * <pre>
 * task runApp(type: JavaExec) {
 *    ...
 *
 *    debugOptions {
 *        enabled = true
 *        port = 5566
 *        server = true
 *        suspend = false
 *    }
 * }
 * </pre>
 */
public class JavaExec extends ConventionTask implements JavaExecSpec {
    private final JavaExecAction javaExecHandleBuilder;
    private final Property<String> mainModule;
    private final Property<String> mainClass;
    private final ConfigurableFileCollection classpath;
    private final ModularClasspathHandling modularClasspathHandling;
    private final Property<ExecResult> execResult;

    public JavaExec() {
        ObjectFactory objectFactory = getObjectFactory();
        this.mainModule = objectFactory.property(String.class);
        this.mainClass = objectFactory.property(String.class);
        this.classpath = getFileCollectionFactory().configurableFiles("classpath");
        this.modularClasspathHandling = objectFactory.newInstance(DefaultModularClasspathHandling.class);
        execResult = getObjectFactory().property(ExecResult.class);

        javaExecHandleBuilder = getDslExecActionFactory().newDecoratedJavaExecAction();
        javaExecHandleBuilder.getMainClass().convention(mainClass);
        javaExecHandleBuilder.getMainModule().convention(mainModule);
        javaExecHandleBuilder.getModularClasspathHandling().getInferModulePath().convention(modularClasspathHandling.getInferModulePath());
        javaExecHandleBuilder.setClasspath(classpath);
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected DslExecActionFactory getDslExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected FileCollectionFactory getFileCollectionFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected JavaModuleDetector getJavaModuleDetector() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void exec() {
        setJvmArgs(getJvmArgs()); // convention mapping for 'jvmArgs'
        execResult.set(javaExecHandleBuilder.execute());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllJvmArgs() {
        return javaExecHandleBuilder.getAllJvmArgs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllJvmArgs(List<String> arguments) {
        javaExecHandleBuilder.setAllJvmArgs(arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllJvmArgs(Iterable<?> arguments) {
        javaExecHandleBuilder.setAllJvmArgs(arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getJvmArgs() {
        return javaExecHandleBuilder.getJvmArgs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJvmArgs(List<String> arguments) {
        javaExecHandleBuilder.setJvmArgs(arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJvmArgs(Iterable<?> arguments) {
        javaExecHandleBuilder.setJvmArgs(arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec jvmArgs(Iterable<?> arguments) {
        javaExecHandleBuilder.jvmArgs(arguments);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec jvmArgs(Object... arguments) {
        javaExecHandleBuilder.jvmArgs(arguments);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getSystemProperties() {
        return javaExecHandleBuilder.getSystemProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSystemProperties(Map<String, ?> properties) {
        javaExecHandleBuilder.setSystemProperties(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec systemProperties(Map<String, ?> properties) {
        javaExecHandleBuilder.systemProperties(properties);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec systemProperty(String name, Object value) {
        javaExecHandleBuilder.systemProperty(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileCollection getBootstrapClasspath() {
        return javaExecHandleBuilder.getBootstrapClasspath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBootstrapClasspath(FileCollection classpath) {
        javaExecHandleBuilder.setBootstrapClasspath(classpath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec bootstrapClasspath(Object... classpath) {
        javaExecHandleBuilder.bootstrapClasspath(classpath);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMinHeapSize() {
        return javaExecHandleBuilder.getMinHeapSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMinHeapSize(String heapSize) {
        javaExecHandleBuilder.setMinHeapSize(heapSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultCharacterEncoding() {
        return javaExecHandleBuilder.getDefaultCharacterEncoding();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultCharacterEncoding(String defaultCharacterEncoding) {
        javaExecHandleBuilder.setDefaultCharacterEncoding(defaultCharacterEncoding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMaxHeapSize() {
        return javaExecHandleBuilder.getMaxHeapSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxHeapSize(String heapSize) {
        javaExecHandleBuilder.setMaxHeapSize(heapSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEnableAssertions() {
        return javaExecHandleBuilder.getEnableAssertions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnableAssertions(boolean enabled) {
        javaExecHandleBuilder.setEnableAssertions(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDebug() {
        return javaExecHandleBuilder.getDebug();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Option(option = "debug-jvm", description = "Enable debugging for the process. The process is started suspended and listening on port 5005.")
    public void setDebug(boolean enabled) {
        javaExecHandleBuilder.setDebug(enabled);
    }

    @Override
    public JavaDebugOptions getDebugOptions() {
        return javaExecHandleBuilder.getDebugOptions();
    }

    @Override
    public void debugOptions(Action<JavaDebugOptions> action) {
        javaExecHandleBuilder.debugOptions(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property<String> getMainModule() {
        return mainModule;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Property<String> getMainClass() {
        return mainClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMain() {
        return getMainClass().getOrNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec setMain(String mainClassName) {
        getMainClass().set(mainClassName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getArgs() {
        return javaExecHandleBuilder.getArgs();
    }

    /**
     * Parses an argument list from {@code args} and passes it to {@link #setArgs(List)}.
     *
     * <p>
     * The parser supports both single quote ({@code '}) and double quote ({@code "}) as quote delimiters.
     * For example, to pass the argument {@code foo bar}, use {@code "foo bar"}.
     * </p>
     * <p>
     * Note: the parser does <strong>not</strong> support using backslash to escape quotes. If this is needed,
     * use the other quote delimiter around it.
     * For example, to pass the argument {@code 'singly quoted'}, use {@code "'singly quoted'"}.
     * </p>
     *
     * @param args Args for the main class. Will be parsed into an argument list.
     * @return this
     * @since 4.9
     */
    @Option(option = "args", description = "Command line arguments passed to the main class.")
    public JavaExec setArgsString(String args) {
        return setArgs(Arrays.asList(Commandline.translateCommandline(args)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec setArgs(List<String> applicationArgs) {
        javaExecHandleBuilder.setArgs(applicationArgs);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec setArgs(Iterable<?> applicationArgs) {
        javaExecHandleBuilder.setArgs(applicationArgs);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec args(Object... args) {
        javaExecHandleBuilder.args(args);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExecSpec args(Iterable<?> args) {
        javaExecHandleBuilder.args(args);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommandLineArgumentProvider> getArgumentProviders() {
        return javaExecHandleBuilder.getArgumentProviders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec setClasspath(FileCollection classpath) {
        this.classpath.setFrom(classpath);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec classpath(Object... paths) {
        classpath.from(paths);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileCollection getClasspath() {
        return classpath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModularClasspathHandling getModularClasspathHandling() {
        return modularClasspathHandling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec copyTo(JavaForkOptions options) {
        javaExecHandleBuilder.copyTo(options);
        return this;
    }

    /**
     * Returns the version of the Java executable specified by {@link #getExecutable()}.
     *
     * @since 5.2
     */
    @Input
    public JavaVersion getJavaVersion() {
        return getServices().get(JvmVersionDetector.class).getJavaVersion(getExecutable());
    }

    /**
     * {@inheritDoc}
     */
    @Internal("covered by getJavaVersion")
    @Nullable
    @Override
    public String getExecutable() {
        return javaExecHandleBuilder.getExecutable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutable(String executable) {
        javaExecHandleBuilder.setExecutable(executable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutable(Object executable) {
        javaExecHandleBuilder.setExecutable(executable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec executable(Object executable) {
        javaExecHandleBuilder.executable(executable);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Internal
    public File getWorkingDir() {
        return javaExecHandleBuilder.getWorkingDir();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkingDir(File dir) {
        javaExecHandleBuilder.setWorkingDir(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkingDir(Object dir) {
        javaExecHandleBuilder.setWorkingDir(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec workingDir(Object dir) {
        javaExecHandleBuilder.workingDir(dir);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Internal
    public Map<String, Object> getEnvironment() {
        return javaExecHandleBuilder.getEnvironment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnvironment(Map<String, ?> environmentVariables) {
        javaExecHandleBuilder.setEnvironment(environmentVariables);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec environment(String name, Object value) {
        javaExecHandleBuilder.environment(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec environment(Map<String, ?> environmentVariables) {
        javaExecHandleBuilder.environment(environmentVariables);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec copyTo(ProcessForkOptions target) {
        javaExecHandleBuilder.copyTo(target);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec setStandardInput(InputStream inputStream) {
        javaExecHandleBuilder.setStandardInput(inputStream);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Internal
    public InputStream getStandardInput() {
        return javaExecHandleBuilder.getStandardInput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec setStandardOutput(OutputStream outputStream) {
        javaExecHandleBuilder.setStandardOutput(outputStream);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Internal
    public OutputStream getStandardOutput() {
        return javaExecHandleBuilder.getStandardOutput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExec setErrorOutput(OutputStream outputStream) {
        javaExecHandleBuilder.setErrorOutput(outputStream);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Internal
    public OutputStream getErrorOutput() {
        return javaExecHandleBuilder.getErrorOutput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaExecSpec setIgnoreExitValue(boolean ignoreExitValue) {
        javaExecHandleBuilder.setIgnoreExitValue(ignoreExitValue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Input
    public boolean isIgnoreExitValue() {
        return javaExecHandleBuilder.isIgnoreExitValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Internal
    public List<String> getCommandLine() {
        return javaExecHandleBuilder.getCommandLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommandLineArgumentProvider> getJvmArgumentProviders() {
        return javaExecHandleBuilder.getJvmArgumentProviders();
    }

    /**
     * Returns the result for the command run by this task. The provider has no value if this task has not been executed yet.
     *
     * @return A provider of the result.
     * @since 6.1
     */
    @Internal
    @Incubating
    public Provider<ExecResult> getExecutionResult() {
        return execResult;
    }
}
