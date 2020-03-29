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
package org.gradle.api.internal.artifacts.ivyservice.modulecache.artifacts;

import org.gradle.api.internal.artifacts.ivyservice.ArtifactCacheLockingManager;
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.resource.local.FileAccessTracker;
import org.gradle.util.BuildCommencedTimeProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class ReadOnlyModuleArtifactCache extends DefaultModuleArtifactCache {
    public ReadOnlyModuleArtifactCache(String persistentCacheFile, BuildCommencedTimeProvider timeProvider, ArtifactCacheLockingManager artifactCacheLockingManager, FileAccessTracker fileAccessTracker, Path commonRootPath) {
        super(persistentCacheFile, timeProvider, artifactCacheLockingManager, fileAccessTracker, commonRootPath);
    }

    @Override
    public void store(ArtifactAtRepositoryKey key, File artifactFile, HashCode moduleDescriptorHash) {
        operationShouldNotHaveBeenCalled();
    }

    @Override
    public void storeMissing(ArtifactAtRepositoryKey key, List<String> attemptedLocations, HashCode descriptorHash) {
        operationShouldNotHaveBeenCalled();
    }

    @Override
    protected void storeInternal(ArtifactAtRepositoryKey key, CachedArtifact entry) {
        operationShouldNotHaveBeenCalled();
    }

    @Override
    public void clear(ArtifactAtRepositoryKey key) {
        operationShouldNotHaveBeenCalled();
    }

    private static void operationShouldNotHaveBeenCalled() {
        throw new UnsupportedOperationException("A write operation shouldn't have been called in a read-only cache");
    }
}
