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

import com.google.common.collect.Maps;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.internal.artifacts.ComponentSelectorConverter;
import org.gradle.api.internal.artifacts.ResolvedConfigurationIdentifier;
import org.gradle.api.internal.artifacts.ResolvedVersionConstraint;
import org.gradle.api.internal.artifacts.dependencies.DefaultResolvedVersionConstraint;
import org.gradle.api.internal.artifacts.ivyservice.dependencysubstitution.DependencySubstitutionApplicator;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.Version;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.ModuleConflictResolver;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.ModuleExclusions;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.selectors.ComponentStateFactory;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.selectors.SelectorStateResolver;
import org.gradle.api.internal.attributes.AttributeDesugaring;
import org.gradle.api.internal.attributes.AttributesSchemaInternal;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Pair;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.component.model.ConfigurationMetadata;
import org.gradle.internal.component.model.DependencyMetadata;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.resolve.resolver.ComponentMetaDataResolver;
import org.gradle.internal.resolve.resolver.DependencyToComponentIdResolver;
import org.gradle.internal.resolve.result.ComponentResolveResult;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global resolution state.
 */
class ResolveState implements ComponentStateFactory<ComponentState> {
    private final Spec<? super DependencyMetadata> edgeFilter;
    private final Map<ModuleIdentifier, ModuleResolveState> modules;
    private final Map<ResolvedConfigurationIdentifier, NodeState> nodes;
    private final Map<Pair<ComponentSelector, Boolean>, SelectorState> selectors;
    private final RootNode root;
    private final IdGenerator<Long> idGenerator;
    private final DependencyToComponentIdResolver idResolver;
    private final ComponentMetaDataResolver metaDataResolver;
    private final Deque<NodeState> queue;
    private final AttributesSchemaInternal attributesSchema;
    private final ModuleExclusions moduleExclusions;
    private final DeselectVersionAction deselectVersionAction = new DeselectVersionAction(this);
    private final ReplaceSelectionWithConflictResultAction replaceSelectionWithConflictResultAction;
    private final ComponentSelectorConverter componentSelectorConverter;
    private final ImmutableAttributesFactory attributesFactory;
    private final DependencySubstitutionApplicator dependencySubstitutionApplicator;
    private final VersionSelectorScheme versionSelectorScheme;
    private final Comparator<Version> versionComparator;
    private final VersionParser versionParser;
    private final SelectorStateResolver<ComponentState> selectorStateResolver;
    private final ResolveOptimizations resolveOptimizations;
    private final Map<VersionConstraint, ResolvedVersionConstraint> resolvedVersionConstraints = Maps.newHashMap();
    private final AttributeDesugaring attributeDesugaring;
    private final boolean denyDynamicSelector;

    public ResolveState(IdGenerator<Long> idGenerator,
                        ComponentResolveResult rootResult,
                        String rootConfigurationName,
                        DependencyToComponentIdResolver idResolver,
                        ComponentMetaDataResolver metaDataResolver,
                        Spec<? super DependencyMetadata> edgeFilter,
                        AttributesSchemaInternal attributesSchema,
                        ModuleExclusions moduleExclusions,
                        ComponentSelectorConverter componentSelectorConverter,
                        ImmutableAttributesFactory attributesFactory,
                        DependencySubstitutionApplicator dependencySubstitutionApplicator,
                        VersionSelectorScheme versionSelectorScheme,
                        Comparator<Version> versionComparator,
                        VersionParser versionParser,
                        ModuleConflictResolver conflictResolver,
                        boolean denyDynamicSelectorm,
                        int graphSize) {
        this.idGenerator = idGenerator;
        this.idResolver = idResolver;
        this.metaDataResolver = metaDataResolver;
        this.edgeFilter = edgeFilter;
        this.attributesSchema = attributesSchema;
        this.moduleExclusions = moduleExclusions;
        this.componentSelectorConverter = componentSelectorConverter;
        this.attributesFactory = attributesFactory;
        this.dependencySubstitutionApplicator = dependencySubstitutionApplicator;
        this.versionSelectorScheme = versionSelectorScheme;
        this.versionComparator = versionComparator;
        this.versionParser = versionParser;
        this.modules = new LinkedHashMap<ModuleIdentifier, ModuleResolveState>(graphSize);
        this.nodes = new LinkedHashMap<ResolvedConfigurationIdentifier, NodeState>(3 * graphSize / 2);
        this.selectors = new LinkedHashMap<Pair<ComponentSelector, Boolean>, SelectorState>(5 * graphSize / 2);
        this.queue = new ArrayDeque<NodeState>(graphSize);
        this.resolveOptimizations = new ResolveOptimizations();
        this.attributeDesugaring = new AttributeDesugaring(attributesFactory);
        ComponentState rootVersion = getRevision(rootResult.getId(), rootResult.getModuleVersionId(), rootResult.getMetadata());
        final ResolvedConfigurationIdentifier id = new ResolvedConfigurationIdentifier(rootVersion.getId(), rootConfigurationName);
        ConfigurationMetadata configurationMetadata = rootVersion.getMetadata().getConfiguration(id.getConfiguration());
        root = new RootNode(idGenerator.generateId(), rootVersion, id, this, configurationMetadata);
        nodes.put(root.getResolvedConfigurationId(), root);
        root.getComponent().getModule().select(root.getComponent());
        this.replaceSelectionWithConflictResultAction = new ReplaceSelectionWithConflictResultAction(this);
        selectorStateResolver = new SelectorStateResolver<ComponentState>(conflictResolver, this, rootVersion, resolveOptimizations);
        this.denyDynamicSelector = denyDynamicSelectorm;
        getModule(rootResult.getModuleVersionId().getModule()).setSelectorStateResolver(selectorStateResolver);
    }

