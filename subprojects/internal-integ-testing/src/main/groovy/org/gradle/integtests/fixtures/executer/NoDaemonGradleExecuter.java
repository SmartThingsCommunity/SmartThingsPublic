/*
 * Copyright 2010 the original author or authors.
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

package org.gradle.integtests.fixtures.executer;

import org.gradle.api.Action;
import org.gradle.api.internal.artifacts.ivyservice.ArtifactCachesProvider;
import org.gradle.api.internal.file.TestFiles;
import org.gradle.internal.Factory;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.AbstractExecHandleBuilder;
import org.gradle.process.internal.DefaultExecHandleBuilder;
import org.gradle.process.internal.ExecHandleBuilder;
import org.gradle.process.internal.JvmOptions;
import org.gradle.test.fixtures.file.TestDirectoryProvider;
import org.gradle.test.fixtures.file.TestFile;
import org.gradle.testfixtures.internal.NativeServicesTestFixture;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.Assert.fail;

public class NoDaemonGradleExecuter extends AbstractGradleExecuter {

    public NoDaemonGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider) {
        super(distribution, testDirectoryProvider);
    }

    public NoDaemonGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider, GradleVersion version) {
        super(distribution, testDirectoryProvider, version);
    }

    public NoDaemonGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider, GradleVersion gradleVersion, IntegrationTestBuildContext buildContext) {
        super(distribution, testDirectoryProvider, gradleVersion, buildContext);
    }

    @Override
    public void assertCanExecute() throws AssertionError {
        if (!getDistribution().isSupportsSpacesInGradleAndJavaOpts()) {
            Map<String, String> environmentVars = buildInvocation().environmentVars;
            for (String envVarName : Arrays.asList("JAVA_OPTS", "GRADLE_OPTS")) {
                String envVarValue = environmentVars.get(envVarName);
                if (envVarValue == null) {
                    continue;
                }
                for (String arg : JvmOptions.fromString(envVarValue)) {
                    if (arg.contains(" ")) {
                        throw new AssertionError(String.format("Env var %s contains arg with space (%s) which is not supported by Gradle %s", envVarName, arg, getDistribution().getVersion().getVersion()));
                    }
                }
            }
        }
    }

    @Override
    protected void transformInvocation(GradleInvocation invocation) {
        if (getDistribution().isSupportsSpacesInGradleAndJavaOpts()) {
            // Mix the implicit launcher JVM args in with the requested JVM args
            super.transformInvocation(invocation);
        } else {
            // Need to move those implicit JVM args that contain a space to the Gradle command-line (if possible)
            // Note that this isn't strictly correct as some system properties can only be set on JVM start up.
            // Should change the implementation to deal with these properly
            for (String jvmArg : invocation.implicitLauncherJvmArgs) {
                if (!jvmArg.contains(" ")) {
                    invocation.launcherJvmArgs.add(jvmArg);
                } else if (jvmArg.startsWith("-D")) {
                    invocation.args.add(jvmArg);
                } else {
                    throw new UnsupportedOperationException(String.format("Cannot handle launcher JVM arg '%s' as it contains whitespace. This is not supported by Gradle %s.",
                        jvmArg, getDistribution().getVersion().getVersion()));
                }
            }
        }
        invocation.implicitLauncherJvmArgs.clear();

        // Inject the launcher JVM args via one of the environment variables
        Map<String, String> environmentVars = invocation.environmentVars;
        String jvmOptsEnvVar;
        if (!environmentVars.containsKey("GRADLE_OPTS")) {
            jvmOptsEnvVar = "GRADLE_OPTS";
        } else if (!environmentVars.containsKey("JAVA_OPTS")) {
            jvmOptsEnvVar = "JAVA_OPTS";
        } else {
            // This could be handled, just not implemented yet
            throw new UnsupportedOperationException(String.format("Both GRADLE_OPTS and JAVA_OPTS environment variables are being used. Cannot provide JVM args %s to Gradle command.", invocation.launcherJvmArgs));
        }
        final String value = toJvmArgsString(invocation.launcherJvmArgs);
        environmentVars.put(jvmOptsEnvVar, value);

        // Add a JAVA_HOME if none provided
        if (!environmentVars.containsKey("JAVA_HOME")) {
            environmentVars.put("JAVA_HOME", getJavaHome().getAbsolutePath());
        }
    }

    @Override
    protected List<String> getAllArgs() {
        List<String> args = new ArrayList<String>();
        args.addAll(super.getAllArgs());
        addPropagatedSystemProperties(args);
        return args;
    }

    private void addPropagatedSystemProperties(List<String> args) {
        for (String propName : PROPAGATED_SYSTEM_PROPERTIES) {
            String propValue = System.getProperty(propName);
            if (propValue != null) {
                args.add("-D" + propName + "=" + propValue);
            }
        }
    }

    @Override
    protected boolean supportsWhiteSpaceInEnvVars() {
        final Jvm current = Jvm.current();
        if (getJavaHome().equals(current.getJavaHome())) {
            // we can tell for sure
            return current.getJavaVersion().isJava7Compatible();
        } else {
            // TODO improve lookup by reusing AvailableJavaHomes testfixture
            // for now we play it safe and just return false;
            return false;
        }
    }

    @Override
    protected GradleHandle createGradleHandle() {
        return createForkingGradleHandle(getResultAssertion(), getDefaultCharacterEncoding(), getExecHandleFactory()).start();
    }

    protected Factory<? extends AbstractExecHandleBuilder> getExecHandleFactory() {
        return new Factory<DefaultExecHandleBuilder>() {
            @Override
            public DefaultExecHandleBuilder create() {
                TestFile gradleHomeDir = getDistribution().getGradleHomeDir();
                if (!gradleHomeDir.isDirectory()) {
                    fail(gradleHomeDir + " is not a directory.\n"
                        + "If you are running tests from IDE make sure that gradle tasks that prepare the test image were executed. Last time it was 'intTestImage' task.");
                }

                NativeServicesTestFixture.initialize();
                DefaultExecHandleBuilder builder = new DefaultExecHandleBuilder(TestFiles.pathToFileResolver(), Executors.newCachedThreadPool()) {
                    @Override
                    public File getWorkingDir() {
                        // Override this, so that the working directory is not canonicalised. Some int tests require that
                        // the working directory is not canonicalised
                        return NoDaemonGradleExecuter.this.getWorkingDir();
                    }
                };

                // Clear the user's environment
                builder.environment("GRADLE_HOME", "");
                builder.environment("JAVA_HOME", "");
                builder.environment("GRADLE_OPTS", "");
                builder.environment("JAVA_OPTS", "");
                builder.environment(ArtifactCachesProvider.READONLY_CACHE_ENV_VAR, "");

                GradleInvocation invocation = buildInvocation();

                builder.environment(invocation.environmentVars);
                builder.workingDir(getWorkingDir());
                builder.setStandardInput(connectStdIn());

                builder.args(invocation.args);

                ExecHandlerConfigurer configurer = OperatingSystem.current().isWindows() ? new WindowsConfigurer() : new UnixConfigurer();
                configurer.configure(builder);
                getLogger().debug(String.format("Execute in %s with: %s %s", builder.getWorkingDir(), builder.getExecutable(), builder.getArgs()));
                return builder;
            }
        };
    }

    protected ForkingGradleHandle createForkingGradleHandle(Action<ExecutionResult> resultAssertion, String encoding, Factory<? extends AbstractExecHandleBuilder> execHandleFactory) {
        return new ForkingGradleHandle(getStdinPipe(), isUseDaemon(), resultAssertion, encoding, execHandleFactory, getDurationMeasurement());
    }

    @Override
    protected ExecutionResult doRun() {
        return startHandle().waitForFinish();
    }

    @Override
    protected ExecutionFailure doRunWithFailure() {
        return start().waitForFailure();
    }

    private interface ExecHandlerConfigurer {
        void configure(ExecHandleBuilder builder);
    }

    private class WindowsConfigurer implements ExecHandlerConfigurer {
        @Override
        public void configure(ExecHandleBuilder builder) {
            String cmd;
            if (getExecutable() != null) {
                cmd = getExecutable().replace('/', File.separatorChar);
            } else {
                cmd = "gradle";
            }
            builder.executable("cmd");

            List<String> allArgs = builder.getArgs();
            builder.setArgs(Arrays.asList("/c", cmd));
            builder.args(allArgs);

            String gradleHome = getDistribution().getGradleHomeDir().getAbsolutePath();

            // NOTE: Windows uses Path, but allows asking for PATH, and PATH
            //       is set within builder object for some things such
            //       as CommandLineIntegrationTest, try PATH first, and
            //       then revert to default of Path if null
            Object path = builder.getEnvironment().get("PATH");
            if (path == null) {
                path = builder.getEnvironment().get("Path");
            }
            path = String.format("%s\\bin;%s", gradleHome, path);
            builder.environment("PATH", path);
            builder.environment("Path", path);
        }
    }

    private class UnixConfigurer implements ExecHandlerConfigurer {
        @Override
        public void configure(ExecHandleBuilder builder) {
            if (getExecutable() != null) {
                File exe = new File(getExecutable());
                if (exe.isAbsolute()) {
                    builder.executable(exe.getAbsolutePath());
                } else {
                    builder.executable(String.format("%s/%s", getWorkingDir().getAbsolutePath(), getExecutable()));
                }
            } else {
                builder.executable(String.format("%s/bin/gradle", getDistribution().getGradleHomeDir().getAbsolutePath()));
            }
        }
    }

}
