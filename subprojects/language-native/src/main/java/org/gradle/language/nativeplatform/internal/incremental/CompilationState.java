/*
 * Copyright 2013 the original author or authors.
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
package org.gradle.language.nativeplatform.internal.incremental;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Set;

/**
 * An immutable snapshot of compilation state.
 */
public class CompilationState {
    private final ImmutableMap<File, SourceFileState> fileStates;

    public CompilationState(ImmutableMap<File, SourceFileState> fileStates) {
        this.fileStates = fileStates;
    }

    public CompilationState() {
        fileStates = ImmutableMap.of();
    }

    public Set<File> getSourceInputs() {
        return fileStates.keySet();
    }

    public ImmutableMap<File, SourceFileState> getFileStates() {
        return fileStates;
    }

    public SourceFileState getState(File file) {
        return fileStates.get(file);
    }
}
