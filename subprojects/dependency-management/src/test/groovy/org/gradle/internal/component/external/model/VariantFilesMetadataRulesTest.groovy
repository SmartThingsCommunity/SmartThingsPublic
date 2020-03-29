/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.internal.component.external.model

import com.google.common.collect.ImmutableListMultimap
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.MutableVariantFilesMetadata
import org.gradle.api.attributes.Attribute
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.artifacts.DependencyManagementTestUtil
import org.gradle.api.internal.artifacts.JavaEcosystemSupport
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.gradle.api.internal.attributes.DefaultAttributesSchema
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.internal.component.external.descriptor.Artifact
import org.gradle.internal.component.external.descriptor.Configuration
import org.gradle.internal.component.external.descriptor.MavenScope
import org.gradle.internal.component.external.model.ivy.IvyDependencyDescriptor
import org.gradle.internal.component.external.model.maven.MavenDependencyDescriptor
import org.gradle.internal.component.external.model.maven.MavenDependencyType
import org.gradle.internal.component.model.ComponentAttributeMatcher
import org.gradle.internal.component.model.DefaultIvyArtifactName
import org.gradle.internal.component.model.LocalComponentDependencyMetadata
import org.gradle.util.AttributeTestUtil
import org.gradle.util.SnapshotTestUtil
import org.gradle.util.TestUtil
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.internal.component.external.model.DefaultModuleComponentSelector.newSelector

class VariantFilesMetadataRulesTest extends Specification {
    @Shared versionIdentifier = new DefaultModuleVersionIdentifier("org.test", "producer", "1.0")
    @Shared componentIdentifier = DefaultModuleComponentIdentifier.newId(versionIdentifier)
    @Shared attributes = AttributeTestUtil.attributesFactory().of(Attribute.of("someAttribute", String), "someValue")
    @Shared schema = createSchema()
    @Shared mavenMetadataFactory = DependencyManagementTestUtil.mavenMetadataFactory()
    @Shared ivyMetadataFactory = DependencyManagementTestUtil.ivyMetadataFactory()
    @Shared defaultVariant

    private DefaultAttributesSchema createSchema() {
        def schema = new DefaultAttributesSchema(new ComponentAttributeMatcher(), TestUtil.instantiatorFactory(), SnapshotTestUtil.valueSnapshotter())
        DependencyManagementTestUtil.platformSupport().configureSchema(schema)
        JavaEcosystemSupport.configureSchema(schema, TestUtil.objectFactory())
        schema
    }

    private ivyComponentMetadata(String[] deps) {
        def dependencies = deps.collect { name ->
            new IvyDependencyDescriptor(newSelector(DefaultModuleIdentifier.newId('org.test', name), '1.0'), ImmutableListMultimap.of('runtime', 'runtime'))
        }
        def runtimeConf = new Configuration('runtime', true, true, [])
        def defaultConf = new Configuration('default', true, true, ['runtime'])
        def artifact = new Artifact(new DefaultIvyArtifactName('producer', 'jar', 'jar'), ['runtime'] as Set)
        ivyMetadataFactory.create(componentIdentifier, dependencies, [defaultConf, runtimeConf], [artifact], [])
    }

    private mavenComponentMetadata(String[] deps) {
        def dependencies = deps.collect { name ->
            new MavenDependencyDescriptor(MavenScope.Compile, MavenDependencyType.DEPENDENCY, newSelector(DefaultModuleIdentifier.newId("org.test", name), "1.0"), null, [])
        }
        def metadata = mavenMetadataFactory.create(componentIdentifier, dependencies)
        metadata.getVariantMetadataRules().setVariantDerivationStrategy(new JavaEcosystemVariantDerivationStrategy())
        metadata
    }

    private gradleComponentMetadata(String[] deps) {
        def metadata = mavenMetadataFactory.create(componentIdentifier, [])
        defaultVariant = metadata.addVariant("runtime", attributes)
        deps.each { name ->
            defaultVariant.addDependency("org.test", name, new DefaultMutableVersionConstraint("1.0"), [], null, ImmutableAttributes.EMPTY, [], false, null)
        }
        defaultVariant.addFile("producer-1.0.jar", "producer-1.0.jar")
        metadata
    }

    @Unroll
    def "variant file metadata rules are evaluated once and lazily for #metadataType metadata"() {
        given:
        def rule = Mock(Action)

        when:
        metadata.getVariantMetadataRules().addVariantFilesAction(new VariantMetadataRules.VariantAction<MutableVariantFilesMetadata>({ true }, rule))
        def variant = selectTargetConfigurationMetadata(metadata)

        then:
        0 * rule.execute(_)
        when:
        variant.artifacts

        then:
        1 * rule.execute(_)

        when:
        variant.artifacts
        variant.artifacts
        variant.artifacts

        then:
        0 * rule.execute(_)

        where:
        metadataType | metadata
        "maven"      | mavenComponentMetadata()
        "ivy"        | ivyComponentMetadata()
        "gradle"     | gradleComponentMetadata()
    }

