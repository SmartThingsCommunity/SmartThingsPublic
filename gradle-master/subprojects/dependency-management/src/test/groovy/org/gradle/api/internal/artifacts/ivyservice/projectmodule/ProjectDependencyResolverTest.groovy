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
package org.gradle.api.internal.artifacts.ivyservice.projectmodule

import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.internal.artifacts.component.ComponentIdentifierFactory
import org.gradle.api.internal.project.ProjectState
import org.gradle.api.internal.project.ProjectStateRegistry
import org.gradle.internal.component.local.model.LocalComponentMetadata
import org.gradle.internal.component.local.model.TestComponentIdentifiers
import org.gradle.internal.component.model.ComponentOverrideMetadata
import org.gradle.internal.component.model.DefaultComponentOverrideMetadata
import org.gradle.internal.component.model.DependencyMetadata
import org.gradle.internal.resolve.ModuleVersionResolveException
import org.gradle.internal.resolve.result.BuildableComponentIdResolveResult
import org.gradle.internal.resolve.result.BuildableComponentResolveResult
import spock.lang.Specification

import static org.gradle.internal.component.local.model.TestComponentIdentifiers.newProjectId

class ProjectDependencyResolverTest extends Specification {
    final LocalComponentRegistry registry = Mock()
    final ComponentIdentifierFactory componentIdentifierFactory = Mock()
    final ProjectStateRegistry projectRegistry = Stub()
    final ProjectDependencyResolver resolver = new ProjectDependencyResolver(registry, componentIdentifierFactory, projectRegistry)

    def setup() {
        def projectState = Stub(ProjectState)
        _ * projectRegistry.stateFor(_) >> projectState
        _ * projectState.withMutableState(_) >> { Runnable action -> action.run() }
    }

    def "resolves project dependency"() {
        setup:
        def selector = TestComponentIdentifiers.newSelector(":project")
        def componentMetaData = Mock(LocalComponentMetadata)
        def result = Mock(BuildableComponentIdResolveResult)
        def dependencyMetaData = Stub(DependencyMetadata) {
            getSelector() >> selector
        }
        def id = newProjectId(":project")

        when:
        resolver.resolve(dependencyMetaData, null, null, result)

        then:
        1 * componentIdentifierFactory.createProjectComponentIdentifier(selector) >> id
        1 * registry.getComponent(id) >> componentMetaData
        1 * result.resolved(componentMetaData)
        0 * result._
    }

    def "resolves project component"() {
        setup:
        def componentMetaData = Mock(LocalComponentMetadata)
        def result = Mock(BuildableComponentResolveResult)
        def projectComponentId = newProjectId(":projectPath")

        when:
        resolver.resolve(projectComponentId, DefaultComponentOverrideMetadata.EMPTY, result)

        then:
        1 * registry.getComponent(projectComponentId) >> componentMetaData
        1 * result.resolved(componentMetaData)
        0 * result._
    }

    def "doesn't try to resolve non-project dependency"() {
        def result = Mock(BuildableComponentIdResolveResult)
        def dependencyMetaData = Stub(DependencyMetadata)
        def targetModuleId = Stub(ModuleIdentifier)

        when:
        resolver.resolve(dependencyMetaData, null, null, result)

        then:
        0 * registry.getComponent(_)
        0 * _
    }

    def "doesn't try to resolve non-project identifier"() {
        def result = Mock(BuildableComponentResolveResult)
        def componentIdentifier = Mock(ComponentIdentifier)
        def overrideMetaData = Mock(ComponentOverrideMetadata)

        when:
        resolver.resolve(componentIdentifier, overrideMetaData, result)

        then:
        0 * registry.getComponent(_)
        0 * _
    }

    def "adds failure to resolution result if project does not exist"() {
        def result = Mock(BuildableComponentResolveResult)
        def componentIdentifier = newProjectId(":doesnotexist")
        def overrideMetaData = Mock(ComponentOverrideMetadata)

        when:
        registry.getComponent(_) >> null
        and:
        resolver.resolve(componentIdentifier, overrideMetaData, result)

        then:
        1 * result.failed(_) >> { ModuleVersionResolveException failure ->
            assert failure.message == "project :doesnotexist not found."
        }
        0 * _
    }
}
