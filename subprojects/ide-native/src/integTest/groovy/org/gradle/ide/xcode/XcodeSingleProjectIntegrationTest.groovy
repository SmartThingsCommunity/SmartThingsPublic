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

package org.gradle.ide.xcode

import org.gradle.ide.xcode.fixtures.AbstractXcodeIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution

class XcodeSingleProjectIntegrationTest extends AbstractXcodeIntegrationSpec {

    @ToBeFixedForInstantExecution
    def "create xcode workspace when no language plugins are applied"() {
        when:
        succeeds("xcode")

        then:
        result.assertTasksExecuted(":xcodeProject", ":xcodeProjectWorkspaceSettings", ":xcodeWorkspaceWorkspaceSettings", ":xcodeWorkspace", ":xcode")

        def workspace = rootXcodeWorkspace
        workspace.contentFile.assertHasProjects("${rootProjectName}.xcodeproj")

        def project = rootXcodeProject.projectFile
        project.mainGroup.assertHasChildren(['build.gradle'])
        project.assertNoTargets()
    }

    @ToBeFixedForInstantExecution
    def "cleanXcode remove all XCode generated project files"() {
        requireSwiftToolChain()

        given:
        buildFile << """
apply plugin: 'swift-application'
"""

        when:
        succeeds("xcode")

        then:
        executedAndNotSkipped(":xcodeProject", ":xcodeProjectWorkspaceSettings", ":xcodeScheme", ":xcodeWorkspace", ":xcodeWorkspaceWorkspaceSettings", ":xcode")

        def project = rootXcodeProject
        project.projectFile.getFile().assertExists()
        project.schemeFiles*.file*.assertExists()
        project.workspaceSettingsFile.assertExists()
        project.dir.assertExists()

        when:
        succeeds("cleanXcode")

        then:
        executedAndNotSkipped(":cleanXcodeProject")

        project.projectFile.getFile().assertDoesNotExist()
        project.schemeFiles*.file*.assertDoesNotExist()
        project.workspaceSettingsFile.assertDoesNotExist()
        project.dir.assertDoesNotExist()
    }
}
