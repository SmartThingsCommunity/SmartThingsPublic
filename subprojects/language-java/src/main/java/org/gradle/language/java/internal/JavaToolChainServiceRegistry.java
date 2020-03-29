/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.language.java.internal;

import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.api.internal.tasks.CurrentJvmJavaToolChain;
import org.gradle.api.internal.tasks.JavaToolChainFactory;
import org.gradle.api.internal.tasks.compile.DefaultJavaCompilerFactory;
import org.gradle.api.internal.tasks.compile.JavaCompilerFactory;
import org.gradle.api.internal.tasks.compile.JavaHomeBasedJavaCompilerFactory;
import org.gradle.api.internal.tasks.compile.processing.AnnotationProcessorDetector;
import org.gradle.internal.Factory;
import org.gradle.internal.jvm.inspection.JvmVersionDetector;
import org.gradle.internal.service.ServiceRegistration;
import org.gradle.internal.service.scopes.AbstractPluginServiceRegistry;
import org.gradle.jvm.internal.toolchain.JavaToolChainInternal;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.ExecHandleFactory;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.process.internal.worker.child.WorkerDirectoryProvider;
import org.gradle.workers.internal.ActionExecutionSpecFactory;
import org.gradle.workers.internal.WorkerDaemonFactory;

import javax.tools.JavaCompiler;

public class JavaToolChainServiceRegistry extends AbstractPluginServiceRegistry {
    @Override
    public void registerBuildSessionServices(ServiceRegistration registration) {
        registration.addProvider(new BuildSessionScopeCompileServices());
    }
    @Override
    public void registerProjectServices(ServiceRegistration registration) {
        registration.addProvider(new ProjectScopeCompileServices());
    }

    private static class BuildSessionScopeCompileServices {
        Factory<JavaCompiler> createJavaHomeBasedJavaCompilerFactory() {
            return new JavaHomeBasedJavaCompilerFactory();
        }
    }

    private static class ProjectScopeCompileServices {
        JavaCompilerFactory createJavaCompilerFactory(WorkerDaemonFactory workerDaemonFactory, Factory<JavaCompiler> javaHomeBasedJavaCompilerFactory, JavaForkOptionsFactory forkOptionsFactory, WorkerDirectoryProvider workerDirectoryProvider, ExecHandleFactory execHandleFactory, AnnotationProcessorDetector processorDetector, ClassPathRegistry classPathRegistry, ActionExecutionSpecFactory actionExecutionSpecFactory) {
            return new DefaultJavaCompilerFactory(workerDirectoryProvider, workerDaemonFactory, javaHomeBasedJavaCompilerFactory, forkOptionsFactory, execHandleFactory, processorDetector, classPathRegistry, actionExecutionSpecFactory);
        }

        JavaToolChainInternal createJavaToolChain(JavaCompilerFactory compilerFactory, ExecActionFactory execActionFactory) {
            return new CurrentJvmJavaToolChain(compilerFactory, execActionFactory);
        }

        JavaToolChainFactory createJavaToolChainFactory(JavaCompilerFactory compilerFactory, ExecActionFactory execActionFactory, JvmVersionDetector jvmVersionDetector) {
            return new JavaToolChainFactory(compilerFactory, execActionFactory, jvmVersionDetector);
        }
    }
}
