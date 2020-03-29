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

package org.gradle.internal.service.scopes;

import org.gradle.StartParameter;
import org.gradle.api.internal.FeaturePreviews;
import org.gradle.api.internal.attributes.DefaultImmutableAttributesFactory;
import org.gradle.api.internal.cache.StringInterner;
import org.gradle.api.internal.changedetection.state.BuildScopeFileTimeStampInspector;
import org.gradle.api.internal.changedetection.state.CrossBuildFileHashCache;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.model.NamedObjectInstantiator;
import org.gradle.api.internal.project.BuildOperationCrossProjectConfigurator;
import org.gradle.api.internal.project.CrossProjectConfigurator;
import org.gradle.api.model.ObjectFactory;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.internal.CacheRepositoryServices;
import org.gradle.cache.internal.CacheScopeMapping;
import org.gradle.cache.internal.CleanupActionFactory;
import org.gradle.cache.internal.InMemoryCacheDecoratorFactory;
import org.gradle.cache.internal.VersionStrategy;
import org.gradle.deployment.internal.DefaultDeploymentRegistry;
import org.gradle.groovy.scripts.internal.DefaultScriptSourceHasher;
import org.gradle.groovy.scripts.internal.ScriptSourceHasher;
import org.gradle.initialization.BuildCancellationToken;
import org.gradle.initialization.BuildClientMetaData;
import org.gradle.initialization.BuildEventConsumer;
import org.gradle.initialization.BuildRequestMetaData;
import org.gradle.initialization.layout.BuildLayout;
import org.gradle.initialization.layout.BuildLayoutConfiguration;
import org.gradle.initialization.layout.BuildLayoutFactory;
import org.gradle.initialization.layout.ProjectCacheDir;
import org.gradle.internal.buildevents.BuildStartedTime;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.featurelifecycle.DeprecatedUsageBuildOperationProgressBroadcaster;
import org.gradle.internal.file.Deleter;
import org.gradle.internal.filewatch.PendingChangesManager;
import org.gradle.internal.hash.ChecksumService;
import org.gradle.internal.hash.DefaultChecksumService;
import org.gradle.internal.isolation.IsolatableFactory;
import org.gradle.internal.jpms.JavaModuleDetector;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.nativeintegration.filesystem.FileSystem;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.BuildOperationListenerManager;
import org.gradle.internal.operations.CurrentBuildOperationRef;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.resources.ProjectLeaseRegistry;
import org.gradle.internal.scopeids.PersistentScopeIdLoader;
import org.gradle.internal.scopeids.ScopeIdsServices;
import org.gradle.internal.scopeids.id.UserScopeId;
import org.gradle.internal.scopeids.id.WorkspaceScopeId;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.time.Clock;
import org.gradle.internal.work.AsyncWorkTracker;
import org.gradle.internal.work.DefaultAsyncWorkTracker;
import org.gradle.plugin.use.internal.InjectedPluginClasspath;
import org.gradle.process.internal.ExecFactory;

import java.io.Closeable;
import java.io.File;

/**
 * Contains the services for a single build session, which could be a single build or multiple builds when in continuous mode.
 */
public class BuildSessionScopeServices extends DefaultServiceRegistry {

    public BuildSessionScopeServices(final ServiceRegistry parent, CrossBuildSessionScopeServices crossBuildSessionScopeServices, final StartParameter startParameter, BuildRequestMetaData buildRequestMetaData, ClassPath injectedPluginClassPath, BuildCancellationToken buildCancellationToken, BuildClientMetaData buildClientMetaData, BuildEventConsumer buildEventConsumer) {
        super(parent);
        addProvider(crossBuildSessionScopeServices);
        register(registration -> {
            add(StartParameter.class, startParameter);
            for (PluginServiceRegistry pluginServiceRegistry : parent.getAll(PluginServiceRegistry.class)) {
                pluginServiceRegistry.registerBuildSessionServices(registration);
            }
        });
        add(InjectedPluginClasspath.class, new InjectedPluginClasspath(injectedPluginClassPath));
        add(BuildCancellationToken.class, buildCancellationToken);
        add(BuildRequestMetaData.class, buildRequestMetaData);
        add(BuildClientMetaData.class, buildClientMetaData);
        add(BuildEventConsumer.class, buildEventConsumer);
        addProvider(new CacheRepositoryServices(startParameter.getGradleUserHomeDir(), startParameter.getProjectCacheDir()));

        // Must be no higher than this scope as needs cache repository services.
        addProvider(new ScopeIdsServices());
    }

    PendingChangesManager createPendingChangesManager(ListenerManager listenerManager) {
        return new PendingChangesManager(listenerManager);
    }

    DefaultDeploymentRegistry createDeploymentRegistry(PendingChangesManager pendingChangesManager, BuildOperationExecutor buildOperationExecutor, ObjectFactory objectFactory) {
        return new DefaultDeploymentRegistry(pendingChangesManager, buildOperationExecutor, objectFactory);
    }

    ListenerManager createListenerManager(ListenerManager parent) {
        return parent.createChild();
    }

    CrossProjectConfigurator createCrossProjectConfigurator(BuildOperationExecutor buildOperationExecutor) {
        return new BuildOperationCrossProjectConfigurator(buildOperationExecutor);
    }

