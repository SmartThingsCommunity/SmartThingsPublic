/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.internal.scopeids;

import org.gradle.cache.FileLockManager;
import org.gradle.cache.internal.CacheScopeMapping;
import org.gradle.initialization.layout.ProjectCacheDir;
import org.gradle.internal.file.Chmod;
import org.gradle.internal.id.UniqueId;

@SuppressWarnings("unused")
public class ScopeIdsServices {

    protected PersistentScopeIdLoader createPersistentScopeIdLoader(
        ProjectCacheDir projectCacheDir,
        CacheScopeMapping cacheScopeMapping,
        PersistentScopeIdStoreFactory persistentScopeIdStoreFactory
    ) {
        return new DefaultPersistentScopeIdLoader(projectCacheDir, cacheScopeMapping, persistentScopeIdStoreFactory, UniqueId.factory());
    }

    protected PersistentScopeIdStoreFactory createPersistentScopeIdStoreFactory(
        FileLockManager fileLockManager,
        Chmod chmod
    ) {
        return new PersistentScopeIdStoreFactory(chmod, fileLockManager);
    }

}
