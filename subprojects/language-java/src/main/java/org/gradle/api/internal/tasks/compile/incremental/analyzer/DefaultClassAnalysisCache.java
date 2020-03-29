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

package org.gradle.api.internal.tasks.compile.incremental.analyzer;

import org.gradle.api.internal.tasks.compile.incremental.deps.ClassAnalysis;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.internal.MinimalPersistentCache;
import org.gradle.internal.hash.HashCode;

/**
 * Cross-process, global cache of class bytecode/dependency analysis. Required to make incremental java compilation fast.
 * The class analysis results are cached globally, so if one project caches ClassA, it can be used by some other project.
 */
public class DefaultClassAnalysisCache extends MinimalPersistentCache<HashCode, ClassAnalysis> implements ClassAnalysisCache {
    public DefaultClassAnalysisCache(PersistentIndexedCache<HashCode, ClassAnalysis> cache) {
        super(cache);
    }
}
