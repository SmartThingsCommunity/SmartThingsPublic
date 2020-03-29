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
package org.gradle.api.internal.artifacts.repositories.layout;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.resolver.PatternBasedResolver;

import java.net.URI;
import java.util.Set;

/**
 * A Repository Layout that applies the following patterns:
 * <ul>
 *     <li>Artifacts: $baseUri/{@value IvyArtifactRepository#IVY_ARTIFACT_PATTERN}</li>
 *     <li>Ivy: $baseUri/{@value IvyArtifactRepository#IVY_ARTIFACT_PATTERN}</li>
 * </ul>
 */
public class IvyRepositoryLayout extends AbstractRepositoryLayout {

    @Override
    public void apply(URI baseUri, PatternBasedResolver resolver) {
        if (baseUri == null) {
            return;
        }

        resolver.addArtifactLocation(baseUri, IvyArtifactRepository.IVY_ARTIFACT_PATTERN);
        resolver.addDescriptorLocation(baseUri, IvyArtifactRepository.IVY_ARTIFACT_PATTERN);
    }

    @Override
    public Set<String> getIvyPatterns() {
        return ImmutableSet.of(IvyArtifactRepository.IVY_ARTIFACT_PATTERN);
    }

    @Override
    public Set<String> getArtifactPatterns() {
        return ImmutableSet.of(IvyArtifactRepository.IVY_ARTIFACT_PATTERN);
    }
}
