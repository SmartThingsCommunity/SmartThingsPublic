/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.internal.component.external.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.capabilities.CapabilitiesMetadata;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.Factory;
import org.gradle.internal.component.model.DefaultVariantMetadata;
import org.gradle.internal.component.model.ExcludeMetadata;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.component.model.ModuleConfigurationMetadata;
import org.gradle.internal.component.model.VariantResolveMetadata;

import java.util.List;
import java.util.Set;

public abstract class AbstractConfigurationMetadata implements ModuleConfigurationMetadata {
    private final ModuleComponentIdentifier componentId;
    private final String name;
    private final ImmutableList<? extends ModuleComponentArtifactMetadata> artifacts;
    private final boolean transitive;
    private final boolean visible;
    private final ImmutableSet<String> hierarchy;
    private final ImmutableList<ExcludeMetadata> excludes;
    private final ImmutableAttributes attributes;
    private final ImmutableCapabilities capabilities;
    private boolean mavenArtifactDiscovery;

    // Should be final, and set in constructor
    private ImmutableList<ModuleDependencyMetadata> configDependencies;
    private Factory<List<ModuleDependencyMetadata>> configDependenciesFactory;

    AbstractConfigurationMetadata(ModuleComponentIdentifier componentId, String name, boolean transitive, boolean visible,
                                  ImmutableList<? extends ModuleComponentArtifactMetadata> artifacts, ImmutableSet<String> hierarchy,
                                  ImmutableList<ExcludeMetadata> excludes, ImmutableAttributes attributes,
                                  ImmutableList<ModuleDependencyMetadata> configDependencies, ImmutableCapabilities capabilities,
                                  boolean mavenArtifactDiscovery) {

        this.componentId = componentId;
        this.name = name;
        this.transitive = transitive;
        this.visible = visible;
        this.artifacts = artifacts;
        this.hierarchy = hierarchy;
        this.excludes = excludes;
        this.attributes = attributes;
        this.configDependencies = configDependencies;
        this.capabilities = capabilities;
        this.mavenArtifactDiscovery = mavenArtifactDiscovery;
    }

    AbstractConfigurationMetadata(ModuleComponentIdentifier componentId, String name, boolean transitive, boolean visible,
                                  ImmutableList<? extends ModuleComponentArtifactMetadata> artifacts, ImmutableSet<String> hierarchy,
                                  ImmutableList<ExcludeMetadata> excludes, ImmutableAttributes attributes,
                                  Factory<List<ModuleDependencyMetadata>> configDependenciesFactory,
                                  ImmutableCapabilities capabilities,
                                  boolean mavenArtifactDiscovery) {

        this.componentId = componentId;
        this.name = name;
        this.transitive = transitive;
        this.visible = visible;
        this.artifacts = artifacts;
        this.hierarchy = hierarchy;
        this.excludes = excludes;
        this.attributes = attributes;
        this.configDependenciesFactory = configDependenciesFactory;
        this.capabilities = capabilities;
        this.mavenArtifactDiscovery = mavenArtifactDiscovery;
    }

    @Override
    public DisplayName asDescribable() {
        return Describables.of(componentId, "configuration", name);
    }

    @Override
    public String toString() {
        return asDescribable().getDisplayName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableSet<String> getHierarchy() {
        return hierarchy;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean isCanBeConsumed() {
        return true;
    }

    @Override
    public List<String> getConsumptionAlternatives() {
        return null;
    }

    @Override
    public boolean isCanBeResolved() {
        return false;
    }

    public void setDependencies(List<ModuleDependencyMetadata> dependencies) {
        assert this.configDependencies == null; // Can only set once: should really be part of the constructor
        this.configDependencies = ImmutableList.copyOf(dependencies);
    }

    public void setConfigDependenciesFactory(Factory<List<ModuleDependencyMetadata>> dependenciesFactory) {
        assert this.configDependencies == null; // Can only set once: should really be part of the constructor
        assert this.configDependenciesFactory == null; // Can only set once: should really be part of the constructor
        this.configDependenciesFactory = dependenciesFactory;
    }

    @Override
    public ImmutableList<? extends ModuleComponentArtifactMetadata> getArtifacts() {
        return artifacts;
    }

    @Override
    public Set<? extends VariantResolveMetadata> getVariants() {
        return ImmutableSet.of(new DefaultVariantMetadata(asDescribable(), getAttributes(), getArtifacts(), getCapabilities()));
    }

    @Override
    public ImmutableList<ExcludeMetadata> getExcludes() {
        return excludes;
    }

    @Override
    public ModuleComponentArtifactMetadata artifact(IvyArtifactName artifact) {
        return new DefaultModuleComponentArtifactMetadata(componentId, artifact);
    }

    @Override
    public ImmutableAttributes getAttributes() {
        return attributes;
    }

    @Override
    public CapabilitiesMetadata getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean requiresMavenArtifactDiscovery() {
        return mavenArtifactDiscovery;
    }

    ImmutableList<ModuleDependencyMetadata> getConfigDependencies() {
        if (configDependenciesFactory != null) {
            configDependencies = ImmutableList.copyOf(configDependenciesFactory.create());
            configDependenciesFactory = null;
        }
        return configDependencies;
    }

    protected ModuleComponentIdentifier getComponentId() {
        return componentId;
    }

}
