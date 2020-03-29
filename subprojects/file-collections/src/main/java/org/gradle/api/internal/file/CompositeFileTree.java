/*
 * Copyright 2009 the original author or authors.
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

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.Cast;
import org.gradle.internal.Factory;

import java.util.List;

import static org.gradle.api.internal.file.AbstractFileTree.fileVisitorFrom;
import static org.gradle.util.ConfigureUtil.configure;

/**
 * A {@link FileTree} that contains the union of zero or more file trees.
 */
public abstract class CompositeFileTree extends CompositeFileCollection implements FileTreeInternal {
    public CompositeFileTree(Factory<PatternSet> patternSetFactory) {
        super(patternSetFactory);
    }

    public CompositeFileTree() {
        super();
    }

    @Override
    protected List<? extends FileTreeInternal> getSourceCollections() {
        return Cast.uncheckedNonnullCast(super.getSourceCollections());
    }

    @Override
    public FileTree plus(FileTree fileTree) {
        return new UnionFileTree(this, Cast.cast(FileTreeInternal.class, fileTree));
    }

    @Override
    public FileTree matching(final Closure filterConfigClosure) {
        return new FilteredFileTree(this, () -> {
            // For backwards compatibility, run the closure each time the file tree contents are queried
            return configure(filterConfigClosure, patternSetFactory.create());
        });
    }

    @Override
    public FileTree matching(final Action<? super PatternFilterable> filterConfigAction) {
        return new FilteredFileTree(this, () -> {
            // For backwards compatibility, run the action each time the file tree contents are queried
            PatternSet patternSet = patternSetFactory.create();
            filterConfigAction.execute(patternSet);
            return patternSet;
        });
    }

    @Override
    public FileTree matching(final PatternFilterable patterns) {
        return new FilteredFileTree(this, () -> {
            if (patterns instanceof PatternSet) {
                return (PatternSet) patterns;
            }
            PatternSet patternSet = patternSetFactory.create();
            patternSet.copyFrom(patterns);
            return patternSet;
        });
    }

    @Override
    public FileTree visit(Closure visitor) {
        return visit(fileVisitorFrom(visitor));
    }

    @Override
    public FileTree visit(Action<? super FileVisitDetails> visitor) {
        for (FileTree tree : getSourceCollections()) {
            tree.visit(visitor);
        }
        return this;
    }

    @Override
    public FileTree visit(FileVisitor visitor) {
        for (FileTree tree : getSourceCollections()) {
            tree.visit(visitor);
        }
        return this;
    }

    @Override
    public FileTree getAsFileTree() {
        return this;
    }

}
