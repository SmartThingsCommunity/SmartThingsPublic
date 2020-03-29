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

package org.gradle.caching.internal.controller.operations;

import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.internal.operations.BuildCacheArchiveUnpackBuildOperationType;

public class UnpackOperationDetails implements BuildCacheArchiveUnpackBuildOperationType.Details {

    private final BuildCacheKey key;
    private final long archiveSize;

    public UnpackOperationDetails(BuildCacheKey key, long archiveSize) {
        this.key = key;
        this.archiveSize = archiveSize;
    }

    @Override
    public String getCacheKey() {
        return key.getHashCode();
    }

    @Override
    public long getArchiveSize() {
        return archiveSize;
    }
}
