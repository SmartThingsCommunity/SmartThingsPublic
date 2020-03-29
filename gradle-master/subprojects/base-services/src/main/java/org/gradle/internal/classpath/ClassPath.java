/*
 * Copyright 2012 the original author or authors.
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

import org.gradle.api.specs.Spec;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * An immutable classpath.
 */
public interface ClassPath {

    ClassPath EMPTY = new DefaultClassPath();

    boolean isEmpty();

    List<URI> getAsURIs();

    List<File> getAsFiles();

    List<URL> getAsURLs();

    URL[] getAsURLArray();

    ClassPath plus(Collection<File> classPath);

    ClassPath plus(ClassPath classPath);

    ClassPath removeIf(Spec<? super File> filter);
}
