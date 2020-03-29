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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder;

import com.google.common.collect.Lists;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ComponentSelectionReason;
import org.gradle.api.artifacts.result.ResolvedVariantResult;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.RepositoryChainModuleSource;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.ComponentResolutionState;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.DependencyGraphComponent;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.conflicts.VersionConflictResolutionDetails;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.ComponentSelectionDescriptorInternal;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.ComponentSelectionReasonInternal;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.ComponentSelectionReasons;
import org.gradle.internal.Pair;
import org.gradle.internal.component.external.model.ImmutableCapability;
import org.gradle.internal.component.model.ComponentOverrideMetadata;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.component.model.DefaultComponentOverrideMetadata;
import org.gradle.internal.resolve.ModuleVersionResolveException;
import org.gradle.internal.resolve.resolver.ComponentMetaDataResolver;
import org.gradle.internal.resolve.result.DefaultBuildableComponentResolveResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * Resolution state for a given component
 */
public class ComponentState implements ComponentResolutionState, DependencyGraphComponent {
    private final ComponentIdentifier componentIdentifier;
    private final ModuleVersionIdentifier id;
    private final ComponentMetaDataResolver resolver;
    private final List<NodeState> nodes = Lists.newLinkedList();
    private final Long resultId;
    private final ModuleResolveState module;
    private final List<ComponentSelectionDescriptorInternal> selectionCauses = Lists.newArrayList();
    private final ImmutableCapability implicitCapability;
    private final int hashCode;

    private volatile ComponentResolveMetadata metadata;

    private ComponentSelectionState state = ComponentSelectionState.Selectable;
    private ModuleVersionResolveException metadataResolveFailure;
    private ModuleSelectors<SelectorState> selectors;
    private DependencyGraphBuilder.VisitState visitState = DependencyGraphBuilder.VisitState.NotSeen;

    private boolean rejected;
    private boolean root;
    private Pair<Capability, Collection<NodeState>> capabilityReject;

