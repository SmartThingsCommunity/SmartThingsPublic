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

package org.gradle.integtests.resolve

import org.gradle.integtests.fixtures.AbstractDependencyResolutionTest
import org.gradle.integtests.fixtures.FluidDependenciesResolveRunner
import org.junit.runner.RunWith

@RunWith(FluidDependenciesResolveRunner)
class FilteredConfigurationIntegrationTest extends AbstractDependencyResolutionTest {
    def "can query files for filtered first level dependencies"() {
        mavenRepo.module("group", "test1", "1.0").publish()
        mavenRepo.module("group", "test2", "1.0").publish()

        settingsFile << """
rootProject.name = "main"
include "child1", "child2"
"""
        buildFile << """
allprojects {
    repositories {
        maven { url '${mavenRepo.uri}' }
    }
    configurations { 
        compile
        create('default') { extendsFrom compile }
    }
}
artifacts {
    compile file("main.jar")
}
dependencies {
    compile files("lib.jar")
    compile "group:test1:1.0"
    compile project(':child1')
    compile project(':child2')
}
project(':child1') {
    artifacts {
        compile file("child1.jar")
    }
    dependencies {
        compile files("child1-lib.jar")
        compile "group:test2:1.0"
    }
}
project(':child2') {
    artifacts {
        compile file("child2.jar")
    }
}

task verify {
    doLast {
        println "file-dependencies: " + configurations.compile.files { it instanceof FileCollectionDependency }.collect { it.name }
        println "file-dependencies resolved-config: " + configurations.compile.resolvedConfiguration.getFiles { it instanceof FileCollectionDependency }.collect { it.name }
        println "file-dependencies artifacts: " + configurations.compile.resolvedConfiguration.lenientConfiguration.getArtifacts { it instanceof FileCollectionDependency }.collect { it.file.name }
        println "external-dependencies: " + configurations.compile.files { it instanceof ExternalDependency }.collect { it.name }
        println "external-dependencies resolved-config: " + configurations.compile.resolvedConfiguration.getFiles { it instanceof ExternalDependency }.collect { it.name }
        println "external-dependencies artifacts: " + configurations.compile.resolvedConfiguration.lenientConfiguration.getArtifacts { it instanceof ExternalDependency }.collect { it.file.name }
        println "child1-dependencies: " + configurations.compile.files { it instanceof ProjectDependency && it.dependencyProject.name == 'child1' }.collect { it.name }
        println "child1-dependencies resolved-config: " + configurations.compile.resolvedConfiguration.getFiles { it instanceof ProjectDependency && it.dependencyProject.name == 'child1' }.collect { it.name }
        println "child1-dependencies artifacts: " + configurations.compile.resolvedConfiguration.lenientConfiguration.getArtifacts { it instanceof ProjectDependency && it.dependencyProject.name == 'child1' }.collect { it.file.name }
    }
}
"""

        when:
        run "verify"

        then:
        outputContains("file-dependencies: [lib.jar]")
        outputContains("file-dependencies resolved-config: [lib.jar]")
        outputContains("file-dependencies artifacts: []")
        outputContains("external-dependencies: [test1-1.0.jar]")
        outputContains("external-dependencies resolved-config: [test1-1.0.jar]")
        outputContains("external-dependencies artifacts: [test1-1.0.jar]")
        outputContains("child1-dependencies: [child1.jar, child1-lib.jar, test2-1.0.jar]")
        outputContains("child1-dependencies resolved-config: [child1.jar, child1-lib.jar, test2-1.0.jar]")
        outputContains("child1-dependencies artifacts: [child1.jar, test2-1.0.jar]")
    }

    def "can query files for filtered first level dependencies when there is a cycle in the dependency graph"() {
        mavenRepo.module("group", "test1", "1.0").publish()
        mavenRepo.module("group", "test2", "1.0").publish()

        settingsFile << """
rootProject.name = "main"
include "child1", "child2"
"""
        buildFile << """
allprojects {
    repositories {
        maven { url '${mavenRepo.uri}' }
    }
    configurations { 
        compile
        create('default') { extendsFrom compile }
    }
}
dependencies {
    compile files("lib.jar")
    compile "group:test1:1.0"
    compile project(':child1')
}
artifacts {
    compile file("main.jar")
}
project(':child1') {
    artifacts {
        compile file("child1.jar")
    }
    dependencies {
        compile files("child1-lib.jar")
        compile "group:test2:1.0"
        compile project(":")
    }
}

task verify {
    doLast {
        println "external-dependencies: " + configurations.compile.files { it instanceof ExternalDependency }.collect { it.name }
        println "external-dependencies artifacts: " + configurations.compile.resolvedConfiguration.lenientConfiguration.getArtifacts { it instanceof ExternalDependency }.collect { it.file.name }
        println "child1-dependencies: " + configurations.compile.files { it instanceof ProjectDependency && it.dependencyProject.name == 'child1' }.collect { it.name }
        println "child1-dependencies artifacts: " + configurations.compile.resolvedConfiguration.lenientConfiguration.getArtifacts { it instanceof ProjectDependency && it.dependencyProject.name == 'child1' }.collect { it.file.name }
    }
}
"""

        when:
        run "verify"

        then:
        outputContains("external-dependencies: [test1-1.0.jar]")
        outputContains("external-dependencies artifacts: [test1-1.0.jar]")
        outputContains("child1-dependencies: [child1.jar, child1-lib.jar, test2-1.0.jar, main.jar, lib.jar, test1-1.0.jar]")
        outputContains("child1-dependencies artifacts: [child1.jar, test2-1.0.jar, main.jar, test1-1.0.jar]")
    }

    // Note: this captures existing behaviour (all files are built) rather than desired behaviour (only those files reachable from selected deps are built)
    def "can use filtered configuration as task input"() {
        settingsFile << """
rootProject.name = "main"
include "child1", "child2"
"""
        buildFile << """
allprojects {
    configurations { 
        compile
        create('default') { extendsFrom compile }
    }
    task jar {
        outputs.file file("\${project.name}.jar")
    }
    task lib {
        outputs.file file("\${project.name}-lib.jar")
    }
}
artifacts {
    compile file: jar.outputs.files.singleFile, builtBy: jar
}
dependencies {
    compile lib.outputs.files
    compile project(':child1')
}
project(':child1') {
    artifacts {
        compile file: jar.outputs.files.singleFile, builtBy: jar
    }
    dependencies {
        compile lib.outputs.files
    }
}

task verify {
    inputs.files configurations.compile.fileCollection { it instanceof ProjectDependency }
}
"""

        when:
        run "verify"

        then:
        // Should not be including ':lib' as it's not required
        result.assertTasksExecuted(":lib", ":child1:jar", ":child1:lib", ":verify")
    }

}
