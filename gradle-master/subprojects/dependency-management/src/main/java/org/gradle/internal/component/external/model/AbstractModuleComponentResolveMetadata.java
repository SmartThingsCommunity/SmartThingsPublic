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

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.attributes.AttributesSchemaInternal;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.component.model.ConfigurationMetadata;
import org.gradle.internal.component.model.DefaultIvyArtifactName;
import org.gradle.internal.component.model.ImmutableModuleSources;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.component.model.ModuleSources;

import javax.annotation.Nullable;
import java.util.List;

abstract class AbstractModuleComponentResolveMetadata implements ModuleComponentResolveMetadata {
    private final ImmutableAttributesFactory attributesFactory;
    private final ModuleVersionIdentifier moduleVersionIdentifier;
    private final ModuleComponentIdentifier componentIdentifier;
    private final boolean changing;
    private final boolean missing;
    private final List<String> statusScheme;
    private final ImmutableModuleSources moduleSources;
    private final ImmutableList<? extends ComponentVariant> variants;
    private final ImmutableAttributes attributes;
    private final ImmutableList<? extends VirtualComponentIdentifier> platformOwners;
    private final AttributesSchemaInternal schema;

    public AbstractModuleComponentResolveMetadata(AbstractMutableModuleComponentResolveMetadata metadata) {
        this.componentIdentifier = metadata.getId();
        this.moduleVersionIdentifier = metadata.getModuleVersionId();
        changing = metadata.isChanging();
        missing = metadata.isMissing();
        statusScheme = metadata.getStatusScheme();
        moduleSources = ImmutableModuleSources.of(metadata.getSources());
        attributesFactory = metadata.getAttributesFactory();
        schema = metadata.getAttributesSchema();
        attributes = extractAttributes(metadata);
        variants = metadata.getVariants();
        platformOwners = metadata.getPlatformOwners() == null ? ImmutableList.of() : ImmutableList.copyOf(metadata.getPlatformOwners());
    }

    public AbstractModuleComponentResolveMetadata(AbstractModuleComponentResolveMetadata metadata, ImmutableList<? extends ComponentVariant> variants) {
        this.componentIdentifier = metadata.getId();
        this.moduleVersionIdentifier = metadata.getModuleVersionId();
        changing = metadata.isChanging();
        missing = metadata.isMissing();
        statusScheme = metadata.getStatusScheme();
        moduleSources = ImmutableModuleSources.of(metadata.getSources());
        attributesFactory = metadata.getAttributesFactory();
        schema = metadata.getAttributesSchema();
        attributes = metadata.getAttributes();
        this.variants = variants;
        this.platformOwners = metadata.getPlatformOwners();
    }

    public AbstractModuleComponentResolveMetadata(AbstractModuleComponentResolveMetadata metadata) {
        this.componentIdentifier = metadata.componentIdentifier;
        this.moduleVersionIdentifier = metadata.moduleVersionIdentifier;
        changing = metadata.changing;
        missing = metadata.missing;
        statusScheme = metadata.statusScheme;
        moduleSources = metadata.moduleSources;
        attributesFactory = metadata.attributesFactory;
        schema = metadata.schema;
        attributes = metadata.attributes;
        variants = metadata.variants;
        platformOwners = metadata.platformOwners;
    }

    public AbstractModuleComponentResolveMetadata(AbstractModuleComponentResolveMetadata metadata, ModuleSources sources) {
        this.componentIdentifier = metadata.componentIdentifier;
        this.moduleVersionIdentifier = metadata.moduleVersionIdentifier;
        changing = metadata.changing;
        missing = metadata.missing;
        statusScheme = metadata.statusScheme;
        attributesFactory = metadata.attributesFactory;
        schema = metadata.schema;
        attributes = metadata.attributes;
        variants = metadata.variants;
        platformOwners = metadata.platformOwners;
        moduleSources = ImmutableModuleSources.of(sources);
    }

    private static ImmutableAttributes extractAttributes(AbstractMutableModuleComponentResolveMetadata metadata) {
        return ((AttributeContainerInternal) metadata.getAttributes()).asImmutable();
    }

    @Override
    public ImmutableAttributesFactory getAttributesFactory() {
        return attributesFactory;
    }

    @Override
    public boolean isChanging() {
        return changing;
    }

    @Override
    public boolean isMissing() {
        return missing;
    }

    @Override
    public List<String> getStatusScheme() {
        return statusScheme;
    }

    @Override
    public ModuleComponentIdentifier getId() {
        return componentIdentifier;
    }

    @Override
    public ModuleVersionIdentifier getModuleVersionId() {
        return moduleVersionIdentifier;
    }

    @Override
    public ModuleSources getSources() {
        return moduleSources;
    }

    @Override
    public String toString() {
        return componentIdentifier.getDisplayName();
    }

    @Nullable
    @Override
    public AttributesSchemaInternal getAttributesSchema() {
        return schema;
    }

    @Override
    public ImmutableAttributes getAttributes() {
        return attributes;
    }

    @Override
    public String getStatus() {
        return attributes.getAttribute(ProjectInternal.STATUS_ATTRIBUTE);
    }

    @Override
    public ImmutableList<? extends ComponentVariant> getVariants() {
        return variants;
    }

    @Override
    public ModuleComponentArtifactMetadata artifact(String type, @Nullable String extension, @Nullable String classifier) {
        IvyArtifactName ivyArtifactName = new DefaultIvyArtifactName(getModuleVersionId().getName(), type, extension, classifier);
        return new DefaultModuleComponentArtifactMetadata(getId(), ivyArtifactName);
    }

    /**
     * If there are no variants defined in the metadata, but the implementation knows how to provide variants it can do that here.
     * If it can not provide variants, absent must be returned to fall back to traditional configuration selection.
     */
    protected Optional<ImmutableList<? extends ConfigurationMetadata>> maybeDeriveVariants() {
        return Optional.absent();
    }

    @Override
    public ImmutableList<? extends VirtualComponentIdentifier> getPlatformOwners() {
        return platformOwners;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractModuleComponentResolveMetadata that = (AbstractModuleComponentResolveMetadata) o;
        return changing == that.changing
            && missing == that.missing
            && Objects.equal(moduleVersionIdentifier, that.moduleVersionIdentifier)
            && Objects.equal(componentIdentifier, that.componentIdentifier)
            && Objects.equal(statusScheme, that.statusScheme)
            && Objects.equal(moduleSources, that.moduleSources)
            && Objects.equal(attributes, that.attributes)
            && Objects.equal(variants, that.variants);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
            moduleVersionIdentifier,
            componentIdentifier,
            changing,
            missing,
            statusScheme,
            moduleSources,
            attributes,
            variants);
    }
}
