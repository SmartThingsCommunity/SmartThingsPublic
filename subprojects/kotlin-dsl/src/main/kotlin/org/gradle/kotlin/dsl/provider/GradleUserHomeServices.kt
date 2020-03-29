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
package org.gradle.kotlin.dsl.provider

import org.gradle.cache.CacheRepository
import org.gradle.cache.internal.CacheScopeMapping
import org.gradle.cache.internal.CrossBuildInMemoryCacheFactory


internal
object GradleUserHomeServices {

    @Suppress("unused")
    fun createKotlinScriptClassloadingCache(
        cacheFactory: CrossBuildInMemoryCacheFactory
    ) = KotlinScriptClassloadingCache(cacheFactory)

    @Suppress("unused")
    fun createScriptCacheRepository(
        cacheScopeMapping: CacheScopeMapping,
        cacheRepository: CacheRepository
    ) = ScriptCacheRepository(cacheScopeMapping, cacheRepository)
}
