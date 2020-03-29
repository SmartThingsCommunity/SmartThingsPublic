/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact;

import com.google.common.collect.Lists;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.transform.VariantSelector;
import org.gradle.api.specs.Spec;

import java.util.ArrayList;
import java.util.List;

import static org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvedArtifactSet.EMPTY;

public class DefaultVisitedArtifactResults implements VisitedArtifactsResults {
    private final ResolutionStrategy.SortOrder sortOrder;
    // Index of the artifact set == the id of the artifact set
    private final List<ArtifactSet> artifactsById;

    public DefaultVisitedArtifactResults(ResolutionStrategy.SortOrder sortOrder, List<ArtifactSet> artifactsById) {
        this.sortOrder = sortOrder;
        this.artifactsById = artifactsById;
    }

    @Override
    public SelectedArtifactResults select(Spec<? super ComponentIdentifier> componentFilter, VariantSelector selector) {
        if (artifactsById.isEmpty()) {
            return NoArtifactResults.INSTANCE;
        }

        List<ResolvedArtifactSet> resolvedArtifactSets = new ArrayList<ResolvedArtifactSet>(artifactsById.size());
        for (ArtifactSet artifactSet : artifactsById) {
            ResolvedArtifactSet resolvedArtifacts = artifactSet.select(componentFilter, selector);
            resolvedArtifactSets.add(resolvedArtifacts);
        }

        if (sortOrder == ResolutionStrategy.SortOrder.DEPENDENCY_FIRST) {
            resolvedArtifactSets = Lists.reverse(resolvedArtifactSets);
        }

        ResolvedArtifactSet composite = CompositeResolvedArtifactSet.of(resolvedArtifactSets);
        return new DefaultSelectedArtifactResults(sortOrder, composite, resolvedArtifactSets);
    }

    private static class NoArtifactResults implements SelectedArtifactResults {

        private static final NoArtifactResults INSTANCE = new NoArtifactResults();

        @Override
        public ResolvedArtifactSet getArtifacts() {
            return EMPTY;
        }

        @Override
        public ResolvedArtifactSet getArtifactsWithId(int id) {
            return EMPTY;
        }
    }

    private static class DefaultSelectedArtifactResults implements SelectedArtifactResults {
        private final ResolvedArtifactSet allArtifacts;
        private final ResolutionStrategy.SortOrder sortOrder;
        // Index of the artifact set == the id of the artifact set, but reversed when sort order is dependency first
        private final List<ResolvedArtifactSet> resolvedArtifactsById;

        DefaultSelectedArtifactResults(ResolutionStrategy.SortOrder sortOrder, ResolvedArtifactSet allArtifacts, List<ResolvedArtifactSet> resolvedArtifactsById) {
            this.sortOrder = sortOrder;
            this.allArtifacts = allArtifacts;
            this.resolvedArtifactsById = resolvedArtifactsById;
        }

        @Override
        public ResolvedArtifactSet getArtifacts() {
            return allArtifacts;
        }

        @Override
        public ResolvedArtifactSet getArtifactsWithId(int id) {
            if (sortOrder == ResolutionStrategy.SortOrder.DEPENDENCY_FIRST) {
                return resolvedArtifactsById.get(resolvedArtifactsById.size() - id - 1);
            }
            return resolvedArtifactsById.get(id);
        }
    }
}
