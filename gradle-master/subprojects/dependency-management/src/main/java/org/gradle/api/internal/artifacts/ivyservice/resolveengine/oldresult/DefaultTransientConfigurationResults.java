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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.oldresult;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.DependencyGraphNodeResult;

import java.util.Map;

public class DefaultTransientConfigurationResults implements TransientConfigurationResults {
    private final Map<Dependency, DependencyGraphNodeResult> firstLevelDependencies;
    private final DependencyGraphNodeResult root;

    public DefaultTransientConfigurationResults(DependencyGraphNodeResult root, Map<Dependency, DependencyGraphNodeResult> firstLevelDependencies) {
        this.firstLevelDependencies = firstLevelDependencies;
        this.root = root;
    }

    @Override
    public Map<Dependency, DependencyGraphNodeResult> getFirstLevelDependencies() {
        return firstLevelDependencies;
    }

    @Override
    public DependencyGraphNodeResult getRootNode() {
        return root;
    }
}
