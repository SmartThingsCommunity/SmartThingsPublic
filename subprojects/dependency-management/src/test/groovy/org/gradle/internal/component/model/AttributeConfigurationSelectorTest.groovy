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

package org.gradle.internal.component.model

import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import org.gradle.api.artifacts.ArtifactIdentifier
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.capabilities.CapabilitiesMetadata
import org.gradle.api.capabilities.Capability
import org.gradle.api.internal.attributes.AttributesSchemaInternal
import org.gradle.api.internal.attributes.DefaultAttributesSchema
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.internal.component.AmbiguousConfigurationSelectionException
import org.gradle.internal.component.NoMatchingConfigurationSelectionException
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier
import org.gradle.internal.component.external.model.ModuleComponentArtifactMetadata
import org.gradle.util.SnapshotTestUtil
import org.gradle.util.TestUtil
import org.gradle.util.TextUtil
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.util.AttributeTestUtil.attributes

class AttributeConfigurationSelectorTest extends Specification {
    private final AttributesSchemaInternal attributesSchema = new DefaultAttributesSchema(new ComponentAttributeMatcher(), TestUtil.instantiatorFactory(), SnapshotTestUtil.valueSnapshotter())

    private ComponentResolveMetadata targetComponent
    private ConfigurationMetadata selected
    private ImmutableAttributes consumerAttributes = ImmutableAttributes.EMPTY
    private List<Capability> requestedCapabilities = []
    private List<IvyArtifactName> artifacts = []

    @Unroll
    def "selects a variant when there's no ambiguity"() {
        given:
        component(
                variant("api", attributes('org.gradle.usage': 'java-api')),
                variant("runtime", attributes('org.gradle.usage': 'java-runtime'))
        )

        and:
        consumerAttributes('org.gradle.usage': usage)

        when:
        performSelection()

        then:
        selected.name == expected

        where:
        usage          | expected
        'java-api'     | 'api'
        'java-runtime' | 'runtime'
    }

