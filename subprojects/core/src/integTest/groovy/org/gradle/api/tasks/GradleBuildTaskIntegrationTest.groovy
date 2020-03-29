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

package org.gradle.api.tasks

import org.gradle.initialization.RunNestedBuildBuildOperationType
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.BuildOperationsFixture
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.test.fixtures.server.http.BlockingHttpServer
import org.junit.Rule
import spock.lang.Unroll

class GradleBuildTaskIntegrationTest extends AbstractIntegrationSpec {

    def buildOperations = new BuildOperationsFixture(executer, testDirectoryProvider)

    @ToBeFixedForInstantExecution
    def "handles properties which are not String when calling GradleBuild"() {
        given:
        settingsFile << "rootProject.name = 'parent'"
        buildFile << """
            task buildInBuild(type:GradleBuild) {
                buildFile = 'other.gradle'
                startParameter.projectProperties['foo'] = true // not a String
            }
        """
        file('other.gradle') << 'assert foo==true'

        when:
        run 'buildInBuild'

        then:
        noExceptionThrown()
    }

    @ToBeFixedForInstantExecution
    def "can set build path"() {
        given:
        settingsFile << "rootProject.name = 'parent'"
        buildFile << """
            task b1(type:GradleBuild) {
                tasks = ["t"]
                buildName = 'bp'
            }
            task t
        """

        when:
        run 'b1'

        then:
        executed(":bp:t")
    }

