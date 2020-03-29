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

package org.gradle.api.internal.tasks.compile.incremental.classpath;

import org.gradle.cache.internal.Cache;
import org.gradle.internal.hash.HashCode;

import java.io.File;

public interface ClasspathEntrySnapshotCache extends Cache<File, ClasspathEntrySnapshot> {
    /**
     * Finds the snapshot associated with the file at the time it had the given hash code. May return null if no snapshot was found in the cache.
     */
    ClasspathEntrySnapshot get(File file, HashCode hash);
}
