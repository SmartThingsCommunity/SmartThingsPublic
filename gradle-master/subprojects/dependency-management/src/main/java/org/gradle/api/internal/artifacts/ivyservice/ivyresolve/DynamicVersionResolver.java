/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.ivyresolve;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ComponentMetadataSupplierDetails;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.internal.artifacts.ComponentMetadataProcessorFactory;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.configurations.dynamicversion.CachePolicy;
import org.gradle.api.internal.artifacts.dependencies.DefaultImmutableVersionConstraint;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.Version;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelector;
import org.gradle.api.internal.artifacts.repositories.ArtifactResolutionDetails;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.internal.action.InstantiatingAction;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;
import org.gradle.internal.component.external.model.ModuleComponentResolveMetadata;
import org.gradle.internal.component.external.model.ModuleDependencyMetadata;
import org.gradle.internal.component.model.DefaultComponentOverrideMetadata;
import org.gradle.internal.component.model.DependencyMetadata;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.resolve.ModuleVersionNotFoundException;
import org.gradle.internal.resolve.ModuleVersionResolveException;
import org.gradle.internal.resolve.RejectedByAttributesVersion;
import org.gradle.internal.resolve.RejectedByRuleVersion;
import org.gradle.internal.resolve.RejectedBySelectorVersion;
import org.gradle.internal.resolve.RejectedVersion;
import org.gradle.internal.resolve.caching.ComponentMetadataSupplierRuleExecutor;
import org.gradle.internal.resolve.result.BuildableComponentIdResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.ComponentSelectionContext;
import org.gradle.internal.resolve.result.DefaultBuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.DefaultBuildableModuleVersionListingResolveResult;
import org.gradle.internal.resolve.result.ResourceAwareResolveResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.gradle.internal.resolve.ResolveExceptionAnalyzer.hasCriticalFailure;
import static org.gradle.internal.resolve.ResolveExceptionAnalyzer.isCriticalFailure;
import static org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult.State.Failed;
import static org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult.State.Resolved;

