/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.performance.fixture


import org.gradle.integtests.fixtures.executer.IntegrationTestBuildContext
import org.gradle.integtests.fixtures.versions.ReleasedVersionDistributions
import org.gradle.performance.results.CrossVersionPerformanceResults
import org.gradle.performance.results.DataReporter
import org.gradle.performance.results.ResultsStore

/**
 * Runs cross version performance tests using Gradle's internal infrastructure.
 *
 * Consider using {@link GradleProfilerCrossVersionPerformanceTestRunner} instead, as it is meant to replace this class.
 */
class GradleInternalCrossVersionPerformanceTestRunner extends AbstractCrossVersionPerformanceTestRunner {

    private CompositeBuildExperimentListener buildExperimentListeners = new CompositeBuildExperimentListener()
    private CompositeInvocationCustomizer invocationCustomizers = new CompositeInvocationCustomizer()

    GradleInternalCrossVersionPerformanceTestRunner(BuildExperimentRunner experimentRunner, ResultsStore resultsStore, DataReporter<CrossVersionPerformanceResults> reporter, ReleasedVersionDistributions releases, IntegrationTestBuildContext buildContext) {
        super(experimentRunner, resultsStore, reporter, releases, buildContext)
    }

    void addBuildExperimentListener(BuildExperimentListener buildExperimentListener) {
        buildExperimentListeners.addListener(buildExperimentListener)
    }

    void addInvocationCustomizer(InvocationCustomizer invocationCustomizer) {
        invocationCustomizers.addCustomizer(invocationCustomizer)
    }

    @Override
    protected void configureGradleBuildExperimentSpec(GradleBuildExperimentSpec.GradleBuilder builder) {
        builder
            .listener(buildExperimentListeners)
            .invocationCustomizer(invocationCustomizers)

    }
}
