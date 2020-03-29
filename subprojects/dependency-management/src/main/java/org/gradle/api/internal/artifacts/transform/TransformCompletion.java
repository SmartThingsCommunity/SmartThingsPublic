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

package org.gradle.api.internal.artifacts.transform;

import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ArtifactVisitor;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvedArtifactSet;
import org.gradle.api.internal.attributes.AttributeContainerInternal;

import java.util.Map;

public class TransformCompletion implements ResolvedArtifactSet.Completion {
    private final AttributeContainerInternal attributes;
    private final ResolvedArtifactSet.Completion delegate;
    private final Map<ComponentArtifactIdentifier, TransformationResult> artifactResults;

    public TransformCompletion(ResolvedArtifactSet.Completion delegate, AttributeContainerInternal attributes, Map<ComponentArtifactIdentifier, TransformationResult> artifactResults) {
        this.delegate = delegate;
        this.attributes = attributes;
        this.artifactResults = artifactResults;
    }

    @Override
    public void visit(ArtifactVisitor visitor) {
        delegate.visit(new TransformingArtifactVisitor(visitor, attributes, artifactResults));
    }
}
