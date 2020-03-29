/*
 * Copyright 2020 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.internal.CacheScopeMapping;
import org.gradle.cache.internal.UsedGradleVersions;
import org.gradle.internal.Factory;
import org.gradle.internal.file.FileAccessTimeJournal;
import org.gradle.internal.serialize.Serializer;
import org.gradle.util.IncubationLogger;

import java.io.Closeable;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class DefaultArtifactCaches implements ArtifactCachesProvider {
    private final static Logger LOGGER = Logging.getLogger(DefaultArtifactCaches.class);

    private final DefaultArtifactCacheMetadata writableCacheMetadata;
    private final DefaultArtifactCacheMetadata readOnlyCacheMetadata;
    private final LateInitWritableArtifactCacheLockingManager writableArtifactCacheLockingManager;
    private final ReadOnlyArtifactCacheLockingManager readOnlyArtifactCacheLockingManager;

    public DefaultArtifactCaches(CacheScopeMapping cacheScopeMapping,
                                 CacheRepository cacheRepository,
                                 Factory<WritableArtifactCacheLockingParameters> writableArtifactCacheLockingParametersFactory,
                                 DocumentationRegistry documentationRegistry) {
        writableCacheMetadata = new DefaultArtifactCacheMetadata(cacheScopeMapping);
        writableArtifactCacheLockingManager = new LateInitWritableArtifactCacheLockingManager(() -> {
            WritableArtifactCacheLockingParameters params = writableArtifactCacheLockingParametersFactory.create();
            return new WritableArtifactCacheLockingManager(cacheRepository, writableCacheMetadata, params.getFileAccessTimeJournal(), params.getUsedGradleVersions());
        });
        String roCache = System.getenv(READONLY_CACHE_ENV_VAR);
        if (StringUtils.isNotEmpty(roCache)) {
            IncubationLogger.incubatingFeatureUsed("Shared read-only dependency cache");
            File baseDir = validateReadOnlyCache(documentationRegistry, new File(roCache));
            if (baseDir != null) {
                readOnlyCacheMetadata = new DefaultArtifactCacheMetadata(cacheScopeMapping, baseDir);
                readOnlyArtifactCacheLockingManager = new ReadOnlyArtifactCacheLockingManager(cacheRepository, readOnlyCacheMetadata);
            } else {
                readOnlyCacheMetadata = null;
                readOnlyArtifactCacheLockingManager = null;
            }
        } else {
            readOnlyCacheMetadata = null;
            readOnlyArtifactCacheLockingManager = null;
        }
    }

    private static File validateReadOnlyCache(DocumentationRegistry documentationRegistry, File cacheDir) {
        if (!cacheDir.exists()) {
            LOGGER.warn("The read-only dependency cache is disabled because of a configuration problem:");
            LOGGER.warn("The " + READONLY_CACHE_ENV_VAR + " environment variable was set to " + cacheDir + " which doesn't exist!");
            return null;
        }
        File root = CacheLayout.ROOT.getPath(cacheDir);
        if (!root.exists()) {
            String docLink = documentationRegistry.getDocumentationFor("dependency_resolution", "sub:shared-readonly-cache");
            LOGGER.warn("The read-only dependency cache is disabled because of a configuration problem:");
            LOGGER.warn("Read-only cache is configured but the directory layout isn't expected. You must have a pre-populated " +
                CacheLayout.ROOT.getKey() + " directory at " + root + " . Please follow the instructions at " + docLink);
            return null;
        }
        return cacheDir;
    }

    @Override
    public ArtifactCacheMetadata getWritableCacheMetadata() {
        return writableCacheMetadata;
    }

    @Override
    public Optional<ArtifactCacheMetadata> getReadOnlyCacheMetadata() {
        return Optional.ofNullable(readOnlyCacheMetadata);
    }

    @Override
    public ArtifactCacheLockingManager getWritableCacheLockingManager() {
        return writableArtifactCacheLockingManager;
    }

    @Override
    public Optional<ArtifactCacheLockingManager> getReadOnlyCacheLockingManager() {
        return Optional.ofNullable(readOnlyArtifactCacheLockingManager);
    }

    @Override
    public List<File> getAdditiveCacheRoots() {
        ImmutableList.Builder<File> builder = ImmutableList.builderWithExpectedSize(4);
        builder.add(writableCacheMetadata.getFileStoreDirectory());
        builder.add(writableCacheMetadata.getTransformsStoreDirectory());
        if (readOnlyCacheMetadata != null) {
            builder.add(readOnlyCacheMetadata.getFileStoreDirectory());
            builder.add(readOnlyCacheMetadata.getTransformsStoreDirectory());
        }
        return builder.build();
    }

    @Override
    public void close() {
        writableArtifactCacheLockingManager.close();
        if (readOnlyArtifactCacheLockingManager != null) {
            readOnlyArtifactCacheLockingManager.close();
        }
    }

    public interface WritableArtifactCacheLockingParameters {
        FileAccessTimeJournal getFileAccessTimeJournal();

        UsedGradleVersions getUsedGradleVersions();
    }

    private static class LateInitWritableArtifactCacheLockingManager implements ArtifactCacheLockingManager, Closeable {
        private final Factory<WritableArtifactCacheLockingManager> factory;
        private volatile WritableArtifactCacheLockingManager delegate;

        private LateInitWritableArtifactCacheLockingManager(Factory<WritableArtifactCacheLockingManager> factory) {
            this.factory = factory;
        }

        private WritableArtifactCacheLockingManager getDelegate() {
            if (delegate == null) {
                synchronized (factory) {
                    if (delegate == null) {
                        delegate = factory.create();
                    }
                }
            }
            return delegate;
        }

        @Override
        public void close() {
            if (delegate != null) {
                delegate.close();
            }
        }

        @Override
        public <T> T withFileLock(Factory<? extends T> action) {
            return getDelegate().withFileLock(action);
        }

        @Override
        public void withFileLock(Runnable action) {
            getDelegate().withFileLock(action);
        }

        @Override
        public <T> T useCache(Factory<? extends T> action) {
            return getDelegate().useCache(action);
        }

        @Override
        public void useCache(Runnable action) {
            getDelegate().useCache(action);
        }

        @Override
        public <K, V> PersistentIndexedCache<K, V> createCache(String cacheName, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
            return getDelegate().createCache(cacheName, keySerializer, valueSerializer);
        }
    }
}
