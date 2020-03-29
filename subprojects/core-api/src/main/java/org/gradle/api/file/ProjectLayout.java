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

package org.gradle.api.file;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.io.File;

/**
 * Provides access to several important locations for a project.
 *
 * <p>An instance of this type can be injected into a task, plugin or other object by annotating a public constructor or method with {@code javax.inject.Inject}. It is also available via {@link org.gradle.api.Project#getLayout()}.
 *
 * @since 4.1
 */
public interface ProjectLayout {
    /**
     * Returns the project directory.
     */
    Directory getProjectDirectory();

    /**
     * Returns the build directory for the project.
     */
    DirectoryProperty getBuildDirectory();

    /**
     * Creates a {@link RegularFile} provider whose location is calculated from the given {@link Provider}.
     */
    Provider<RegularFile> file(Provider<File> file);

    /**
     * Creates a {@link Directory} provider whose location is calculated from the given {@link Provider}.
     *
     * @since 6.0
     */
    Provider<Directory> dir(Provider<File> file);

    /**
     * <p>Creates a read-only {@link FileCollection} containing the given files, as defined by {@link Project#files(Object...)}.
     *
     * <p>This method can also be used to create an empty collection, but the collection may not be mutated later.</p>
     *
     * @param  paths The paths to the files. May be empty.
     * @return The file collection. Never returns null.
     * @since 4.8
     */
    FileCollection files(Object... paths);

    /**
     * <p>Returns a mutable {@link ConfigurableFileCollection} containing the given files, as defined by {@link Project#files(Object...)}.
     *
     * <p>This method can also be used to create an empty collection, which can later be mutated to add elements.</p>
     *
     * @param paths The paths to the files. May be empty.
     * @return The file collection. Never returns null.
     * @since 4.8
     * @deprecated Please use {@link ObjectFactory#fileCollection()} instead.
     */
    @Deprecated
    ConfigurableFileCollection configurableFiles(Object... paths);
}
