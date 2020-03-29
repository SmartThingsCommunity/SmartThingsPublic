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

package org.gradle.api.internal.artifacts.configurations;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Describable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.DependencyConstraintSet;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.PublishArtifactSet;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.CompositeDomainObjectSet;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.DomainObjectContext;
import org.gradle.api.internal.artifacts.ConfigurationResolver;
import org.gradle.api.internal.artifacts.DefaultDependencyConstraintSet;
import org.gradle.api.internal.artifacts.DefaultDependencySet;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
import org.gradle.api.internal.artifacts.DefaultPublishArtifactSet;
import org.gradle.api.internal.artifacts.DefaultResolverResults;
import org.gradle.api.internal.artifacts.ExcludeRuleNotationConverter;
import org.gradle.api.internal.artifacts.Module;
import org.gradle.api.internal.artifacts.ResolverResults;
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyConstraint;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.artifacts.ivyservice.DefaultLenientConfiguration;
import org.gradle.api.internal.artifacts.ivyservice.ResolvedArtifactCollectingVisitor;
import org.gradle.api.internal.artifacts.ivyservice.ResolvedFileCollectionVisitor;
import org.gradle.api.internal.artifacts.ivyservice.ResolvedFilesCollectingVisitor;
import org.gradle.api.internal.artifacts.ivyservice.moduleconverter.RootComponentMetadataBuilder;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ArtifactVisitor;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.SelectedArtifactSet;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.projectresult.ResolvedProjectConfiguration;
import org.gradle.api.internal.artifacts.transform.DefaultExtraExecutionGraphDependenciesResolverFactory;
import org.gradle.api.internal.artifacts.transform.ExtraExecutionGraphDependenciesResolverFactory;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.attributes.ImmutableAttributeContainerWithErrorMessage;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.collections.DomainObjectCollectionFactory;
import org.gradle.api.internal.file.AbstractFileCollection;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.ProjectStateRegistry;
import org.gradle.api.internal.tasks.FailureCollectingTaskDependencyResolveContext;
import org.gradle.api.internal.tasks.NodeExecutionContext;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.internal.tasks.WorkNodeAction;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.configuration.internal.UserCodeApplicationContext;
import org.gradle.initialization.ProjectAccessListener;
import org.gradle.internal.Actions;
import org.gradle.internal.Cast;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.Factories;
import org.gradle.internal.Factory;
import org.gradle.internal.ImmutableActionSet;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.concurrent.GradleThread;
import org.gradle.internal.deprecation.DeprecatableConfiguration;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.internal.event.ListenerBroadcast;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.RunnableBuildOperation;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.typeconversion.NotationParser;
import org.gradle.util.CollectionUtils;
import org.gradle.util.ConfigureUtil;
import org.gradle.util.Path;
import org.gradle.util.WrapUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.gradle.api.internal.artifacts.configurations.ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED;
import static org.gradle.api.internal.artifacts.configurations.ConfigurationInternal.InternalState.BUILD_DEPENDENCIES_RESOLVED;
import static org.gradle.api.internal.artifacts.configurations.ConfigurationInternal.InternalState.GRAPH_RESOLVED;
import static org.gradle.api.internal.artifacts.configurations.ConfigurationInternal.InternalState.UNRESOLVED;
import static org.gradle.util.ConfigureUtil.configure;

public class DefaultConfiguration extends AbstractFileCollection implements ConfigurationInternal, MutationValidator {

    private static final Action<Throwable> DEFAULT_ERROR_HANDLER = throwable -> {
        throw UncheckedException.throwAsUncheckedException(throwable);
    };

    private final ConfigurationResolver resolver;
    private final ListenerManager listenerManager;
    private final DependencyMetaDataProvider metaDataProvider;
    private final DefaultDependencySet dependencies;
    private final DefaultDependencyConstraintSet dependencyConstraints;
    private final DefaultDomainObjectSet<Dependency> ownDependencies;
    private final DefaultDomainObjectSet<DependencyConstraint> ownDependencyConstraints;
    private final DomainObjectContext owner;
    private final ProjectStateRegistry projectStateRegistry;
    private CompositeDomainObjectSet<Dependency> inheritedDependencies;
    private CompositeDomainObjectSet<DependencyConstraint> inheritedDependencyConstraints;
    private DefaultDependencySet allDependencies;
    private DefaultDependencyConstraintSet allDependencyConstraints;
    private ImmutableActionSet<DependencySet> defaultDependencyActions = ImmutableActionSet.empty();
    private ImmutableActionSet<DependencySet> withDependencyActions = ImmutableActionSet.empty();
    private final DefaultPublishArtifactSet artifacts;
    private final DefaultDomainObjectSet<PublishArtifact> ownArtifacts;
    private CompositeDomainObjectSet<PublishArtifact> inheritedArtifacts;
    private DefaultPublishArtifactSet allArtifacts;
    private final ConfigurationResolvableDependencies resolvableDependencies;
    private ListenerBroadcast<DependencyResolutionListener> dependencyResolutionListeners;
    private final BuildOperationExecutor buildOperationExecutor;
    private final Instantiator instantiator;
    private final NotationParser<Object, ConfigurablePublishArtifact> artifactNotationParser;
    private final NotationParser<Object, Capability> capabilityNotationParser;
    private final ProjectAccessListener projectAccessListener;
    private final ProjectFinder projectFinder;
    private Factory<ResolutionStrategyInternal> resolutionStrategyFactory;
    private ResolutionStrategyInternal resolutionStrategy;
    private final FileCollectionFactory fileCollectionFactory;
    private final DocumentationRegistry documentationRegistry;

    private final Set<MutationValidator> childMutationValidators = Sets.newHashSet();
    private final MutationValidator parentMutationValidator = DefaultConfiguration.this::validateParentMutation;
    private final RootComponentMetadataBuilder rootComponentMetadataBuilder;
    private final ConfigurationsProvider configurationsProvider;

    private final Path identityPath;
    private final Path path;

    // These fields are not covered by mutation lock
    private final String name;
    private final DefaultConfigurationPublications outgoing;

    private boolean visible = true;
    private boolean transitive = true;
    private Set<Configuration> extendsFrom = new LinkedHashSet<Configuration>();
    private String description;
    private final Set<Object> excludeRules = new LinkedHashSet<Object>();
    private Set<ExcludeRule> parsedExcludeRules;

    private final ProjectStateRegistry.SafeExclusiveLock resolutionLock;
    private final Object observationLock = new Object();
    private volatile InternalState observedState = UNRESOLVED;
    private volatile InternalState resolvedState = UNRESOLVED;
    private boolean insideBeforeResolve;

    private ResolverResults cachedResolverResults;
    private boolean dependenciesModified;
    private boolean canBeConsumed = true;
    private boolean canBeResolved = true;

    private boolean canBeMutated = true;
    private AttributeContainerInternal configurationAttributes;
    private final DomainObjectContext domainObjectContext;
    private final ImmutableAttributesFactory attributesFactory;
    private final ConfigurationFileCollection intrinsicFiles;

    private final DisplayName displayName;
    private final UserCodeApplicationContext userCodeApplicationContext;
    private final DomainObjectCollectionFactory domainObjectCollectionFactory;

    private final AtomicInteger copyCount = new AtomicInteger(0);

    private Action<? super ConfigurationInternal> beforeLocking;

    private List<String> declarationAlternatives;
    private List<String> consumptionAlternatives;
    private List<String> resolutionAlternatives;

