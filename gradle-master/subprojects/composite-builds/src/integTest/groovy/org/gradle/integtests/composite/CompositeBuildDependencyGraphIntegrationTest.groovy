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

package org.gradle.integtests.composite

import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.integtests.fixtures.build.BuildTestFile
import org.gradle.integtests.fixtures.resolve.ResolveTestFixture
import spock.lang.Unroll

/**
 * Tests for resolving dependency graph with substitution within a composite build.
 */
class CompositeBuildDependencyGraphIntegrationTest extends AbstractCompositeBuildIntegrationTest {
    BuildTestFile buildB
    ResolveTestFixture resolve
    def buildArgs = []

    def setup() {
        mavenRepo.module("org.test", "buildB", "1.0").publish()

        resolve = new ResolveTestFixture(buildA.buildFile).expectDefaultConfiguration("runtime")

        buildB = multiProjectBuild("buildB", ['b1', 'b2']) {
            buildFile << """
                allprojects {
                    apply plugin: 'java-library'
                    version "2.0"

                    repositories {
                        maven { url "${mavenRepo.uri}" }
                    }
                }
"""
        }
        includedBuilds << buildB
    }

    def "reports failure to configure one participant build"() {
        given:
        def buildC = singleProjectBuild("buildC") {
            buildFile << """
                throw new RuntimeException('exception thrown on configure')
"""
        }
        includedBuilds << buildC

        when:
        checkDependenciesFails()

        then:
        failure.assertHasDescription("A problem occurred evaluating project ':buildC'.")
            .assertHasCause("exception thrown on configure")
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "does no substitution when no project matches external dependencies"() {
        given:
        mavenRepo.module("org.different", "buildB", "1.0").publish()
        mavenRepo.module("org.test", "buildC", "1.0").publish()

        buildA.buildFile << """
            dependencies {
                implementation "org.different:buildB:1.0"
                implementation "org.test:buildC:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            module("org.different:buildB:1.0")
            module("org.test:buildC:1.0")
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes external dependency with root project dependency"() {
        given:
        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }

        and:
        executed ":buildB:jar"
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "can resolve dependency graph without building artifacts"() {
        given:
        resolve.withoutBuildingArtifacts()

        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }

        and:
        notExecuted ":buildB:jar"
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes external dependencies with project dependencies using --include-build"() {
        given:
        singleProjectBuild("buildC") {
            buildFile << """
                apply plugin: 'java'
"""
        }
        withArgs(["--include-build", '../buildB', "--include-build", '../buildC'])
        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
                implementation "org.test:buildC:1.0"
            }
"""
        includedBuilds = []

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
            edge("org.test:buildC:1.0", "project :buildC", "org.test:buildC:1.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes external dependencies with subproject dependencies"() {
        given:
        buildA.buildFile << """
            dependencies {
                implementation "org.test:b1:1.0"
                implementation "org.test:b2:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:b1:1.0", "project :buildB:b1", "org.test:b1:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
            edge("org.test:b2:1.0", "project :buildB:b2", "org.test:b2:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes external dependency with project dependency from same participant build"() {
        given:
        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
            }
"""
        buildB.buildFile << """
            dependencies {
                implementation "org.test:b2:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
                edge("org.test:b2:1.0", "project :buildB:b2", "org.test:b2:2.0") {
                    configuration = "runtimeElements"
                    compositeSubstitute()
                }
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes external dependency with subproject dependency that has transitive dependencies"() {
        given:
        def transitive1 = mavenRepo.module("org.test", "transitive1").publish()
        mavenRepo.module("org.test", "transitive2").dependsOn(transitive1).publish()
        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
            }
"""
        buildB.buildFile << """
            dependencies {
                implementation "org.test:transitive2:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
                module("org.test:transitive2:1.0") {
                    module("org.test:transitive1:1.0")
                }
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes external dependency with subproject dependency that has transitive project dependencies"() {
        given:
        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
            }
"""
        buildB.settingsFile << """
include ':b1:b11'
"""
        buildB.buildFile << """
            dependencies {
                implementation project(':b1')
            }

            project(":b1") {
                dependencies {
                    implementation project("b11") // Relative project path
                }
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
                project(":buildB:b1", "org.test:b1:2.0") {
                    project(":buildB:b1:b11", "org.test:b11:2.0") {}
                }
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "honours excludes defined in substituted subproject dependency that has transitive dependencies"() {
        given:
        def transitive1 = mavenRepo.module("org.test", "transitive1").publish()
        mavenRepo.module("org.test", "transitive2").dependsOn(transitive1).publish()
        buildA.buildFile << """
            dependencies {
                implementation("org.test:buildB:1.0")
            }
"""
        buildB.buildFile << """
            dependencies {
                implementation("org.test:transitive2:1.0")  {
                    exclude module: 'transitive1'
                }
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
                module("org.test:transitive2:1.0")
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes transitive dependency of substituted project dependency"() {
        given:
        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
            }
"""
        buildB.buildFile << """
            dependencies {
                implementation "org.test:buildC:1.0"
            }
"""
        def buildC = singleProjectBuild("buildC") {
            buildFile << """
                apply plugin: 'java'
"""
        }
        includedBuilds << buildC

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
                edge("org.test:buildC:1.0", "project :buildC", "org.test:buildC:1.0") {
                    configuration = "runtimeElements"
                    compositeSubstitute()
                }
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes transitive dependency of non-substituted external dependency"() {
        given:
        mavenRepo.module("org.external", "external-dep", '1.0').dependsOn("org.test", "buildB", "1.0").publish()

        buildA.buildFile << """
            dependencies {
                implementation "org.external:external-dep:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            module("org.external:external-dep:1.0") {
                edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                    compositeSubstitute()
                }
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes forced direct dependency"() {
        given:
        buildA.buildFile << """
            dependencies {
                implementation("org.test:buildB:1.0") { force = true }
            }
"""

        when:
        executer.expectDeprecationWarning()
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes transitive dependency with forced version"() {
        given:
        mavenRepo.module("org.external", "external-dep", '1.0').dependsOn("org.test", "buildB", "1.0").publish()

        buildA.buildFile << """
            dependencies {
                implementation "org.external:external-dep:1.0"
            }
            configurations.runtimeClasspath.resolutionStrategy.force("org.test:buildB:5.0")
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            module("org.external:external-dep:1.0") {
                edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                    compositeSubstitute()
                }
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes transitive dependency based on result of resolution rules"() {
        given:
        mavenRepo.module("org.external", "external-dep", '1.0')
            .dependsOn("org.test", "something", "1.0")
            .dependsOn("org.other", "something-else", "1.0")
            .publish()

        buildA.buildFile << """
            dependencies {
                implementation "org.external:external-dep:1.0"
            }
            configurations.runtimeClasspath.resolutionStrategy {
                eachDependency { DependencyResolveDetails details ->
                    if (details.requested.name == 'something') {
                        details.useTarget "org.test:buildB:1.0"
                    }
                }
                dependencySubstitution {
                    substitute module("org.other:something-else:1.0") with module("org.test:b1:1.0")
                }
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            module("org.external:external-dep:1.0") {
                edge("org.test:something:1.0", "project :buildB", "org.test:buildB:2.0") {
                    compositeSubstitute()
                }
                edge("org.other:something-else:1.0", "project :buildB:b1", "org.test:b1:2.0") {
                    compositeSubstitute()
                }
            }
        }
    }

    @Unroll
    @ToBeFixedForInstantExecution(because = "composite builds")
    def "evaluates subprojects when substituting external dependencies with #name"() {
        given:
        buildA.buildFile << """
            dependencies {
                implementation "group.requires.subproject.evaluation:b1:1.0"
            }
"""

        buildB.file("b1", "build.gradle") << """
afterEvaluate {
    group = 'group.requires.subproject.evaluation'
}
"""

        when:
        withArgs(args)
        checkDependencies()

        then:
        checkGraph {
            edge("group.requires.subproject.evaluation:b1:1.0", "project :buildB:b1", "group.requires.subproject.evaluation:b1:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }

        where:
        name                  | args
        "regular build"       | []
        "configure on demand" | ["--configure-on-demand"]
        "parallel"            | ["--parallel"]
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "substitutes dependency in composite containing participants with same root directory name"() {
        given:
        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
                implementation "org.test:buildC:1.0"
            }
"""

        def buildC = rootDir.file("hierarchy", "buildB");
        buildC.file('settings.gradle') << """
            rootProject.name = 'buildC'
"""
        buildC.file('build.gradle') << """
            apply plugin: 'java'
            group = 'org.test'
            version = '1.0'
"""

        includeBuildAs(buildC, 'buildC')

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
            edge("org.test:buildC:1.0", "project :buildC", "org.test:buildC:1.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "can substitute dependencies in composite with duplicate publication if not involved in resolution"() {
        given:
        def buildC = multiProjectBuild("buildC", ['a2', 'b2', 'c1']) {
            buildFile << """
                allprojects {
                    apply plugin: 'java'
                }
"""
        }
        includedBuilds << buildC

        buildA.buildFile << """
            dependencies {
                implementation "org.test:b1:1.0"
                implementation "org.test:c1:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:b1:1.0", "project :buildB:b1", "org.test:b1:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
            edge("org.test:c1:1.0", "project :buildC:c1", "org.test:c1:1.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }
    }

    def "reports failure to resolve dependencies when substitution is ambiguous"() {
        given:
        def buildC = multiProjectBuild("buildC", ['a1', 'b1']) {
            buildFile << """
                allprojects {
                    apply plugin: 'java'
                    version = '3.0'
                }
"""
        }
        includedBuilds << buildC

        buildA.buildFile << """
            dependencies {
                implementation "org.test:b1:1.0"
            }
"""

        when:
        checkDependenciesFails()

        then:
        failure.assertHasCause("Module version 'org.test:b1:1.0' is not unique in composite: can be provided by [project :buildB:b1, project :buildC:b1].")
    }

    def "reports failure to resolve dependencies when substitution is ambiguous within single participant"() {
        given:
        buildB
        def buildC = multiProjectBuild("buildC", ['c1', 'c2']);
        buildC.settingsFile << """
            include ':nested:c1'
"""
        buildC.buildFile << """
            allprojects {
                apply plugin: 'java'
            }
"""
        includedBuilds << buildC

        buildA.buildFile << """
            dependencies {
                implementation "org.test:c1:1.0"
            }
"""

        when:
        checkDependenciesFails()

        then:
        failure.assertHasCause("Module version 'org.test:c1:1.0' is not unique in composite: can be provided by [project :buildC:c1, project :buildC:nested:c1].")
    }

    def "reports failure to resolve dependencies when transitive dependency substitution is ambiguous"() {
        given:
        transitiveDependencyIsAmbiguous("'org.test:b1:2.0'")

        when:
        checkDependenciesFails()

        then:
        failure.assertHasCause("Module version 'org.test:b1:2.0' is not unique in composite: can be provided by [project :buildB:b1, project :buildC:b1].")
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "resolve transitive project dependency that is ambiguous in the composite"() {
        given:
        transitiveDependencyIsAmbiguous("project(':b1')")

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
                project(":buildB:b1", "org.test:b1:2.0") {}
            }
        }
    }

    def transitiveDependencyIsAmbiguous(String dependencyNotation) {
        def buildC = multiProjectBuild("buildC", ['b1']) {
            buildFile << """
                allprojects {
                    apply plugin: 'java'
                    version = '3.0'
                }
"""
        }
        includedBuilds << buildC

        buildB.buildFile << """
            dependencies {
                implementation ${dependencyNotation}
            }
"""

        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
            }
"""
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "handles unused participant with no defined configurations"() {
        given:
        def buildC = singleProjectBuild("buildC")
        includedBuilds << buildC

        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildB:1.0"
            }
"""

        when:
        checkDependencies()

        then:
        checkGraph {
            edge("org.test:buildB:1.0", "project :buildB", "org.test:buildB:2.0") {
                configuration = "runtimeElements"
                compositeSubstitute()
            }
        }
    }

    def "reports failure when substituted project does not have requested configuration"() {
        given:
        def buildC = singleProjectBuild("buildC")
        includedBuilds << buildC

        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildC:1.0"
            }
"""

        when:
        checkDependenciesFails()

        then:
        failure.assertHasCause("Unable to find a matching configuration of project :buildC:\n" +
            "  - None of the consumable configurations have attributes.")
    }

    @ToBeFixedForInstantExecution(because = "composite builds")
    def "includes build identifier in error message on failure to resolve dependencies of included build"() {
        def m = mavenRepo.module("org.test", "test", "1.2")

        given:
        def buildC = singleProjectBuild("buildC")
        includedBuilds << buildC

        buildA.buildFile << """
            dependencies {
                implementation "org.test:buildC:1.0"
            }
        """
        buildC.buildFile << """
            repositories {
                maven { url '$mavenRepo.uri' }
            }

            configurations { 
                buildInputs 
                create('default')
            }
            
            dependencies {
                buildInputs "org.test:test:1.2"
            }
            
            task buildOutputs {
                inputs.files configurations.buildInputs
                doLast {
                    configurations.buildInputs.each { }
                }
            }
            
            artifacts {
                "default" file: file("out.jar"), builtBy: buildOutputs
            }
        """

        when:
        checkDependenciesFails()

        then:
        failure.assertHasDescription("Could not determine the dependencies of task ':buildC:buildOutputs'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':buildC:buildInputs'.")
        failure.assertHasCause("""Could not find org.test:test:1.2.
Searched in the following locations:
  - ${m.pom.file.toURL()}
If the artifact you are trying to retrieve can be found in the repository but without metadata in 'Maven POM' format, you need to adjust the 'metadataSources { ... }' of the repository declaration.
Required by:
    project :buildC""")

        when:
        m.publish()
        m.artifact.file.delete()

        checkDependenciesFails()

        then:
        failure.assertHasDescription("Execution failed for task ':buildC:buildOutputs'.")
        failure.assertHasCause("Could not resolve all files for configuration ':buildC:buildInputs'.")
        failure.assertHasCause("Could not find test-1.2.jar (org.test:test:1.2).")
    }

    private void withArgs(List<String> args) {
        buildArgs = args as List
    }

    private void checkDependencies() {
        resolve.prepare()
        execute(buildA, ":checkDeps", buildArgs)
    }

    private void checkDependenciesFails() {
        resolve.prepare()
        fails(buildA, ":checkDeps", buildArgs)
    }

    void checkGraph(@DelegatesTo(ResolveTestFixture.NodeBuilder) Closure closure) {
        resolve.expectGraph {
            root(":", "org.test:buildA:1.0", closure)
        }
    }
}
