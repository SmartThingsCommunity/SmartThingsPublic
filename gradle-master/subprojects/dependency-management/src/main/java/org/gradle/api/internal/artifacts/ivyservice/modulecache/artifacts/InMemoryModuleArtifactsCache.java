/*
 * Copyright 2018 the original author or authors.
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

import com.google.common.collect.Maps;
import org.gradle.util.BuildCommencedTimeProvider;

import java.util.Map;

public class InMemoryModuleArtifactsCache extends AbstractArtifactsCache {
    private final Map<ArtifactsAtRepositoryKey, ModuleArtifactsCacheEntry> inMemoryCache = Maps.newConcurrentMap();
    private final AbstractArtifactsCache delegate;

    public InMemoryModuleArtifactsCache(BuildCommencedTimeProvider timeProvider) {
        super(timeProvider);
        this.delegate = null;
    }

    public InMemoryModuleArtifactsCache(BuildCommencedTimeProvider timeProvider, AbstractArtifactsCache delegate) {
        super(timeProvider);
        this.delegate = delegate;
    }

    @Override
    protected void store(ArtifactsAtRepositoryKey key, ModuleArtifactsCacheEntry entry) {
        inMemoryCache.put(key, entry);
        if (delegate != null) {
            delegate.store(key, entry);
        }
    }

    @Override
    protected ModuleArtifactsCacheEntry get(ArtifactsAtRepositoryKey key) {
        ModuleArtifactsCacheEntry entry = inMemoryCache.get(key);
        if (entry == null && delegate!=null) {
            entry = delegate.get(key);
            if (entry != null) {
                inMemoryCache.put(key, entry);
            }
        }
        return entry;
    }

}
