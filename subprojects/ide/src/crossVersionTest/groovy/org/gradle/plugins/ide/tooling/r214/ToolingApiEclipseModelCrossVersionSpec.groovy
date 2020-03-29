/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.plugins.ide.tooling.r214

import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.test.fixtures.maven.MavenFileRepository
import org.gradle.tooling.model.UnsupportedMethodException
import org.gradle.tooling.model.eclipse.EclipseExternalDependency
import org.gradle.tooling.model.eclipse.EclipseProject

class ToolingApiEclipseModelCrossVersionSpec extends ToolingApiSpecification {

    def setup() {
        def mavenRepo = new MavenFileRepository(file("maven-repo"))
        mavenRepo.module("org.example", "example-lib", "1.0").publish()

        settingsFile << "rootProject.name = 'root'; include 'sub'"
        buildFile << """allprojects { apply plugin: 'java' }
repositories { maven { url '${mavenRepo.uri}' } }
dependencies {
    compile project(':sub')
    compile 'org.example:example-lib:1.0'
}"""

    }

    @TargetGradleVersion(">=2.6 <2.14")
    def "older Gradle versions throw UnsupportedMethodException when classpath attributes are accessed"() {
        setup:
        EclipseProject rootProject = loadToolingModel(EclipseProject)
        Collection<EclipseExternalDependency> projectDependencies = rootProject.getProjectDependencies()
        Collection<EclipseExternalDependency> externalDependencies = rootProject.getClasspath()

        when:
        projectDependencies[0].getClasspathAttributes()

        then:
        thrown UnsupportedMethodException

        when:
        externalDependencies[0].getClasspathAttributes()

        then:
        thrown UnsupportedMethodException
    }

    @TargetGradleVersion(">=2.14 <3.0")
    def "each dependency has an empty list of classpath attributes"() {
        when:
        EclipseProject rootProject = loadToolingModel(EclipseProject)
        Collection<EclipseExternalDependency> projectDependencies = rootProject.getProjectDependencies()
        Collection<EclipseExternalDependency> externalDependencies = rootProject.getClasspath()

        then:
        projectDependencies.size() == 1
        projectDependencies[0].classpathAttributes.isEmpty()
        externalDependencies.size() == 1
        externalDependencies[0].classpathAttributes.isEmpty()
    }
}
