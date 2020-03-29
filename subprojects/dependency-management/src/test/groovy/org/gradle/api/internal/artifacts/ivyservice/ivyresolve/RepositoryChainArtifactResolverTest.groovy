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

package org.gradle.api.internal.artifacts.ivyservice.ivyresolve

import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ArtifactSet
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.specs.ExcludeSpec
import org.gradle.api.internal.artifacts.type.ArtifactTypeRegistry
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.internal.component.model.ComponentArtifactMetadata
import org.gradle.internal.component.model.ComponentArtifacts
import org.gradle.internal.component.model.ComponentResolveMetadata
import org.gradle.internal.component.model.ConfigurationMetadata
import org.gradle.internal.component.model.ImmutableModuleSources
import org.gradle.internal.component.model.ModuleSource
import org.gradle.internal.resolve.result.DefaultBuildableArtifactResolveResult
import spock.lang.Specification

class RepositoryChainArtifactResolverTest extends Specification {
    final artifact = Mock(ComponentArtifactMetadata)
    final component = Mock(ComponentResolveMetadata)
    final originalSource = Mock(ModuleSource)
    final result = new DefaultBuildableArtifactResolveResult()

    def repo1 = Mock(ModuleComponentRepository) {
        getId() >> "repo1"
    }
    def localAccess2 = Mock(ModuleComponentRepositoryAccess)
    def remoteAccess2 = Mock(ModuleComponentRepositoryAccess)
    def repo2 = Mock(ModuleComponentRepository) {
        getLocalAccess() >> localAccess2
        getRemoteAccess() >> remoteAccess2
        getId() >> "repo2"
    }
    def repo2Source = new RepositoryChainModuleSource(repo2)

    final RepositoryChainArtifactResolver resolver = new RepositoryChainArtifactResolver()

    def setup() {
        resolver.add(repo1)
        resolver.add(repo2)
    }

    def "uses module artifacts from local access to repository defined by module source"() {
        def artifacts = Mock(ComponentArtifacts)
        def configuration = Stub(ConfigurationMetadata)
        def artifactTypeRegistry = Stub(ArtifactTypeRegistry)
        def exclusion = Stub(ExcludeSpec)
        def artifactSet = Stub(ArtifactSet)

        when:
        def result = resolver.resolveArtifacts(component, configuration, artifactTypeRegistry, exclusion, ImmutableAttributes.EMPTY)

        then:
        result == artifactSet

        and:
        _ * component.getSources() >> ImmutableModuleSources.of(repo2Source)
        1 * repo2.artifactCache >> [:]
        1 * repo2.getLocalAccess() >> localAccess2
        1 * localAccess2.resolveArtifacts(component, configuration, _) >> {
            it[2].resolved(artifacts)
        }
        1 * artifacts.getArtifactsFor(component, configuration, resolver, [:], artifactTypeRegistry, exclusion, ImmutableAttributes.EMPTY) >> artifactSet
        0 * _._
    }

    def "uses module artifacts from remote access to repository defined by module source"() {
        def artifacts = Mock(ComponentArtifacts)
        def configuration = Stub(ConfigurationMetadata)
        def artifactTypeRegistry = Stub(ArtifactTypeRegistry)
        def exclusion = Stub(ExcludeSpec)
        def artifactSet = Stub(ArtifactSet)

        when:
        def result = resolver.resolveArtifacts(component, configuration, artifactTypeRegistry, exclusion, ImmutableAttributes.EMPTY)

        then:
        result == artifactSet

        and:
        _ * component.getSources() >> ImmutableModuleSources.of(repo2Source)
        1 * repo2.artifactCache >> [:]
        1 * repo2.getLocalAccess() >> localAccess2
        1 * localAccess2.resolveArtifacts(component, configuration, _)
        1 * repo2.getRemoteAccess() >> remoteAccess2
        1 * remoteAccess2.resolveArtifacts(component, configuration, _) >> {
            it[2].resolved(artifacts)
        }
        1 * artifacts.getArtifactsFor(component, configuration, resolver, [:], artifactTypeRegistry, exclusion, ImmutableAttributes.EMPTY) >> artifactSet
        0 * _._
    }

    def "locates artifact with local access in repository defined by module source"() {
        def artifactFile = Mock(File)
        def artifact = Mock(ComponentArtifactMetadata)
        def moduleSources = ImmutableModuleSources.of(repo2Source)
        when:
        resolver.resolveArtifact(artifact, moduleSources, result)

        then:
        1 * repo2.getLocalAccess() >> localAccess2
        1 * localAccess2.resolveArtifact(artifact, moduleSources, result) >> {
            it[2].resolved(artifactFile)
        }
        0 * _._

        and:
        result.result == artifactFile
    }

    def "locates artifact with remote access in repository defined by module source"() {
        def artifactFile = Mock(File)
        def artifact = Mock(ComponentArtifactMetadata)
        def moduleSources = ImmutableModuleSources.of(repo2Source)
        when:
        resolver.resolveArtifact(artifact, moduleSources, result)

        then:
        1 * repo2.getLocalAccess() >> localAccess2
        1 * localAccess2.resolveArtifact(artifact, moduleSources, result)
        1 * repo2.getRemoteAccess() >> remoteAccess2
        1 * remoteAccess2.resolveArtifact(artifact, moduleSources, result) >> {
            it[2].resolved(artifactFile)
        }
        0 * _._

        and:
        result.result == artifactFile
    }
}
