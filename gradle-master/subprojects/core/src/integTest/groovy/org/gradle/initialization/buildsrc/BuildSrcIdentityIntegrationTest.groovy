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

package org.gradle.initialization.buildsrc

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import spock.lang.Unroll

class BuildSrcIdentityIntegrationTest extends AbstractIntegrationSpec {
    @Unroll
    def "includes build identifier in logging output with #display"() {
        file("buildSrc/build.gradle") << """
            println "configuring \$project.path"
            classes.doLast { t ->
                println "classes of \$t.path"
            }
        """
        file("buildSrc/settings.gradle") << settings << "\n"

        when:
        run()

        then:
        outputContains("> Configure project :buildSrc")
        result.groupedOutput.task(":buildSrc:classes").output.contains("classes of :classes")

        where:
        settings                     | display
        ""                           | "default root project name"
        "rootProject.name='someLib'" | "configured root project name"
    }

    @Unroll
    @ToBeFixedForInstantExecution(because = "Task.getProject() during execution")
    def "includes build identifier in dependency report with #display"() {
        file("buildSrc/settings.gradle") << """
            $settings
            include 'b1', 'b2'
        """

        file("buildSrc/build.gradle") << """
            allprojects { apply plugin: 'java' }
            dependencies { implementation project(':b1') }
            project(':b1') { dependencies { implementation project(':b2') } }
            classes.dependsOn tasks.dependencies
        """

        when:
        run()

        then:
        outputContains("""
runtimeClasspath - Runtime classpath of source set 'main'.
\\--- project :buildSrc:b1
     \\--- project :buildSrc:b2
""")

        where:
        settings                     | display
        ""                           | "default root project name"
        "rootProject.name='someLib'" | "configured root project name"
    }

    @Unroll
    def "includes build identifier in error message on failure to resolve dependencies of build with #display"() {
        def m = mavenRepo.module("org.test", "test", "1.2")

        given:
        file("buildSrc/settings.gradle") << settings << "\n"
        def buildSrc = file("buildSrc/build.gradle")
        buildSrc << """
            repositories {
                maven { url '$mavenRepo.uri' }
            }

            dependencies {
                implementation "org.test:test:1.2"
            }
        """
        file("buildSrc/src/main/java/Thing.java") << "class Thing { }"

        when:
        fails()

        then:
        failure.assertHasDescription("Execution failed for task ':buildSrc:compileJava'.")
        failure.assertHasCause("Could not resolve all files for configuration ':buildSrc:compileClasspath'.")
        failure.assertHasCause("""Could not find org.test:test:1.2.
Searched in the following locations:
  - ${m.pom.file.toURL()}
If the artifact you are trying to retrieve can be found in the repository but without metadata in 'Maven POM' format, you need to adjust the 'metadataSources { ... }' of the repository declaration.
Required by:
    project :buildSrc""")

        when:
        m.publish()
        m.artifact.file.delete()

        fails()

        then:
        failure.assertHasDescription("Execution failed for task ':buildSrc:compileJava'.")
        failure.assertHasCause("Could not resolve all files for configuration ':buildSrc:compileClasspath'.")
        failure.assertHasCause("Could not find test-1.2.jar (org.test:test:1.2).")

        where:
        settings                     | display
        ""                           | "default root project name"
        "rootProject.name='someLib'" | "configured root project name"
    }

    @Unroll
    def "includes build identifier in task failure error message with #display"() {
        file("buildSrc/settings.gradle") << settings << "\n"
        def buildSrc = file("buildSrc/build.gradle")
        buildSrc << """
            classes.doLast {
                throw new RuntimeException("broken")
            }
        """

        when:
        fails()

        then:
        failure.assertHasDescription("Execution failed for task ':buildSrc:classes'.")
        failure.assertHasCause("broken")

        where:
        settings                     | display
        ""                           | "default root project name"
        "rootProject.name='someLib'" | "configured root project name"
    }

    @Unroll
    def "includes build identifier in dependency resolution results with #display"() {
        given:
        file("buildSrc/settings.gradle") << """
            ${settings}
            include 'a'
        """
        file("buildSrc/build.gradle") << """
            dependencies {
                implementation project(':a')
            }
            project(':a') {
                apply plugin: 'java'
            }
            classes.doLast {
                def components = configurations.compileClasspath.incoming.resolutionResult.allComponents.id
                assert components.size() == 2
                assert components[0].build.name == 'buildSrc'
                assert components[0].build.currentBuild
                assert components[0].projectPath == ':'
                assert components[0].projectName == 'buildSrc'
                assert components[1].build.name == 'buildSrc'
                assert components[1].build.currentBuild
                assert components[1].projectPath == ':a'
                assert components[1].projectName == 'a'

                def selectors = configurations.runtimeClasspath.incoming.resolutionResult.allDependencies.requested
                assert selectors.size() == 1
                assert selectors[0].displayName == 'project :buildSrc:a'
                assert selectors[0].buildName == 'buildSrc'
                assert selectors[0].projectPath == ':a'
            }
        """

        expect:
        succeeds()

        where:
        settings                     | display
        ""                           | "default root project name"
        "rootProject.name='someLib'" | "configured root project name"
    }
}
