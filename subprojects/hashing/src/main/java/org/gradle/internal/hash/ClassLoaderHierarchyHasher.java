/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.internal.hash;

import javax.annotation.Nullable;

/**
 * Provides a combined hash for a hierarchy of classloaders.
 */
public interface ClassLoaderHierarchyHasher {
    /**
     * Returns a hash for the given classloader hierarchy, or {@code null}
     * if the hierarchy contains any classloaders that are not known to Gradle.
     */
    @Nullable
    HashCode getClassLoaderHash(ClassLoader classLoader);
}