    @ToBeFixedForInstantExecution
    def "fails when build path is not unique"() {
        given:
        settingsFile << "rootProject.name = 'parent'"
        buildFile << """
            task b1(type:GradleBuild) {
                tasks = ["t"]
                buildName = 'bp'
            }
            task b2(type:GradleBuild) {
                tasks = ["t"]
                buildName = 'bp'
            }
            task t
        """

        when:
        fails 'b1', 'b2'

        then:
        failure.assertHasDescription("Execution failed for task ':b2'")
        failure.assertHasCause("Included build $testDirectory has build path :bp which is the same as included build $testDirectory")
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "shows deprecation warning when accessing #displayName when configuring GradleBuild task"() {
        given:
        settingsFile << "rootProject.name = 'parent'"
        buildFile << """
            task buildInBuild(type:GradleBuild) {
                buildFile = 'other.gradle'
            }

            ${codeUnderTest}
        """
        file('other.gradle') << 'assert true'

        when:
        executer.expectDeprecationWarning()
        run 'buildInBuild'

        then:
        outputContains("${displayName} method has been deprecated. This is scheduled to be removed in Gradle 7.0.")

        where:
        displayName                                | codeUnderTest
        "StartParameter.setSearchUpwards(boolean)" | "buildInBuild.startParameter.searchUpwards = true"
        "StartParameter.isSearchUpwards()"         | "buildInBuild.startParameter.searchUpwards"
        "StartParameter.useEmptySettings()"        | "buildInBuild.startParameter.useEmptySettings()"
        "StartParameter.isUseEmptySettings()"      | "buildInBuild.startParameter.useEmptySettings"
    }

    @ToBeFixedForInstantExecution
    def "nested build can use Gradle home directory that is different to outer build"() {
        given:
        def dir = file("other-home")
        settingsFile << "rootProject.name = 'parent'"
        buildFile << """
            task otherBuild(type:GradleBuild) {
                buildFile = 'other.gradle'
                startParameter.gradleUserHomeDir = file("${dir.toURI()}")
            }
        """

        file('other.gradle') << '''
println "user home dir: " + gradle.gradleUserHomeDir
println "build script code source: " + getClass().protectionDomain.codeSource.location
'''

        when:
        run 'otherBuild'

        then:
        output.contains("user home dir: $dir")
        output.contains("build script code source: ${dir.toURI()}")
    }

    @ToBeFixedForInstantExecution
    def "nested build can have buildSrc"() {
        given:
        buildFile << """
            task otherBuild(type:GradleBuild) {
                dir = 'other'
            }
        """
        file('other/settings.gradle') << "rootProject.name = 'other'"
        file('other/buildSrc/src/main/java/Thing.java') << "class Thing { }"
        file('other/build.gradle') << """
            new Thing()
        """

        when:
        run 'otherBuild'

        then:
        result.assertTaskExecuted(":other:buildSrc:assemble")
    }

    def "buildSrc can have nested build"() {
        given:
        file('buildSrc/src/main/java/Thing.java') << "class Thing { }"
        file('buildSrc/build.gradle') << """
            task otherBuild(type:GradleBuild) {
                dir = '../other'
                tasks = ['build']
            }
            classes.dependsOn(otherBuild)
        """
        file('other/settings.gradle') << ""
        file('other/build.gradle') << """
            task build
        """

        when:
        run()

        then:
        result.assertTaskExecuted(":buildSrc:other:build")
        result.assertTaskExecuted(":buildSrc:otherBuild")
    }

    @ToBeFixedForInstantExecution
    def "nested build can nest more builds"() {
        given:
        buildFile << """
            task otherBuild(type:GradleBuild) {
                dir = 'other'
                tasks = ['otherBuild']
            }
        """
        file('other/settings.gradle').touch()
        file('other/build.gradle') << """
            task otherBuild(type:GradleBuild) {
                dir = '../other2'
                tasks = ['build']
            }
        """
        file('other2/settings.gradle').touch()
        file('other2/build.gradle') << """
            task build
        """

        when:
        run 'otherBuild'

        then:
        // TODO - Fix test fixtures to allow assertions on buildSrc tasks rather than relying on output scraping in tests
        outputContains(":other:otherBuild")
        outputContains(":other:other2:build")
    }

    @ToBeFixedForInstantExecution
    def "nested build can contain project dependencies"() {
        given:
        buildFile << """
            task otherBuild(type:GradleBuild) {
                dir = 'other'
                tasks = ['resolve']
            }
        """
        file("other/settings.gradle") << """
            include 'a', 'b'
        """
        file("other/build.gradle") << """
            allprojects { configurations.create('default') }
            dependencies { "default" project(':a') }
            project(':a') {
                dependencies { "default" project(':b') }
            }
            task resolve {
                inputs.files configurations.default
                doLast {
                }
            }
        """

        expect:
        succeeds 'otherBuild'
    }

    @Rule
    BlockingHttpServer barrier = new BlockingHttpServer()

    @ToBeFixedForInstantExecution
    def "can run multiple GradleBuild tasks concurrently"() {
        barrier.start()

        given:

        /**
         * Setup a build where a `GradleBuild` task while another `GradleBuild` is currently running another build but has not yet finished running the settings file for that build.
         */

        settingsFile << """
            rootProject.name = 'root'
            include '1', '2'
"""
        buildFile << """
            subprojects {
                task otherBuild(type:GradleBuild) {
                    dir = "\${rootProject.file('subprojects')}"
                    tasks = ['log']
                    buildName = project.name + "nested"
                }
                otherBuild.doFirst {
                    ${barrier.callFromBuildUsingExpression('project.name + "-started"')}
                }
                otherBuild.doLast {
                    ${barrier.callFromBuildUsingExpression('project.name + "-finished"')}
                }
            }
            task otherBuild(type:GradleBuild) {
                dir = "main"
                tasks = ['log']
            }
        """
        file('main/settings.gradle') << """
            ${barrier.callFromBuild("child-build-started")}
            ${barrier.callFromBuild("child-build-finished")}
        """
        file('main/build.gradle') << """
            assert gradle.parent.rootProject.name == 'root'
            task log { }
        """
        file('subprojects/settings.gradle') << ""
        file('subprojects/build.gradle') << """
            assert gradle.parent.rootProject.name == 'root'
            task log { }
        """

        barrier.expectConcurrent("child-build-started", "1-started", "2-started")
        barrier.expectConcurrent("child-build-finished", "1-finished", "2-finished")

        when:
        executer.withArgument("--parallel")
        run 'otherBuild'

        then:
        noExceptionThrown()

        and:
        def runNestedBuildOps = buildOperations.all(RunNestedBuildBuildOperationType)
        runNestedBuildOps.size() == 3
    }
}
