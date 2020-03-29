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
package org.gradle.api.internal.artifacts.ivyservice.modulecache;

import org.gradle.api.artifacts.ResolvedModuleVersion;
import org.gradle.api.internal.artifacts.ivyservice.modulecache.dynamicversions.DefaultResolvedModuleVersion;
import org.gradle.internal.component.external.model.ModuleComponentResolveMetadata;
import org.gradle.internal.component.model.ModuleSources;
import org.gradle.util.BuildCommencedTimeProvider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class DefaultCachedMetadata implements ModuleMetadataCache.CachedMetadata {
    private final long ageMillis;
    private final ModuleComponentResolveMetadata metadata;

    private volatile Map<Integer, ModuleComponentResolveMetadata> processedMetadataByRules;

    DefaultCachedMetadata(ModuleMetadataCacheEntry entry, ModuleComponentResolveMetadata metadata, BuildCommencedTimeProvider timeProvider) {
        this.ageMillis = timeProvider.getCurrentTime() - entry.createTimestamp;
        this.metadata = metadata;
    }

    @Override
    public boolean isMissing() {
        return metadata == null;
    }

    @Override
    public ModuleSources getModuleSources() {
        return metadata.getSources();
    }

    @Override
    public ResolvedModuleVersion getModuleVersion() {
        return isMissing() ? null : new DefaultResolvedModuleVersion(getMetadata().getModuleVersionId());
    }

    @Override
    public ModuleComponentResolveMetadata getMetadata() {
        return metadata;
    }

    @Override
    public long getAgeMillis() {
        return ageMillis;
    }

    @Nullable
    @Override
    public ModuleComponentResolveMetadata getProcessedMetadata(int key) {
        if (processedMetadataByRules != null) {
            return processedMetadataByRules.get(key);
        }
        return null;
    }

    @Override
    public synchronized void putProcessedMetadata(int hash, ModuleComponentResolveMetadata processed) {
        if (processedMetadataByRules == null) {
            processedMetadataByRules = Collections.singletonMap(hash, processed);
            return;
        } else if (processedMetadataByRules.size() == 1) {
            processedMetadataByRules = new ConcurrentHashMap<>(processedMetadataByRules);
        }
        processedMetadataByRules.put(hash, processed);
    }
}
