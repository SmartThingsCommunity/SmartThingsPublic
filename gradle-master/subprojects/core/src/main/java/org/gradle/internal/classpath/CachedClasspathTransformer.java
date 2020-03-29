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

package org.gradle.internal.classpath;

import java.net.URL;
import java.util.Collection;

/**
 * Represents a transformer that takes a given ClassPath and transforms it to a ClassPath with cached jars
 */
public interface CachedClasspathTransformer {
    /**
     * Transform a ClassPath object to a ClassPath with cached jars
     */
    ClassPath transform(ClassPath classPath);

    /**
     * Transform a collection of urls to a new collection where the file urls are cached jars
     */
    Collection<URL> transform(Collection<URL> urls);
}