public class DynamicVersionResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicVersionResolver.class);

    private final List<ModuleComponentRepository> repositories = new ArrayList<ModuleComponentRepository>();
    private final List<String> repositoryNames = new ArrayList<String>();
    private final VersionedComponentChooser versionedComponentChooser;
    private final VersionParser versionParser;
    private final Transformer<ModuleComponentResolveMetadata, RepositoryChainModuleResolution> metaDataFactory;
    private final ImmutableAttributesFactory attributesFactory;
    private final ComponentMetadataProcessorFactory componentMetadataProcessor;
    private final ComponentMetadataSupplierRuleExecutor componentMetadataSupplierRuleExecutor;
    private final CachePolicy cachePolicy;

    public DynamicVersionResolver(VersionedComponentChooser versionedComponentChooser, VersionParser versionParser, Transformer<ModuleComponentResolveMetadata, RepositoryChainModuleResolution> metaDataFactory, ImmutableAttributesFactory attributesFactory, ComponentMetadataProcessorFactory componentMetadataProcessor, ComponentMetadataSupplierRuleExecutor componentMetadataSupplierRuleExecutor, CachePolicy cachePolicy) {
        this.versionedComponentChooser = versionedComponentChooser;
        this.versionParser = versionParser;
        this.metaDataFactory = metaDataFactory;
        this.attributesFactory = attributesFactory;
        this.componentMetadataProcessor = componentMetadataProcessor;
        this.componentMetadataSupplierRuleExecutor = componentMetadataSupplierRuleExecutor;
        this.cachePolicy = cachePolicy;
    }

    public void add(ModuleComponentRepository repository) {
        repositories.add(repository);
        repositoryNames.add(repository.getName());
    }

    public void resolve(ModuleDependencyMetadata dependency, VersionSelector versionSelector, VersionSelector rejectedVersionSelector, AttributeContainer consumerAttributes, BuildableComponentIdResolveResult result) {
        ModuleComponentSelector requested = dependency.getSelector();
        LOGGER.debug("Attempting to resolve version for {} using repositories {}", requested, repositoryNames);
        List<Throwable> errors = new ArrayList<Throwable>();

        List<RepositoryResolveState> resolveStates = Lists.newArrayListWithCapacity(repositories.size());
        for (ModuleComponentRepository repository : repositories) {
            resolveStates.add(new RepositoryResolveState(versionedComponentChooser, dependency, repository, versionSelector, rejectedVersionSelector, versionParser, consumerAttributes, attributesFactory, componentMetadataProcessor, componentMetadataSupplierRuleExecutor, cachePolicy));
        }

        final RepositoryChainModuleResolution latestResolved = findLatestModule(resolveStates, errors);
        if (latestResolved != null) {
            LOGGER.debug("Using {} from {}", latestResolved.module.getModuleVersionId(), latestResolved.repository);
            for (Throwable error : errors) {
                LOGGER.debug("Discarding resolve failure.", error);
            }

            found(result, resolveStates, latestResolved);
            return;
        }
        if (!errors.isEmpty()) {
            result.failed(new ModuleVersionResolveException(requested, errors));
        } else {
            notFound(result, requested, resolveStates);
        }
    }

    private void found(BuildableComponentIdResolveResult result, List<RepositoryResolveState> resolveStates, RepositoryChainModuleResolution latestResolved) {
        for (RepositoryResolveState resolveState : resolveStates) {
            resolveState.registerAttempts(result);
        }
        result.resolved(metaDataFactory.transform(latestResolved));
    }

    private void notFound(BuildableComponentIdResolveResult result, ModuleComponentSelector requested, List<RepositoryResolveState> resolveStates) {
        for (RepositoryResolveState resolveState : resolveStates) {
            resolveState.applyTo(result);
        }
        if (result.isRejected()) {
            // We have a matching component id that was rejected. These are handled later in the resolution process
            // (after conflict resolution), so it is not a failure at this stage.
            return;
        }
        result.failed(new ModuleVersionNotFoundException(requested, result.getAttempted(), result.getUnmatchedVersions(), result.getRejectedVersions()));
    }

    private RepositoryChainModuleResolution findLatestModule(List<RepositoryResolveState> resolveStates, Collection<Throwable> failures) {
        LinkedList<RepositoryResolveState> queue = new LinkedList<RepositoryResolveState>();
        queue.addAll(resolveStates);

        LinkedList<RepositoryResolveState> missing = new LinkedList<RepositoryResolveState>();

        // A first pass to do local resolves only
        RepositoryChainModuleResolution best = findLatestModule(queue, failures, missing);
        if (hasCriticalFailure(failures)) {
            return null;
        }
        if (best != null) {
            return best;
        }

        // Nothing found - do a second pass
        queue.addAll(missing);
        missing.clear();
        return findLatestModule(queue, failures, missing);
    }

    private RepositoryChainModuleResolution findLatestModule(LinkedList<RepositoryResolveState> queue, Collection<Throwable> failures, Collection<RepositoryResolveState> missing) {
        RepositoryChainModuleResolution best = null;
        while (!queue.isEmpty()) {
            RepositoryResolveState request = queue.removeFirst();
            try {
                request.resolve();
            } catch (Exception t) {
                failures.add(t);
                if (isCriticalFailure(t)) {
                    queue.clear();
                }
                continue;
            }
            switch (request.resolvedVersionMetadata.getState()) {
                case Failed:
                    failures.add(request.resolvedVersionMetadata.getFailure());
                    if (isCriticalFailure(request.resolvedVersionMetadata.getFailure())) {
                        queue.clear();
                    }
                    break;
                case Missing:
                case Unknown:
                    // Queue this up for checking again later
                    // This is done because we're checking what we have locally in cache, and there may be nothing
                    // so we're queuing it back so that the next time we check in remote access.
                    if (request.canMakeFurtherAttempts()) {
                        missing.add(request);
                    }
                    break;
                case Resolved:
                    RepositoryChainModuleResolution moduleResolution = new RepositoryChainModuleResolution(request.repository, request.resolvedVersionMetadata.getMetaData());
                    best = chooseBest(best, moduleResolution);
                    break;
                default:
                    throw new IllegalStateException("Unexpected state for resolution: " + request.resolvedVersionMetadata.getState());
            }
        }

        return best;
    }

    private RepositoryChainModuleResolution chooseBest(RepositoryChainModuleResolution one, RepositoryChainModuleResolution two) {
        if (one == null || two == null) {
            return two == null ? one : two;
        }
        return versionedComponentChooser.selectNewestComponent(one.module, two.module) == one.module ? one : two;
    }

    private static class AttemptCollector implements Action<ResourceAwareResolveResult> {
        private final List<String> attempts = new ArrayList<String>();

        @Override
        public void execute(ResourceAwareResolveResult resourceAwareResolveResult) {
            attempts.addAll(resourceAwareResolveResult.getAttempted());
        }

        public void applyTo(ResourceAwareResolveResult result) {
            for (String url : attempts) {
                result.attempted(url);
            }
        }
    }

    /**
     * This class contains state used to resolve a component from a specific repository. It can be used in multiple passes,
     * (local access, remote access), and will be used for 2 different steps:
     *
     * 1. selecting a version, thanks to the versioned component chooser, for a specific version selector
     * 2. once the selection is done, fetch metadata for this component
     */
    private static class RepositoryResolveState implements ComponentSelectionContext {
        private final VersionedComponentChooser versionedComponentChooser;
        private final BuildableModuleComponentMetaDataResolveResult resolvedVersionMetadata = new DefaultBuildableModuleComponentMetaDataResolveResult();
        private final Map<String, CandidateResult> candidateComponents = new LinkedHashMap<String, CandidateResult>();
        private final Set<String> unmatchedVersions = Sets.newLinkedHashSet();
        private final Set<RejectedVersion> rejectedVersions = Sets.newLinkedHashSet();
        private final VersionListResult versionListingResult;
        private final ModuleComponentRepository repository;
        private final AttemptCollector attemptCollector;
        private final ModuleDependencyMetadata dependency;
        private final VersionSelector versionSelector;
        private final VersionSelector rejectedVersionSelector;
        private final VersionParser versionParser;
        private final ImmutableAttributes consumerAttributes;
        private final ComponentMetadataProcessorFactory componentMetadataProcessorFactory;
        private final ImmutableAttributesFactory attributesFactory;
        private final ComponentMetadataSupplierRuleExecutor metadataSupplierRuleExecutor;
        private final CachePolicy cachePolicy;
        private ModuleComponentIdentifier firstRejected = null;


        public RepositoryResolveState(VersionedComponentChooser versionedComponentChooser, ModuleDependencyMetadata dependency, ModuleComponentRepository repository, VersionSelector versionSelector, VersionSelector rejectedVersionSelector, VersionParser versionParser, AttributeContainer consumerAttributes, ImmutableAttributesFactory attributesFactory, ComponentMetadataProcessorFactory componentMetadataProcessorFactory, ComponentMetadataSupplierRuleExecutor metadataSupplierRuleExecutor, CachePolicy cachePolicy) {
            this.versionedComponentChooser = versionedComponentChooser;
            this.dependency = dependency;
            this.versionSelector = versionSelector;
            this.rejectedVersionSelector = rejectedVersionSelector;
            this.repository = repository;
            this.versionParser = versionParser;
            this.componentMetadataProcessorFactory = componentMetadataProcessorFactory;
            this.attributesFactory = attributesFactory;
            this.metadataSupplierRuleExecutor = metadataSupplierRuleExecutor;
            this.cachePolicy = cachePolicy;
            this.attemptCollector = new AttemptCollector();
            this.consumerAttributes = buildAttributes(consumerAttributes, attributesFactory);
            versionListingResult = new VersionListResult(dependency, repository);
        }

        private ImmutableAttributes buildAttributes(AttributeContainer consumerAttributes, ImmutableAttributesFactory attributesFactory) {
            ImmutableAttributes immutableConsumerAttributes = ((AttributeContainerInternal) consumerAttributes).asImmutable();
            ImmutableAttributes dependencyAttributes = ((AttributeContainerInternal) dependency.getSelector().getAttributes()).asImmutable();
            return attributesFactory.concat(immutableConsumerAttributes, dependencyAttributes);
        }

        public boolean canMakeFurtherAttempts() {
            return versionListingResult.canMakeFurtherAttempts();
        }

        void resolve() {
            versionListingResult.resolve();
            switch (versionListingResult.result.getState()) {
                case Failed:
                    resolvedVersionMetadata.failed(versionListingResult.result.getFailure());
                    break;
                case Listed:
                    selectMatchingVersionAndResolve();
                    break;
                case Unknown:
                    break;
                default:
                    throw new IllegalStateException("Unexpected state for version list result.");
            }
        }

        private void selectMatchingVersionAndResolve() {
            // TODO - reuse metaData if it was already fetched to select the component from the version list
            versionedComponentChooser.selectNewestMatchingComponent(candidates(), this, versionSelector, rejectedVersionSelector, consumerAttributes);
        }

        @Override
        public void matches(ModuleComponentIdentifier moduleComponentIdentifier) {
            String version = moduleComponentIdentifier.getVersion();
            CandidateResult candidateResult = candidateComponents.get(version);
            candidateResult.tryResolveMetadata(resolvedVersionMetadata);
        }

        @Override
        public void failed(ModuleVersionResolveException failure) {
            resolvedVersionMetadata.failed(failure);
        }

        @Override
        public void noMatchFound() {
            resolvedVersionMetadata.missing();
        }

        @Override
        public void notMatched(ModuleComponentIdentifier id, VersionSelector requestedVersionMatcher) {
            unmatchedVersions.add(id.getVersion());
        }

        @Override
        public void rejectedByRule(RejectedByRuleVersion id) {
            rejectedVersions.add(id);
        }

        @Override
        public void doesNotMatchConsumerAttributes(RejectedByAttributesVersion rejectedVersion) {
            rejectedVersions.add(rejectedVersion);
        }

        @Override
        public Action<? super ArtifactResolutionDetails> getContentFilter() {
            if (repository instanceof FilteredModuleComponentRepository) {
                return ((FilteredModuleComponentRepository) repository).getFilterAction();
            }
            return null;
        }

        @Override
        public String getConfigurationName() {
            if (repository instanceof FilteredModuleComponentRepository) {
                return ((FilteredModuleComponentRepository) repository).getConsumerName();
            }
            return null;
        }

        @Override
        public ImmutableAttributes getConsumerAttributes() {
            return consumerAttributes;
        }

        @Override
        public void rejectedBySelector(ModuleComponentIdentifier id, VersionSelector versionSelector) {
            if (firstRejected == null) {
                firstRejected = id;
            }
            rejectedVersions.add(new RejectedBySelectorVersion(id, versionSelector));
        }

        private List<CandidateResult> candidates() {
            List<CandidateResult> candidates = new ArrayList<CandidateResult>();
            for (String version : versionListingResult.result.getVersions()) {
                CandidateResult candidateResult = candidateComponents.get(version);
                if (candidateResult == null) {
                    candidateResult = new CandidateResult(dependency, version, repository, attemptCollector, versionParser, componentMetadataProcessorFactory, attributesFactory, metadataSupplierRuleExecutor, cachePolicy);
                    candidateComponents.put(version, candidateResult);
                }
                candidates.add(candidateResult);
            }
            return candidates;
        }

        protected void applyTo(BuildableComponentIdResolveResult target) {
            registerAttempts(target);

            if (firstRejected != null) {
                target.rejected(firstRejected, DefaultModuleVersionIdentifier.newId(firstRejected));
            }
        }

        private void registerAttempts(BuildableComponentIdResolveResult target) {
            versionListingResult.applyTo(target);
            attemptCollector.applyTo(target);
            target.unmatched(unmatchedVersions);
            target.rejections(rejectedVersions);
        }
    }

    private static class CandidateResult implements ModuleComponentResolveState {
        private final ModuleComponentIdentifier identifier;
        private final ModuleComponentRepository repository;
        private final AttemptCollector attemptCollector;
        private final ModuleDependencyMetadata dependencyMetadata;
        private final Version version;
        private final ComponentMetadataProcessorFactory componentMetadataProcessorFactory;
        private final ImmutableAttributesFactory attributesFactory;
        private final ComponentMetadataSupplierRuleExecutor supplierRuleExecutor;
        private boolean searchedLocally;
        private boolean searchedRemotely;
        private final DefaultBuildableModuleComponentMetaDataResolveResult result = new DefaultBuildableModuleComponentMetaDataResolveResult();
        private final CachePolicy cachePolicy;

        public CandidateResult(ModuleDependencyMetadata dependencyMetadata, String version, ModuleComponentRepository repository, AttemptCollector attemptCollector, VersionParser versionParser, ComponentMetadataProcessorFactory componentMetadataProcessorFactory, ImmutableAttributesFactory attributesFactory, ComponentMetadataSupplierRuleExecutor supplierRuleExecutor, CachePolicy cachePolicy) {
            this.dependencyMetadata = dependencyMetadata;
            this.componentMetadataProcessorFactory = componentMetadataProcessorFactory;
            this.attributesFactory = attributesFactory;
            this.supplierRuleExecutor = supplierRuleExecutor;
            this.cachePolicy = cachePolicy;
            this.version = versionParser.transform(version);
            this.repository = repository;
            this.attemptCollector = attemptCollector;
            ModuleComponentSelector requested = dependencyMetadata.getSelector();
            this.identifier = DefaultModuleComponentIdentifier.newId(requested.getModuleIdentifier(), version);
        }

        @Override
        public ModuleComponentIdentifier getId() {
            return identifier;
        }

        @Override
        public Version getVersion() {
            return version;
        }

        @Override
        public BuildableModuleComponentMetaDataResolveResult resolve() {
            if (!searchedLocally) {
                searchedLocally = true;
                process(repository.getLocalAccess(), result);
                if (result.hasResult() && result.isAuthoritative()) {
                    // Authoritative result means don't do remote search
                    searchedRemotely = true;
                }
            }
            if (result.getState() == Resolved || result.getState() == Failed) {
                return result;
            }
            if (!searchedRemotely) {
                searchedRemotely = true;
                process(repository.getRemoteAccess(), result);
            }
            return result;
        }

        @Override
        public ComponentMetadataProcessorFactory getComponentMetadataProcessorFactory() {
            return componentMetadataProcessorFactory;
        }

        @Override
        public ImmutableAttributesFactory getAttributesFactory() {
            return attributesFactory;
        }

        @Override
        public InstantiatingAction<ComponentMetadataSupplierDetails> getComponentMetadataSupplier() {
            return repository.getComponentMetadataSupplier();
        }

        @Override
        public ComponentMetadataSupplierRuleExecutor getComponentMetadataSupplierExecutor() {
            return supplierRuleExecutor;
        }

        @Override
        public CachePolicy getCachePolicy() {
            return cachePolicy;
        }

        private void process(ModuleComponentRepositoryAccess access, DefaultBuildableModuleComponentMetaDataResolveResult result) {
            DependencyMetadata dependency = dependencyMetadata.withRequestedVersion(new DefaultImmutableVersionConstraint(version.getSource()));
            IvyArtifactName firstArtifact = dependency.getArtifacts().isEmpty() ? null : dependency.getArtifacts().get(0);
            access.resolveComponentMetaData(identifier, DefaultComponentOverrideMetadata.forDependency(dependency.isChanging(), firstArtifact, DefaultComponentOverrideMetadata.extractClientModule(dependency)), result);
            attemptCollector.execute(result);
        }

        /**
         * Once a version has been selected, this tries to resolve metadata for this specific version. If it can it
         * will copy the result to the target builder
         *
         * @param target where to put metadata
         */
        private void tryResolveMetadata(BuildableModuleComponentMetaDataResolveResult target) {
            BuildableModuleComponentMetaDataResolveResult result = resolve();
            switch (result.getState()) {
                case Resolved:
                    target.resolved(result.getMetaData());
                    return;
                case Missing:
                    result.applyTo(target);
                    target.missing();
                    return;
                case Failed:
                    target.failed(result.getFailure());
                    return;
                case Unknown:
                    return;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private static class VersionListResult {
        private final DefaultBuildableModuleVersionListingResolveResult result = new DefaultBuildableModuleVersionListingResolveResult();
        private final ModuleComponentRepository repository;
        private final ModuleDependencyMetadata dependency;

        private boolean searchedLocally;
        private boolean searchedRemotely;

        public VersionListResult(ModuleDependencyMetadata dependency, ModuleComponentRepository repository) {
            this.dependency = dependency;
            this.repository = repository;
        }

        void resolve() {
            if (!searchedLocally) {
                searchedLocally = true;
                process(dependency, repository.getLocalAccess());
                if (result.hasResult()) {
                    if (result.isAuthoritative()) {
                        // Authoritative result - don't need to try remote
                        searchedRemotely = true;
                    }
                    return;
                }
                // Otherwise, try remotely
            }
            if (!searchedRemotely) {
                searchedRemotely = true;
                process(dependency, repository.getRemoteAccess());
            }

            // Otherwise, just reuse previous result
        }

        public boolean canMakeFurtherAttempts() {
            return !searchedRemotely;
        }

        public void applyTo(ResourceAwareResolveResult target) {
            result.applyTo(target);
        }

        private void process(ModuleDependencyMetadata dynamicVersionDependency, ModuleComponentRepositoryAccess moduleAccess) {
            moduleAccess.listModuleVersions(dynamicVersionDependency, result);
        }
    }

}
