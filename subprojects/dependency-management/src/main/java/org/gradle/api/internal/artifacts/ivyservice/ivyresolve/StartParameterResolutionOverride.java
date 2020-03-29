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

import org.gradle.StartParameter;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.verification.DependencyVerificationMode;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.artifacts.configurations.ResolutionStrategyInternal;
import org.gradle.api.internal.artifacts.configurations.dynamicversion.CachePolicy;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.verification.ChecksumAndSignatureVerificationOverride;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.verification.DependencyVerificationOverride;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.verification.writer.WriteDependencyVerificationFile;
import org.gradle.api.internal.artifacts.ivyservice.resolutionstrategy.ExternalResourceCachePolicy;
import org.gradle.api.internal.artifacts.repositories.resolver.MetadataFetchingCost;
import org.gradle.api.internal.artifacts.verification.signatures.SignatureVerificationServiceFactory;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.api.internal.properties.GradleProperties;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.resources.ResourceException;
import org.gradle.internal.Factory;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.component.external.model.ModuleDependencyMetadata;
import org.gradle.internal.component.model.ComponentArtifactMetadata;
import org.gradle.internal.component.model.ComponentOverrideMetadata;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.component.model.ConfigurationMetadata;
import org.gradle.internal.component.model.ModuleSources;
import org.gradle.internal.concurrent.Stoppable;
import org.gradle.internal.hash.ChecksumService;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.resolve.ArtifactResolveException;
import org.gradle.internal.resolve.ModuleVersionResolveException;
import org.gradle.internal.resolve.result.BuildableArtifactResolveResult;
import org.gradle.internal.resolve.result.BuildableArtifactSetResolveResult;
import org.gradle.internal.resolve.result.BuildableComponentArtifactsResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleVersionListingResolveResult;
import org.gradle.internal.resource.ReadableContent;
import org.gradle.internal.resource.metadata.ExternalResourceMetaData;
import org.gradle.internal.resource.transfer.ExternalResourceConnector;
import org.gradle.internal.resource.transfer.ExternalResourceReadResponse;
import org.gradle.util.BuildCommencedTimeProvider;
import org.gradle.util.IncubationLogger;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class StartParameterResolutionOverride {
    private final StartParameter startParameter;
    private final File gradleDir;

    public StartParameterResolutionOverride(StartParameter startParameter, File gradleDir) {
        this.startParameter = startParameter;
        this.gradleDir = gradleDir;
    }

    public void applyToCachePolicy(CachePolicy cachePolicy) {
        if (startParameter.isOffline()) {
            cachePolicy.setOffline();
        } else if (startParameter.isRefreshDependencies()) {
            cachePolicy.setRefreshDependencies();
        }
    }

    public ModuleComponentRepository overrideModuleVersionRepository(ModuleComponentRepository original) {
        if (startParameter.isOffline()) {
            return new OfflineModuleComponentRepository(original);
        }
        return original;
    }

    public DependencyVerificationOverride dependencyVerificationOverride(BuildOperationExecutor buildOperationExecutor,
                                                                         ChecksumService checksumService,
                                                                         SignatureVerificationServiceFactory signatureVerificationServiceFactory,
                                                                         DocumentationRegistry documentationRegistry,
                                                                         BuildCommencedTimeProvider timeProvider,
                                                                         Factory<GradleProperties> gradlePropertiesFactory) {
        List<String> checksums = startParameter.getWriteDependencyVerifications();
        if (!checksums.isEmpty()) {
            IncubationLogger.incubatingFeatureUsed("Dependency verification");
            return DisablingVerificationOverride.of(
                new WriteDependencyVerificationFile(gradleDir, buildOperationExecutor, checksums, checksumService, signatureVerificationServiceFactory, startParameter.isDryRun(), startParameter.isExportKeys())
            );
        } else {
            File verificationsFile = DependencyVerificationOverride.dependencyVerificationsFile(gradleDir);
            File keyringsFile = DependencyVerificationOverride.keyringsFile(gradleDir);
            if (verificationsFile.exists()) {
                if (startParameter.getDependencyVerificationMode() == DependencyVerificationMode.OFF) {
                    return DependencyVerificationOverride.NO_VERIFICATION;
                }
                IncubationLogger.incubatingFeatureUsed("Dependency verification");
                try {
                    File sessionReportDir = computeReportDirectory(timeProvider);
                    return DisablingVerificationOverride.of(
                        new ChecksumAndSignatureVerificationOverride(buildOperationExecutor, startParameter.getGradleUserHomeDir(), verificationsFile, keyringsFile, checksumService, signatureVerificationServiceFactory, startParameter.getDependencyVerificationMode(), documentationRegistry, sessionReportDir, gradlePropertiesFactory)
                    );
                } catch (Exception e) {
                    return new FailureVerificationOverride(e);
                }
            }
        }
        return DependencyVerificationOverride.NO_VERIFICATION;
    }

    private File computeReportDirectory(BuildCommencedTimeProvider timeProvider) {
        // TODO: This is not quite correct: we're using the "root project" build directory
        // but technically speaking, this can be changed _after_ this service is created.
        // There's currently no good way to figure that out.
        File buildDir = new File(gradleDir.getParentFile(), "build");
        File reportsDirectory = new File(buildDir, "reports");
        File verifReportsDirectory = new File(reportsDirectory, "dependency-verification");
        return new File(verifReportsDirectory, "at-" + timeProvider.getCurrentTime());
    }

    private static class OfflineModuleComponentRepository extends BaseModuleComponentRepository {

        private final FailedRemoteAccess failedRemoteAccess = new FailedRemoteAccess();

        public OfflineModuleComponentRepository(ModuleComponentRepository original) {
            super(original);
        }

        @Override
        public ModuleComponentRepositoryAccess getRemoteAccess() {
            return failedRemoteAccess;
        }
    }

    private static class FailedRemoteAccess implements ModuleComponentRepositoryAccess {
        @Override
        public String toString() {
            return "offline remote";
        }

        @Override
        public void listModuleVersions(ModuleDependencyMetadata dependency, BuildableModuleVersionListingResolveResult result) {
            result.failed(new ModuleVersionResolveException(dependency.getSelector(), () -> String.format("No cached version listing for %s available for offline mode.", dependency.getSelector())));
        }

        @Override
        public void resolveComponentMetaData(ModuleComponentIdentifier moduleComponentIdentifier, ComponentOverrideMetadata requestMetaData, BuildableModuleComponentMetaDataResolveResult result) {
            result.failed(new ModuleVersionResolveException(moduleComponentIdentifier, () -> String.format("No cached version of %s available for offline mode.", moduleComponentIdentifier.getDisplayName())));
        }

        @Override
        public void resolveArtifactsWithType(ComponentResolveMetadata component, ArtifactType artifactType, BuildableArtifactSetResolveResult result) {
            result.failed(new ArtifactResolveException(component.getId(), "No cached version available for offline mode"));
        }

        @Override
        public void resolveArtifacts(ComponentResolveMetadata component, ConfigurationMetadata variant, BuildableComponentArtifactsResolveResult result) {
            result.failed(new ArtifactResolveException(component.getId(), "No cached version available for offline mode"));
        }

        @Override
        public void resolveArtifact(ComponentArtifactMetadata artifact, ModuleSources moduleSources, BuildableArtifactResolveResult result) {
            result.failed(new ArtifactResolveException(artifact.getId(), "No cached version available for offline mode"));
        }

        @Override
        public MetadataFetchingCost estimateMetadataFetchingCost(ModuleComponentIdentifier moduleComponentIdentifier) {
            return MetadataFetchingCost.CHEAP;
        }
    }

    public ExternalResourceCachePolicy overrideExternalResourceCachePolicy(ExternalResourceCachePolicy original) {
        if (startParameter.isOffline()) {
            return ageMillis -> false;
        }
        return original;
    }

    public ExternalResourceConnector overrideExternalResourceConnector(ExternalResourceConnector original) {
        if (startParameter.isOffline()) {
            return new OfflineExternalResourceConnector();
        }
        return original;
    }

    private static class OfflineExternalResourceConnector implements ExternalResourceConnector {
        @Nullable
        @Override
        public ExternalResourceReadResponse openResource(URI location, boolean revalidate) throws ResourceException {
            throw offlineResource(location);
        }

        @Nullable
        @Override
        public ExternalResourceMetaData getMetaData(URI location, boolean revalidate) throws ResourceException {
            throw offlineResource(location);
        }

        @Nullable
        @Override
        public List<String> list(URI parent) throws ResourceException {
            throw offlineResource(parent);
        }

        @Override
        public void upload(ReadableContent resource, URI destination) throws IOException {
            throw new ResourceException(destination, String.format("Cannot upload to '%s' in offline mode.", destination));
        }

        private ResourceException offlineResource(URI source) {
            return new ResourceException(source, String.format("No cached resource '%s' available for offline mode.", source));
        }
    }

    private static class FailureVerificationOverride implements DependencyVerificationOverride {
        private final Exception error;

        private FailureVerificationOverride(Exception error) {
            this.error = error;
        }

        @Override
        public ModuleComponentRepository overrideDependencyVerification(ModuleComponentRepository original, String resolveContextName, ResolutionStrategyInternal resolutionStrategy) {
            throw new GradleException("Dependency verification cannot be performed", error);
        }
    }

    public static class DisablingVerificationOverride implements DependencyVerificationOverride, Stoppable {
        private final static Logger LOGGER = Logging.getLogger(DependencyVerificationOverride.class);

        private final DependencyVerificationOverride delegate;

        public static DisablingVerificationOverride of(DependencyVerificationOverride delegate) {
            return new DisablingVerificationOverride(delegate);
        }

        private DisablingVerificationOverride(DependencyVerificationOverride delegate) {
            this.delegate = delegate;
        }

        @Override
        public ModuleComponentRepository overrideDependencyVerification(ModuleComponentRepository original, String resolveContextName, ResolutionStrategyInternal resolutionStrategy) {
            if (resolutionStrategy.isDependencyVerificationEnabled()) {
                return delegate.overrideDependencyVerification(original, resolveContextName, resolutionStrategy);
            } else {
                LOGGER.warn("Dependency verification has been disabled for configuration " + resolveContextName);
                return original;
            }
        }

        @Override
        public void buildFinished(Gradle gradle) {
            delegate.buildFinished(gradle);
        }

        @Override
        public void artifactsAccessed(String displayName) {
            delegate.artifactsAccessed(displayName);
        }

        @Override
        public ResolvedArtifactResult verifiedArtifact(ResolvedArtifactResult artifact) {
            return delegate.verifiedArtifact(artifact);
        }

        @Override
        public void stop() {
            if (delegate instanceof Stoppable) {
                ((Stoppable) delegate).stop();
            } else if (delegate instanceof Closeable) {
                try {
                    ((Closeable) delegate).close();
                } catch (IOException e) {
                    throw UncheckedException.throwAsUncheckedException(e);
                }
            }
        }
    }
}
