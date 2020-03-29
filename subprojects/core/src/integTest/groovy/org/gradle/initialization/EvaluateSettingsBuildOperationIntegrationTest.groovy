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

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.BuildOperationsFixture
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.internal.operations.trace.BuildOperationRecord

class EvaluateSettingsBuildOperationIntegrationTest extends AbstractIntegrationSpec {

    final buildOperations = new BuildOperationsFixture(executer, temporaryFolder)

    def "settings details are exposed"() {
        settingsFile << ""

        when:
        succeeds('help')

        then:
        verifySettings(operation(), settingsFile)
        operation().details.buildPath == ":"
    }

    def "settings with master folder are exposed"() {

        def customSettingsFile = file("master/settings.gradle")
        customSettingsFile << """
        includeFlat "a"
        """

        def projectDirectory = testDirectory.createDir("a")

        when:
        projectDir(projectDirectory)
        succeeds('help')

        then:
        verifySettings(operation(), customSettingsFile)
        operation().details.buildPath == ":"
    }

    def "settings set via cmdline flag are exposed"() {
        def customSettingsDir = file("custom")
        customSettingsDir.mkdirs()
        def customSettingsFile = new File(customSettingsDir, "settings.gradle")
        customSettingsFile << """

        include "a"
        """

        when:
        executer.withArguments("--settings-file", customSettingsFile.absolutePath)
        succeeds('help')

        then:
        verifySettings(operation(), customSettingsFile)
        operation().details.buildPath == ":"
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "composite participants expose their settings details"() {
        settingsFile << """
            include "a"
            includeBuild "nested"

            rootProject.name = "root"
            rootProject.buildFileName = 'root.gradle'

        """

        def nestedSettingsFile = file("nested/settings.gradle")
        nestedSettingsFile << """
            rootProject.name = "nested"
        """
        file("nested/build.gradle") << """
        group = "org.acme"
        version = "1.0"
        """

        when:
        succeeds('help')

        then:
        operations().size() == 2
        verifySettings(operations()[0], settingsFile)
        operations()[0].details.buildPath == ":"
        verifySettings(operations()[1], nestedSettingsFile)
        operations()[1].details.buildPath == ":nested"
    }

    def 'can configure feature preview in settings'() {
        given:
        settingsFile << '''
enableFeaturePreview('GROOVY_COMPILATION_AVOIDANCE')
'''
        expect:
        succeeds('help')
    }

    private List<BuildOperationRecord> operations() {
        buildOperations.all(EvaluateSettingsBuildOperationType)
    }

    private BuildOperationRecord operation() {
        assert operations().size() == 1
        operations()[0]
    }

    private void verifySettings(BuildOperationRecord operation, File settingsFile) {
        assert operation.details.settingsDir == settingsFile.parentFile.absolutePath
        assert operation.details.settingsFile == settingsFile.absolutePath
    }

}
