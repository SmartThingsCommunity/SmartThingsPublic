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
package org.gradle.api.internal.artifacts.ivyservice;

import org.gradle.cache.internal.CacheScopeMapping;
import org.gradle.cache.internal.CacheVersion;
import org.gradle.cache.internal.VersionStrategy;

import java.io.File;

public class DefaultArtifactCacheMetadata implements ArtifactCacheMetadata {

    public static final CacheVersion CACHE_LAYOUT_VERSION = CacheLayout.META_DATA.getVersion();
    private final File cacheDir;
    private final File transformsDir;

    public DefaultArtifactCacheMetadata(CacheScopeMapping cacheScopeMapping) {
        this(cacheScopeMapping, null);
    }

    public DefaultArtifactCacheMetadata(CacheScopeMapping cacheScopeMapping, File baseDir) {
        cacheDir = cacheScopeMapping.getBaseDirectory(baseDir, CacheLayout.ROOT.getKey(), VersionStrategy.SharedCache);
        transformsDir = cacheScopeMapping.getBaseDirectory(baseDir, CacheLayout.TRANSFORMS.getKey(), VersionStrategy.SharedCache);
    }

    @Override
    public File getCacheDir() {
        return cacheDir;
    }

    @Override
    public File getTransformsStoreDirectory() {
        return transformsDir;
    }

    @Override
    public File getFileStoreDirectory() {
        return createCacheRelativeDir(CacheLayout.FILE_STORE);
    }

    @Override
    public File getExternalResourcesStoreDirectory() {
        return createCacheRelativeDir(CacheLayout.RESOURCES);
    }

    @Override
    public File getMetaDataStoreDirectory() {
        return new File(createCacheRelativeDir(CacheLayout.META_DATA), "descriptors");
    }

    private File createCacheRelativeDir(CacheLayout cacheLayout) {
        return cacheLayout.getPath(getCacheDir());
    }
}
