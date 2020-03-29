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
package org.gradle.api.internal.artifacts.ivyservice.projectmodule;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.component.ComponentWithVariants;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.execution.ProjectConfigurer;
import org.gradle.internal.logging.text.TreeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A service that will resolve a ProjectDependency into publication coordinates, to use for publishing.
 * For now is a simple implementation, but at some point could utilise components in the dependency project, usage in the referencing project, etc.
 */
public class DefaultProjectDependencyPublicationResolver implements ProjectDependencyPublicationResolver {
    private final ProjectPublicationRegistry publicationRegistry;
    private final ProjectConfigurer projectConfigurer;

    public DefaultProjectDependencyPublicationResolver(ProjectPublicationRegistry publicationRegistry, ProjectConfigurer projectConfigurer) {
        this.publicationRegistry = publicationRegistry;
        this.projectConfigurer = projectConfigurer;
    }

    @Override
    public <T> T resolve(Class<T> coordsType, ProjectDependency dependency) {
        // Could probably apply some caching and some immutable types

        ProjectInternal dependencyProject = (ProjectInternal) dependency.getDependencyProject();
        // Ensure target project is configured
        projectConfigurer.configureFully(dependencyProject);

        List<ProjectComponentPublication> publications = new ArrayList<ProjectComponentPublication>();
        for (ProjectComponentPublication publication : publicationRegistry.getPublications(ProjectComponentPublication.class, dependencyProject.getIdentityPath())) {
            if (!publication.isLegacy() && publication.getCoordinates(coordsType) != null) {
                publications.add(publication);
            }
        }

        if (publications.isEmpty()) {
            // Project has no publications: simply use the project name in place of the dependency name
            if (coordsType.isAssignableFrom(ModuleVersionIdentifier.class)) {
                return coordsType.cast(DefaultModuleVersionIdentifier.newId(dependency.getGroup(), dependencyProject.getName(), dependency.getVersion()));
            }
            throw new UnsupportedOperationException(String.format("Could not find any publications of type %s in %s.", coordsType.getSimpleName(), dependencyProject.getDisplayName()));
        }

        // Select all entry points. An entry point is a publication that does not contain a component whose parent is also published
        Set<SoftwareComponent> ignored = new HashSet<SoftwareComponent>();
        for (ProjectComponentPublication publication : publications) {
            if (publication.getComponent() != null && publication.getComponent() instanceof ComponentWithVariants) {
                ComponentWithVariants parent = (ComponentWithVariants) publication.getComponent();
                ignored.addAll(parent.getVariants());
            }
        }
        Set<ProjectComponentPublication> topLevel = new LinkedHashSet<ProjectComponentPublication>();
        Set<ProjectComponentPublication> topLevelWithComponent = new LinkedHashSet<ProjectComponentPublication>();
        for (ProjectComponentPublication publication : publications) {
            if (!publication.isAlias() && (publication.getComponent() == null || !ignored.contains(publication.getComponent()))) {
                topLevel.add(publication);
                if (publication.getComponent() != null) {
                    topLevelWithComponent.add(publication);
                }
            }
        }

        if (topLevelWithComponent.size() == 1) {
            return topLevelWithComponent.iterator().next().getCoordinates(coordsType);
        }

        // See if all entry points have the same identifier
        Iterator<ProjectComponentPublication> iterator = topLevel.iterator();
        T candidate = iterator.next().getCoordinates(coordsType);
        while (iterator.hasNext()) {
            T alternative = iterator.next().getCoordinates(coordsType);
            if (!candidate.equals(alternative)) {
                TreeFormatter formatter = new TreeFormatter();
                formatter.node("Publishing is not able to resolve a dependency on a project with multiple publications that have different coordinates.");
                formatter.node("Found the following publications in " + dependencyProject.getDisplayName());
                formatter.startChildren();
                for (ProjectComponentPublication publication : topLevel) {
                    formatter.node(publication.getDisplayName().getCapitalizedDisplayName() + " with coordinates " + publication.getCoordinates(coordsType));
                }
                formatter.endChildren();
                throw new UnsupportedOperationException(formatter.toString());
            }
        }
        return candidate;
    }
}