    def "fails to select a variant when there are more than one candidate"() {
        given:
        component(
                variant("api1", attributes('org.gradle.usage': 'java-api')),
                variant("api2", attributes('org.gradle.usage': 'java-api'))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api')

        when:
        performSelection()

        then:
        AmbiguousConfigurationSelectionException e = thrown()
        failsWith(e, '''Cannot choose between the following variants of org:lib:1.0:
  - api1
  - api2
All of them match the consumer attributes:
  - Variant 'api1' capability org:lib:1.0:
      - Compatible attribute:
          - Required org.gradle.usage 'java-api' and found compatible value 'java-api'.
  - Variant 'api2' capability org:lib:1.0:
      - Compatible attribute:
          - Required org.gradle.usage 'java-api' and found compatible value 'java-api'.''')
    }

    def "fails to select a variant when there no matching candidates"() {
        given:
        component(
                variant("api", attributes('org.gradle.usage': 'java-api')),
                variant("runtime", attributes('org.gradle.usage': 'java-runtime'))
        )

        and:
        consumerAttributes('org.gradle.usage': 'cplusplus-headers')

        when:
        performSelection()

        then:
        NoMatchingConfigurationSelectionException e = thrown()
        failsWith(e, '''Unable to find a matching variant of org:lib:1.0:
  - Variant 'api' capability org:lib:1.0:
      - Incompatible attribute:
          - Required org.gradle.usage 'cplusplus-headers' and found incompatible value 'java-api'.
  - Variant 'runtime' capability org:lib:1.0:
      - Incompatible attribute:
          - Required org.gradle.usage 'cplusplus-headers' and found incompatible value 'java-runtime'.''')
    }

    @Unroll
    def "can select a variant thanks to the capabilities"() {
        given:
        component(
                variant("api1", attributes('org.gradle.usage': 'java-api'), capability('first')),
                variant("api2", attributes('org.gradle.usage': 'java-api'), capability('second'))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api')
        requestCapability capability(cap)

        when:
        performSelection()

        then:
        selected.name == expected

        where:
        cap      | expected
        'first'  | 'api1'
        'second' | 'api2'
    }

    @Unroll
    def "can select a variant thanks to the implicit capability"() {
        given:
        component(
                variant("api1", attributes('org.gradle.usage': 'java-api')),
                variant("api2", attributes('org.gradle.usage': 'java-api'), capability('second'))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api')

        if (cap) {
            requestCapability capability(cap)
        }

        when:
        performSelection()

        then:
        selected.name == expected

        where:
        cap      | expected
        null     | 'api1'
        'lib'    | 'api1'
        'second' | 'api2'
    }


    def "fails if more than one variant provides the implicit capability"() {
        given:
        component(
                variant("api1", attributes('org.gradle.usage': 'java-api')),
                variant("api2", attributes('org.gradle.usage': 'java-api')),
                variant("api3", attributes('org.gradle.usage': 'java-api'), capability('lib'), capability('second'))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api')

        requestCapability capability('lib')

        when:
        performSelection()

        then:
        AmbiguousConfigurationSelectionException e = thrown()
        failsWith(e, '''Cannot choose between the following variants of org:lib:1.0:
  - api1
  - api2
  - api3
All of them match the consumer attributes:
  - Variant 'api1' capability org:lib:1.0:
      - Compatible attribute:
          - Required org.gradle.usage 'java-api' and found compatible value 'java-api'.
  - Variant 'api2' capability org:lib:1.0:
      - Compatible attribute:
          - Required org.gradle.usage 'java-api' and found compatible value 'java-api'.
  - Variant 'api3' capabilities org:lib:1.0 and org:second:1.0:
      - Compatible attribute:
          - Required org.gradle.usage 'java-api' and found compatible value 'java-api'.''')
    }

    def "should select the variant which matches the most attributes"() {
        given:
        component(
                variant("first", attributes('org.gradle.usage': 'java-api')),
                variant("second", attributes('org.gradle.usage': 'java-api', 'other': true))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api')

        when:
        performSelection()

        then:
        selected.name == 'first'
    }

    def "should not select variant whenever 2 variants provide different extra attributes"() {
        given:
        component(
                variant("first", attributes('org.gradle.usage': 'java-api', extra: 'v1')),
                variant("second", attributes('org.gradle.usage': 'java-api', other: true))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api')

        when:
        performSelection()

        then:
        AmbiguousConfigurationSelectionException e = thrown()
        failsWith(e, '''Cannot choose between the following variants of org:lib:1.0:
  - first
  - second
All of them match the consumer attributes:
  - Variant 'first' capability org:lib:1.0:
      - Unmatched attribute: Found extra 'v1' but wasn't required.
      - Compatible attribute:
          - Required org.gradle.usage 'java-api' and found compatible value 'java-api'.
  - Variant 'second' capability org:lib:1.0:
      - Unmatched attribute: Found other 'true' but wasn't required.
      - Compatible attribute:
          - Required org.gradle.usage 'java-api' and found compatible value 'java-api'.''')

    }

    def "should select the variant matching most closely the requested attributes when they provide more than one extra attributes"() {
        given:
        component(
                variant("first", attributes('org.gradle.usage': 'java-api', extra: 'v1', other: true)),
                variant("second", attributes('org.gradle.usage': 'java-api', other: true))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api')

        when:
        performSelection()

        then:
        selected.name == 'second'

    }


    def "should select the variant which matches the most attributes and producer doesn't have requested value"() {
        given:
        component(
                variant("first", attributes('org.gradle.usage': 'java-api')),
                variant("second", attributes('org.gradle.usage': 'java-runtime', 'other': true))
        )
        attributesSchema.attribute(Attribute.of("org.gradle.usage", String)) {
            it.compatibilityRules.add(UsageCompatibilityRule)
        }
        and:
        consumerAttributes('org.gradle.usage': 'java-api', 'other': true)

        when:
        performSelection()

        then:
        selected.name == 'second'
    }

    def "should select the variant which matches the requested classifier"() {
        def variant1 = variant("first", ImmutableAttributes.EMPTY)
        def variant2 = variant("second", ImmutableAttributes.EMPTY)

        given:
        variant1.getArtifacts() >> ImmutableList.of(artifact('foo', null))
        variant2.getArtifacts() >> ImmutableList.of(artifact('foo', 'classy'))
        component(variant1, variant2)

        and:
        requireArtifact('foo', 'jar', 'jar', 'classy')

        when:
        performSelection()

        then:
        selected.name == 'second'
    }

    def "should select the variant with the most exact capability match"() {
        given:
        component(
            variant('A', attributes('org.gradle.usage': 'java-api', 'other': 'c'), capability('first'), capability('second')),
            variant('B', attributes('org.gradle.usage': 'java-api', 'other': 'c'), capability('first'))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api', 'other': 'c')
        requestCapability capability('first')

        when:
        performSelection()

        then:
        selected.name == 'B' // B matches best: capabilities match exactly, attributes match exactly
    }
    
    def "should select the variant with the most exact match in case of ambiguity between attributes and capabilities"() {
        given:
        attributesSchema.attribute(Attribute.of('other', String)) {
            it.compatibilityRules.add(AllCompatibilityRule)
        }
        component(
            variant('A', attributes('org.gradle.usage': 'java-api', 'other': 'a'), capability('first'), capability('second')),
            variant('B', attributes('org.gradle.usage': 'java-api', 'other': 'b'), capability('first')),
            variant('C', attributes('org.gradle.usage': 'java-api'), capability('first'))
        )

        and:
        consumerAttributes('org.gradle.usage': 'java-api', 'other': 'c')
        requestCapability capability('first')

        when:
        performSelection()

        then:
        selected.name == 'B' // B matches best: capabilities match exactly, attribute 'other' was requested and a compatible value is provided (variant C does not provide any value for 'other')
    }

    private void performSelection() {
        selected = AttributeConfigurationSelector.selectConfigurationUsingAttributeMatching(
                consumerAttributes,
                requestedCapabilities,
                targetComponent,
                attributesSchema,
                artifacts
        )
    }

    private void requireArtifact(String name = "foo", String type = "jar", String ext = "jar", String classifier = null) {
        artifacts << new DefaultIvyArtifactName(name, type, ext, classifier)
    }

    private ModuleComponentArtifactMetadata artifact(String name, String classifier) {
        Stub(ModuleComponentArtifactMetadata) {
            getId() >> Stub(ModuleComponentArtifactIdentifier)
            toArtifactIdentifier() >> Stub(ArtifactIdentifier) {
                getName() >> name
                getType() >> "jar"
                getExtension() >> "jar"
                getClassifier() >> classifier
            }
        }
    }

    private consumerAttributes(Map<String, Object> attrs) {
        this.consumerAttributes = attributes(attrs)
    }

    private void requestCapability(Capability c) {
        requestedCapabilities << c
    }

    private void component(ConfigurationMetadata... variants) {
        targetComponent = Stub(ComponentResolveMetadata) {
            getModuleVersionId() >> Stub(ModuleVersionIdentifier) {
                getGroup() >> 'org'
                getName() >> 'lib'
                getVersion() >> '1.0'
            }
            getId() >> Stub(ComponentIdentifier) {
                getDisplayName() >> 'org:lib:1.0'
            }
            getVariantsForGraphTraversal() >> Optional.of(
                    ImmutableList.copyOf(variants)
            )
            getAttributesSchema() >> attributesSchema
        }
    }

    private ConfigurationMetadata variant(String name, ImmutableAttributes attributes, Capability... capabilities) {
        Stub(ConfigurationMetadata) {
            getName() >> name
            getAttributes() >> attributes
            getCapabilities() >> Mock(CapabilitiesMetadata) {
                getCapabilities() >> ImmutableList.copyOf(capabilities)
            }
        }
    }

    private Capability capability(String group, String name, String version = '1.0') {
        Stub(Capability) {
            getGroup() >> group
            getName() >> name
            getVersion() >> version
        }
    }

    private Capability capability(String name) {
        capability('org', name)
    }

    private void failsWith(Throwable e, String message) {
        String actualMessage = TextUtil.normaliseLineSeparators(e.message)
        String expectedMessage = TextUtil.normaliseLineSeparators(message)
        assert actualMessage == expectedMessage
    }

    private static class UsageCompatibilityRule implements AttributeCompatibilityRule<String> {

        @Override
        void execute(CompatibilityCheckDetails<String> details) {
            if (details.consumerValue == 'java-api' && details.producerValue == 'java-runtime') {
                details.compatible()
            }
        }
    }

    private static class AllCompatibilityRule implements AttributeCompatibilityRule<String> {

        @Override
        void execute(CompatibilityCheckDetails<String> details) {
            details.compatible()
        }
    }
}
