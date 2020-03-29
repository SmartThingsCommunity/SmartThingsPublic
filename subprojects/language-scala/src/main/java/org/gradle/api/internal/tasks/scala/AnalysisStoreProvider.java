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

package org.gradle.api.internal.tasks.scala;

import org.gradle.cache.internal.Cache;
import org.gradle.cache.internal.MapBackedCache;
import org.gradle.internal.Factory;
import xsbti.compile.AnalysisStore;
import xsbti.compile.FileAnalysisStore;

import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class AnalysisStoreProvider {

    private final Cache<File, AnalysisStore> cache = new MapBackedCache<File, AnalysisStore>(new ConcurrentHashMap<>());

    AnalysisStore get(final File analysisFile) {
        return AnalysisStore.getCachedStore(cache.get(analysisFile, new Factory<AnalysisStore>() {
            @Nullable
            @Override
            public AnalysisStore create() {
                return AnalysisStore.getThreadSafeStore(FileAnalysisStore.getDefault(analysisFile));
            }
        }));
    }
}
