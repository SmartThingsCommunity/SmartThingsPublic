/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.plugins.javascript.base

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class JavaScriptBasePluginTest extends Specification {
    Project project = ProjectBuilder.builder().build()

    def "extension is available"() {
        when:
        project.pluginManager.apply(JavaScriptBasePlugin)

        then:
        project.javaScript != null
    }

    def "can get public repo"() {
        when:
        project.pluginManager.apply(JavaScriptBasePlugin)

        then:
        project.repositories.javaScript.gradle()
        project.repositories.gradleJs instanceof MavenArtifactRepository
        MavenArtifactRepository repo = project.repositories.gradleJs as MavenArtifactRepository
        repo.url.toString() == JavaScriptRepositoriesExtension.GRADLE_PUBLIC_JAVASCRIPT_REPO_URL
    }

}