    @Unroll
    def "variant file metadata rules are not evaluated if their variant is not selected for #metadataType metadata"() {
        given:
        def rule = Mock(Action)

        when:
        metadata.getVariantMetadataRules().addVariantFilesAction(new VariantMetadataRules.VariantAction<MutableVariantFilesMetadata>({ false }, rule))
        selectTargetConfigurationMetadata(metadata).artifacts

        then:
        0 * rule.execute(_)

        where:
        metadataType | metadata
        "maven"      | mavenComponentMetadata()
        "ivy"        | ivyComponentMetadata()
        "gradle"     | gradleComponentMetadata()
    }

    @Unroll
    def "new variant can be added to #metadataType metadata"() {
        when:
        metadata.getVariantMetadataRules().addVariant("new-variant", "runtime", false)
        def immutableMetadata = metadata.asImmutable()
        def variants = immutableMetadata.variantsForGraphTraversal.get()
        def baseVariant = variants.find { it.name == 'runtime' }
        if (metadataType == "ivy") {
            // no variants are derived for plain ivy, but we can use a ivy configuration
            baseVariant = immutableMetadata.getConfiguration('runtime')
        }

        then:
        variants.size() == initialVariantCount + 1
        variants.last().name == 'new-variant'
        variants.last().attributes == baseVariant.attributes
        variants.last().capabilities == baseVariant.capabilities
        variants.last().dependencies == baseVariant.dependencies
        variants.last().artifacts.size() == 1
        variants.last().artifacts[0].id.displayName == 'producer-1.0.jar (org.test:producer:1.0)'

        where:
        metadataType | metadata                       | initialVariantCount
        "maven"      | mavenComponentMetadata('dep')  | 6 // default derivation strategy for maven
        "ivy"        | ivyComponentMetadata('dep')    | 0 // there is no derivation strategy for ivy
        "gradle"     | gradleComponentMetadata('dep') | 1 // 'runtime' added in test setup
    }

    @Unroll
    def "new variant can be added to #metadataType metadata without base"() {
        when:
        metadata.getVariantMetadataRules().addVariant("new-variant")
        def immutableMetadata = metadata.asImmutable()
        def variants = immutableMetadata.variantsForGraphTraversal.get()

        then:
        variants.size() == initialVariantCount + 1
        variants.last().name == 'new-variant'
        variants.last().attributes == immutableMetadata.attributes
        variants.last().capabilities == ImmutableCapabilities.EMPTY
        variants.last().dependencies == []
        variants.last().artifacts.empty

        where:
        metadataType | metadata                       | initialVariantCount
        "maven"      | mavenComponentMetadata('dep')  | 6 // default derivation strategy for maven
        "ivy"        | ivyComponentMetadata('dep')    | 0 // there is no derivation strategy for ivy
        "gradle"     | gradleComponentMetadata('dep') | 1 // 'runtime' added in test setup
    }

    @Unroll
    def "base variant metadata rules are not evaluated if the new variant is not selected for #metadataType metadata"() {
        given:
        def rule = Mock(Action)

        when:
        metadata.variantMetadataRules.addVariant('new-variant', 'runtime', false)
        metadata.variantMetadataRules.addVariantFilesAction(new VariantMetadataRules.VariantAction<MutableVariantFilesMetadata>({ true }, rule))
        def newVariant =  metadata.asImmutable().variantsForGraphTraversal.get().find { it.name == 'new-variant' }

        then:
        0 * rule.execute(_)
        when:
        newVariant.artifacts

        then:
        2 * rule.execute(_)

        when:
        newVariant.artifacts
        newVariant.artifacts
        newVariant.artifacts

        then:
        0 * rule.execute(_)

        where:
        metadataType | metadata
        "maven"      | mavenComponentMetadata()
        "ivy"        | ivyComponentMetadata()
        "gradle"     | gradleComponentMetadata()
    }

    @Unroll
    def "throws error for non-existing base in #metadataType metadata"() {
        when:
        metadata.getVariantMetadataRules().addVariant("new-variant", "not-exist", false)
        def immutableMetadata = metadata.asImmutable()
        immutableMetadata.variantsForGraphTraversal.get()

        then:
        InvalidUserDataException e = thrown()
        e.message == "$baseType 'not-exist' not defined in module org.test:producer:1.0"

        where:
        metadataType | metadata                       | baseType
        "maven"      | mavenComponentMetadata('dep')  | 'Variant'
        "ivy"        | ivyComponentMetadata('dep')    | 'Configuration'
        "gradle"     | gradleComponentMetadata('dep') | 'Variant'
    }