    public DefaultConfiguration(DomainObjectContext domainObjectContext,
                                String name,
                                ConfigurationsProvider configurationsProvider,
                                ConfigurationResolver resolver,
                                ListenerManager listenerManager,
                                DependencyMetaDataProvider metaDataProvider,
                                Factory<ResolutionStrategyInternal> resolutionStrategyFactory,
                                ProjectAccessListener projectAccessListener,
                                ProjectFinder projectFinder,
                                FileCollectionFactory fileCollectionFactory,
                                BuildOperationExecutor buildOperationExecutor,
                                Instantiator instantiator,
                                NotationParser<Object, ConfigurablePublishArtifact> artifactNotationParser,
                                NotationParser<Object, Capability> capabilityNotationParser,
                                ImmutableAttributesFactory attributesFactory,
                                RootComponentMetadataBuilder rootComponentMetadataBuilder,
                                DocumentationRegistry documentationRegistry,
                                UserCodeApplicationContext userCodeApplicationContext,
                                DomainObjectContext owner,
                                ProjectStateRegistry projectStateRegistry,
                                DomainObjectCollectionFactory domainObjectCollectionFactory
    ) {
        this.userCodeApplicationContext = userCodeApplicationContext;
        this.projectStateRegistry = projectStateRegistry;
        this.domainObjectCollectionFactory = domainObjectCollectionFactory;
        this.identityPath = domainObjectContext.identityPath(name);
        this.name = name;
        this.configurationsProvider = configurationsProvider;
        this.resolver = resolver;
        this.listenerManager = listenerManager;
        this.metaDataProvider = metaDataProvider;
        this.resolutionStrategyFactory = resolutionStrategyFactory;
        this.projectAccessListener = projectAccessListener;
        this.projectFinder = projectFinder;
        this.fileCollectionFactory = fileCollectionFactory;
        this.dependencyResolutionListeners = listenerManager.createAnonymousBroadcaster(DependencyResolutionListener.class);
        this.buildOperationExecutor = buildOperationExecutor;
        this.instantiator = instantiator;
        this.artifactNotationParser = artifactNotationParser;
        this.capabilityNotationParser = capabilityNotationParser;
        this.attributesFactory = attributesFactory;
        this.configurationAttributes = attributesFactory.mutable();
        this.domainObjectContext = domainObjectContext;
        this.intrinsicFiles = new ConfigurationFileCollection(Specs.satisfyAll());
        this.documentationRegistry = documentationRegistry;
        this.resolutionLock = projectStateRegistry.newExclusiveOperationLock();
        this.resolvableDependencies = instantiator.newInstance(ConfigurationResolvableDependencies.class, this);

        displayName = Describables.memoize(new ConfigurationDescription(identityPath));

        this.ownDependencies = (DefaultDomainObjectSet<Dependency>) domainObjectCollectionFactory.newDomainObjectSet(Dependency.class);
        this.ownDependencies.beforeCollectionChanges(validateMutationType(this, MutationType.DEPENDENCIES));
        this.ownDependencyConstraints = (DefaultDomainObjectSet<DependencyConstraint>) domainObjectCollectionFactory.newDomainObjectSet(DependencyConstraint.class);
        this.ownDependencyConstraints.beforeCollectionChanges(validateMutationType(this, MutationType.DEPENDENCIES));

        this.dependencies = new DefaultDependencySet(Describables.of(displayName, "dependencies"), this, ownDependencies);
        this.dependencyConstraints = new DefaultDependencyConstraintSet(Describables.of(displayName, "dependency constraints"), this, ownDependencyConstraints);

        this.ownArtifacts = (DefaultDomainObjectSet<PublishArtifact>) domainObjectCollectionFactory.newDomainObjectSet(PublishArtifact.class);
        this.ownArtifacts.beforeCollectionChanges(validateMutationType(this, MutationType.ARTIFACTS));

        this.artifacts = new DefaultPublishArtifactSet(Describables.of(displayName, "artifacts"), ownArtifacts, fileCollectionFactory);

        this.outgoing = instantiator.newInstance(DefaultConfigurationPublications.class, displayName, artifacts, new AllArtifactsProvider(), configurationAttributes, instantiator, artifactNotationParser, capabilityNotationParser, fileCollectionFactory, attributesFactory, domainObjectCollectionFactory);
        this.rootComponentMetadataBuilder = rootComponentMetadataBuilder;
        this.owner = owner;
        path = domainObjectContext.projectPath(name);
    }

