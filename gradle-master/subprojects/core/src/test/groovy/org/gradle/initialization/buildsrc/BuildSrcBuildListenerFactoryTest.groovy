/*
 * Copyright 2013 the original author or authors.
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

import org.gradle.StartParameter
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.component.BuildableJavaComponent
import org.gradle.api.internal.component.ComponentRegistry
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.project.ProjectState
import org.gradle.internal.service.ServiceRegistry
import spock.lang.Specification

class BuildSrcBuildListenerFactoryTest extends Specification {

    def startParameter = Mock(StartParameter)
    def projectState = Mock(ProjectState) {
        withMutableState(_) >> { args ->
            args[0].create()
        }
    }
    def component = Stub(BuildableJavaComponent) {
        getRuntimeClasspath() >> Stub(FileCollection)
    }
    def services = Mock(ServiceRegistry) {
        get(ComponentRegistry) >> Stub(ComponentRegistry) {
            getMainComponent() >> component
        }
    }
    def project = Mock(ProjectInternal) {
        getServices() >> services
        getMutationState() >> projectState
    }
    def gradle = Mock(GradleInternal) {
        getStartParameter() >> startParameter
        getRootProject() >> project
    }

    def "configures task names"() {
        def listener = new BuildSrcBuildListenerFactory().create()
        component.getBuildTasks() >> ['barBuild']

        when:
        listener.onConfigure(gradle)

        then:
        1 * startParameter.setTaskNames(['barBuild'])
    }

    def "executes buildSrc configuration action after projects are loaded"() {
        def action = Mock(Action)
        def listener = new BuildSrcBuildListenerFactory(action).create()

        when:
        listener.projectsLoaded(gradle)

        then:
        1 * action.execute(project)
    }
}
