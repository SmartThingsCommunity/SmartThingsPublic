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
package org.gradle.launcher.daemon.client;

import org.gradle.api.GradleException;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.internal.classpath.DefaultModuleRegistry;
import org.gradle.api.internal.classpath.ModuleRegistry;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.concurrent.CompositeStoppable;
import org.gradle.internal.installation.CurrentGradleInstallation;
import org.gradle.internal.installation.GradleInstallation;
import org.gradle.internal.io.StreamByteBuffer;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.serialize.FlushableEncoder;
import org.gradle.internal.serialize.kryo.KryoBackedEncoder;
import org.gradle.internal.stream.EncodedStream;
import org.gradle.internal.time.Time;
import org.gradle.internal.time.Timer;
import org.gradle.launcher.daemon.DaemonExecHandleBuilder;
import org.gradle.launcher.daemon.bootstrap.DaemonOutputConsumer;
import org.gradle.launcher.daemon.bootstrap.GradleDaemon;
import org.gradle.launcher.daemon.configuration.DaemonParameters;
import org.gradle.launcher.daemon.diagnostics.DaemonStartupInfo;
import org.gradle.launcher.daemon.registry.DaemonDir;
import org.gradle.process.internal.DefaultExecActionFactory;
import org.gradle.process.internal.ExecHandle;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GFileUtils;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DefaultDaemonStarter implements DaemonStarter {
    private static final Logger LOGGER = Logging.getLogger(DefaultDaemonStarter.class);

    private final DaemonDir daemonDir;
    private final DaemonParameters daemonParameters;
    private final DaemonGreeter daemonGreeter;
    private final JvmVersionValidator versionValidator;

    public DefaultDaemonStarter(DaemonDir daemonDir, DaemonParameters daemonParameters, DaemonGreeter daemonGreeter, JvmVersionValidator versionValidator) {
        this.daemonDir = daemonDir;
        this.daemonParameters = daemonParameters;
        this.daemonGreeter = daemonGreeter;
        this.versionValidator = versionValidator;
    }

    @Override
    public DaemonStartupInfo startDaemon(boolean singleUse) {
        String daemonUid = UUID.randomUUID().toString();

        GradleInstallation gradleInstallation = CurrentGradleInstallation.get();
        ModuleRegistry registry = new DefaultModuleRegistry(gradleInstallation);
        ClassPath classpath;
        List<File> searchClassPath;
        if (gradleInstallation == null) {
            // When not running from a Gradle distro, need runtime impl for launcher plus the search path to look for other modules
            classpath = registry.getModule("gradle-launcher").getAllRequiredModulesClasspath();
            searchClassPath = registry.getAdditionalClassPath().getAsFiles();
        } else {
            // When running from a Gradle distro, only need launcher jar. The daemon can find everything from there.
            classpath = registry.getModule("gradle-launcher").getImplementationClasspath();
            searchClassPath = Collections.emptyList();
        }
        if (classpath.isEmpty()) {
            throw new IllegalStateException("Unable to construct a bootstrap classpath when starting the daemon");
        }

        versionValidator.validate(daemonParameters);

        List<String> daemonArgs = new ArrayList<String>();
        daemonArgs.addAll(getPriorityArgs(daemonParameters.getPriority()));
        daemonArgs.add(daemonParameters.getEffectiveJvm().getJavaExecutable().getAbsolutePath());

        List<String> daemonOpts = daemonParameters.getEffectiveJvmArgs();
        daemonArgs.addAll(daemonOpts);
        daemonArgs.add("-cp");
        daemonArgs.add(CollectionUtils.join(File.pathSeparator, classpath.getAsFiles()));

        if (Boolean.getBoolean("org.gradle.daemon.debug")) {
            daemonArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
        }
        LOGGER.debug("Using daemon args: {}", daemonArgs);

        daemonArgs.add(GradleDaemon.class.getName());
        // Version isn't used, except by a human looking at the output of jps.
        daemonArgs.add(GradleVersion.current().getVersion());

        // Serialize configuration to daemon via the process' stdin
        StreamByteBuffer buffer = new StreamByteBuffer();
        FlushableEncoder encoder = new KryoBackedEncoder(new EncodedStream.EncodedOutput(buffer.getOutputStream()));
        try {
            encoder.writeString(daemonParameters.getGradleUserHomeDir().getAbsolutePath());
            encoder.writeString(daemonDir.getBaseDir().getAbsolutePath());
            encoder.writeSmallInt(daemonParameters.getIdleTimeout());
            encoder.writeSmallInt(daemonParameters.getPeriodicCheckInterval());
            encoder.writeBoolean(singleUse);
            encoder.writeString(daemonUid);
            encoder.writeSmallInt(daemonParameters.getPriority().ordinal());
            encoder.writeSmallInt(daemonOpts.size());
            for (String daemonOpt : daemonOpts) {
                encoder.writeString(daemonOpt);
            }
            encoder.writeSmallInt(searchClassPath.size());
            for (File file : searchClassPath) {
                encoder.writeString(file.getAbsolutePath());
            }
            encoder.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        InputStream stdInput = buffer.getInputStream();

        return startProcess(daemonArgs, daemonDir.getVersionedDir(), stdInput);
    }

    private List<String> getPriorityArgs(DaemonParameters.Priority priority) {
        if (priority == DaemonParameters.Priority.NORMAL) {
            return Collections.emptyList();
        }
        OperatingSystem os = OperatingSystem.current();
        if (os.isUnix()) {
            return Arrays.asList("nice", "-n", "10");
        } else if (os.isWindows()) {
            return Arrays.asList("cmd", "/C", "start", "\"Gradle build daemon\"", "/B", "/belownormal", "/WAIT");
        } else {
            return Collections.emptyList();
        }
    }

    private DaemonStartupInfo startProcess(List<String> args, File workingDir, InputStream stdInput) {
        LOGGER.debug("Starting daemon process: workingDir = {}, daemonArgs: {}", workingDir, args);
        Timer clock = Time.startTimer();
        try {
            GFileUtils.mkdirs(workingDir);

            DaemonOutputConsumer outputConsumer = new DaemonOutputConsumer();

            // This factory should be injected but leaves non-daemon threads running when used from the tooling API client
            DefaultExecActionFactory execActionFactory = DefaultExecActionFactory.root();
            try {
                ExecHandle handle = new DaemonExecHandleBuilder().build(args, workingDir, outputConsumer, stdInput, execActionFactory.newExec());

                handle.start();
                LOGGER.debug("Gradle daemon process is starting. Waiting for the daemon to detach...");
                handle.waitForFinish();
                LOGGER.debug("Gradle daemon process is now detached.");
            } finally {
                CompositeStoppable.stoppable(execActionFactory).stop();
            }

            return daemonGreeter.parseDaemonOutput(outputConsumer.getProcessOutput(), args);
        } catch (GradleException e) {
            throw e;
        } catch (Exception e) {
            throw new GradleException("Could not start Gradle daemon.", e);
        } finally {
            LOGGER.info("An attempt to start the daemon took {}.", clock.getElapsed());
        }
    }

}