    private static Action<Void> validateMutationType(final MutationValidator mutationValidator, final MutationType type) {
        return arg -> mutationValidator.validateMutation(type);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State getState() {
        if (resolvedState == ARTIFACTS_RESOLVED || resolvedState == GRAPH_RESOLVED) {
            if (cachedResolverResults.hasError()) {
                return State.RESOLVED_WITH_FAILURES;
            } else {
                return State.RESOLVED;
            }
        } else {
            return State.UNRESOLVED;
        }
    }

    public InternalState getResolvedState() {
        return resolvedState;
    }

    @Override
    public Module getModule() {
        return metaDataProvider.getModule();
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public Configuration setVisible(boolean visible) {
        validateMutation(MutationType.DEPENDENCIES);
        this.visible = visible;
        return this;
    }

    @Override
    public Set<Configuration> getExtendsFrom() {
        return Collections.unmodifiableSet(extendsFrom);
    }

    @Override
    public Configuration setExtendsFrom(Iterable<Configuration> extendsFrom) {
        validateMutation(MutationType.DEPENDENCIES);
        for (Configuration configuration : this.extendsFrom) {
            if (inheritedArtifacts != null) {
                inheritedArtifacts.removeCollection(configuration.getAllArtifacts());
            }
            if (inheritedDependencies != null) {
                inheritedDependencies.removeCollection(configuration.getAllDependencies());
            }
            if (inheritedDependencyConstraints != null) {
                inheritedDependencyConstraints.removeCollection(configuration.getAllDependencyConstraints());
            }
            ((ConfigurationInternal) configuration).removeMutationValidator(parentMutationValidator);
        }
        this.extendsFrom = new LinkedHashSet<Configuration>();
        for (Configuration configuration : extendsFrom) {
            extendsFrom(configuration);
        }
        return this;
    }

    @Override
    public Configuration extendsFrom(Configuration... extendsFrom) {
        validateMutation(MutationType.DEPENDENCIES);
        for (Configuration configuration : extendsFrom) {
            if (configuration.getHierarchy().contains(this)) {
                throw new InvalidUserDataException(String.format(
                    "Cyclic extendsFrom from %s and %s is not allowed. See existing hierarchy: %s", this,
                    configuration, configuration.getHierarchy()));
            }
            if (this.extendsFrom.add(configuration)) {
                if (inheritedArtifacts != null) {
                    inheritedArtifacts.addCollection(configuration.getAllArtifacts());
                }
                if (inheritedDependencies != null) {
                    inheritedDependencies.addCollection(configuration.getAllDependencies());
                }
                if (inheritedDependencyConstraints != null) {
                    inheritedDependencyConstraints.addCollection(configuration.getAllDependencyConstraints());
                }
                ((ConfigurationInternal) configuration).addMutationValidator(parentMutationValidator);
            }
        }
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public Configuration setTransitive(boolean transitive) {
        validateMutation(MutationType.DEPENDENCIES);
        this.transitive = transitive;
        return this;
    }

    @Override
    @Nullable
    public String getDescription() {
        return description;
    }

    @Override
    public Configuration setDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    @Override
    public Set<Configuration> getHierarchy() {
        if (extendsFrom.isEmpty()) {
            return Collections.singleton(this);
        }
        Set<Configuration> result = WrapUtil.toLinkedSet(this);
        collectSuperConfigs(this, result);
        return result;
    }

    private void collectSuperConfigs(Configuration configuration, Set<Configuration> result) {
        for (Configuration superConfig : configuration.getExtendsFrom()) {
            if (result.contains(superConfig)) {
                result.remove(superConfig);
            }
            result.add(superConfig);
            collectSuperConfigs(superConfig, result);
        }
    }

    @Override
    public Configuration defaultDependencies(final Action<? super DependencySet> action) {
        validateMutation(MutationType.DEPENDENCIES);
        defaultDependencyActions = defaultDependencyActions.add(dependencies -> {
            if (dependencies.isEmpty()) {
                action.execute(dependencies);
            }
        });
        return this;
    }

    @Override
    public Configuration withDependencies(final Action<? super DependencySet> action) {
        validateMutation(MutationType.DEPENDENCIES);
        withDependencyActions = withDependencyActions.add(action);
        return this;
    }

    @Override
    public void runDependencyActions() {
        defaultDependencyActions.execute(dependencies);
        withDependencyActions.execute(dependencies);

        // Discard actions after execution
        defaultDependencyActions = ImmutableActionSet.empty();
        withDependencyActions = ImmutableActionSet.empty();

        for (Configuration superConfig : extendsFrom) {
            ((ConfigurationInternal) superConfig).runDependencyActions();
        }
    }

    @Override
    public Set<Configuration> getAll() {
        return ImmutableSet.copyOf(configurationsProvider.getAll());
    }

    @Override
    public Set<File> resolve() {
        return getFiles();
    }

    @Override
    public Iterator<File> iterator() {
        return intrinsicFiles.iterator();
    }

    @Override
    public Set<File> getFiles() {
        return intrinsicFiles.getFiles();
    }

    @Override
    public void visitStructure(FileCollectionStructureVisitor visitor) {
        intrinsicFiles.visitStructure(visitor);
    }

    @Override
    public boolean contains(File file) {
        return intrinsicFiles.contains(file);
    }

    @Override
    public boolean isEmpty() {
        return intrinsicFiles.isEmpty();
    }

    @Override
    public Set<File> files(Dependency... dependencies) {
        return fileCollection(dependencies).getFiles();
    }

    @Override
    public Set<File> files(Closure dependencySpecClosure) {
        return fileCollection(dependencySpecClosure).getFiles();
    }

    @Override
    public Set<File> files(Spec<? super Dependency> dependencySpec) {
        return fileCollection(dependencySpec).getFiles();
    }

    @Override
    public FileCollection fileCollection(Spec<? super Dependency> dependencySpec) {
        return new ConfigurationFileCollection(dependencySpec);
    }

    @Override
    public FileCollection fileCollection(Closure dependencySpecClosure) {
        return new ConfigurationFileCollection(dependencySpecClosure);
    }

    @Override
    public FileCollection fileCollection(Dependency... dependencies) {
        return new ConfigurationFileCollection(WrapUtil.toLinkedSet(dependencies));
    }

    @Override
    public void markAsObserved(InternalState requestedState) {
        markThisObserved(requestedState);
        markParentsObserved(requestedState);
    }

    private void markThisObserved(InternalState requestedState) {
        synchronized (observationLock) {
            if (observedState.compareTo(requestedState) < 0) {
                observedState = requestedState;
            }
        }
    }

    private void markParentsObserved(InternalState requestedState) {
        for (Configuration configuration : extendsFrom) {
            ((ConfigurationInternal) configuration).markAsObserved(requestedState);
        }
    }

    @Override
    public ResolvedConfiguration getResolvedConfiguration() {
        resolveToStateOrLater(ARTIFACTS_RESOLVED);
        return cachedResolverResults.getResolvedConfiguration();
    }

    private void resolveToStateOrLater(final InternalState requestedState) {
        assertIsResolvable();
        warnIfConfigurationIsDeprecatedForResolving();

        if (!owner.getModel().hasMutableState()) {
            if (!GradleThread.isManaged()) {
                // Error if we are executing in a user-managed thread.
                throw new IllegalStateException("The configuration " + identityPath.toString() + " was resolved from a thread not managed by Gradle.");
            } else {
                // We don't have mutable access to the project, so we throw a deprecation warning and then continue with
                // lenient locking.
                DeprecationLogger.deprecateBehaviour("The configuration " + identityPath.toString() + " was resolved without accessing the project in a safe manner.  This may happen when a configuration is resolved from a different project.")
                    .willBeRemovedInGradle7()
                    .withUserManual("viewing_debugging_dependencies", "sub:resolving-unsafe-configuration-resolution-errors")
                    .nagUser();
                owner.getModel().withLenientState(() -> resolveExclusively(requestedState));
            }
        } else {
            resolveExclusively(requestedState);
        }
    }

    private void warnIfConfigurationIsDeprecatedForResolving() {
        if (resolutionAlternatives != null) {
            DeprecationLogger.deprecateConfiguration(this.name).forResolution().replaceWith(resolutionAlternatives)
                .willBecomeAnErrorInGradle7()
                .withUpgradeGuideSection(5, "dependencies_should_no_longer_be_declared_using_the_compile_and_runtime_configurations")
                .nagUser();
        }
    }

    private void resolveExclusively(InternalState requestedState) {
        resolutionLock.withLock(() -> {
            if (requestedState == GRAPH_RESOLVED || requestedState == ARTIFACTS_RESOLVED) {
                resolveGraphIfRequired(requestedState);
            }
            if (requestedState == ARTIFACTS_RESOLVED) {
                resolveArtifactsIfRequired();
            }
        });
    }

    /**
     * Must be called from {@link #resolveExclusively(InternalState)} only.
     */
    private void resolveGraphIfRequired(final InternalState requestedState) {
        if (resolvedState == ARTIFACTS_RESOLVED || resolvedState == GRAPH_RESOLVED) {
            if (dependenciesModified) {
                throw new InvalidUserDataException(String.format("Attempted to resolve %s that has been resolved previously.", getDisplayName()));
            }
            return;
        }

        buildOperationExecutor.run(new RunnableBuildOperation() {
            @Override
            public void run(BuildOperationContext context) {
                runDependencyActions();
                preventFromFurtherMutation();

                ResolvableDependenciesInternal incoming = (ResolvableDependenciesInternal) getIncoming();
                performPreResolveActions(incoming);
                cachedResolverResults = new DefaultResolverResults();
                resolver.resolveGraph(DefaultConfiguration.this, cachedResolverResults);
                dependenciesModified = false;
                resolvedState = GRAPH_RESOLVED;

                // Mark all affected configurations as observed
                markParentsObserved(requestedState);
                markReferencedProjectConfigurationsObserved(requestedState);

                if (!cachedResolverResults.hasError()) {
                    dependencyResolutionListeners.getSource().afterResolve(incoming);
                    // Discard listeners
                    dependencyResolutionListeners.removeAll();
                }
                captureBuildOperationResult(context);
            }

            private void captureBuildOperationResult(BuildOperationContext context) {
                Throwable failure = cachedResolverResults.getFailure();
                if (failure != null) {
                    context.failed(failure);
                }
                // When dependency resolution has failed, we don't want the build operation listeners to fail as well
                // because:
                // 1. the `failed` method will have been called with the user facing error
                // 2. such an error may still lead to a valid dependency graph
                ResolutionResult resolutionResult = cachedResolverResults.getResolutionResult();
                context.setResult(ResolveConfigurationResolutionBuildOperationResult.create(resolutionResult, attributesFactory));
            }

            @Override
            public BuildOperationDescriptor.Builder description() {
                String displayName = "Resolve dependencies of " + identityPath;
                Path projectPath = domainObjectContext.getProjectPath();
                String projectPathString = domainObjectContext.isScript() ? null : (projectPath == null ? null : projectPath.getPath());
                return BuildOperationDescriptor.displayName(displayName)
                    .progressDisplayName(displayName)
                    .details(new ResolveConfigurationResolutionBuildOperationDetails(
                        getName(),
                        domainObjectContext.isScript(),
                        getDescription(),
                        domainObjectContext.getBuildPath().getPath(),
                        projectPathString,
                        isVisible(),
                        isTransitive(),
                        resolver.getRepositories()
                    ));
            }
        });
    }

    private void performPreResolveActions(ResolvableDependencies incoming) {
        DependencyResolutionListener dependencyResolutionListener = dependencyResolutionListeners.getSource();
        insideBeforeResolve = true;
        try {
            dependencyResolutionListener.beforeResolve(incoming);
        } finally {
            insideBeforeResolve = false;
        }
    }

    private void markReferencedProjectConfigurationsObserved(final InternalState requestedState) {
        for (ResolvedProjectConfiguration projectResult : cachedResolverResults.getResolvedLocalComponents().getResolvedProjectConfigurations()) {
            ProjectInternal project = projectFinder.getProject(projectResult.getId().getProjectPath());
            ConfigurationInternal targetConfig = (ConfigurationInternal) project.getConfigurations().getByName(projectResult.getTargetConfiguration());
            targetConfig.markAsObserved(requestedState);
        }
    }

    /**
     * Must be called from {@link #resolveExclusively(InternalState)} only.
     */
    private void resolveArtifactsIfRequired() {
        if (resolvedState == ARTIFACTS_RESOLVED) {
            return;
        }
        if (resolvedState != GRAPH_RESOLVED) {
            throw new IllegalStateException("Cannot resolve artifacts before graph has been resolved.");
        }
        resolver.resolveArtifacts(DefaultConfiguration.this, cachedResolverResults);
        resolvedState = ARTIFACTS_RESOLVED;
    }

    @Override
    public ExtraExecutionGraphDependenciesResolverFactory getDependenciesResolver() {
        return new DefaultExtraExecutionGraphDependenciesResolverFactory(this::getResultsForBuildDependencies, this::getResultsForArtifacts, new ResolveGraphAction(this), fileCollectionFactory);
    }

    private ResolverResults getResultsForBuildDependencies() {
        if (resolvedState == UNRESOLVED) {
            throw new IllegalStateException("Cannot query results until resolution has happened.");
        }
        return cachedResolverResults;
    }

    private ResolverResults getResultsForArtifacts() {
        resolveExclusively(ARTIFACTS_RESOLVED);
        return cachedResolverResults;
    }

    private ResolverResults resolveGraphForBuildDependenciesIfRequired() {
        if (getResolutionStrategy().resolveGraphToDetermineTaskDependencies()) {
            // Force graph resolution as this is required to calculate build dependencies
            resolveToStateOrLater(GRAPH_RESOLVED);
        }
        if (resolvedState == UNRESOLVED) {
            // Traverse graph
            ResolverResults results = new DefaultResolverResults();
            resolver.resolveBuildDependencies(DefaultConfiguration.this, results);
            resolvedState = BUILD_DEPENDENCIES_RESOLVED;
            cachedResolverResults = results;
        }
        // Otherwise, already have a result, so reuse it
        return cachedResolverResults;
    }

    @Override
    public void visitDependencies(TaskDependencyResolveContext context) {
        assertIsResolvable();
        context.add(intrinsicFiles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskDependency getTaskDependencyFromProjectDependency(final boolean useDependedOn, final String taskName) {
        if (useDependedOn) {
            return new TasksFromProjectDependencies(taskName, getAllDependencies(), projectAccessListener);
        } else {
            return new TasksFromDependentProjects(taskName, getName());
        }
    }

    @Override
    public DependencySet getDependencies() {
        return dependencies;
    }

    @Override
    public DependencySet getAllDependencies() {
        if (allDependencies == null) {
            initAllDependencies();
        }
        return allDependencies;
    }

    private synchronized void initAllDependencies() {
        if (allDependencies != null) {
            return;
        }
        inheritedDependencies = domainObjectCollectionFactory.newDomainObjectSet(Dependency.class, ownDependencies);
        for (Configuration configuration : this.extendsFrom) {
            inheritedDependencies.addCollection(configuration.getAllDependencies());
        }
        allDependencies = new DefaultDependencySet(Describables.of(displayName, "all dependencies"), this, inheritedDependencies);
    }

    @Override
    public DependencyConstraintSet getDependencyConstraints() {
        return dependencyConstraints;
    }

    @Override
    public DependencyConstraintSet getAllDependencyConstraints() {
        if (allDependencyConstraints == null) {
            initAllDependencyConstraints();
        }
        return allDependencyConstraints;
    }

    private synchronized void initAllDependencyConstraints() {
        if (allDependencyConstraints != null) {
            return;
        }
        inheritedDependencyConstraints = domainObjectCollectionFactory.newDomainObjectSet(DependencyConstraint.class, ownDependencyConstraints);
        for (Configuration configuration : this.extendsFrom) {
            inheritedDependencyConstraints.addCollection(configuration.getAllDependencyConstraints());
        }
        allDependencyConstraints = new DefaultDependencyConstraintSet(Describables.of(displayName, "all dependency constraints"), this, inheritedDependencyConstraints);
    }

    @Override
    public PublishArtifactSet getArtifacts() {
        return artifacts;
    }

    @Override
    public PublishArtifactSet getAllArtifacts() {
        initAllArtifacts();
        return allArtifacts;
    }

    private synchronized void initAllArtifacts() {
        if (allArtifacts != null) {
            return;
        }
        DisplayName displayName = Describables.of(this.displayName, "all artifacts");

        if (!canBeMutated && extendsFrom.isEmpty()) {
            // No further mutation is allowed and there's no parent: the artifact set corresponds to this configuration own artifacts
            this.allArtifacts = new DefaultPublishArtifactSet(displayName, ownArtifacts, fileCollectionFactory);
            return;
        }

        if (canBeMutated) {
            // If the configuration can still be mutated, we need to create a composite
            inheritedArtifacts = domainObjectCollectionFactory.newDomainObjectSet(PublishArtifact.class, ownArtifacts);
        }
        for (Configuration configuration : this.extendsFrom) {
            PublishArtifactSet allArtifacts = configuration.getAllArtifacts();
            if (inheritedArtifacts != null || !allArtifacts.isEmpty()) {
                if (inheritedArtifacts == null) {
                    // This configuration cannot be mutated, but some parent configurations provide artifacts
                    inheritedArtifacts = domainObjectCollectionFactory.newDomainObjectSet(PublishArtifact.class, ownArtifacts);
                }
                inheritedArtifacts.addCollection(allArtifacts);
            }
        }
        if (inheritedArtifacts != null) {
            this.allArtifacts = new DefaultPublishArtifactSet(displayName, inheritedArtifacts, fileCollectionFactory);
        } else {
            this.allArtifacts = new DefaultPublishArtifactSet(displayName, ownArtifacts, fileCollectionFactory);
        }
    }

    @Override
    public Set<ExcludeRule> getExcludeRules() {
        initExcludeRules();
        return Collections.unmodifiableSet(parsedExcludeRules);
    }

    @Override
    public Set<ExcludeRule> getAllExcludeRules() {
        Set<ExcludeRule> result = Sets.newLinkedHashSet();
        result.addAll(getExcludeRules());
        for (Configuration config : extendsFrom) {
            result.addAll(((ConfigurationInternal) config).getAllExcludeRules());
        }
        return result;
    }

    /**
     * Synchronize read access to excludes. Mutation does not need to be thread-safe.
     */
    private synchronized void initExcludeRules() {
        if (parsedExcludeRules == null) {
            NotationParser<Object, ExcludeRule> parser = ExcludeRuleNotationConverter.parser();
            parsedExcludeRules = Sets.newLinkedHashSet();
            for (Object excludeRule : excludeRules) {
                parsedExcludeRules.add(parser.parseNotation(excludeRule));
            }
        }
    }

    public void setExcludeRules(Set<ExcludeRule> excludeRules) {
        validateMutation(MutationType.DEPENDENCIES);
        parsedExcludeRules = null;
        this.excludeRules.clear();
        this.excludeRules.addAll(excludeRules);
    }

    @Override
    public DefaultConfiguration exclude(Map<String, String> excludeRuleArgs) {
        validateMutation(MutationType.DEPENDENCIES);
        parsedExcludeRules = null;
        excludeRules.add(excludeRuleArgs);
        return this;
    }

    @Override
    public String getUploadTaskName() {
        return Configurations.uploadTaskName(getName());
    }

    @Override
    public String getDisplayName() {
        return displayName.getDisplayName();
    }

    @Override
    public ResolvableDependencies getIncoming() {
        return resolvableDependencies;
    }

    @Override
    public ConfigurationPublications getOutgoing() {
        return outgoing;
    }

    @Override
    public OutgoingVariant convertToOutgoingVariant() {
        return outgoing.convertToOutgoingVariant();
    }

    @Override
    public void beforeLocking(Action<? super ConfigurationInternal> action) {
        if (canBeMutated) {
            if (beforeLocking != null) {
                beforeLocking = Actions.composite(beforeLocking, action);
            } else {
                beforeLocking = action;
            }
        }
    }

    @Override
    public void preventFromFurtherMutation() {
        // TODO This should use the same `MutationValidator` infrastructure that we use for other mutation types
        if (canBeMutated) {
            if (beforeLocking != null) {
                beforeLocking.execute(this);
                beforeLocking = null;
            }
            AttributeContainerInternal delegatee = configurationAttributes.asImmutable();
            configurationAttributes = new ImmutableAttributeContainerWithErrorMessage(delegatee, this.displayName);
            outgoing.preventFromFurtherMutation();
            canBeMutated = false;
        }
    }

    @Override
    public void outgoing(Action<? super ConfigurationPublications> action) {
        action.execute(outgoing);
    }

    @Override
    public ConfigurationInternal copy() {
        return createCopy(getDependencies(), getDependencyConstraints(), false);
    }

    @Override
    public Configuration copyRecursive() {
        return createCopy(getAllDependencies(), getAllDependencyConstraints(), true);
    }

    @Override
    public Configuration copy(Spec<? super Dependency> dependencySpec) {
        return createCopy(CollectionUtils.filter(getDependencies(), dependencySpec), getDependencyConstraints(), false);
    }

    @Override
    public Configuration copyRecursive(Spec<? super Dependency> dependencySpec) {
        return createCopy(CollectionUtils.filter(getAllDependencies(), dependencySpec), getAllDependencyConstraints(), true);
    }

    private DefaultConfiguration createCopy(Set<Dependency> dependencies, Set<DependencyConstraint> dependencyConstraints, boolean recursive) {
        DetachedConfigurationsProvider configurationsProvider = new DetachedConfigurationsProvider();
        RootComponentMetadataBuilder rootComponentMetadataBuilder = this.rootComponentMetadataBuilder.withConfigurationsProvider(configurationsProvider);

        String newName = getNameWithCopySuffix();

        Factory<ResolutionStrategyInternal> childResolutionStrategy = resolutionStrategy != null ? Factories.constant(resolutionStrategy.copy()) : resolutionStrategyFactory;
        DefaultConfiguration copiedConfiguration = instantiator.newInstance(DefaultConfiguration.class, domainObjectContext, newName,
            configurationsProvider, resolver, listenerManager, metaDataProvider, childResolutionStrategy, projectAccessListener, projectFinder, fileCollectionFactory, buildOperationExecutor, instantiator, artifactNotationParser, capabilityNotationParser, attributesFactory,
            rootComponentMetadataBuilder, documentationRegistry, userCodeApplicationContext, owner, projectStateRegistry, domainObjectCollectionFactory);
        configurationsProvider.setTheOnlyConfiguration(copiedConfiguration);
        // state, cachedResolvedConfiguration, and extendsFrom intentionally not copied - must re-resolve copy
        // copying extendsFrom could mess up dependencies when copy was re-resolved

        copiedConfiguration.visible = visible;
        copiedConfiguration.transitive = transitive;
        copiedConfiguration.description = description;

        copiedConfiguration.defaultDependencyActions = defaultDependencyActions;
        copiedConfiguration.withDependencyActions = withDependencyActions;
        copiedConfiguration.dependencyResolutionListeners = dependencyResolutionListeners.copy();

        copiedConfiguration.canBeConsumed = canBeConsumed;
        copiedConfiguration.canBeResolved = canBeResolved;

        copiedConfiguration.getArtifacts().addAll(getAllArtifacts());

        if (!configurationAttributes.isEmpty()) {
            for (Attribute<?> attribute : configurationAttributes.keySet()) {
                Object value = configurationAttributes.getAttribute(attribute);
                copiedConfiguration.getAttributes().attribute(Cast.uncheckedNonnullCast(attribute), value);
            }
        }

        // todo An ExcludeRule is a value object but we don't enforce immutability for DefaultExcludeRule as strong as we
        // should (we expose the Map). We should provide a better API for ExcludeRule (I don't want to use unmodifiable Map).
        // As soon as DefaultExcludeRule is truly immutable, we don't need to create a new instance of DefaultExcludeRule.
        for (ExcludeRule excludeRule : getAllExcludeRules()) {
            copiedConfiguration.excludeRules.add(new DefaultExcludeRule(excludeRule.getGroup(), excludeRule.getModule()));
        }

        DomainObjectSet<Dependency> copiedDependencies = copiedConfiguration.getDependencies();
        for (Dependency dependency : dependencies) {
            copiedDependencies.add(dependency.copy());
        }
        DomainObjectSet<DependencyConstraint> copiedDependencyConstraints = copiedConfiguration.getDependencyConstraints();
        for (DependencyConstraint dependencyConstraint : dependencyConstraints) {
            copiedDependencyConstraints.add(((DefaultDependencyConstraint) dependencyConstraint).copy());
        }
        return copiedConfiguration;
    }

    private String getNameWithCopySuffix() {
        int count = copyCount.incrementAndGet();
        String copyName = name + "Copy";
        return count == 1
            ? copyName
            : copyName + count;
    }

    @Override
    public Configuration copy(Closure dependencySpec) {
        return copy(Specs.convertClosureToSpec(dependencySpec));
    }

    @Override
    public Configuration copyRecursive(Closure dependencySpec) {
        return copyRecursive(Specs.convertClosureToSpec(dependencySpec));
    }

    @Override
    public ResolutionStrategyInternal getResolutionStrategy() {
        if (resolutionStrategy == null) {
            resolutionStrategy = resolutionStrategyFactory.create();
            resolutionStrategy.setMutationValidator(this);
            resolutionStrategyFactory = null;
        }
        return resolutionStrategy;
    }

    @Override
    public ComponentResolveMetadata toRootComponentMetaData() {
        return rootComponentMetadataBuilder.toRootComponentMetaData();
    }

    @Override
    public String getPath() {
        return path.getPath();
    }

    @Override
    public Path getIdentityPath() {
        return identityPath;
    }

    @Override
    public Configuration resolutionStrategy(Closure closure) {
        configure(closure, getResolutionStrategy());
        return this;
    }

    @Override
    public Configuration resolutionStrategy(Action<? super ResolutionStrategy> action) {
        action.execute(getResolutionStrategy());
        return this;
    }

    @Override
    public void addMutationValidator(MutationValidator validator) {
        childMutationValidators.add(validator);
    }

    @Override
    public void removeMutationValidator(MutationValidator validator) {
        childMutationValidators.remove(validator);
    }

    private void validateParentMutation(MutationType type) {
        // Strategy changes in a parent configuration do not affect this configuration, or any of its children, in any way
        if (type == MutationType.STRATEGY) {
            return;
        }

        preventIllegalParentMutation(type);
        markAsModified(type);
        notifyChildren(type);
    }

    @Override
    public void validateMutation(MutationType type) {
        preventIllegalMutation(type);
        markAsModified(type);
        notifyChildren(type);
    }

    private void preventIllegalParentMutation(MutationType type) {
        // TODO Deprecate and eventually prevent these mutations in parent when already resolved
        if (type == MutationType.DEPENDENCY_ATTRIBUTES) {
            return;
        }

        if (resolvedState == ARTIFACTS_RESOLVED) {
            throw new InvalidUserDataException(String.format("Cannot change %s of parent of %s after it has been resolved", type, getDisplayName()));
        } else if (resolvedState == GRAPH_RESOLVED) {
            if (type == MutationType.DEPENDENCIES) {
                throw new InvalidUserDataException(String.format("Cannot change %s of parent of %s after task dependencies have been resolved", type, getDisplayName()));
            }
        }
    }

    private void preventIllegalMutation(MutationType type) {
        // TODO: Deprecate and eventually prevent these mutations when already resolved
        if (type == MutationType.DEPENDENCY_ATTRIBUTES) {
            return;
        }

        if (resolvedState == ARTIFACTS_RESOLVED) {
            // The public result for the configuration has been calculated.
            // It is an error to change anything that would change the dependencies or artifacts
            throw new InvalidUserDataException(String.format("Cannot change %s of dependency %s after it has been resolved.", type, getDisplayName()));
        } else if (resolvedState == GRAPH_RESOLVED) {
            // The task dependencies for the configuration have been calculated using Configuration.getBuildDependencies().
            throw new InvalidUserDataException(String.format("Cannot change %s of dependency %s after task dependencies have been resolved", type, getDisplayName()));
        } else if (observedState == GRAPH_RESOLVED || observedState == ARTIFACTS_RESOLVED) {
            // The configuration has been used in a resolution, and it is an error for build logic to change any dependencies,
            // exclude rules or parent configurations (values that will affect the resolved graph).
            if (type != MutationType.STRATEGY) {
                String extraMessage = insideBeforeResolve ? " Use 'defaultDependencies' instead of 'beforeResolve' to specify default dependencies for a configuration." : "";
                throw new InvalidUserDataException(String.format("Cannot change %s of dependency %s after it has been included in dependency resolution.%s", type, getDisplayName(), extraMessage));
            }
        }
    }

    private void markAsModified(MutationType type) {
        // TODO: Should not be ignoring DEPENDENCY_ATTRIBUTE modifications after resolve
        if (type == MutationType.DEPENDENCY_ATTRIBUTES) {
            return;
        }
        // Strategy mutations will not require a re-resolve
        if (type == MutationType.STRATEGY) {
            return;
        }
        dependenciesModified = true;
    }

    private void notifyChildren(MutationType type) {
        // Notify child configurations
        for (MutationValidator validator : childMutationValidators) {
            validator.validateMutation(type);
        }
    }

    private static class ConfigurationDescription implements Describable {
        private final Path identityPath;

        ConfigurationDescription(Path identityPath) {
            this.identityPath = identityPath;
        }

        @Override
        public String getDisplayName() {
            return "configuration '" + identityPath + "'";
        }
    }

    private class ConfigurationFileCollection extends AbstractFileCollection {
        private final Spec<? super Dependency> dependencySpec;
        private final AttributeContainerInternal viewAttributes;
        private final Spec<? super ComponentIdentifier> componentSpec;
        private final boolean lenient;
        private final boolean allowNoMatchingVariants;
        private SelectedArtifactSet selectedArtifacts;

        private ConfigurationFileCollection(Spec<? super Dependency> dependencySpec) {
            assertIsResolvable();
            this.dependencySpec = dependencySpec;
            this.viewAttributes = configurationAttributes;
            this.componentSpec = Specs.satisfyAll();
            lenient = false;
            allowNoMatchingVariants = false;
        }

        private ConfigurationFileCollection(Spec<? super Dependency> dependencySpec, AttributeContainerInternal viewAttributes,
                                            Spec<? super ComponentIdentifier> componentSpec, boolean lenient, boolean allowNoMatchingVariants) {
            this.dependencySpec = dependencySpec;
            this.viewAttributes = viewAttributes.asImmutable();
            this.componentSpec = componentSpec;
            this.lenient = lenient;
            this.allowNoMatchingVariants = allowNoMatchingVariants;
        }

        private ConfigurationFileCollection(Closure dependencySpecClosure) {
            this(Specs.convertClosureToSpec(dependencySpecClosure));
        }

        private ConfigurationFileCollection(final Set<Dependency> dependencies) {
            this(dependencies::contains);
        }

        @Override
        public void visitDependencies(TaskDependencyResolveContext context) {
            assertIsResolvable();
            ResolverResults results = resolveGraphForBuildDependenciesIfRequired();
            SelectedArtifactSet selected = results.getVisitedArtifacts().select(dependencySpec, viewAttributes, componentSpec, allowNoMatchingVariants);
            FailureCollectingTaskDependencyResolveContext collectingContext = new FailureCollectingTaskDependencyResolveContext(context);
            selected.visitDependencies(collectingContext);
            if (!lenient) {
                rethrowFailure("task dependencies", collectingContext.getFailures());
            }
        }

        public Spec<? super Dependency> getDependencySpec() {
            return dependencySpec;
        }

        @Override
        public String getDisplayName() {
            return DefaultConfiguration.this.getDisplayName();
        }

        @Override
        public Set<File> getFiles() {
            ResolvedFilesCollectingVisitor visitor = new ResolvedFilesCollectingVisitor();
            visitContents(visitor);
            return visitor.getFiles();
        }

        @Override
        public void visitStructure(FileCollectionStructureVisitor visitor) {
            ResolvedFilesCollectingVisitor collectingVisitor = new ResolvedFileCollectionVisitor(visitor);
            visitContents(collectingVisitor);
        }

        private void visitContents(ResolvedFilesCollectingVisitor visitor) {
            getSelectedArtifacts().visitArtifacts(visitor, lenient);

            if (!lenient) {
                rethrowFailure("files", visitor.getFailures());
            }
        }

        private SelectedArtifactSet getSelectedArtifacts() {
            if (selectedArtifacts == null) {
                resolveToStateOrLater(ARTIFACTS_RESOLVED);
                selectedArtifacts = cachedResolverResults.getVisitedArtifacts().select(dependencySpec, viewAttributes, componentSpec, allowNoMatchingVariants);
            }
            return selectedArtifacts;
        }
    }

    private void rethrowFailure(String type, Collection<Throwable> failures) {
        if (failures.isEmpty()) {
            return;
        }
        if (failures.size() == 1) {
            Throwable failure = failures.iterator().next();
            if (failure instanceof ResolveException) {
                throw UncheckedException.throwAsUncheckedException(failure);
            }
        }
        throw new DefaultLenientConfiguration.ArtifactResolveException(type, getIdentityPath().toString(), getDisplayName(), failures);
    }

    private void assertIsResolvable() {
        if (!canBeResolved) {
            throw new IllegalStateException("Resolving dependency configuration '" + name + "' is not allowed as it is defined as 'canBeResolved=false'.\nInstead, a resolvable ('canBeResolved=true') dependency configuration that extends '" + name + "' should be resolved.");
        }
    }

    @Override
    public AttributeContainerInternal getAttributes() {
        return configurationAttributes;
    }

    @Override
    public Configuration attributes(Action<? super AttributeContainer> action) {
        action.execute(configurationAttributes);
        return this;
    }

    @Override
    public boolean isCanBeConsumed() {
        return canBeConsumed;
    }

    @Override
    public void setCanBeConsumed(boolean allowed) {
        validateMutation(MutationType.ROLE);
        canBeConsumed = allowed;
    }

    @Override
    public boolean isCanBeResolved() {
        return canBeResolved;
    }

    @Override
    public void setCanBeResolved(boolean allowed) {
        validateMutation(MutationType.ROLE);
        canBeResolved = allowed;
    }

    @VisibleForTesting
    ListenerBroadcast<DependencyResolutionListener> getDependencyResolutionListeners() {
        return dependencyResolutionListeners;
    }


    @Override
    @Nullable
    public List<String> getDeclarationAlternatives() {
        return declarationAlternatives;
    }

    @Nullable
    @Override
    public List<String> getConsumptionAlternatives() {
        return consumptionAlternatives;
    }

    @Nullable
    @Override
    public List<String> getResolutionAlternatives() {
        return resolutionAlternatives;
    }

    @Override
    public boolean isFullyDeprecated() {
        return declarationAlternatives != null &&
            (!canBeConsumed || consumptionAlternatives != null) &&
            (!canBeResolved || resolutionAlternatives != null);
    }

    @Override
    public DeprecatableConfiguration deprecateForDeclaration(String... alternativesForDeclaring) {
        this.declarationAlternatives = ImmutableList.copyOf(alternativesForDeclaring);
        return this;
    }

    @Override
    public DeprecatableConfiguration deprecateForConsumption(String... alternativesForConsumption) {
        this.consumptionAlternatives = ImmutableList.copyOf(alternativesForConsumption);
        return this;
    }

    @Override
    public DeprecatableConfiguration deprecateForResolution(String... alternativesForResolving) {
        this.resolutionAlternatives = ImmutableList.copyOf(alternativesForResolving);
        return this;
    }

    /**
     * Print a formatted representation of a Configuration
     */
    public String dump() {
        StringBuilder reply = new StringBuilder();

        reply.append("\nConfiguration:");
        reply.append("  class='" + this.getClass() + "'");
        reply.append("  name='" + this.getName() + "'");
        reply.append("  hashcode='" + this.hashCode() + "'");

        reply.append("\nLocal Dependencies:");
        if (getDependencies().size() > 0) {
            for (Dependency d : getDependencies()) {
                reply.append("\n   " + d);
            }
        } else {
            reply.append("\n   none");
        }

        reply.append("\nLocal Artifacts:");
        if (getArtifacts().size() > 0) {
            for (PublishArtifact a : getArtifacts()) {
                reply.append("\n   " + a);
            }
        } else {
            reply.append("\n   none");
        }

        reply.append("\nAll Dependencies:");
        if (getAllDependencies().size() > 0) {
            for (Dependency d : getAllDependencies()) {
                reply.append("\n   " + d);
            }
        } else {
            reply.append("\n   none");
        }


        reply.append("\nAll Artifacts:");
        if (getAllArtifacts().size() > 0) {
            for (PublishArtifact a : getAllArtifacts()) {
                reply.append("\n   " + a);
            }
        } else {
            reply.append("\n   none");
        }

        return reply.toString();
    }

    public class ConfigurationResolvableDependencies implements ResolvableDependenciesInternal {

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getPath() {
            return path.getPath();
        }

        @Override
        public String toString() {
            return "dependencies '" + getIdentityPath() + "'";
        }

        @Override
        public FileCollection getFiles() {
            return new ConfigurationFileCollection(Specs.satisfyAll());
        }

        @Override
        public DependencySet getDependencies() {
            runDependencyActions();
            return getAllDependencies();
        }

        @Override
        public DependencyConstraintSet getDependencyConstraints() {
            runDependencyActions();
            return getAllDependencyConstraints();
        }

        @Override
        public void beforeResolve(Action<? super ResolvableDependencies> action) {
            dependencyResolutionListeners.add("beforeResolve", userCodeApplicationContext.decorateWithCurrent(action));
        }

        @Override
        public void beforeResolve(Closure action) {
            beforeResolve(ConfigureUtil.configureUsing(action));
        }

        @Override
        public void afterResolve(Action<? super ResolvableDependencies> action) {
            dependencyResolutionListeners.add("afterResolve", userCodeApplicationContext.decorateWithCurrent(action));
        }

        @Override
        public void afterResolve(Closure action) {
            afterResolve(ConfigureUtil.configureUsing(action));
        }

        @Override
        public ResolutionResult getResolutionResult() {
            return new LenientResolutionResult(DEFAULT_ERROR_HANDLER);
        }

        @Override
        public ArtifactCollection getArtifacts() {
            return new ConfigurationArtifactCollection();
        }

        @Override
        public ArtifactView artifactView(Action<? super ArtifactView.ViewConfiguration> configAction) {
            ArtifactViewConfiguration config = createArtifactViewConfiguration();
            configAction.execute(config);
            return createArtifactView(config);
        }

        private ArtifactView createArtifactView(ArtifactViewConfiguration config) {
            ImmutableAttributes viewAttributes = config.lockViewAttributes();
            // This is a little coincidental: if view attributes have not been accessed, don't allow no matching variants
            boolean allowNoMatchingVariants = config.attributesUsed;
            return new ConfigurationArtifactView(viewAttributes, config.lockComponentFilter(), config.lenient, allowNoMatchingVariants);
        }

        private DefaultConfiguration.ArtifactViewConfiguration createArtifactViewConfiguration() {
            return instantiator.newInstance(ArtifactViewConfiguration.class, attributesFactory, configurationAttributes);
        }

        @Override
        public AttributeContainer getAttributes() {
            return configurationAttributes;
        }

        @Override
        public ResolutionResult getResolutionResult(Action<? super Throwable> errorHandler) {
            return new LenientResolutionResult(errorHandler);
        }

        private class ConfigurationArtifactView implements ArtifactView {
            private final ImmutableAttributes viewAttributes;
            private final Spec<? super ComponentIdentifier> componentFilter;
            private final boolean lenient;
            private final boolean allowNoMatchingVariants;

            ConfigurationArtifactView(ImmutableAttributes viewAttributes, Spec<? super ComponentIdentifier> componentFilter, boolean lenient, boolean allowNoMatchingVariants) {
                this.viewAttributes = viewAttributes;
                this.componentFilter = componentFilter;
                this.lenient = lenient;
                this.allowNoMatchingVariants = allowNoMatchingVariants;
            }

            @Override
            public AttributeContainer getAttributes() {
                return viewAttributes;
            }

            @Override
            public ArtifactCollection getArtifacts() {
                return new ConfigurationArtifactCollection(viewAttributes, componentFilter, lenient, allowNoMatchingVariants);
            }

            @Override
            public FileCollection getFiles() {
                return new ConfigurationFileCollection(Specs.satisfyAll(), viewAttributes, componentFilter, lenient, allowNoMatchingVariants);
            }
        }

        private void assertArtifactsResolved() {
            DefaultConfiguration.this.resolveToStateOrLater(ARTIFACTS_RESOLVED);
        }

        private class LenientResolutionResult implements ResolutionResult {
            private final Action<? super Throwable> errorHandler;
            private volatile ResolutionResult delegate;

            private LenientResolutionResult(Action<? super Throwable> errorHandler) {
                this.errorHandler = errorHandler;
            }

            private void resolve() {
                if (delegate == null) {
                    synchronized (this) {
                        if (delegate == null) {
                            assertArtifactsResolved();
                            delegate = cachedResolverResults.getResolutionResult();
                            Throwable failure = cachedResolverResults.consumeNonFatalFailure();
                            if (failure != null) {
                                errorHandler.execute(failure);
                            }
                        }
                    }
                }
            }

            @Override
            public ResolvedComponentResult getRoot() {
                resolve();
                return delegate.getRoot();
            }

            @Override
            public Set<? extends DependencyResult> getAllDependencies() {
                resolve();
                return delegate.getAllDependencies();
            }

            @Override
            public void allDependencies(Action<? super DependencyResult> action) {
                resolve();
                delegate.allDependencies(action);
            }

            @Override
            public void allDependencies(Closure closure) {
                resolve();
                delegate.allDependencies(closure);
            }

            @Override
            public Set<ResolvedComponentResult> getAllComponents() {
                resolve();
                return delegate.getAllComponents();
            }

            @Override
            public void allComponents(Action<? super ResolvedComponentResult> action) {
                resolve();
                delegate.allComponents(action);
            }

            @Override
            public void allComponents(Closure closure) {
                resolve();
                delegate.allComponents(closure);
            }

            @Override
            public AttributeContainer getRequestedAttributes() {
                return delegate.getRequestedAttributes();
            }

            @Override
            public int hashCode() {
                resolve();
                return delegate.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof LenientResolutionResult) {
                    resolve();
                    return delegate.equals(((LenientResolutionResult) obj).delegate);
                }
                return false;
            }

            @Override
            public String toString() {
                return "lenient resolution result for " + delegate;
            }
        }

    }

    public static class ArtifactViewConfiguration implements ArtifactView.ViewConfiguration {

        private final ImmutableAttributesFactory attributesFactory;
        private final AttributeContainerInternal configurationAttributes;
        private AttributeContainerInternal viewAttributes;
        private Spec<? super ComponentIdentifier> componentFilter;
        private boolean lenient;
        private boolean attributesUsed;

        public ArtifactViewConfiguration(ImmutableAttributesFactory attributesFactory, AttributeContainerInternal configurationAttributes) {
            this.attributesFactory = attributesFactory;
            this.configurationAttributes = configurationAttributes;
        }

        @Override
        public AttributeContainer getAttributes() {
            if (viewAttributes == null) {
                viewAttributes = attributesFactory.mutable(configurationAttributes);
                attributesUsed = true;
            }
            return viewAttributes;
        }

        @Override
        public ArtifactViewConfiguration attributes(Action<? super AttributeContainer> action) {
            action.execute(getAttributes());
            return this;
        }

        @Override
        public ArtifactViewConfiguration componentFilter(Spec<? super ComponentIdentifier> componentFilter) {
            assertComponentFilterUnset();
            this.componentFilter = componentFilter;
            return this;
        }

        @Override
        public boolean isLenient() {
            return lenient;
        }

        @Override
        public void setLenient(boolean lenient) {
            this.lenient = lenient;
        }

        @Override
        public ArtifactViewConfiguration lenient(boolean lenient) {
            this.lenient = lenient;
            return this;
        }

        private void assertComponentFilterUnset() {
            if (componentFilter != null) {
                throw new IllegalStateException("The component filter can only be set once before the view was computed");
            }
        }

        private Spec<? super ComponentIdentifier> lockComponentFilter() {
            if (componentFilter == null) {
                componentFilter = Specs.satisfyAll();
            }
            return componentFilter;
        }

        private ImmutableAttributes lockViewAttributes() {
            if (viewAttributes == null) {
                viewAttributes = configurationAttributes.asImmutable();
            } else {
                viewAttributes = viewAttributes.asImmutable();
            }
            return viewAttributes.asImmutable();
        }
    }

    private class ConfigurationArtifactCollection implements ArtifactCollectionInternal {
        private final ConfigurationFileCollection fileCollection;
        private final boolean lenient;
        private Set<ResolvedArtifactResult> artifactResults;
        private Set<Throwable> failures;

        ConfigurationArtifactCollection() {
            this(configurationAttributes, Specs.satisfyAll(), false, false);
        }

        ConfigurationArtifactCollection(AttributeContainerInternal attributes, Spec<? super ComponentIdentifier> componentFilter, boolean lenient, boolean allowNoMatchingVariants) {
            assertIsResolvable();
            AttributeContainerInternal viewAttributes = attributes.asImmutable();
            this.fileCollection = new ConfigurationFileCollection(Specs.satisfyAll(), viewAttributes, componentFilter, lenient, allowNoMatchingVariants);
            this.lenient = lenient;
        }

        @Override
        public FileCollection getArtifactFiles() {
            return fileCollection;
        }

        @Override
        public Set<ResolvedArtifactResult> getArtifacts() {
            ensureResolved();
            return artifactResults;
        }

        @Override
        public Iterator<ResolvedArtifactResult> iterator() {
            ensureResolved();
            return artifactResults.iterator();
        }

        @Override
        public Collection<Throwable> getFailures() {
            ensureResolved();
            return failures;
        }

        @Override
        public void visitArtifacts(ArtifactVisitor visitor) {
            // TODO - if already resolved, use the results
            fileCollection.getSelectedArtifacts().visitArtifacts(visitor, lenient);
        }

        private synchronized void ensureResolved() {
            if (artifactResults != null) {
                return;
            }

            ResolvedArtifactCollectingVisitor visitor = new ResolvedArtifactCollectingVisitor();
            fileCollection.getSelectedArtifacts().visitArtifacts(visitor, lenient);

            artifactResults = visitor.getArtifacts();
            failures = visitor.getFailures();

            if (!lenient) {
                rethrowFailure("artifacts", failures);
            }
        }
    }

    private class AllArtifactsProvider implements PublishArtifactSetProvider {

        @Override
        public PublishArtifactSet getPublishArtifactSet() {
            return getAllArtifacts();
        }
    }

    public static class ResolveGraphAction implements WorkNodeAction {
        private final DefaultConfiguration configuration;

        public ResolveGraphAction(DefaultConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public String toString() {
            return "resolve graph for " + configuration;
        }

        @Nullable
        @Override
        public Project getProject() {
            return configuration.owner.getProject();
        }

        @Override
        public void run(NodeExecutionContext context) {
            configuration.resolveExclusively(GRAPH_RESOLVED);
        }
    }
}
