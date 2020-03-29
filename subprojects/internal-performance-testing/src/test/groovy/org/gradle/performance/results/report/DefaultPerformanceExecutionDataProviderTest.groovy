/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.performance.results.report

import org.gradle.performance.ResultSpecification
import org.gradle.performance.results.MeasuredOperationList
import org.gradle.performance.results.ResultsStore
import org.gradle.performance.results.ScenarioBuildResultData
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Subject

class DefaultPerformanceExecutionDataProviderTest extends ResultSpecification {
    @Rule
    TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider(getClass())

    @Subject
    DefaultPerformanceExecutionDataProvider provider

    File resultsJson = tmpDir.file('results.json')
    ResultsStore mockStore = Mock(ResultsStore)

    def setup() {
        resultsJson << '[]'
        provider = new DefaultPerformanceExecutionDataProvider(mockStore, resultsJson)
    }

    def 'can sort scenarios correctly'() {
        when:
        List buildResults = [
            createLowConfidenceRegressedData(),
            createLowConfidenceImprovedData(),
            createHighConfidenceImprovedData(),
            createHighConfidenceRegressedData(),
            createFailedData()
        ]
        buildResults.sort(DefaultPerformanceExecutionDataProvider.SCENARIO_COMPARATOR)

        then:
        buildResults.collect { it.scenarioName } == ['failed', 'highConfidenceRegressed', 'lowConfidenceRegressed', 'lowConfidenceImproved', 'highConfidenceImproved']
    }

    private ScenarioBuildResultData createFailedData() {
        return new ScenarioBuildResultData(scenarioName: 'failed', status: 'FAILURE', currentBuildExecutions: [Mock(ScenarioBuildResultData.ExecutionData)])
    }

    private ScenarioBuildResultData createHighConfidenceImprovedData() {
        // 95% confidence -50% difference
        return createResult('highConfidenceImproved', [2, 2, 2], [1, 1, 1])
    }

    private ScenarioBuildResultData createLowConfidenceImprovedData() {
        // 68% confidence -50% difference
        return createResult('lowConfidenceImproved', [2], [1])
    }

    private ScenarioBuildResultData createLowConfidenceRegressedData() {
        // 68% confidence 100% difference
        return createResult("lowConfidenceRegressed", [1], [2])
    }

    private ScenarioBuildResultData createHighConfidenceRegressedData() {
        // 91% confidence 100% difference
        return createResult('highConfidenceRegressed', [1, 1, 1, 2], [2, 2, 2, 2])
    }

    private ScenarioBuildResultData createResult(String name, List<Integer> baseVersionResult, List<Integer> currentVersionResult) {
        MeasuredOperationList baseVersion = measuredOperationList(baseVersionResult)
        MeasuredOperationList currentVersion = measuredOperationList(currentVersionResult)
        ScenarioBuildResultData.ExecutionData execution = new ScenarioBuildResultData.ExecutionData(new Date().getTime(), '', baseVersion, currentVersion)
        return new ScenarioBuildResultData(scenarioName: name, status: 'SUCCESS', currentBuildExecutions: [execution])
    }

}
