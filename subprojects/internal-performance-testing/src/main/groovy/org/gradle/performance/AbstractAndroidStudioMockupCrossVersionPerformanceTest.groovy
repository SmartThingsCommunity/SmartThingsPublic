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
package org.gradle.performance

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.performance.fixture.TestProjectLocator
import org.gradle.tooling.BuildActionExecuter

/**
 * This is the base class for performance tests aimed at simulating what happens in Android Studio. In particular, it will
 * use the Tooling API to synchronize project or run builds.
 *
 * Those tests support custom models, which are found in the internalAndroidPerformanceTesting project. Individual tests
 * need to extend this class and tell what is the name of the class that will query the model and the name of the method.
 * This method will be passed a {@link org.gradle.tooling.ProjectConnection} instance.
 *
 */
@CompileStatic
public abstract class AbstractAndroidStudioMockupCrossVersionPerformanceTest extends AbstractToolingApiCrossVersionPerformanceTest {

    @Override
    void experiment(String projectName, @DelegatesTo(AndroidStudioExperiment) Closure<?> spec) {
        experiment = new AndroidStudioExperiment(projectName)
        performanceTestIdProvider.testSpec = experiment
        def clone = spec.rehydrate(experiment, this, this)
        clone.resolveStrategy = Closure.DELEGATE_FIRST
        clone.call(experiment)
    }

    public class AndroidStudioExperiment extends AbstractToolingApiCrossVersionPerformanceTest.ToolingApiExperiment {

        AndroidStudioExperiment(String projectName) {
            super(projectName)
        }

        void action(String className) {
            action(className, null)
        }

        void action(String className, @DelegatesTo(value = BuildActionExecuter, strategy = Closure.DELEGATE_FIRST) Closure config) {
            action {
                AbstractAndroidStudioMockupCrossVersionPerformanceTest.this.tapiClassLoader.loadClass("org.gradle.tooling.BuildAction") //make sure BuildAction is available in the Gradle version we are currently running
                def proxy = { exec ->
                    config.delegate = exec
                    config.resolveStrategy = Closure.DELEGATE_FIRST
                    config.call()
                }.asType(it.class.classLoader.loadClass(Action.name))
                AbstractAndroidStudioMockupCrossVersionPerformanceTest.this.tapiClassLoader.loadClass(className).invokeMethod('withProjectConnection', [it, proxy] as Object[])
            }
        }

        @Override
        List<File> getExtraTestClassPath() {
            def testProjectLocator = new TestProjectLocator()
            def projectDir = testProjectLocator.findProjectDir(projectName)
            def classpathFile = new File(projectDir, 'tapi-classpath.txt')
            if (!classpathFile.exists()) {
                throw new IllegalStateException("Cannot find the TAPI classpath file at $classpathFile. Make sure the template contains it.")
            }
            (classpathFile as String[]).findAll { it }.collect { new File(it) }
        }
    }

}
