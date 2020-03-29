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

package org.gradle.internal.build;

import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.api.internal.BuildDefinition;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A registry of all the builds present in a build tree.
 */
public interface BuildStateRegistry {
    /**
     * Creates the root build.
     */
    RootBuildState createRootBuild(BuildDefinition buildDefinition);

    /**
     * Attaches the root build.
     */
    void attachRootBuild(RootBuildState rootBuild);

    /**
     * Returns the root build of the build tree.
     *
     * @throws IllegalStateException When root build has not been attached.
     */
    RootBuildState getRootBuild() throws IllegalStateException;

    /**
     * Returns all children of the root build.
     */
    Collection<? extends IncludedBuildState> getIncludedBuilds();

    /**
     * Locates an included build by {@link BuildIdentifier}, if present. Fails if not an included build.
     */
    IncludedBuildState getIncludedBuild(BuildIdentifier buildIdentifier) throws IllegalArgumentException;

    /**
     * Locates a build. Fails if not present.
     */
    BuildState getBuild(BuildIdentifier buildIdentifier) throws IllegalArgumentException;

    /**
     * Notification that the settings have been loaded for the root build.
     *
     * This shouldn't be on this interface, as this is state for the root build that should be managed internally by the {@link RootBuildState} instance instead. This method is here to allow transition towards that structure.
     */
    void finalizeIncludedBuilds();

    /**
     * Notification that the root build is about to be configured.
     *
     * This shouldn't be on this interface, as this is state for the root build that should be managed internally by the {@link RootBuildState} instance instead. This method is here to allow transition towards that structure.
     */
    void beforeConfigureRootBuild();

    /**
     * Creates an included build. An included build is-a nested build whose projects and outputs are treated as part of the composite build.
     */
    IncludedBuildState addIncludedBuild(BuildDefinition buildDefinition);

    /**
     * Creates an implicit included build. An implicit build is-a nested build that is managed by Gradle and whose outputs are used by dependency resolution.
     */
    IncludedBuildState addImplicitIncludedBuild(BuildDefinition buildDefinition);

    /**
     * Creates a standalone nested build.
     */
    StandAloneNestedBuild addBuildSrcNestedBuild(BuildDefinition buildDefinition, BuildState owner);

    /**
     * Creates a new standalone nested build tree.
     */
    NestedRootBuild addNestedBuildTree(BuildDefinition buildDefinition, BuildState owner, @Nullable String buildName);
}
