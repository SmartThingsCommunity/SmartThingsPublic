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

package org.gradle.api.internal.artifacts.result;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.internal.Actions;
import org.gradle.internal.Factory;
import org.gradle.util.ConfigureUtil;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultResolutionResult implements ResolutionResult {

    private final Factory<ResolvedComponentResult> rootSource;
    private final AttributeContainer requestedAttributes;

    public DefaultResolutionResult(Factory<ResolvedComponentResult> rootSource, AttributeContainer requestedAttributes) {
        assert rootSource != null;
        this.rootSource = rootSource;
        this.requestedAttributes = requestedAttributes;
    }

    @Override
    public ResolvedComponentResult getRoot() {
        return rootSource.create();
    }

    @Override
    public Set<? extends DependencyResult> getAllDependencies() {
        final Set<DependencyResult> out = new LinkedHashSet<DependencyResult>();
        allDependencies(out::add);
        return out;
    }

    @Override
    public void allDependencies(Action<? super DependencyResult> action) {
        eachElement(getRoot(), Actions.doNothing(), action, new HashSet<ResolvedComponentResult>());
    }

    @Override
    public void allDependencies(final Closure closure) {
        allDependencies(ConfigureUtil.configureUsing(closure));
    }

    private void eachElement(ResolvedComponentResult node,
                             Action<? super ResolvedComponentResult> moduleAction, Action<? super DependencyResult> dependencyAction,
                             Set<ResolvedComponentResult> visited) {
        if (!visited.add(node)) {
            return;
        }
        moduleAction.execute(node);
        for (DependencyResult d : node.getDependencies()) {
            dependencyAction.execute(d);
            if (d instanceof ResolvedDependencyResult) {
                eachElement(((ResolvedDependencyResult) d).getSelected(), moduleAction, dependencyAction, visited);
            }
        }
    }

    @Override
    public Set<ResolvedComponentResult> getAllComponents() {
        final Set<ResolvedComponentResult> out = new LinkedHashSet<ResolvedComponentResult>();
        eachElement(getRoot(), Actions.doNothing(), Actions.doNothing(), out);
        return out;
    }

    @Override
    public void allComponents(final Action<? super ResolvedComponentResult> action) {
        eachElement(getRoot(), action, Actions.doNothing(), new HashSet<ResolvedComponentResult>());
    }

    @Override
    public void allComponents(final Closure closure) {
        allComponents(ConfigureUtil.configureUsing(closure));
    }

    @Override
    public AttributeContainer getRequestedAttributes() {
        return requestedAttributes;
    }

}
