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

import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.integtests.fixtures.build.BuildTestFile
import spock.lang.Unroll

class CompositeBuildIdentityIntegrationTest extends AbstractCompositeBuildIntegrationTest {
    BuildTestFile buildB

    def setup() {
        buildB = multiProjectBuild("buildB", ['b1', 'b2']) {
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
    def "includes build identifier in logging output with #display"() {
        dependency "org.test:${dependencyName}:1.0"

        buildB.settingsFile << settings << "\n"
        buildB.buildFile << """
            println "configuring \$project.path"
            classes.doLast { t ->
                println "classes of \$t.path"
            }
        """

        when:
        execute(buildA, ":assemble")

        then:
        outputContains("> Configure project :${buildName}")
        result.groupedOutput.task(":${buildName}:classes").output.contains("classes of :classes")

        where:
        settings                     | buildName | dependencyName | display
        ""                           | "buildB"  | "buildB"       | "default root project name"
        "rootProject.name='someLib'" | "buildB"  | "someLib"      | "configured root project name"
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "includes build identifier in dependency report with #display"() {
        dependency "org.test:${dependencyName}:1.0"

        buildB.settingsFile << settings << "\n"
        buildB.buildFile << """
            dependencies { implementation project(':b1') }
        """

        when:
        execute(buildA, ":dependencies")

        then:
        outputContains("""
runtimeClasspath - Runtime classpath of source set 'main'.
\\--- org.test:${dependencyName}:1.0 -> project :${buildName}
     \\--- project :${buildName}:b1
""")

        where:
        settings                     | buildName | dependencyName | display
        ""                           | "buildB"  | "buildB"       | "default root project name"
        "rootProject.name='someLib'" | "buildB"  | "someLib"      | "configured root project name"
    }

    @Unroll
    def "includes build identifier in error message on failure to resolve dependencies of build with #display"() {
        dependency "org.test:${dependencyName}:1.0"

        buildB.settingsFile << settings << "\n"
        buildB.buildFile << """
            dependencies { implementation "test:test:1.2" }
        """

        when:
        fails(buildA, ":assemble")

        then:
        failure.assertHasDescription("Could not determine the dependencies of task ':${buildName}:compileJava'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':${buildName}:compileClasspath'.")
        failure.assertHasCause("""Cannot resolve external dependency test:test:1.2 because no repositories are defined.
Required by:
    project :${buildName}""")

        where:
        settings                     | buildName | dependencyName | display
        ""                           | "buildB"  | "buildB"       | "default root project name"
        "rootProject.name='someLib'" | "buildB"  | "someLib"      | "configured root project name"
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "includes build identifier in task failure error message with #display"() {
        dependency "org.test:${dependencyName}:1.0"

        buildB.settingsFile << settings << "\n"
        buildB.buildFile << """
            classes.doLast {
                throw new RuntimeException("broken")
            }
        """

        when:
        fails(buildA, ":assemble")

        then:
        failure.assertHasDescription("Execution failed for task ':${buildName}:classes'.")
        failure.assertHasCause("broken")

        where:
        settings                     | buildName | dependencyName | display
        ""                           | "buildB"  | "buildB"       | "default root project name"
        "rootProject.name='someLib'" | "buildB"  | "someLib"      | "configured root project name"
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "includes build identifier in dependency resolution results with #display"() {
        dependency "org.test:${dependencyName}:1.0"

        buildB.settingsFile << settings << "\n"
        buildB.buildFile << """
            dependencies { implementation project(':b1') }
        """

        buildA.buildFile << """
            classes.doLast {
                def components = configurations.runtimeClasspath.incoming.resolutionResult.allComponents.id
                assert components.size() == 3
                assert components[0].build.name == ':'
                assert components[0].build.currentBuild
                assert components[0].projectPath == ':'
                assert components[0].projectName == 'buildA'
                assert components[1].build.name == '${buildName}'
                assert !components[1].build.currentBuild
                assert components[1].projectPath == ':'
                assert components[1].projectName == '${buildName}'
                assert components[2].build.name == '${buildName}'
                assert !components[2].build.currentBuild
                assert components[2].projectPath == ':b1'
                assert components[2].projectName == 'b1'

                def selectors = configurations.runtimeClasspath.incoming.resolutionResult.allDependencies.requested
                assert selectors.size() == 2
                assert selectors[0].displayName == 'org.test:${dependencyName}:1.0'
                assert selectors[1].displayName == 'project :${buildName}:b1'
                // TODO - should be build name
                assert selectors[1].buildName == 'buildB'
                assert selectors[1].projectPath == ':b1'
            }
        """

        expect:
        execute(buildA, ":assemble")

        where:
        settings                     | buildName | dependencyName | display
        ""                           | "buildB"  | "buildB"       | "default root project name"
        "rootProject.name='someLib'" | "buildB"  | "someLib"      | "configured root project name"
    }
}
