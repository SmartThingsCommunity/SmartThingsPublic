/*
 * Copyright 2014 the original author or authors.
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
package org.gradle.api.internal.artifacts.repositories.resolver

import com.google.common.collect.ImmutableList
import org.gradle.api.artifacts.ComponentMetadataListerDetails
import org.gradle.api.artifacts.ComponentMetadataSupplierDetails
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory
import org.gradle.api.internal.artifacts.repositories.metadata.DefaultIvyDescriptorMetadataSource
import org.gradle.api.internal.artifacts.repositories.metadata.ImmutableMetadataSources
import org.gradle.api.internal.artifacts.repositories.metadata.IvyMetadataArtifactProvider
import org.gradle.api.internal.artifacts.repositories.metadata.MetadataArtifactProvider
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport
import org.gradle.internal.action.ConfigurableRule
import org.gradle.internal.action.DefaultConfigurableRules
import org.gradle.internal.action.InstantiatingAction
import org.gradle.internal.component.model.DefaultComponentOverrideMetadata
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.resolve.result.DefaultBuildableModuleComponentMetaDataResolveResult
import org.gradle.internal.resource.local.FileResourceRepository
import org.gradle.internal.resource.local.FileStore
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder
import org.gradle.internal.resource.transfer.CacheAwareExternalResourceAccessor
import org.gradle.util.TestUtil
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier.newId
import static org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult.State.Missing

class IvyResolverTest extends Specification {
    def externalResourceAccessor = Mock(CacheAwareExternalResourceAccessor)

    static ModuleIdentifier mid(String group, String name) {
        DefaultModuleIdentifier.newId(group, name)
    }

    def "has useful string representation"() {
        expect:
        def resolver = resolver()
        resolver.toString() == "Ivy repository 'repo'"
    }

    def "resolvers are differentiated by m2compatible flag"() {
        given:
        def resolver1 = resolver()
        def resolver2 = resolver()

        resolver1.addIvyPattern(new IvyResourcePattern("ivy1"))
        resolver1.addArtifactPattern(new IvyResourcePattern("artifact1"))
        resolver2.addIvyPattern(new IvyResourcePattern("ivy1"))
        resolver2.addArtifactPattern(new IvyResourcePattern("artifact1"))
        resolver2.m2compatible = true

        expect:
        resolver1.id != resolver2.id
    }

    @Unroll
    def "remote access fails directly for module id #moduleId with layout #layoutPattern"() {
        given:
        def overrideMetadata = DefaultComponentOverrideMetadata.EMPTY
        def result = new DefaultBuildableModuleComponentMetaDataResolveResult()

        when:
        resolver(layoutPattern).getRemoteAccess().resolveComponentMetaData(moduleId, overrideMetadata, result)

        then:
        result.state == Missing
        0 * externalResourceAccessor._
        0 * _

        where:
        moduleId                        | layoutPattern
        newId(mid("", ""), "")          | IvyArtifactRepository.GRADLE_IVY_PATTERN
        newId(mid("", ""), "")          | "[module]"
        newId(mid("group", ""), "1")    | IvyArtifactRepository.GRADLE_IVY_PATTERN
        newId(mid("group", ""), "1")    | "[module]"
        newId(mid("", "name"), "1")     | IvyArtifactRepository.GRADLE_IVY_PATTERN
        newId(mid("", "name"), "1")     | "[organisation]/[module]"
        newId(mid("group", "name"), "") | IvyArtifactRepository.GRADLE_IVY_PATTERN
        newId(mid("group", "name"), "") | "[module]-[revision]"
        newId(mid("", "name"), "")      | "([branch])[organisation]/[module]-[revision]"
        newId(mid("", "name"), "")      | "([organisation])/[module]-[revision]"
        newId(mid("", "name"), "")      | "([branch])[organization]/[module]-[revision]"
        newId(mid("", "name"), "")      | "([organization])/[module]-[revision]"
    }

    @Unroll
    def "remote access attempts to access metadata for id #moduleId with layout #layoutPattern"() {
        given:
        def overrideMetadata = DefaultComponentOverrideMetadata.EMPTY
        def result = new DefaultBuildableModuleComponentMetaDataResolveResult()

        when:
        resolver(layoutPattern).getRemoteAccess().resolveComponentMetaData(moduleId, overrideMetadata, result)

        then:
        1 * externalResourceAccessor.getResource(_, _, _, _) >> null
        0 * _

        where:
        moduleId                         | layoutPattern
        newId(mid("group", "name"), "1") | IvyArtifactRepository.GRADLE_IVY_PATTERN
        newId(mid("group", "name"), "1") | "[module]"
        newId(mid("", "name"), "1")      | "[module]"
        newId(mid("", "name"), "1")      | "[module]-[revision]"
        newId(mid("group", "name"), "")  | "[module]"
        newId(mid("group", "name"), "")  | "[organisation]/[module]"
        newId(mid("", "name"), "1")      | "([organisation]/)[module]-[revision]"
        newId(mid("group", "name"), "")  | "[organisation]/[module]-([revision])"
        newId(mid("group", "name"), "")  | "[organization]/[module]"
        newId(mid("", "name"), "1")      | "([organization]/)[module]-[revision]"
        newId(mid("group", "name"), "")  | "[organization]/[module]-([revision])"
    }


    def "resolvers are differentiated by useGradleMetadata flag"() {
        given:
        def resolver1 = resolver()
        def resolver2 = resolver(null, true)

        resolver1.addIvyPattern(new IvyResourcePattern("ivy1"))
        resolver1.addArtifactPattern(new IvyResourcePattern("artifact1"))
        resolver2.addIvyPattern(new IvyResourcePattern("ivy1"))
        resolver2.addArtifactPattern(new IvyResourcePattern("artifact1"))

        expect:
        resolver1.id != resolver2.id
    }

    def "resolvers are differentiated by alwaysProvidesMetadataForModules flag"() {
        given:
        def resolver1 = resolver(null, false, false)
        def resolver2 = resolver(null, false, true)

        resolver1.addIvyPattern(new IvyResourcePattern("ivy1"))
        resolver1.addArtifactPattern(new IvyResourcePattern("artifact1"))
        resolver2.addIvyPattern(new IvyResourcePattern("ivy1"))
        resolver2.addArtifactPattern(new IvyResourcePattern("artifact1"))

        expect:
        resolver1.id != resolver2.id
    }

    private IvyResolver resolver(String ivyPattern = null, boolean useGradleMetadata = false, boolean alwaysProvidesMetadataForModules = false) {
        def transport = Stub(RepositoryTransport)
        transport.resourceAccessor >> externalResourceAccessor

        MetadataArtifactProvider metadataArtifactProvider = new IvyMetadataArtifactProvider()
        def fileResourceRepository = Stub(FileResourceRepository)
        def moduleIdentifierFactory = Stub(ImmutableModuleIdentifierFactory)
        ImmutableMetadataSources metadataSources = Stub() {
            sources() >> {
                ImmutableList.of(new DefaultIvyDescriptorMetadataSource(
                    metadataArtifactProvider,
                    null,
                    fileResourceRepository,
                    TestUtil.checksumService
                ))
            }
            appendId(_) >> { args ->
                args[0].putBoolean(useGradleMetadata)
                args[0].putBoolean(alwaysProvidesMetadataForModules)
            }
        }

        def supplier = new InstantiatingAction<ComponentMetadataSupplierDetails>(DefaultConfigurableRules.of(Stub(ConfigurableRule)), TestUtil.instantiatorFactory().inject(), Stub(InstantiatingAction.ExceptionHandler))
        def lister = new InstantiatingAction<ComponentMetadataListerDetails>(DefaultConfigurableRules.of(Stub(ConfigurableRule)), TestUtil.instantiatorFactory().inject(), Stub(InstantiatingAction.ExceptionHandler))

        new IvyResolver(
                "repo",
                transport,
                Stub(LocallyAvailableResourceFinder),
                false,
                Stub(FileStore),
                moduleIdentifierFactory,
                supplier,
                lister,
                metadataSources,
                metadataArtifactProvider, Mock(Instantiator),
                TestUtil.checksumService).with {
            if (ivyPattern) {
                it.addDescriptorLocation(URI.create(""), ivyPattern)
            }
            it
        }
    }
}