    @Unroll
    def "does not add a variant for non-existing base in #metadataType metadata if lenient"() {
        given:
        def rule = Mock(Action)

        when:
        metadata.getVariantMetadataRules().addVariant("new-variant", "not-exist", true)
        metadata.variantMetadataRules.addVariantFilesAction(new VariantMetadataRules.VariantAction<MutableVariantFilesMetadata>({ true }, rule))
        def immutableMetadata = metadata.asImmutable()
        def variants = immutableMetadata.variantsForGraphTraversal.get()

        then:
        0 * rule.execute(_)
        variants.size() == initialVariantCount
        !variants.any { it.name == 'new-variant' }
        !variants.any { it.name == 'not-exist' }

        where:
        metadataType | metadata                       | initialVariantCount
        "maven"      | mavenComponentMetadata('dep')  | 6 // default derivation strategy for maven
        "ivy"        | ivyComponentMetadata('dep')    | 0 // there is no derivation strategy for ivy
        "gradle"     | gradleComponentMetadata('dep') | 1 // 'runtime' added in test setup
    }

    @Unroll
    def "variant file metadata rules can add files to #metadataType metadata"() {
        given:
        def rule = { MutableVariantFilesMetadata files ->
            files.addFile("added1.zip")
            files.addFile("added2", "../path.jar")
        }

        when:
        metadata.getVariantMetadataRules().addVariantFilesAction(new VariantMetadataRules.VariantAction<MutableVariantFilesMetadata>({ true }, rule))
        def artifacts = selectTargetConfigurationMetadata(metadata).artifacts

        then:
        println(artifacts)
        artifacts.size() == 3
        artifacts[0].id.toString() == 'producer-1.0.jar (org.test:producer:1.0)'
        artifacts[1].id.toString() == 'added1.zip (org.test:producer:1.0)'
        artifacts[2].id.toString() == 'added2 (org.test:producer:1.0)'

        artifacts[1].relativeUrl == 'added1.zip'
        artifacts[2].relativeUrl == '../path.jar'

        metadataType == "gradle" ? artifacts[0] instanceof UrlBackedArtifactMetadata : artifacts[0] instanceof DefaultModuleComponentArtifactMetadata
        artifacts[1] instanceof UrlBackedArtifactMetadata
        artifacts[2] instanceof UrlBackedArtifactMetadata

        where:
        metadataType | metadata
        "maven"      | mavenComponentMetadata()
        "ivy"        | ivyComponentMetadata()
        "gradle"     | gradleComponentMetadata()
    }

    @Unroll
    def "variant file metadata rules can remove files from #metadataType metadata"() {
        given:
        def rule = { MutableVariantFilesMetadata files ->
            files.removeAllFiles() // remove original file
            files.addFile("added1.zip")
            files.removeAllFiles() // remove just added file
            files.addFile("added2", "../path.jar")
        }

        when:
        metadata.getVariantMetadataRules().addVariantFilesAction(new VariantMetadataRules.VariantAction<MutableVariantFilesMetadata>({ true }, rule))
        def artifacts = selectTargetConfigurationMetadata(metadata).artifacts

        then:
        println(artifacts)
        artifacts.size() == 1
        artifacts[0].id.toString() == 'added2 (org.test:producer:1.0)'
        artifacts[0].relativeUrl == '../path.jar'
        artifacts[0] instanceof UrlBackedArtifactMetadata

        where:
        metadataType | metadata
        "maven"      | mavenComponentMetadata()
        "ivy"        | ivyComponentMetadata()
        "gradle"     | gradleComponentMetadata()
    }

    def selectTargetConfigurationMetadata(MutableModuleComponentResolveMetadata targetComponent) {
        selectTargetConfigurationMetadata(targetComponent.asImmutable())
    }

    def selectTargetConfigurationMetadata(ModuleComponentResolveMetadata immutable) {
        def componentIdentifier = DefaultModuleComponentIdentifier.newId(DefaultModuleIdentifier.newId("org.test", "consumer"), "1.0")
        def consumerIdentifier = DefaultModuleVersionIdentifier.newId(componentIdentifier)
        def componentSelector = newSelector(consumerIdentifier.module, new DefaultMutableVersionConstraint(consumerIdentifier.version))
        def consumer = new LocalComponentDependencyMetadata(componentIdentifier, componentSelector, "default", attributes, ImmutableAttributes.EMPTY, null, [] as List, [], false, false, true, false, false, null)

        consumer.selectConfigurations(attributes, immutable, schema, [] as Set)[0]
    }
}
