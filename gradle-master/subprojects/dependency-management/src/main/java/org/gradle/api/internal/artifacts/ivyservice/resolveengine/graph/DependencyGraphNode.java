/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph;

import org.gradle.api.artifacts.result.ResolvedVariantResult;
import org.gradle.api.internal.artifacts.ResolvedConfigurationIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.ComponentResolutionState;
import org.gradle.internal.component.local.model.LocalFileDependencyMetadata;
import org.gradle.internal.component.model.ConfigurationMetadata;

import java.util.Collection;
import java.util.Set;

/**
 * A node in the dependency graph. Represents a configuration.
 */
public interface DependencyGraphNode {
    /**
     * Returns a simple id for this node, unique across all nodes in the same graph.
     * This id cannot be used across graphs.
     */
    Long getNodeId();

    boolean isRoot();

    ResolvedConfigurationIdentifier getResolvedConfigurationId();

    DependencyGraphComponent getOwner();

    Collection<? extends DependencyGraphEdge> getIncomingEdges();

    Collection<? extends DependencyGraphEdge> getOutgoingEdges();

    /**
     * The outgoing file dependencies of this node. Should be modelled edges to another node, but are treated separately for now.
     */
    Set<? extends LocalFileDependencyMetadata> getOutgoingFileEdges();

    ConfigurationMetadata getMetadata();

    boolean isSelected();

    ComponentResolutionState getComponent();

    ResolvedVariantResult getResolvedVariant();
}