    public Collection<ModuleResolveState> getModules() {
        return modules.values();
    }

    Spec<? super DependencyMetadata> getEdgeFilter() {
        return edgeFilter;
    }

    RootNode getRoot() {
        return root;
    }

    public ModuleResolveState getModule(ModuleIdentifier id) {
        return modules.computeIfAbsent(id, mid -> new ModuleResolveState(idGenerator, id, metaDataResolver, attributesFactory, versionComparator, versionParser, selectorStateResolver, resolveOptimizations));
    }

    @Override
    public ComponentState getRevision(ComponentIdentifier componentIdentifier, ModuleVersionIdentifier id, ComponentResolveMetadata metadata) {
        ComponentState componentState = getModule(id.getModule()).getVersion(id, componentIdentifier);
        if (!componentState.alreadyResolved()) {
            componentState.setMetadata(metadata);
        }
        return componentState;
    }

    public Collection<NodeState> getNodes() {
        return nodes.values();
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public NodeState getNode(ComponentState module, ConfigurationMetadata configurationMetadata) {
        ResolvedConfigurationIdentifier id = new ResolvedConfigurationIdentifier(module.getId(), configurationMetadata.getName());
        return nodes.computeIfAbsent(id, rci -> new NodeState(idGenerator.generateId(), id, module, this, configurationMetadata));
    }

    public Collection<SelectorState> getSelectors() {
        return selectors.values();
    }

    public SelectorState getSelector(DependencyState dependencyState, boolean ignoreVersion) {
        SelectorState selectorState = selectors.computeIfAbsent(Pair.of(dependencyState.getRequested(), ignoreVersion), req -> {
            ModuleIdentifier moduleIdentifier = dependencyState.getModuleIdentifier();
            return new SelectorState(idGenerator.generateId(), dependencyState, idResolver, this, moduleIdentifier, ignoreVersion);
        });
        selectorState.update(dependencyState);
        return selectorState;
    }

    public NodeState peek() {
        return queue.isEmpty() ? null : queue.getFirst();
    }

    public NodeState pop() {
        NodeState next = queue.removeFirst();
        return next.dequeue();
    }

    /**
     * Called when a change is made to a configuration node, such that its dependency graph <em>may</em> now be larger than it previously was, and the node should be visited.
     */
    public void onMoreSelected(NodeState node) {
        // Add to the end of the queue, so that we traverse the graph in breadth-wise order to pick up as many conflicts as
        // possible before attempting to resolve them
        if (node.enqueue()) {
            queue.addLast(node);
        }
    }

    /**
     * Called when a change is made to a configuration node, such that its dependency graph <em>may</em> now be smaller than it previously was, and the node should be visited.
     */
    public void onFewerSelected(NodeState node) {
        // Add to the front of the queue, to flush out configurations that are no longer required.
        if (node.enqueue()) {
            queue.addFirst(node);
        }
    }

    public AttributesSchemaInternal getAttributesSchema() {
        return attributesSchema;
    }

    public ModuleExclusions getModuleExclusions() {
        return moduleExclusions;
    }

    public DeselectVersionAction getDeselectVersionAction() {
        return deselectVersionAction;
    }

    public ReplaceSelectionWithConflictResultAction getReplaceSelectionWithConflictResultAction() {
        return replaceSelectionWithConflictResultAction;
    }

    public ComponentSelectorConverter getComponentSelectorConverter() {
        return componentSelectorConverter;
    }

    public ImmutableAttributesFactory getAttributesFactory() {
        return attributesFactory;
    }

    public DependencySubstitutionApplicator getDependencySubstitutionApplicator() {
        return dependencySubstitutionApplicator;
    }

    PendingDependenciesVisitor newPendingDependenciesVisitor() {
        return new DefaultPendingDependenciesVisitor(this);
    }

    ResolvedVersionConstraint resolveVersionConstraint(ComponentSelector selector) {
        if (selector instanceof ModuleComponentSelector) {
            return resolveVersionConstraint(((ModuleComponentSelector) selector).getVersionConstraint());
        }
        return null;
    }

    ResolvedVersionConstraint resolveVersionConstraint(VersionConstraint vc) {
        return resolvedVersionConstraints.computeIfAbsent(vc, key -> new DefaultResolvedVersionConstraint(key, versionSelectorScheme));
    }

    ImmutableAttributes desugar(ImmutableAttributes attributes) {
        return attributeDesugaring.desugar(attributes);
    }

    ComponentSelector desugarSelector(ComponentSelector requested) {
        return attributeDesugaring.desugarSelector(requested);
    }

    AttributeDesugaring getAttributeDesugaring() {
        return attributeDesugaring;
    }

    void virtualPlatformInUse() {
        resolveOptimizations.declareVirtualPlatformInUse();
    }

    ResolveOptimizations getResolveOptimizations() {
        return resolveOptimizations;
    }

    public boolean isDenyDynamicSelector() {
        return denyDynamicSelector;
    }
}
