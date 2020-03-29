/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.process.internal

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.gradle.api.internal.ClassPathRegistry
import org.gradle.api.internal.DefaultClassPathProvider
import org.gradle.api.internal.DefaultClassPathRegistry
import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.api.internal.file.TestFiles
import org.gradle.api.internal.file.TmpDirTemporaryFileProvider
import org.gradle.api.logging.LogLevel
import org.gradle.cache.CacheRepository
import org.gradle.cache.internal.CacheFactory
import org.gradle.cache.internal.CacheScopeMapping
import org.gradle.cache.internal.DefaultCacheRepository
import org.gradle.cache.internal.DefaultCacheScopeMapping
import org.gradle.cache.internal.TestFileContentCacheFactory
import org.gradle.internal.id.LongIdGenerator
import org.gradle.internal.jpms.JavaModuleDetector
import org.gradle.internal.jvm.inspection.CachingJvmVersionDetector
import org.gradle.internal.jvm.inspection.DefaultJvmVersionDetector
import org.gradle.internal.logging.LoggingManagerInternal
import org.gradle.internal.logging.TestOutputEventListener
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.logging.services.DefaultLoggingManagerFactory
import org.gradle.internal.logging.services.LoggingServiceRegistry
import org.gradle.internal.operations.CurrentBuildOperationRef
import org.gradle.internal.operations.DefaultBuildOperationRef
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.remote.MessagingServer
import org.gradle.internal.service.DefaultServiceRegistry
import org.gradle.internal.service.ServiceRegistryBuilder
import org.gradle.internal.service.scopes.GlobalScopeServices
import org.gradle.process.internal.health.memory.MemoryManager
import org.gradle.process.internal.worker.DefaultWorkerProcessFactory
import org.gradle.process.internal.worker.child.WorkerProcessClassPathProvider
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.testfixtures.internal.NativeServicesTestFixture
import org.gradle.util.GradleVersion
import org.gradle.util.RedirectStdOutAndErr
import org.junit.Rule
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractWorkerProcessIntegrationSpec extends Specification {
    @Shared DefaultServiceRegistry services = (DefaultServiceRegistry) ServiceRegistryBuilder.builder()
        .parent(NativeServicesTestFixture.getInstance())
        .provider(LoggingServiceRegistry.NO_OP)
        .provider(new GlobalScopeServices(false))
        .build()
    final MessagingServer server = services.get(MessagingServer.class)
    @Rule
    final TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider(getClass())
    @Rule
    final RedirectStdOutAndErr stdout = new RedirectStdOutAndErr()
    final CacheFactory factory = services.get(CacheFactory.class)
    final CacheScopeMapping scopeMapping = new DefaultCacheScopeMapping(tmpDir.testDirectory, null, GradleVersion.current())
    final CacheRepository cacheRepository = new DefaultCacheRepository(scopeMapping, factory)
    final ModuleRegistry moduleRegistry = services.get(ModuleRegistry)
    final WorkerProcessClassPathProvider workerProcessClassPathProvider = new WorkerProcessClassPathProvider(cacheRepository, moduleRegistry)
    final ClassPathRegistry classPathRegistry = new DefaultClassPathRegistry(new DefaultClassPathProvider(moduleRegistry), workerProcessClassPathProvider)
    final JavaExecHandleFactory execHandleFactory = TestFiles.javaExecHandleFactory(tmpDir.testDirectory)
    final OutputEventListener outputEventListener = new TestOutputEventListener()
    DefaultWorkerProcessFactory workerFactory = new DefaultWorkerProcessFactory(loggingManager(LogLevel.DEBUG), server, classPathRegistry, new LongIdGenerator(), tmpDir.file("gradleUserHome"),new TmpDirTemporaryFileProvider(),
        execHandleFactory, new CachingJvmVersionDetector(new DefaultJvmVersionDetector(execHandleFactory)), new JavaModuleDetector(new TestFileContentCacheFactory(), TestFiles.fileCollectionFactory()), outputEventListener, Stub(MemoryManager))

    def setup() {
        CurrentBuildOperationRef.instance().set(new DefaultBuildOperationRef(new OperationIdentifier(123), null))
    }

    def cleanup() {
        CurrentBuildOperationRef.instance().clear()
        workerProcessClassPathProvider.close()
    }

    def cleanupSpec() {
        services.close()
    }

    Class<?> compileWithoutClasspath(String className, String classText) {
        return new GroovyClassLoader(getClass().classLoader).parseClass(classText, className)
    }

    Class<?> compileToDirectoryAndLoad(String className, String classText) {
        def classesDir = tmpDir.createDir("classes/$className")
        def compilationUnit = new CompilationUnit(new GroovyClassLoader(getClass().classLoader))
        compilationUnit.addSource(className, classText)

        def configuration = new CompilerConfiguration()
        configuration.setTargetDirectory(classesDir)

        compilationUnit.setConfiguration(configuration)
        compilationUnit.compile()

        return new URLClassLoader([classesDir.toURI().toURL()] as URL[], getClass().classLoader).loadClass(className)
    }

    public static class RemoteExceptionListener implements TestListenerInterface {
        Throwable ex
        final TestListenerInterface dispatch

        public RemoteExceptionListener(TestListenerInterface dispatch) {
            this.dispatch = dispatch
        }

        void send(String message, int count) {
            try {
                dispatch.send(message, count)
            } catch (Throwable e) {
                ex = e
            }
        }

        public void rethrow() throws Throwable {
            if (ex != null) {
                throw ex
            }
        }
    }

    static LoggingManagerInternal loggingManager(LogLevel logLevel) {
        def loggingManager = LoggingServiceRegistry.newEmbeddableLogging().get(DefaultLoggingManagerFactory).create()
        loggingManager.setLevelInternal(logLevel)
        return loggingManager
    }
}
