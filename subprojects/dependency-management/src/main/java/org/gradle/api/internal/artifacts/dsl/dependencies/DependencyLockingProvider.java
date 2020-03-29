/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.api.internal.artifacts.dsl.dependencies;

import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.LockMode;
import org.gradle.api.provider.Property;

import java.io.File;
import java.util.Set;

public interface DependencyLockingProvider {

    DependencyLockingState loadLockState(String configurationName);

    void persistResolvedDependencies(String configurationName, Set<ModuleComponentIdentifier> resolutionResult, Set<ModuleComponentIdentifier> changingResolvedModules);

    Property<LockMode> getLockMode();

    void buildFinished();

    Property<File> getLockFile();
}
