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
package org.gradle.api.internal.artifacts.repositories;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.ComponentMetadataListerDetails;
import org.gradle.api.artifacts.ComponentMetadataSupplierDetails;
import org.gradle.api.artifacts.repositories.AuthenticationContainer;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyArtifactRepositoryMetaDataProvider;
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout;
import org.gradle.api.artifacts.repositories.RepositoryLayout;
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory;
import org.gradle.api.internal.artifacts.ivyservice.IvyContextManager;
import org.gradle.api.internal.artifacts.ivyservice.IvyContextualMetaDataParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ConfiguredModuleComponentRepository;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.GradleModuleMetadataParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.IvyModuleDescriptorConverter;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.IvyXmlModuleDescriptorParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.MetaDataParser;
import org.gradle.api.internal.artifacts.repositories.descriptor.IvyRepositoryDescriptor;
import org.gradle.api.internal.artifacts.repositories.descriptor.RepositoryDescriptor;
import org.gradle.api.internal.artifacts.repositories.layout.AbstractRepositoryLayout;
import org.gradle.api.internal.artifacts.repositories.layout.DefaultIvyPatternRepositoryLayout;
import org.gradle.api.internal.artifacts.repositories.layout.GradleRepositoryLayout;
import org.gradle.api.internal.artifacts.repositories.layout.IvyRepositoryLayout;
import org.gradle.api.internal.artifacts.repositories.layout.MavenRepositoryLayout;
import org.gradle.api.internal.artifacts.repositories.layout.ResolvedPattern;
import org.gradle.api.internal.artifacts.repositories.metadata.DefaultArtifactMetadataSource;
import org.gradle.api.internal.artifacts.repositories.metadata.DefaultGradleModuleMetadataSource;
import org.gradle.api.internal.artifacts.repositories.metadata.DefaultImmutableMetadataSources;
import org.gradle.api.internal.artifacts.repositories.metadata.DefaultIvyDescriptorMetadataSource;
import org.gradle.api.internal.artifacts.repositories.metadata.ImmutableMetadataSources;
import org.gradle.api.internal.artifacts.repositories.metadata.IvyMetadataArtifactProvider;
import org.gradle.api.internal.artifacts.repositories.metadata.IvyMutableModuleMetadataFactory;
import org.gradle.api.internal.artifacts.repositories.metadata.MetadataSource;
import org.gradle.api.internal.artifacts.repositories.metadata.RedirectingGradleMetadataModuleMetadataSource;
import org.gradle.api.internal.artifacts.repositories.resolver.IvyResolver;
import org.gradle.api.internal.artifacts.repositories.resolver.PatternBasedResolver;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.action.InstantiatingAction;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;
import org.gradle.internal.component.external.model.ModuleComponentArtifactMetadata;
import org.gradle.internal.component.external.model.ivy.MutableIvyModuleResolveMetadata;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.internal.hash.ChecksumService;
import org.gradle.internal.instantiation.InstantiatorFactory;
import org.gradle.internal.isolation.IsolatableFactory;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.resource.local.FileResourceRepository;
import org.gradle.internal.resource.local.FileStore;
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DefaultIvyArtifactRepository extends AbstractAuthenticationSupportedRepository implements IvyArtifactRepository, ResolutionAwareRepository, PublicationAwareRepository {
    private Set<String> schemes = null;
    private AbstractRepositoryLayout layout;
    private final DefaultUrlArtifactRepository urlArtifactRepository;
    private final AdditionalPatternsRepositoryLayout additionalPatternsLayout;
    private final FileResolver fileResolver;
    private final RepositoryTransportFactory transportFactory;
    private final LocallyAvailableResourceFinder<ModuleComponentArtifactMetadata> locallyAvailableResourceFinder;
    private final MetaDataProvider metaDataProvider;
    private final Instantiator instantiator;
    private final FileStore<ModuleComponentArtifactIdentifier> artifactFileStore;
    private final FileStore<String> externalResourcesFileStore;
    private final IvyContextManager ivyContextManager;
    private final ImmutableModuleIdentifierFactory moduleIdentifierFactory;
    private final InstantiatorFactory instantiatorFactory;
    private final FileResourceRepository fileResourceRepository;
    private final GradleModuleMetadataParser moduleMetadataParser;
    private final IvyMutableModuleMetadataFactory metadataFactory;
    private final IsolatableFactory isolatableFactory;
    private final ChecksumService checksumService;
    private final IvyMetadataSources metadataSources = new IvyMetadataSources();

    public DefaultIvyArtifactRepository(FileResolver fileResolver, RepositoryTransportFactory transportFactory,
                                        LocallyAvailableResourceFinder<ModuleComponentArtifactMetadata> locallyAvailableResourceFinder,
                                        FileStore<ModuleComponentArtifactIdentifier> artifactFileStore,
                                        FileStore<String> externalResourcesFileStore,
                                        AuthenticationContainer authenticationContainer,
                                        IvyContextManager ivyContextManager,
                                        ImmutableModuleIdentifierFactory moduleIdentifierFactory,
                                        InstantiatorFactory instantiatorFactory,
                                        FileResourceRepository fileResourceRepository,
                                        GradleModuleMetadataParser moduleMetadataParser,
                                        IvyMutableModuleMetadataFactory metadataFactory,
                                        IsolatableFactory isolatableFactory,
                                        ObjectFactory objectFactory,
                                        DefaultUrlArtifactRepository.Factory urlArtifactRepositoryFactory,
                                        ChecksumService checksumService) {
        super(instantiatorFactory.decorateLenient(), authenticationContainer, objectFactory);
        this.fileResolver = fileResolver;
        this.urlArtifactRepository = urlArtifactRepositoryFactory.create("Ivy", this::getDisplayName);
        this.transportFactory = transportFactory;
        this.locallyAvailableResourceFinder = locallyAvailableResourceFinder;
        this.artifactFileStore = artifactFileStore;
        this.externalResourcesFileStore = externalResourcesFileStore;
        this.additionalPatternsLayout = new AdditionalPatternsRepositoryLayout(fileResolver);
        this.moduleIdentifierFactory = moduleIdentifierFactory;
        this.instantiatorFactory = instantiatorFactory;
        this.fileResourceRepository = fileResourceRepository;
        this.moduleMetadataParser = moduleMetadataParser;
        this.metadataFactory = metadataFactory;
        this.isolatableFactory = isolatableFactory;
        this.checksumService = checksumService;
        this.layout = new GradleRepositoryLayout();
        this.metaDataProvider = new MetaDataProvider();
        this.instantiator = instantiatorFactory.decorateLenient();
        this.ivyContextManager = ivyContextManager;
        this.metadataSources.setDefaults();
    }

    @Override
    public String getDisplayName() {
        URI url = getUrl();
        if (url == null) {
            return super.getDisplayName();
        }
        return super.getDisplayName() + '(' + url + ')';
    }

    @Override
    public IvyResolver createPublisher() {
        return createRealResolver();
    }

    @Override
    public ConfiguredModuleComponentRepository createResolver() {
        return createRealResolver();
    }

    @Override
    protected RepositoryDescriptor createDescriptor() {
        Set<String> schemes = getSchemes();
        validate(schemes);

        String layoutType;
        boolean m2Compatible;
        if (layout instanceof GradleRepositoryLayout) {
            layoutType = "Gradle";
            m2Compatible = false;
        } else if (layout instanceof MavenRepositoryLayout) {
            layoutType = "Maven";
            m2Compatible = true;
        } else if (layout instanceof IvyRepositoryLayout) {
            layoutType = "Ivy";
            m2Compatible = false;
        } else if (layout instanceof DefaultIvyPatternRepositoryLayout) {
            layoutType = "Pattern";
            m2Compatible = ((DefaultIvyPatternRepositoryLayout) layout).getM2Compatible();
        } else {
            layoutType = "Unknown";
            m2Compatible = false;
        }

        return new IvyRepositoryDescriptor.Builder(getName(), urlArtifactRepository.getUrl())
            .setAuthenticated(getConfiguredCredentials() != null)
            .setAuthenticationSchemes(getAuthenticationSchemes())
            .setMetadataSources(metadataSources.asList())
            .setLayoutType(layoutType)
            .setM2Compatible(m2Compatible)
            .setIvyPatterns(Sets.union(layout.getIvyPatterns(), additionalPatternsLayout.ivyPatterns))
            .setArtifactPatterns(Sets.union(layout.getArtifactPatterns(), additionalPatternsLayout.artifactPatterns))
            .create();
    }

    private IvyResolver createRealResolver() {
        Set<String> schemes = getSchemes();
        validate(schemes);

        IvyResolver resolver = createResolver(schemes);
        @Nullable
        URI uri = urlArtifactRepository.getUrl();
        layout.apply(uri, resolver);
        additionalPatternsLayout.apply(uri, resolver);

        return resolver;
    }

    private IvyResolver createResolver(Set<String> schemes) {
        return createResolver(transportFactory.createTransport(schemes, getName(), getConfiguredAuthentication(), urlArtifactRepository.createRedirectVerifier()));
    }

    private void validate(Set<String> schemes) {
        if (schemes.isEmpty()) {
            throw new InvalidUserDataException("You must specify a base url or at least one artifact pattern for an Ivy repository.");
        }
    }

    private Set<String> getSchemes() {
        if (schemes == null) {
            URI uri = getUrl();
            schemes = new LinkedHashSet<>();
            layout.addSchemes(uri, schemes);
            additionalPatternsLayout.addSchemes(uri, schemes);
        }
        return schemes;
    }

    private IvyResolver createResolver(RepositoryTransport transport) {
        Instantiator injector = createInjectorForMetadataSuppliers(transport, instantiatorFactory, getUrl(), externalResourcesFileStore);
        InstantiatingAction<ComponentMetadataSupplierDetails> supplierFactory = createComponentMetadataSupplierFactory(injector, isolatableFactory);
        InstantiatingAction<ComponentMetadataListerDetails> listerFactory = createComponentMetadataVersionLister(injector, isolatableFactory);
        return new IvyResolver(getName(), transport, locallyAvailableResourceFinder, metaDataProvider.dynamicResolve, artifactFileStore, moduleIdentifierFactory, supplierFactory, listerFactory, createMetadataSources(), IvyMetadataArtifactProvider.INSTANCE, injector, checksumService);
    }

    @Override
    public void metadataSources(Action<? super MetadataSources> configureAction) {
        invalidateDescriptor();
        metadataSources.reset();
        configureAction.execute(metadataSources);
    }

    @Override
    public MetadataSources getMetadataSources() {
        return metadataSources;
    }

    private ImmutableMetadataSources createMetadataSources() {
        ImmutableList.Builder<MetadataSource<?>> sources = ImmutableList.builder();
        DefaultGradleModuleMetadataSource gradleModuleMetadataSource = new DefaultGradleModuleMetadataSource(moduleMetadataParser, metadataFactory, true, checksumService);
        if (metadataSources.gradleMetadata) {
            sources.add(gradleModuleMetadataSource);
        }
        if (metadataSources.ivyDescriptor) {
            DefaultIvyDescriptorMetadataSource ivyDescriptorMetadataSource = new DefaultIvyDescriptorMetadataSource(IvyMetadataArtifactProvider.INSTANCE, createIvyDescriptorParser(), fileResourceRepository, checksumService);
            if (metadataSources.ignoreGradleMetadataRedirection) {
                sources.add(ivyDescriptorMetadataSource);
            } else {
                sources.add(new RedirectingGradleMetadataModuleMetadataSource(ivyDescriptorMetadataSource, gradleModuleMetadataSource));
            }
        }
        if (metadataSources.artifact) {
            sources.add(new DefaultArtifactMetadataSource(metadataFactory));
        }
        return new DefaultImmutableMetadataSources(sources.build());
    }

    private MetaDataParser<MutableIvyModuleResolveMetadata> createIvyDescriptorParser() {
        return new IvyContextualMetaDataParser<MutableIvyModuleResolveMetadata>(ivyContextManager, new IvyXmlModuleDescriptorParser(new IvyModuleDescriptorConverter(moduleIdentifierFactory), moduleIdentifierFactory, fileResourceRepository, metadataFactory));
    }

    @Override
    public URI getUrl() {
        return urlArtifactRepository.getUrl();
    }


    @Override
    protected Collection<URI> getRepositoryUrls() {
        // Ivy can resolve files from multiple hosts, so we need to look at all
        // of the possible URLs used by the Ivy resolver to identify all of the repositories
        ImmutableList.Builder<URI> builder = ImmutableList.builder();
        URI root = getUrl();
        if (root != null) {
            builder.add(root);
        }
        for (String pattern : additionalPatternsLayout.artifactPatterns) {
            URI baseUri = new ResolvedPattern(pattern, fileResolver).baseUri;
            if (baseUri != null) {
                builder.add(baseUri);
            }
        }
        for (String pattern : additionalPatternsLayout.ivyPatterns) {
            URI baseUri = new ResolvedPattern(pattern, fileResolver).baseUri;
            if (baseUri != null) {
                builder.add(baseUri);
            }
        }
        return builder.build();
    }

    @Override
    public void setUrl(URI url) {
        invalidateDescriptor();
        urlArtifactRepository.setUrl(url);
    }

    @Override
    public void setUrl(Object url) {
        invalidateDescriptor();
        urlArtifactRepository.setUrl(url);
    }

    @Override
    public void setAllowInsecureProtocol(boolean allowInsecureProtocol) {
        invalidateDescriptor();
        urlArtifactRepository.setAllowInsecureProtocol(allowInsecureProtocol);
    }

    @Override
    public boolean isAllowInsecureProtocol() {
        return urlArtifactRepository.isAllowInsecureProtocol();
    }

    @Override
    public void artifactPattern(String pattern) {
        invalidateDescriptor();
        additionalPatternsLayout.artifactPatterns.add(pattern);
    }

    @Override
    public void ivyPattern(String pattern) {
        invalidateDescriptor();
        additionalPatternsLayout.ivyPatterns.add(pattern);
    }

    @Override
    public void layout(String layoutName) {
        invalidateDescriptor();
        switch (layoutName) {
            case "ivy":
                layout = instantiator.newInstance(IvyRepositoryLayout.class);
                break;
            case "maven":
                layout = instantiator.newInstance(MavenRepositoryLayout.class);
                break;
            case "pattern":
                layout = instantiator.newInstance(DefaultIvyPatternRepositoryLayout.class);
                break;
            default:
                layout = instantiator.newInstance(GradleRepositoryLayout.class);
                break;
        }
    }

    @Override
    public void layout(String layoutName, Closure config) {
        DeprecationLogger.deprecateMethod(IvyArtifactRepository.class, "layout(String, Closure)")
            .replaceWith("IvyArtifactRepository.patternLayout(Action)")
            .willBeRemovedInGradle7()
            .withUserManual("declaring_repositories", "sub:defining_custom_pattern_layout_for_an_ivy_repository")
            .nagUser();
        internalLayout(layoutName, ConfigureUtil.<RepositoryLayout>configureUsing(config));
    }

    @Override
    public void layout(String layoutName, Action<? extends RepositoryLayout> config) {
        DeprecationLogger.deprecateMethod(IvyArtifactRepository.class, "layout(String, Action)")
            .replaceWith("IvyArtifactRepository.patternLayout(Action)")
            .willBeRemovedInGradle7()
            .withUserManual("declaring_repositories", "sub:defining_custom_pattern_layout_for_an_ivy_repository")
            .nagUser();
        internalLayout(layoutName, config);
    }

    private void internalLayout(String layoutName, Action config) {
        layout(layoutName);
        config.execute(layout);
    }

    @Override
    public void patternLayout(Action<? super IvyPatternRepositoryLayout> config) {
        DefaultIvyPatternRepositoryLayout layout = instantiator.newInstance(DefaultIvyPatternRepositoryLayout.class);
        this.layout = layout;
        config.execute(layout);
    }

    @Override
    public IvyArtifactRepositoryMetaDataProvider getResolve() {
        return metaDataProvider;
    }

    /**
     * Layout for applying additional patterns added via {@link #artifactPatterns} and {@link #ivyPatterns}.
     */
    private static class AdditionalPatternsRepositoryLayout extends AbstractRepositoryLayout {
        private final FileResolver fileResolver;
        private final Set<String> artifactPatterns = new LinkedHashSet<String>();
        private final Set<String> ivyPatterns = new LinkedHashSet<String>();

        public AdditionalPatternsRepositoryLayout(FileResolver fileResolver) {
            this.fileResolver = fileResolver;
        }

        @Override
        public void apply(URI baseUri, PatternBasedResolver resolver) {
            for (String artifactPattern : artifactPatterns) {
                ResolvedPattern resolvedPattern = new ResolvedPattern(artifactPattern, fileResolver);
                resolver.addArtifactLocation(resolvedPattern.baseUri, resolvedPattern.pattern);
            }

            Set<String> usedIvyPatterns = ivyPatterns.isEmpty() ? artifactPatterns : ivyPatterns;
            for (String ivyPattern : usedIvyPatterns) {
                ResolvedPattern resolvedPattern = new ResolvedPattern(ivyPattern, fileResolver);
                resolver.addDescriptorLocation(resolvedPattern.baseUri, resolvedPattern.pattern);
            }
        }

        @Override
        public void addSchemes(URI baseUri, Set<String> schemes) {
            for (String pattern : artifactPatterns) {
                schemes.add(new ResolvedPattern(pattern, fileResolver).scheme);
            }
            for (String pattern : ivyPatterns) {
                schemes.add(new ResolvedPattern(pattern, fileResolver).scheme);
            }
        }

        @Override
        public Set<String> getIvyPatterns() {
            return ImmutableSet.copyOf(ivyPatterns);
        }

        @Override
        public Set<String> getArtifactPatterns() {
            return ImmutableSet.copyOf(artifactPatterns);
        }
    }

    private static class MetaDataProvider implements IvyArtifactRepositoryMetaDataProvider {
        boolean dynamicResolve;

        @Override
        public boolean isDynamicMode() {
            return dynamicResolve;
        }

        @Override
        public void setDynamicMode(boolean mode) {
            this.dynamicResolve = mode;
        }
    }

    private static class IvyMetadataSources implements MetadataSources {
        boolean gradleMetadata;
        boolean ivyDescriptor;
        boolean artifact;
        boolean ignoreGradleMetadataRedirection;

        void setDefaults() {
            ivyDescriptor();
            ignoreGradleMetadataRedirection = false;
        }

        void reset() {
            gradleMetadata = false;
            ivyDescriptor = false;
            artifact = false;
            ignoreGradleMetadataRedirection = false;
        }

        /**
         * This is used for reporting purposes on build scans.
         * Changing this means a change of repository for build scans.
         *
         * @return a list of implemented metadata sources, as strings.
         */
        List<String> asList() {
            List<String> list = new ArrayList<String>();
            if (gradleMetadata) {
                list.add("gradleMetadata");
            }
            if (ivyDescriptor) {
                list.add("ivyDescriptor");
            }
            if (artifact) {
                list.add("artifact");
            }
            if (ignoreGradleMetadataRedirection) {
                list.add("ignoreGradleMetadataRedirection");
            }
            return list;
        }

        @Override
        public void gradleMetadata() {
            gradleMetadata = true;
        }

        @Override
        public void ivyDescriptor() {
            ivyDescriptor = true;
        }

        @Override
        public void artifact() {
            artifact = true;
        }

        @Override
        public void ignoreGradleMetadataRedirection() {
            ignoreGradleMetadataRedirection = true;
        }

        @Override
        public boolean isGradleMetadataEnabled() {
            return gradleMetadata;
        }

        @Override
        public boolean isIvyDescriptorEnabled() {
            return ivyDescriptor;
        }

        @Override
        public boolean isArtifactEnabled() {
            return artifact;
        }

        @Override
        public boolean isIgnoreGradleMetadataRedirectionEnabled() {
            return ignoreGradleMetadataRedirection;
        }
    }

}
