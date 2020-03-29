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

package org.gradle.api.internal.tasks.compile.incremental.deps;

import java.util.Set;

public class ClassChanges {

    private final Set<String> modified;
    private final Set<String> addedClasses;

    public ClassChanges(Set<String> modified, Set<String> addedClasses) {
        this.modified = modified;
        this.addedClasses = addedClasses;
    }

    public Set<String> getModified() {
        return modified;
    }

    public Set<String> getAdded() {
        return addedClasses;
    }
}
