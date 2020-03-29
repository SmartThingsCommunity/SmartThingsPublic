/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.artifacts.repositories

import org.gradle.api.InvalidUserDataException
import org.gradle.api.internal.artifacts.DependencyManagementTestUtil
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory
import org.gradle.api.internal.artifacts.repositories.metadata.IvyMutableModuleMetadataFactory
import org.gradle.api.internal.artifacts.repositories.resolver.IvyResolver
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory
import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.internal.file.TestFiles
import org.gradle.api.internal.filestore.DefaultArtifactIdentifierFileStore
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.component.external.model.ModuleComponentArtifactMetadata
import org.gradle.internal.instantiation.InstantiatorFactory
import org.gradle.internal.resource.ExternalResourceRepository
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder
import org.gradle.util.TestUtil
import spock.lang.Specification

class DefaultFlatDirArtifactRepositoryTest extends Specification {
    final FileCollectionFactory fileCollectionFactory = Mock()
    final ExternalResourceRepository resourceRepository = Mock()
    final RepositoryTransport repositoryTransport = Mock()
    final RepositoryTransportFactory transportFactory = Mock()
    final LocallyAvailableResourceFinder<ModuleComponentArtifactMetadata> locallyAvailableResourceFinder = Mock()
    final DefaultArtifactIdentifierFileStore artifactIdentifierFileStore = Stub()
    final ImmutableModuleIdentifierFactory moduleIdentifierFactory = Mock()
    final IvyMutableModuleMetadataFactory metadataFactory = DependencyManagementTestUtil.ivyMetadataFactory()

    final DefaultFlatDirArtifactRepository repository = new DefaultFlatDirArtifactRepository(fileCollectionFactory, transportFactory, locallyAvailableResourceFinder, artifactIdentifierFileStore, moduleIdentifierFactory, metadataFactory, Mock(InstantiatorFactory), Mock(ObjectFactory), TestUtil.checksumService)

    def "creates a repository with multiple root directories"() {
        given:
        def dir1 = new File('a')
        def dir2 = new File('b')
        _ * fileCollectionFactory.resolving(['a', 'b']) >> TestFiles.fixed(dir1, dir2)
        _ * repositoryTransport.repository >> resourceRepository

        and:
        repository.name = 'repo-name'
        repository.dirs('a', 'b')

        when:
        def repo = repository.createResolver()

        then:
        1 * transportFactory.createFileTransport('repo-name') >> repositoryTransport

        and:
        repo instanceof IvyResolver
        def expectedPatterns = [
                "${dir1.toURI()}/[artifact]-[revision](-[classifier]).[ext]",
                "${dir1.toURI()}/[artifact](-[classifier]).[ext]",
                "${dir2.toURI()}/[artifact]-[revision](-[classifier]).[ext]",
                "${dir2.toURI()}/[artifact](-[classifier]).[ext]"
        ]
        repo.ivyPatterns == []
        repo.artifactPatterns == expectedPatterns
    }

    def "fails when no directories specified"() {
        given:
        _ * fileCollectionFactory.resolving(_) >> TestFiles.empty()

        when:
        repository.createResolver()

        then:
        InvalidUserDataException e = thrown()
        e.message == 'You must specify at least one directory for a flat directory repository.'
    }
}