    ComponentState(Long resultId, ModuleResolveState module, ModuleVersionIdentifier id, ComponentIdentifier componentIdentifier, ComponentMetaDataResolver resolver) {
        this.resultId = resultId;
        this.module = module;
        this.id = id;
        this.componentIdentifier = componentIdentifier;
        this.resolver = resolver;
        this.implicitCapability = new ImmutableCapability(id.getGroup(), id.getName(), id.getVersion());
        this.hashCode = 31 * id.hashCode() ^ resultId.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public String getVersion() {
        return id.getVersion();
    }

    @Override
    public Long getResultId() {
        return resultId;
    }

    @Override
    public ModuleVersionIdentifier getId() {
        return id;
    }

    @Override
    public String getRepositoryName() {
        return metadata.getSources().withSource(RepositoryChainModuleSource.class, source -> source
            .map(RepositoryChainModuleSource::getRepositoryName)
            .orElse(null));
    }

    @Override
    public ModuleVersionIdentifier getModuleVersion() {
        return id;
    }

    public ModuleVersionResolveException getMetadataResolveFailure() {
        return metadataResolveFailure;
    }

    public DependencyGraphBuilder.VisitState getVisitState() {
        return visitState;
    }

    public void setVisitState(DependencyGraphBuilder.VisitState visitState) {
        this.visitState = visitState;
    }

    public List<NodeState> getNodes() {
        return nodes;
    }

    ModuleResolveState getModule() {
        return module;
    }

    public void selectAndRestartModule() {
        module.replaceWith(this);
    }

    @Override
    public ComponentResolveMetadata getMetadata() {
        resolve();
        return metadata;
    }

    @Override
    public ComponentIdentifier getComponentId() {
        // Use the resolved component id if available: this ensures that Maven Snapshot ids are correctly reported
        if (metadata != null) {
            return metadata.getId();
        }
        return componentIdentifier;
    }

    /**
     * Restarts all incoming edges for this component, queuing them up for processing.
     */
    public void restartIncomingEdges(ComponentState selected) {
        for (NodeState configuration : nodes) {
            configuration.restart(selected);
        }
    }

    public void setSelectors(ModuleSelectors<SelectorState> selectors) {
        this.selectors = selectors;
    }

    /**
     * Returns true if this module version can be resolved quickly (already resolved or local)
     *
     * @return true if it has been resolved in a cheap way
     */
    public boolean alreadyResolved() {
        return metadata != null || metadataResolveFailure != null;
    }

    public void resolve() {
        if (alreadyResolved()) {
            return;
        }

        ComponentOverrideMetadata componentOverrideMetadata;
        if (selectors != null && selectors.size() > 0) {
            // Taking the first selector here to determine the 'changing' status and 'client module' is our best bet to get the selector that will most likely be chosen in the end.
            // As selectors are sorted accordingly (see ModuleSelectors.SELECTOR_COMPARATOR).
            SelectorState firstSelector = selectors.first();
            componentOverrideMetadata = DefaultComponentOverrideMetadata.forDependency(firstSelector.isChanging(), selectors.getFirstDependencyArtifact(), firstSelector.getClientModule());
        } else {
            componentOverrideMetadata = DefaultComponentOverrideMetadata.EMPTY;
        }
        DefaultBuildableComponentResolveResult result = new DefaultBuildableComponentResolveResult();
        if (tryResolveVirtualPlatform()) {
            return;
        }
        resolver.resolve(componentIdentifier, componentOverrideMetadata, result);

        if (result.getFailure() != null) {
            metadataResolveFailure = result.getFailure();
            return;
        }
        metadata = result.getMetadata();
    }

    private boolean tryResolveVirtualPlatform() {
        if (module.isVirtualPlatform()) {
            for (ComponentState version : module.getAllVersions()) {
                if (version != this) {
                    ComponentResolveMetadata metadata = version.getMetadata();
                    if (metadata instanceof LenientPlatformResolveMetadata) {
                        LenientPlatformResolveMetadata lenient = (LenientPlatformResolveMetadata) metadata;
                        this.metadata = lenient.withVersion((ModuleComponentIdentifier) componentIdentifier, id);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setMetadata(ComponentResolveMetadata metaData) {
        this.metadata = metaData;
        this.metadataResolveFailure = null;
    }

    public void addConfiguration(NodeState node) {
        nodes.add(node);
    }

    private ComponentSelectionReason cachedReason;

    @Override
    public ComponentSelectionReason getSelectionReason() {
        if (root) {
            return ComponentSelectionReasons.root();
        }
        if (cachedReason != null) {
            return cachedReason;
        }
        ComponentSelectionReasonInternal reason = ComponentSelectionReasons.empty();
        for (final SelectorState selectorState : module.getSelectors()) {
            if (selectorState.getFailure() == null) {
                selectorState.addReasonsForSelector(reason);
            }
        }
        for (ComponentSelectionDescriptorInternal selectionCause : VersionConflictResolutionDetails.mergeCauses(selectionCauses)) {
            reason.addCause(selectionCause);
        }
        cachedReason = reason;
        return reason;
    }

    boolean hasStrongOpinion() {
        return StreamSupport.stream(module.getSelectors().spliterator(), false)
            .filter(s -> s.getFailure() == null)
            .anyMatch(SelectorState::hasStrongOpinion);
    }

    @Override
    public void addCause(ComponentSelectionDescriptorInternal componentSelectionDescriptor) {
        selectionCauses.add(componentSelectionDescriptor);
    }

    public void setRoot() {
        this.root = true;
    }

    @Override
    public List<ResolvedVariantResult> getResolvedVariants() {
        List<ResolvedVariantResult> result = null;
        ResolvedVariantResult cur = null;
        for (NodeState node : nodes) {
            if (node.isSelected()) {
                ResolvedVariantResult details = node.getResolvedVariant();
                if (result != null) {
                    result.add(details);
                } else if (cur != null) {
                    result = Lists.newArrayList();
                    result.add(cur);
                    result.add(details);
                } else {
                    cur = details;
                }
            }
        }
        if (result != null) {
            return result;
        }
        if (cur != null) {
            return Collections.singletonList(cur);
        }
        return Collections.emptyList();
    }

    @Override
    public List<ComponentState> getDependents() {
        List<ComponentState> incoming = Lists.newArrayListWithCapacity(nodes.size());
        for (NodeState configuration : nodes) {
            for (EdgeState dependencyEdge : configuration.getIncomingEdges()) {
                incoming.add(dependencyEdge.getFrom().getComponent());
            }
        }
        return incoming;
    }

    @Override
    public Collection<? extends ModuleVersionIdentifier> getAllVersions() {
        Collection<ComponentState> moduleVersions = module.getAllVersions();
        List<ModuleVersionIdentifier> out = Lists.newArrayListWithCapacity(moduleVersions.size());
        for (ComponentState moduleVersion : moduleVersions) {
            out.add(moduleVersion.id);
        }
        return out;
    }

    public boolean isSelected() {
        return state == ComponentSelectionState.Selected;
    }

    public boolean isCandidateForConflictResolution() {
        return state.isCandidateForConflictResolution();
    }

    void evict() {
        state = ComponentSelectionState.Evicted;
    }

    void select() {
        state = ComponentSelectionState.Selected;
    }

    void makeSelectable() {
        state = ComponentSelectionState.Selectable;
    }

    @Override
    public void reject() {
        this.rejected = true;
    }

    public void rejectForCapabilityConflict(Capability capability, Collection<NodeState> conflictedNodes) {
        this.rejected = true;
        if (this.capabilityReject == null) {
            this.capabilityReject = Pair.of(capability, conflictedNodes);
        } else {
            mergeCapabilityRejects(capability, conflictedNodes);
        }
    }

    private void mergeCapabilityRejects(Capability capability, Collection<NodeState> conflictedNodes) {
        // Only merge if about the same capability, otherwise last wins
        if (this.capabilityReject.getLeft().equals(capability)) {
            this.capabilityReject.getRight().addAll(conflictedNodes);
        } else {
            this.capabilityReject = Pair.of(capability, conflictedNodes);
        }
    }

    @Override
    public boolean isRejected() {
        return rejected;
    }

    public String getRejectedErrorMessage() {
        if (capabilityReject != null) {
            return formatCapabilityRejectMessage(module.getId(), capabilityReject);
        } else {
            return new RejectedModuleMessageBuilder().buildFailureMessage(module);
        }

    }

    private String formatCapabilityRejectMessage(ModuleIdentifier id, Pair<Capability, Collection<NodeState>> capabilityConflict) {
        StringBuilder sb = new StringBuilder("Module '");
        sb.append(id).append("' has been rejected:\n");
        sb.append("   Cannot select module with conflict on ");
        Capability capability = capabilityConflict.left;
        sb.append("capability '").append(capability.getGroup()).append(":").append(capability.getName()).append(":").append(capability.getVersion()).append("' also provided by ");
        sb.append(capabilityConflict.getRight());
        return sb.toString();
    }

    @Override
    public Set<VirtualPlatformState> getPlatformOwners() {
        return module.getPlatformOwners();
    }

    @Override
    public VirtualPlatformState getPlatformState() {
        return module.getPlatformState();
    }

    public void removeOutgoingEdges() {
        for (NodeState configuration : getNodes()) {
            configuration.deselect();
        }
    }

    /**
     * Describes the possible states of a component in the graph.
     */
    enum ComponentSelectionState {
        /**
         * A selectable component is either new to the graph, or has been visited before,
         * but wasn't selected because another compatible version was used.
         */
        Selectable(true),

        /**
         * A selected component has been chosen, at some point, as the version to use.
         * This is not for a lifetime: a component can later be evicted through conflict resolution,
         * or another compatible component can be chosen instead if more constraints arise.
         */
        Selected(true),

        /**
         * An evicted component has been evicted and will never, ever be chosen starting from the moment it is evicted.
         * Either because it has been excluded, or because conflict resolution selected a different version.
         */
        Evicted(false);

        private final boolean candidateForConflictResolution;

        ComponentSelectionState(boolean candidateForConflictResolution) {
            this.candidateForConflictResolution = candidateForConflictResolution;
        }

        boolean isCandidateForConflictResolution() {
            return candidateForConflictResolution;
        }
    }

    Capability getImplicitCapability() {
        return implicitCapability;
    }

    Capability findCapability(String group, String name) {
        if (id.getGroup().equals(group) && id.getName().equals(name)) {
            return implicitCapability;
        }
        return null;
    }

    boolean hasMoreThanOneSelectedNodeUsingVariantAwareResolution() {
        int count = 0;
        for (NodeState node : nodes) {
            if (node.isSelectedByVariantAwareResolution()) {
                count++;
                if (count == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentState that = (ComponentState) o;

        return that.resultId.equals(resultId);

    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
