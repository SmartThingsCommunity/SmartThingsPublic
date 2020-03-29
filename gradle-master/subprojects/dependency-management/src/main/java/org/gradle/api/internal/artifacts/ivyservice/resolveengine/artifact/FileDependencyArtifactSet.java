/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact;

import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.transform.VariantSelector;
import org.gradle.api.internal.artifacts.type.ArtifactTypeRegistry;
import org.gradle.api.specs.Spec;
import org.gradle.internal.component.local.model.LocalFileDependencyMetadata;

public class FileDependencyArtifactSet implements ArtifactSet {
    private final LocalFileDependencyMetadata fileDependency;
    private final ArtifactTypeRegistry artifactTypeRegistry;

    public FileDependencyArtifactSet(LocalFileDependencyMetadata fileDependency, ArtifactTypeRegistry artifactTypeRegistry) {
        this.fileDependency = fileDependency;
        this.artifactTypeRegistry = artifactTypeRegistry;
    }

    @Override
    public ResolvedArtifactSet select(Spec<? super ComponentIdentifier> componentFilter, VariantSelector selector) {
        return new LocalFileDependencyBackedArtifactSet(fileDependency, componentFilter, selector, artifactTypeRegistry);
    }

}
