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

package org.gradle.api.internal.file;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.internal.file.collections.MinimalFileSet;
import org.gradle.api.internal.provider.AbstractMappingProvider;
import org.gradle.api.internal.provider.PropertyHost;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.Factory;
import org.gradle.internal.deprecation.DeprecationLogger;

import java.io.File;

public class DefaultProjectLayout implements ProjectLayout, TaskFileVarFactory {
    private final Directory projectDir;
    private final DirectoryProperty buildDir;
    private final FileResolver fileResolver;
    private final TaskDependencyFactory taskDependencyFactory;
    private final Factory<PatternSet> patternSetFactory;
    private final PropertyHost propertyHost;
    private final FileCollectionFactory fileCollectionFactory;
    private final FileFactory fileFactory;

    public DefaultProjectLayout(File projectDir, FileResolver fileResolver, TaskDependencyFactory taskDependencyFactory, Factory<PatternSet> patternSetFactory, PropertyHost propertyHost, FileCollectionFactory fileCollectionFactory, FilePropertyFactory filePropertyFactory, FileFactory fileFactory) {
        this.fileResolver = fileResolver;
        this.taskDependencyFactory = taskDependencyFactory;
        this.patternSetFactory = patternSetFactory;
        this.propertyHost = propertyHost;
        this.fileCollectionFactory = fileCollectionFactory;
        this.fileFactory = fileFactory;
        this.projectDir = fileFactory.dir(projectDir);
        this.buildDir = filePropertyFactory.newDirectoryProperty().fileValue(fileResolver.resolve(Project.DEFAULT_BUILD_DIR_NAME));
    }

    @Override
    public Directory getProjectDirectory() {
        return projectDir;
    }

    @Override
    public DirectoryProperty getBuildDirectory() {
        return buildDir;
    }

    @Override
    public ConfigurableFileCollection newInputFileCollection(Task consumer) {
        return new CachingTaskInputFileCollection(fileResolver, patternSetFactory, taskDependencyFactory, propertyHost);
    }

    @Override
    public FileCollection newCalculatedInputFileCollection(Task consumer, MinimalFileSet calculatedFiles, FileCollection... inputs) {
        return new CalculatedTaskInputFileCollection(consumer.getPath(), calculatedFiles, inputs);
    }

    @Override
    public Provider<RegularFile> file(Provider<File> provider) {
        return new AbstractMappingProvider<RegularFile, File>(RegularFile.class, Providers.internal(provider)) {
            @Override
            protected RegularFile mapValue(File file) {
                return fileFactory.file(fileResolver.resolve(file));
            }
        };
    }

    @Override
    public Provider<Directory> dir(Provider<File> provider) {
        return new AbstractMappingProvider<Directory, File>(Directory.class, Providers.internal(provider)) {
            @Override
            protected Directory mapValue(File file) {
                return fileFactory.dir(fileResolver.resolve(file));
            }
        };
    }

    @Override
    public FileCollection files(Object... paths) {
        return fileCollectionFactory.resolving(paths);
    }

    @Override
    public ConfigurableFileCollection configurableFiles(Object... files) {
        DeprecationLogger.deprecateMethod(ProjectLayout.class, "configurableFiles()").replaceWith("ObjectFactory.fileCollection()")
            .willBeRemovedInGradle7()
            .withUserManual("lazy_configuration", "property_files_api_reference")
            .nagUser();
        return fileCollectionFactory.configurableFiles().from(files);
    }

    /**
     * A temporary home. Should be on the public API somewhere
     */
    public void setBuildDirectory(Object value) {
        buildDir.set(fileResolver.resolve(value));
    }
}
