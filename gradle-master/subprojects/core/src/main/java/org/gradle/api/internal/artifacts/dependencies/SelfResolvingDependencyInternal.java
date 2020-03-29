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

package org.gradle.api.internal.artifacts.dependencies;

import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;

import javax.annotation.Nullable;

public interface SelfResolvingDependencyInternal extends SelfResolvingDependency {
    /**
     * Returns the id of the target component of this dependency, if known. If unknown, an arbitrary identifier is assigned to the files referenced by this dependency.
     */
    @Nullable
    ComponentIdentifier getTargetComponentId();
}
