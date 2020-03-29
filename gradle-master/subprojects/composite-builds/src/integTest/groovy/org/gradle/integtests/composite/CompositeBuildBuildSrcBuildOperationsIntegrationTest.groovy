/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.integtests.composite

import org.gradle.execution.taskgraph.NotifyTaskGraphWhenReadyBuildOperationType
import org.gradle.initialization.ConfigureBuildBuildOperationType
import org.gradle.initialization.LoadBuildBuildOperationType
import org.gradle.initialization.buildsrc.BuildBuildSrcBuildOperationType
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.integtests.fixtures.build.BuildTestFile
import org.gradle.internal.taskgraph.CalculateTaskGraphBuildOperationType
import org.gradle.launcher.exec.RunBuildBuildOperationType
import spock.lang.Unroll

import java.util.regex.Pattern

class CompositeBuildBuildSrcBuildOperationsIntegrationTest extends AbstractCompositeBuildIntegrationTest {
    BuildTestFile buildB

    def setup() {
        buildB = multiProjectBuild("buildB", ['b1', 'b2']) {
            file("buildSrc/src/main/java/Thing.java") << "class Thing { }"
            buildFile << """
                allprojects {
                    apply plugin: 'java'
                }
"""
        }
        includedBuilds << buildB
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "generates configure, task graph and run tasks operations for buildSrc of included builds with #display"() {
        given:
        dependency 'org.test:buildB:1.0'
        buildB.file("buildSrc/settings.gradle") << """
            ${settings}
        """

        when:
        execute(buildA, ":jar", [])

        then:
        executed ":buildB:jar"

        and:
        def root = operations.root(RunBuildBuildOperationType)

        def buildSrcOps = operations.all(BuildBuildSrcBuildOperationType)
        buildSrcOps.size() == 1
        buildSrcOps[0].displayName == "Build buildSrc"
        buildSrcOps[0].details.buildPath == ":buildB"

        def loadOps = operations.all(LoadBuildBuildOperationType)
        loadOps.size() == 3
        loadOps[0].displayName == "Load build"
        loadOps[0].details.buildPath == ":"
        loadOps[0].parentId == root.id

        loadOps[1].displayName == "Load build (:buildB)"
        loadOps[1].details.buildPath == ":buildB"
        loadOps[1].parentId == loadOps[0].id

        loadOps[2].displayName == "Load build (:buildB:buildSrc)"
        loadOps[2].details.buildPath == ":buildB:buildSrc"
        loadOps[2].parentId == buildSrcOps[0].id

        def configureOps = operations.all(ConfigureBuildBuildOperationType)
        configureOps.size() == 3
        configureOps[0].displayName == "Configure build"
        configureOps[0].details.buildPath == ":"
        configureOps[0].parentId == root.id
        configureOps[1].displayName == "Configure build (:buildB:buildSrc)"
        configureOps[1].details.buildPath == ":buildB:buildSrc"
        configureOps[1].parentId == buildSrcOps[0].id
        configureOps[2].displayName == "Configure build (:buildB)"
        configureOps[2].details.buildPath == ":buildB"
        configureOps[2].parentId == configureOps[0].id

        def taskGraphOps = operations.all(CalculateTaskGraphBuildOperationType)
        taskGraphOps.size() == 3
        taskGraphOps[0].displayName == "Calculate task graph (:buildB:buildSrc)"
        taskGraphOps[0].details.buildPath == ":buildB:buildSrc"
        taskGraphOps[0].parentId == buildSrcOps[0].id
        taskGraphOps[1].displayName == "Calculate task graph"
        taskGraphOps[1].details.buildPath == ":"
        taskGraphOps[1].parentId == root.id
        taskGraphOps[2].displayName == "Calculate task graph (:buildB)"
        taskGraphOps[2].details.buildPath == ":buildB"
        taskGraphOps[2].parentId == taskGraphOps[1].id

        def runTasksOps = operations.all(Pattern.compile("Run tasks.*"))
        runTasksOps.size() == 3
        runTasksOps[0].displayName == "Run tasks (:buildB:buildSrc)"
        runTasksOps[0].parentId == buildSrcOps[0].id
        runTasksOps[1].displayName == "Run tasks"
        runTasksOps[1].parentId == root.id
        runTasksOps[2].displayName == "Run tasks (:buildB)"
        runTasksOps[2].parentId == root.id

        def graphNotifyOps = operations.all(NotifyTaskGraphWhenReadyBuildOperationType)
        graphNotifyOps.size() == 3
        graphNotifyOps[0].displayName == 'Notify task graph whenReady listeners (:buildB:buildSrc)'
        graphNotifyOps[0].details.buildPath == ':buildB:buildSrc'
        graphNotifyOps[0].parentId == runTasksOps[0].id
        graphNotifyOps[1].displayName == "Notify task graph whenReady listeners"
        graphNotifyOps[1].details.buildPath == ":"
        graphNotifyOps[1].parentId == runTasksOps[1].id
        graphNotifyOps[2].displayName == "Notify task graph whenReady listeners (:buildB)"
        graphNotifyOps[2].details.buildPath == ":buildB"
        graphNotifyOps[2].parentId == runTasksOps[2].id

        where:
        settings                     | display
        ""                           | "default root project name"
        "rootProject.name='someLib'" | "configured root project name"
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "generates configure, task graph and run tasks operations when all builds have buildSrc with #display"() {
        given:
        dependency 'org.test:buildB:1.0'
        buildB.file("buildSrc/settings.gradle") << """
            ${settings}
        """

        buildA.file("buildSrc/src/main/java/Thing.java") << "class Thing { }"

        when:
        execute(buildA, ":jar", [])

        then:
        executed ":buildB:jar"

        and:
        def root = operations.root(RunBuildBuildOperationType)

        def buildSrcOps = operations.all(BuildBuildSrcBuildOperationType)
        buildSrcOps.size() == 2
        buildSrcOps[0].displayName == "Build buildSrc"
        // TODO should have a buildPath associated
        buildSrcOps[1].displayName == "Build buildSrc"
        // TODO should have a buildPath associated

        def loadOps = operations.all(LoadBuildBuildOperationType)
        loadOps.size() == 4
        loadOps[0].displayName == "Load build"
        loadOps[0].details.buildPath == ":"
        loadOps[0].parentId == root.id

        loadOps[1].displayName == "Load build (:buildB)"
        loadOps[1].details.buildPath == ":buildB"
        loadOps[1].parentId == loadOps[0].id

        loadOps[2].displayName == "Load build (:buildSrc)"
        loadOps[2].details.buildPath == ":buildSrc"
        loadOps[2].parentId == buildSrcOps[0].id

        loadOps[3].displayName == "Load build (:buildB:buildSrc)"
        loadOps[3].details.buildPath == ":buildB:buildSrc"
        loadOps[3].parentId == buildSrcOps[1].id

        def configureOps = operations.all(ConfigureBuildBuildOperationType)
        configureOps.size() == 4
        configureOps[0].displayName == "Configure build (:buildSrc)"
        configureOps[0].details.buildPath == ":buildSrc"
        configureOps[0].parentId == buildSrcOps[0].id
        configureOps[1].displayName == "Configure build"
        configureOps[1].details.buildPath == ":"
        configureOps[1].parentId == root.id
        configureOps[2].displayName == "Configure build (:buildB:buildSrc)"
        configureOps[2].details.buildPath == ":buildB:buildSrc"
        configureOps[2].parentId == buildSrcOps[1].id
        configureOps[3].displayName == "Configure build (:buildB)"
        configureOps[3].details.buildPath == ":buildB"
        configureOps[3].parentId == configureOps[1].id

        def taskGraphOps = operations.all(CalculateTaskGraphBuildOperationType)
        taskGraphOps.size() == 4
        taskGraphOps[0].displayName == "Calculate task graph (:buildSrc)"
        taskGraphOps[0].details.buildPath == ":buildSrc"
        taskGraphOps[0].parentId == buildSrcOps[0].id
        taskGraphOps[1].displayName == "Calculate task graph (:buildB:buildSrc)"
        taskGraphOps[1].details.buildPath == ":buildB:buildSrc"
        taskGraphOps[1].parentId == buildSrcOps[1].id
        taskGraphOps[2].displayName == "Calculate task graph"
        taskGraphOps[2].details.buildPath == ":"
        taskGraphOps[2].parentId == root.id
        taskGraphOps[3].displayName == "Calculate task graph (:buildB)"
        taskGraphOps[3].details.buildPath == ":buildB"
        taskGraphOps[3].parentId == taskGraphOps[2].id

        def runTasksOps = operations.all(Pattern.compile("Run tasks.*"))
        runTasksOps.size() == 4
        runTasksOps[0].displayName == "Run tasks (:buildSrc)"
        runTasksOps[0].parentId == buildSrcOps[0].id
        runTasksOps[1].displayName == "Run tasks (:buildB:buildSrc)"
        runTasksOps[1].parentId == buildSrcOps[1].id
        runTasksOps[2].displayName == "Run tasks"
        runTasksOps[2].parentId == root.id
        runTasksOps[3].displayName == "Run tasks (:buildB)"
        runTasksOps[3].parentId == root.id

        def graphNotifyOps = operations.all(NotifyTaskGraphWhenReadyBuildOperationType)
        graphNotifyOps.size() == 4
        graphNotifyOps[0].displayName == 'Notify task graph whenReady listeners (:buildSrc)'
        graphNotifyOps[0].details.buildPath == ':buildSrc'
        graphNotifyOps[0].parentId == runTasksOps[0].id
        graphNotifyOps[1].displayName == "Notify task graph whenReady listeners (:buildB:buildSrc)"
        graphNotifyOps[1].details.buildPath == ":buildB:buildSrc"
        graphNotifyOps[1].parentId == runTasksOps[1].id
        graphNotifyOps[2].displayName == "Notify task graph whenReady listeners"
        graphNotifyOps[2].details.buildPath == ":"
        graphNotifyOps[2].parentId == runTasksOps[2].id
        graphNotifyOps[3].displayName == "Notify task graph whenReady listeners (:buildB)"
        graphNotifyOps[3].details.buildPath == ":buildB"
        graphNotifyOps[3].parentId == runTasksOps[3].id

        where:
        settings                     | display
        ""                           | "default root project name"
        "rootProject.name='someLib'" | "configured root project name"
    }
}
