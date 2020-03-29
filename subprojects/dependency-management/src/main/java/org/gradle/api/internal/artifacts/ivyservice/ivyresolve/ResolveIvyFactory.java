/*
 * Copyright 2011 the original author or authors.
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

import org.gradle.api.Action;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.internal.artifacts.ComponentMetadataProcessor;
import org.gradle.api.internal.artifacts.ComponentMetadataProcessorFactory;
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory;
import org.gradle.api.internal.artifacts.MetadataResolutionContext;
import org.gradle.api.internal.artifacts.configurations.ResolutionStrategyInternal;
import org.gradle.api.internal.artifacts.configurations.dynamicversion.CachePolicy;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionComparator;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelector;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.verification.DependencyVerificationOverride;
import org.gradle.api.internal.artifacts.ivyservice.modulecache.ModuleRepositoryCacheProvider;
import org.gradle.api.internal.artifacts.ivyservice.resolutionstrategy.DefaultComponentSelectionRules;
import org.gradle.api.internal.artifacts.repositories.AbstractArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.ArtifactResolutionDetails;
import org.gradle.api.internal.artifacts.repositories.ResolutionAwareRepository;
import org.gradle.api.internal.artifacts.repositories.resolver.ExternalResourceResolver;
import org.gradle.api.internal.artifacts.result.DefaultResolvedArtifactResult;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.internal.component.model.ComponentArtifactMetadata;
import org.gradle.internal.component.model.ComponentOverrideMetadata;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.component.model.DependencyMetadata;
import org.gradle.internal.component.model.ModuleSources;
import org.gradle.internal.instantiation.InstantiatorFactory;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.resolve.caching.ComponentMetadataSupplierRuleExecutor;
import org.gradle.internal.resolve.resolver.ArtifactResolver;
import org.gradle.internal.resolve.resolver.ComponentMetaDataResolver;
import org.gradle.internal.resolve.resolver.DependencyToComponentIdResolver;
import org.gradle.internal.resolve.resolver.OriginArtifactSelector;
import org.gradle.internal.resolve.result.BuildableArtifactResolveResult;
import org.gradle.internal.resolve.result.BuildableArtifactSetResolveResult;
import org.gradle.internal.resolve.result.BuildableComponentIdResolveResult;
import org.gradle.internal.resolve.result.BuildableComponentResolveResult;
import org.gradle.util.BuildCommencedTimeProvider;

import java.util.Collection;

public class ResolveIvyFactory {
    private final ModuleRepositoryCacheProvider cacheProvider;
    private final StartParameterResolutionOverride startParameterResolutionOverride;
    private final BuildCommencedTimeProvider timeProvider;
    private final VersionComparator versionComparator;
    private final ImmutableModuleIdentifierFactory moduleIdentifierFactory;
    private final RepositoryBlacklister repositoryBlacklister;
    private final VersionParser versionParser;
    private final InstantiatorFactory instantiatorFactory;

    private final DependencyVerificationOverride dependencyVerificationOverride;

    public ResolveIvyFactory(ModuleRepositoryCacheProvider cacheProvider,
                             StartParameterResolutionOverride startParameterResolutionOverride,
                             DependencyVerificationOverride dependencyVerificationOverride,
                             BuildCommencedTimeProvider timeProvider,
                             VersionComparator versionComparator,
                             ImmutableModuleIdentifierFactory moduleIdentifierFactory,
                             RepositoryBlacklister repositoryBlacklister,
                             VersionParser versionParser,
                             InstantiatorFactory instantiatorFactory) {
        this.cacheProvider = cacheProvider;
        this.startParameterResolutionOverride = startParameterResolutionOverride;
        this.timeProvider = timeProvider;
        this.versionComparator = versionComparator;
        this.moduleIdentifierFactory = moduleIdentifierFactory;
        this.repositoryBlacklister = repositoryBlacklister;
        this.versionParser = versionParser;
        this.instantiatorFactory = instantiatorFactory;
        this.dependencyVerificationOverride = dependencyVerificationOverride;
    }

    public ComponentResolvers create(String resolveContextName,
                                     ResolutionStrategyInternal resolutionStrategy,
                                     Collection<? extends ResolutionAwareRepository> repositories,
                                     ComponentMetadataProcessorFactory metadataProcessor,
                                     AttributeContainer consumerAttributes,
                                     AttributesSchema attributesSchema,
                                     ImmutableAttributesFactory attributesFactory,
                                     ComponentMetadataSupplierRuleExecutor componentMetadataSupplierRuleExecutor) {
        if (repositories.isEmpty()) {
            return new NoRepositoriesResolver();
        }

        CachePolicy cachePolicy = resolutionStrategy.getCachePolicy();
        startParameterResolutionOverride.applyToCachePolicy(cachePolicy);

        UserResolverChain moduleResolver = new UserResolverChain(versionComparator, resolutionStrategy.getComponentSelection(), versionParser, consumerAttributes, attributesSchema, attributesFactory, metadataProcessor, componentMetadataSupplierRuleExecutor, cachePolicy);
        ParentModuleLookupResolver parentModuleResolver = new ParentModuleLookupResolver(versionComparator, moduleIdentifierFactory, versionParser, consumerAttributes, attributesSchema, attributesFactory, metadataProcessor, componentMetadataSupplierRuleExecutor, cachePolicy);

        for (ResolutionAwareRepository repository : repositories) {
            ConfiguredModuleComponentRepository baseRepository = repository.createResolver();

            Instantiator instantiator = instantiatorFactory.inject();
            if (baseRepository instanceof ExternalResourceResolver) {
                ExternalResourceResolver resourceResolver = (ExternalResourceResolver) baseRepository;
                resourceResolver.setComponentResolvers(parentModuleResolver);
                instantiator = resourceResolver.getComponentMetadataInstantiator();
            }
            MetadataResolutionContext metadataResolutionContext = new DefaultMetadataResolutionContext(cachePolicy, instantiator);
            ComponentMetadataProcessor componentMetadataProcessor = metadataProcessor.createComponentMetadataProcessor(metadataResolutionContext);

            ModuleComponentRepository moduleComponentRepository = baseRepository;
            if (baseRepository.isLocal()) {
                moduleComponentRepository = new CachingModuleComponentRepository(moduleComponentRepository, cacheProvider.getInMemoryOnlyCaches(),
                    cachePolicy, timeProvider, componentMetadataProcessor);
                moduleComponentRepository = new LocalModuleComponentRepository(moduleComponentRepository);
            } else {
                moduleComponentRepository = startParameterResolutionOverride.overrideModuleVersionRepository(moduleComponentRepository);
                moduleComponentRepository = new CachingModuleComponentRepository(moduleComponentRepository, cacheProvider.getPersistentCaches(),
                    cachePolicy, timeProvider, componentMetadataProcessor);
            }
            moduleComponentRepository = cacheProvider.getResolvedArtifactCaches().provideResolvedArtifactCache(moduleComponentRepository);

            if (baseRepository.isDynamicResolveMode()) {
                moduleComponentRepository = new IvyDynamicResolveModuleComponentRepository(moduleComponentRepository);
            }
            moduleComponentRepository = new ErrorHandlingModuleComponentRepository(moduleComponentRepository, repositoryBlacklister);
            moduleComponentRepository = filterRepository(repository, moduleComponentRepository, resolveContextName, consumerAttributes);
            moduleComponentRepository = dependencyVerificationOverride.overrideDependencyVerification(moduleComponentRepository, resolveContextName, resolutionStrategy);
            moduleResolver.add(moduleComponentRepository);
            parentModuleResolver.add(moduleComponentRepository);
        }

        return moduleResolver;
    }

    private ModuleComponentRepository filterRepository(ResolutionAwareRepository repository, ModuleComponentRepository moduleComponentRepository, String consumerName, AttributeContainer consumerAttributes) {
        Action<? super ArtifactResolutionDetails> filter = null;
        if (repository instanceof AbstractArtifactRepository) {
            filter = ((AbstractArtifactRepository) repository).getContentFilter();
        }
        moduleComponentRepository = FilteredModuleComponentRepository.of(moduleComponentRepository, filter, consumerName, consumerAttributes);
        return moduleComponentRepository;
    }

    public ArtifactResult verifiedArtifact(DefaultResolvedArtifactResult defaultResolvedArtifactResult) {
        return dependencyVerificationOverride.verifiedArtifact(defaultResolvedArtifactResult);
    }

    /**
     * Provides access to the top-level resolver chain for looking up parent modules when parsing module descriptor files.
     */
    private static class ParentModuleLookupResolver implements ComponentResolvers, DependencyToComponentIdResolver, ComponentMetaDataResolver, ArtifactResolver {
        private final UserResolverChain delegate;

        public ParentModuleLookupResolver(VersionComparator versionComparator, ImmutableModuleIdentifierFactory moduleIdentifierFactory, VersionParser versionParser, AttributeContainer consumerAttributes, AttributesSchema attributesSchema, ImmutableAttributesFactory attributesFactory, ComponentMetadataProcessorFactory componentMetadataProcessorFactory, ComponentMetadataSupplierRuleExecutor componentMetadataSupplierRuleExecutor, CachePolicy cachePolicy) {
            this.delegate = new UserResolverChain(versionComparator, new DefaultComponentSelectionRules(moduleIdentifierFactory), versionParser, consumerAttributes, attributesSchema, attributesFactory, componentMetadataProcessorFactory, componentMetadataSupplierRuleExecutor, cachePolicy);
        }

        public void add(ModuleComponentRepository moduleComponentRepository) {
            delegate.add(moduleComponentRepository);
        }

        @Override
        public DependencyToComponentIdResolver getComponentIdResolver() {
            return this;
        }

        @Override
        public ComponentMetaDataResolver getComponentResolver() {
            return this;
        }

        @Override
        public ArtifactResolver getArtifactResolver() {
            return this;
        }

        @Override
        public OriginArtifactSelector getArtifactSelector() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void resolve(DependencyMetadata dependency, VersionSelector acceptor, VersionSelector rejector, BuildableComponentIdResolveResult result) {
            delegate.getComponentIdResolver().resolve(dependency, acceptor, rejector, result);
        }

        @Override
        public void resolve(final ComponentIdentifier identifier, final ComponentOverrideMetadata componentOverrideMetadata, final BuildableComponentResolveResult result) {
            delegate.getComponentResolver().resolve(identifier, componentOverrideMetadata, result);
        }

        @Override
        public boolean isFetchingMetadataCheap(ComponentIdentifier identifier) {
            return delegate.getComponentResolver().isFetchingMetadataCheap(identifier);
        }

        @Override
        public void resolveArtifactsWithType(final ComponentResolveMetadata component, final ArtifactType artifactType, final BuildableArtifactSetResolveResult result) {
            delegate.getArtifactResolver().resolveArtifactsWithType(component, artifactType, result);
        }

        @Override
        public void resolveArtifact(final ComponentArtifactMetadata artifact, final ModuleSources moduleSources, final BuildableArtifactResolveResult result) {
            delegate.getArtifactResolver().resolveArtifact(artifact, moduleSources, result);
        }
    }

    private static class DefaultMetadataResolutionContext implements MetadataResolutionContext {

        private final CachePolicy cachePolicy;
        private final Instantiator instantiator;

        private DefaultMetadataResolutionContext(CachePolicy cachePolicy, Instantiator instantiator) {
            this.cachePolicy = cachePolicy;
            this.instantiator = instantiator;
        }

        @Override
        public CachePolicy getCachePolicy() {
            return cachePolicy;
        }

        @Override
        public Instantiator getInjectingInstantiator() {
            return instantiator;
        }
    }
}
