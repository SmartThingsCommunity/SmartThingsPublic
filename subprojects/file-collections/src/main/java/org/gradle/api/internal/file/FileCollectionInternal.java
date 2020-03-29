/*
 * Copyright 2015 the original author or authors.
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


import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.tasks.TaskDependencyContainer;

public interface FileCollectionInternal extends FileCollection, TaskDependencyContainer {
    /**
     * Visits the structure of this collection, that is, zero or more atomic sources of files.
     *
     * <p>The implementation should call the most specific methods on {@link FileCollectionStructureVisitor} that it is able to.</p>
     */
    void visitStructure(FileCollectionStructureVisitor visitor);

    /**
     * Some representation of a source of files.
     */
    interface Source {
    }

    /**
     * An opaque source of files.
     */
    Source OTHER = new Source() {
    };
}
