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
package org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder;

import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.internal.artifacts.ComponentSelectorConverter;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.ComponentSelectionDescriptorInternal;
import org.gradle.internal.Describables;
import org.gradle.internal.component.model.DependencyMetadata;
import org.gradle.internal.component.model.ForcingDependencyMetadata;
import org.gradle.internal.component.model.LocalOriginDependencyMetadata;
import org.gradle.internal.resolve.ModuleVersionResolveException;

import java.util.List;

import static org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.ComponentSelectionReasons.BY_ANCESTOR;
import static org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.ComponentSelectionReasons.CONSTRAINT;
import static org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.ComponentSelectionReasons.FORCED;
import static org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.ComponentSelectionReasons.REQUESTED;

class DependencyState {
    private final ComponentSelector requested;
    private final DependencyMetadata dependency;
    private final List<ComponentSelectionDescriptorInternal> ruleDescriptors;
    private final ComponentSelectorConverter componentSelectorConverter;
    private final int hashCode;

    private ModuleIdentifier moduleIdentifier;
    public ModuleVersionResolveException failure;
    private boolean reasonsAlreadyAdded;

    DependencyState(DependencyMetadata dependency, ComponentSelectorConverter componentSelectorConverter) {
        this(dependency, dependency.getSelector(), null, componentSelectorConverter);
    }

    private DependencyState(DependencyMetadata dependency, ComponentSelector requested, List<ComponentSelectionDescriptorInternal> ruleDescriptors, ComponentSelectorConverter componentSelectorConverter) {
        this.dependency = dependency;
        this.requested = requested;
        this.ruleDescriptors = ruleDescriptors;
        this.componentSelectorConverter = componentSelectorConverter;
        this.hashCode = computeHashCode();
    }

    private int computeHashCode() {
        int hashCode = dependency.hashCode();
        hashCode = 31 * hashCode + requested.hashCode();
        return hashCode;
    }

    public ComponentSelector getRequested() {
        return requested;
    }

    public DependencyMetadata getDependency() {
        return dependency;
    }

    public ModuleIdentifier getModuleIdentifier() {
        if (moduleIdentifier == null) {
            moduleIdentifier = componentSelectorConverter.getModule(dependency.getSelector());
        }
        return moduleIdentifier;
    }

    public DependencyState withTarget(ComponentSelector target, List<ComponentSelectionDescriptorInternal> ruleDescriptors) {
        DependencyMetadata targeted = dependency.withTarget(target);
        return new DependencyState(targeted, requested, ruleDescriptors, componentSelectorConverter);
    }

    /**
     * Descriptor for any rules that modify this DependencyState from the original.
     */
    public List<ComponentSelectionDescriptorInternal> getRuleDescriptors() {
        return ruleDescriptors;
    }

    public boolean isForced() {
        if (ruleDescriptors != null) {
            for (ComponentSelectionDescriptorInternal ruleDescriptor : ruleDescriptors) {
                if (ruleDescriptor.isEquivalentToForce()) {
                    return true;
                }
            }
        }
        return isDependencyForced();
    }

    private boolean isDependencyForced() {
        return dependency instanceof ForcingDependencyMetadata && ((ForcingDependencyMetadata) dependency).isForce();
    }

    public boolean isFromLock() {
        return dependency instanceof LocalOriginDependencyMetadata && ((LocalOriginDependencyMetadata) dependency).isFromLock();
    }

    void addSelectionReasons(List<ComponentSelectionDescriptorInternal> reasons) {
        if (reasonsAlreadyAdded) {
            return;
        }
        reasonsAlreadyAdded = true;
        addMainReason(reasons);

        if (ruleDescriptors != null) {
            addRuleDescriptors(reasons);
        }
        if (isDependencyForced()) {
            maybeAddReason(reasons, FORCED);
        }
    }

    private void addRuleDescriptors(List<ComponentSelectionDescriptorInternal> reasons) {
        for (ComponentSelectionDescriptorInternal descriptor : ruleDescriptors) {
            maybeAddReason(reasons, descriptor);
        }
    }

    private void addMainReason(List<ComponentSelectionDescriptorInternal> reasons) {
        ComponentSelectionDescriptorInternal dependencyDescriptor;
        if (reasons != null && reasons.contains(BY_ANCESTOR)) {
            dependencyDescriptor = BY_ANCESTOR;
        } else {
            dependencyDescriptor = dependency.isConstraint() ? CONSTRAINT : REQUESTED;
        }
        String reason = dependency.getReason();
        if (reason != null) {
            dependencyDescriptor = dependencyDescriptor.withDescription(Describables.of(reason));
        }
        maybeAddReason(reasons, dependencyDescriptor);
    }

    private static void maybeAddReason(List<ComponentSelectionDescriptorInternal> reasons, ComponentSelectionDescriptorInternal reason) {
        if (reasons.isEmpty()) {
            reasons.add(reason);
        } else if (isNewReason(reasons, reason)) {
            reasons.add(reason);
        }
    }

    private static boolean isNewReason(List<ComponentSelectionDescriptorInternal> reasons, ComponentSelectionDescriptorInternal reason) {
        return (reasons.size() == 1 && !reason.equals(reasons.get(0)))
            || !reasons.contains(reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // This is a performance optimization, dependency states are deduplicated
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
