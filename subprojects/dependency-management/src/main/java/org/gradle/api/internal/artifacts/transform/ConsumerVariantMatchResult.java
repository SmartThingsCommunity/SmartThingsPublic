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

package org.gradle.api.internal.artifacts.transform;

import com.google.common.collect.Lists;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.attributes.ImmutableAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ConsumerVariantMatchResult {
    private int minDepth;
    private final List<ConsumerVariant> matches;

    ConsumerVariantMatchResult(int estimateSize) {
        matches = Lists.newArrayListWithExpectedSize(estimateSize);
    }

    private ConsumerVariantMatchResult(ConsumerVariantMatchResult other) {
        this.minDepth = other.minDepth;
        this.matches = Collections.unmodifiableList(other.matches);
    }

    public void matched(ImmutableAttributes output, Transformation transformation, int depth) {
        // Collect only the shortest paths
        if (minDepth == 0) {
            minDepth = depth;
        } else if (depth < minDepth) {
            matches.clear();
            minDepth = depth;
        } else if (depth > minDepth) {
            return;
        }
        matches.add(new ConsumerVariant(output, transformation, depth));
    }

    public boolean hasMatches() {
        return !matches.isEmpty();
    }

    public Collection<ConsumerVariant> getMatches() {
        return matches;
    }

    public ConsumerVariantMatchResult asImmutable() {
        return new ConsumerVariantMatchResult(this);
    }

    public static class ConsumerVariant {
        final AttributeContainerInternal attributes;
        final Transformation transformation;
        final int depth;

        public ConsumerVariant(AttributeContainerInternal attributes, Transformation transformation, int depth) {
            this.attributes = attributes;
            this.transformation = transformation;
            this.depth = depth;
        }
    }
}