    BuildLayout createBuildLayout(BuildLayoutFactory buildLayoutFactory, StartParameter startParameter) {
        return buildLayoutFactory.getLayoutFor(new BuildLayoutConfiguration(startParameter));
    }

    ProjectCacheDir createCacheLayout(
        BuildLayout buildLayout,
        Deleter deleter,
        ProgressLoggerFactory progressLoggerFactory,
        StartParameter startParameter
    ) {
        File cacheDir = startParameter.getProjectCacheDir() != null ? startParameter.getProjectCacheDir() : new File(buildLayout.getRootDirectory(), ".gradle");
        return new ProjectCacheDir(cacheDir, progressLoggerFactory, deleter);
    }

    BuildScopeFileTimeStampInspector createFileTimeStampInspector(ProjectCacheDir projectCacheDir, CacheScopeMapping cacheScopeMapping, ListenerManager listenerManager) {
        File workDir = cacheScopeMapping.getBaseDirectory(projectCacheDir.getDir(), "fileChanges", VersionStrategy.CachePerVersion);
        BuildScopeFileTimeStampInspector timeStampInspector = new BuildScopeFileTimeStampInspector(workDir);
        listenerManager.addListener(timeStampInspector);
        return timeStampInspector;
    }

    ScriptSourceHasher createScriptSourceHasher() {
        return new DefaultScriptSourceHasher();
    }

    DefaultImmutableAttributesFactory createImmutableAttributesFactory(IsolatableFactory isolatableFactory, NamedObjectInstantiator instantiator) {
        return new DefaultImmutableAttributesFactory(isolatableFactory, instantiator);
    }

    AsyncWorkTracker createAsyncWorkTracker(ProjectLeaseRegistry projectLeaseRegistry) {
        return new DefaultAsyncWorkTracker(projectLeaseRegistry);
    }

    UserScopeId createUserScopeId(PersistentScopeIdLoader persistentScopeIdLoader) {
        return persistentScopeIdLoader.getUser();
    }

    protected WorkspaceScopeId createWorkspaceScopeId(PersistentScopeIdLoader persistentScopeIdLoader) {
        return persistentScopeIdLoader.getWorkspace();
    }

    BuildStartedTime createBuildStartedTime(Clock clock, BuildRequestMetaData buildRequestMetaData) {
        long currentTime = clock.getCurrentTime();
        return BuildStartedTime.startingAt(Math.min(currentTime, buildRequestMetaData.getStartTime()));
    }

    FeaturePreviews createExperimentalFeatures() {
        return new FeaturePreviews();
    }

    CleanupActionFactory createCleanupActionFactory(BuildOperationExecutor buildOperationExecutor) {
        return new CleanupActionFactory(buildOperationExecutor);
    }

    protected ExecFactory decorateExecFactory(ExecFactory execFactory, FileResolver fileResolver, FileCollectionFactory fileCollectionFactory, Instantiator instantiator, BuildCancellationToken buildCancellationToken, ObjectFactory objectFactory, JavaModuleDetector javaModuleDetector) {
        return execFactory.forContext(fileResolver, fileCollectionFactory, instantiator, buildCancellationToken, objectFactory, javaModuleDetector);
    }

    DeprecatedUsageBuildOperationProgressBroadcaster createDeprecatedUsageBuildOperationProgressBroadcaster(
        Clock clock,
        BuildOperationListenerManager buildOperationListenerManager,
        CurrentBuildOperationRef currentBuildOperationRef
    ) {
        return new DeprecatedUsageBuildOperationProgressBroadcaster(
            clock,
            buildOperationListenerManager.getBroadcaster(),
            currentBuildOperationRef
        );
    }

    CrossBuildFileHashCacheWrapper createCrossBuildChecksumCache(CacheScopeMapping cacheScopeMapping, ProjectCacheDir projectCacheDir, CacheRepository cacheRepository, InMemoryCacheDecoratorFactory inMemoryCacheDecoratorFactory) {
        File cacheDir = cacheScopeMapping.getBaseDirectory(projectCacheDir.getDir(), "checksums", VersionStrategy.SharedCache);
        CrossBuildFileHashCache crossBuildCache = new CrossBuildFileHashCache(cacheDir, cacheRepository, inMemoryCacheDecoratorFactory, CrossBuildFileHashCache.Kind.CHECKSUMS);
        return new CrossBuildFileHashCacheWrapper(crossBuildCache);
    }

    ChecksumService createChecksumService(StringInterner stringInterner, FileSystem fileSystem, CrossBuildFileHashCacheWrapper crossBuildCache, BuildScopeFileTimeStampInspector inspector) {
        return new DefaultChecksumService(stringInterner, crossBuildCache.delegate, fileSystem, inspector);
    }

    // Wraps CrossBuildFileHashCache so that it doesn't conflict
    // with other services in different scopes
    static class CrossBuildFileHashCacheWrapper implements Closeable {
        private final CrossBuildFileHashCache delegate;

        private CrossBuildFileHashCacheWrapper(CrossBuildFileHashCache delegate) {
            this.delegate = delegate;
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
