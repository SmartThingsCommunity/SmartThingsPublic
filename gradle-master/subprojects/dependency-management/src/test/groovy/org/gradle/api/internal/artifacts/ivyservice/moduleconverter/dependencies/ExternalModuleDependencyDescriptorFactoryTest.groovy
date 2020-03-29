/*
 * Copyright 2010 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.moduleconverter.dependencies

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.internal.artifacts.VersionConstraintInternal
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.internal.component.local.model.OpaqueComponentIdentifier
import org.gradle.internal.component.model.LocalOriginDependencyMetadata

class ExternalModuleDependencyDescriptorFactoryTest extends AbstractDependencyDescriptorFactoryInternalSpec {

    ExternalModuleIvyDependencyDescriptorFactory externalModuleDependencyDescriptorFactory =
            new ExternalModuleIvyDependencyDescriptorFactory(excludeRuleConverterStub)
    private final ComponentIdentifier componentId = new OpaqueComponentIdentifier("foo")

    def canConvert() {
        expect:
        !externalModuleDependencyDescriptorFactory.canConvert(Mock(ProjectDependency))
        externalModuleDependencyDescriptorFactory.canConvert(Mock(ExternalModuleDependency))
    }

    def testAddWithNullGroupAndNullVersionShouldHaveEmptyStringModuleRevisionValues() {
        when:
        ModuleDependency dependency = new DefaultExternalModuleDependency(null, "gradle-core", null, TEST_DEP_CONF)
        LocalOriginDependencyMetadata dependencyMetaData = externalModuleDependencyDescriptorFactory.createDependencyDescriptor(componentId, TEST_CONF, null, dependency)
        ModuleComponentSelector selector = (ModuleComponentSelector) dependencyMetaData.getSelector()

        then:
        selector.group == ""
        selector.module == "gradle-core"
        selector.version == ""
        selector.versionConstraint.preferredVersion == ""
    }

    def testCreateFromModuleDependency() {
        when:
        boolean withArtifacts = true
        DefaultExternalModuleDependency moduleDependency = new DefaultExternalModuleDependency("org.gradle",
                "gradle-core", "1.0", null)
        setUpDependency(moduleDependency, withArtifacts)

        LocalOriginDependencyMetadata dependencyMetaData = externalModuleDependencyDescriptorFactory.createDependencyDescriptor(componentId, TEST_CONF, null, moduleDependency)

        then:
        moduleDependency.changing == dependencyMetaData.changing
        moduleDependency.force == dependencyMetaData.force
        moduleDependency.group == dependencyMetaData.selector.group
        moduleDependency.name == dependencyMetaData.selector.module
        moduleDependency.version == dependencyMetaData.selector.version
        ((VersionConstraintInternal) moduleDependency.getVersionConstraint()).asImmutable() == dependencyMetaData.selector.versionConstraint
        assertDependencyDescriptorHasCommonFixtureValues(dependencyMetaData, withArtifacts)
    }

    def testCreateFromModuleDependencyWithoutArtifacts() {
        when:
        boolean withArtifacts = false
        DefaultExternalModuleDependency moduleDependency = new DefaultExternalModuleDependency("org.gradle",
            "gradle-core", "1.0", TEST_DEP_CONF)
        setUpDependency(moduleDependency, withArtifacts)

        LocalOriginDependencyMetadata dependencyMetaData = externalModuleDependencyDescriptorFactory.createDependencyDescriptor(componentId, TEST_CONF, null, moduleDependency)

        then:
        moduleDependency.changing == dependencyMetaData.changing
        moduleDependency.force == dependencyMetaData.force
        moduleDependency.group == dependencyMetaData.selector.group
        moduleDependency.name == dependencyMetaData.selector.module
        moduleDependency.version == dependencyMetaData.selector.version
        ((VersionConstraintInternal) moduleDependency.getVersionConstraint()).asImmutable() == dependencyMetaData.selector.versionConstraint
        assertDependencyDescriptorHasCommonFixtureValues(dependencyMetaData, withArtifacts)
    }
}
