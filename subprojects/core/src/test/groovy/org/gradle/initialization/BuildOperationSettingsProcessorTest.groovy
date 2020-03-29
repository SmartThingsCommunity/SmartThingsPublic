/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.initialization

import org.gradle.StartParameter
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.internal.initialization.ClassLoaderScope
import org.gradle.internal.operations.BuildOperationCategory
import org.gradle.internal.operations.TestBuildOperationExecutor
import spock.lang.Specification

class BuildOperationSettingsProcessorTest extends Specification {

    def buildOperationExecutor = new TestBuildOperationExecutor()
    def settingsProcessor = Mock(SettingsProcessor)
    def gradleInternal = Mock(GradleInternal)
    def settingsLocation = Mock(SettingsLocation)
    def buildOperationScriptPlugin = new BuildOperationSettingsProcessor(settingsProcessor, buildOperationExecutor)
    def classLoaderScope = Mock(ClassLoaderScope)
    def startParameter = Mock(StartParameter)
    def settingsInternal = Mock(SettingsInternal)
    def rootDir = new File("root")

    def "delegates to decorated settings processor"() {
        given:
        settings()

        when:
        buildOperationScriptPlugin.process(gradleInternal, settingsLocation, classLoaderScope, startParameter)

        then:
        1 * settingsProcessor.process(gradleInternal, settingsLocation, classLoaderScope, startParameter) >> settingsInternal
    }

    def "exposes build operation with settings configuration result"() {
        given:
        settings()

        when:
        buildOperationScriptPlugin.process(gradleInternal, settingsLocation, classLoaderScope, startParameter)

        then:
        1 * settingsProcessor.process(gradleInternal, settingsLocation, classLoaderScope, startParameter) >> settingsInternal

        and:
        buildOperationExecutor.operations.size() == 1
        buildOperationExecutor.operations.get(0).displayName == "Evaluate settings"
        buildOperationExecutor.operations.get(0).name == "Evaluate settings"

        buildOperationExecutor.operations.get(0).operationType == BuildOperationCategory.UNCATEGORIZED
        buildOperationExecutor.operations.get(0).details.settingsDir == rootDir.absolutePath
        buildOperationExecutor.operations.get(0).details.settingsFile == "settings.gradle"
    }

    private void settings() {
        _ * settingsInternal.gradle >> gradleInternal
        _ * settingsLocation.settingsDir >> rootDir
        _ * settingsLocation.settingsFile >> new File("settings.gradle")
    }
}
