/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.artifacts;

import org.gradle.api.Action;
import org.gradle.api.Describable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.internal.DelegatingDomainObjectSet;
import org.gradle.api.internal.artifacts.configurations.MutationValidator;
import org.gradle.api.internal.artifacts.dependencies.AbstractModuleDependency;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.Actions;
import org.gradle.internal.deprecation.DeprecatableConfiguration;
import org.gradle.internal.deprecation.DeprecationLogger;

import java.util.Collection;
import java.util.List;

public class DefaultDependencySet extends DelegatingDomainObjectSet<Dependency> implements DependencySet {
    private final Describable displayName;
    private final Configuration clientConfiguration;
    private final Action<? super ModuleDependency> mutationValidator;

    public DefaultDependencySet(Describable displayName, final Configuration clientConfiguration, DomainObjectSet<Dependency> backingSet) {
        super(backingSet);
        this.displayName = displayName;
        this.clientConfiguration = clientConfiguration;
        this.mutationValidator = toMutationValidator(clientConfiguration);
    }

    protected Action<ModuleDependency> toMutationValidator(final Configuration clientConfiguration) {
        return clientConfiguration instanceof MutationValidator ? new MutationValidationAction(clientConfiguration) : Actions.<ModuleDependency>doNothing();
    }

    @Override
    public String toString() {
        return displayName.getDisplayName();
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return clientConfiguration.getBuildDependencies();
    }

    @Override
    public boolean add(final Dependency o) {
        warnIfConfigurationIsDeprecated();
        if (o instanceof AbstractModuleDependency) {
            ((AbstractModuleDependency) o).addMutationValidator(mutationValidator);
        }
        return super.add(o);
    }

    private void warnIfConfigurationIsDeprecated() {
        List<String> alternatives = ((DeprecatableConfiguration) clientConfiguration).getDeclarationAlternatives();
        if (alternatives != null) {
            DeprecationLogger.deprecateConfiguration(clientConfiguration.getName()).forDependencyDeclaration().replaceWith(alternatives)
                .willBecomeAnErrorInGradle7()
                .withUpgradeGuideSection(5, "dependencies_should_no_longer_be_declared_using_the_compile_and_runtime_configurations")
                .nagUser();
        }
    }

    @Override
    public boolean addAll(Collection<? extends Dependency> dependencies) {
        boolean added = false;
        for (Dependency dependency : dependencies) {
            added |= add(dependency);
        }
        return added;
    }

    private static class MutationValidationAction implements Action<ModuleDependency> {
        private final Configuration clientConfiguration;

        public MutationValidationAction(Configuration clientConfiguration) {
            this.clientConfiguration = clientConfiguration;
        }

        @Override
        public void execute(ModuleDependency moduleDependency) {
            ((MutationValidator) clientConfiguration).validateMutation(MutationValidator.MutationType.DEPENDENCY_ATTRIBUTES);
        }
    }
}
