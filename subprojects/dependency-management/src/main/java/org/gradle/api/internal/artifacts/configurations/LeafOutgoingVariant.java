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

package org.gradle.api.internal.artifacts.configurations;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.internal.DisplayName;

import java.util.Set;

class LeafOutgoingVariant implements OutgoingVariant {
    private final AttributeContainerInternal attributes;
    private final Set<? extends PublishArtifact> artifacts;
    private final DisplayName displayName;

    public LeafOutgoingVariant(DisplayName displayName, AttributeContainerInternal attributes, Set<? extends PublishArtifact> artifacts) {
        this.displayName = displayName;
        this.attributes = attributes;
        this.artifacts = artifacts;
    }

    @Override
    public DisplayName asDescribable() {
        return displayName;
    }

    @Override
    public AttributeContainerInternal getAttributes() {
        return attributes;
    }

    @Override
    public Set<? extends PublishArtifact> getArtifacts() {
        return artifacts;
    }

    @Override
    public Set<? extends OutgoingVariant> getChildren() {
        return ImmutableSet.of();
    }
}
