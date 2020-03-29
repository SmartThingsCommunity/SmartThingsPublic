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

import org.gradle.api.Action;
import org.gradle.api.internal.tasks.NodeExecutionContext;

/**
 * A series of {@link TransformationStep}s.
 */
public class TransformationChain implements Transformation {

    private final Transformation first;
    private final Transformation second;
    private final int stepsCount;

    public TransformationChain(Transformation first, Transformation second) {
        this.first = first;
        this.second = second;
        this.stepsCount = first.stepsCount() + second.stepsCount();
    }

    @Override
    public boolean endsWith(Transformation otherTransform) {
        int otherStepsCount = otherTransform.stepsCount();
        if (otherStepsCount > this.stepsCount) {
            return false;
        } else if (otherStepsCount == 1) {
            return second == otherTransform;
        }

        TransformationChain otherChain = (TransformationChain) otherTransform;
        if (otherChain.second != second) {
            return false;
        } else {
            return first.endsWith(otherChain.first);
        }
    }

    @Override
    public int stepsCount() {
        return stepsCount;
    }

    @Override
    public CacheableInvocation<TransformationSubject> createInvocation(TransformationSubject subjectToTransform, ExecutionGraphDependenciesResolver dependenciesResolver, NodeExecutionContext context) {
        CacheableInvocation<TransformationSubject> invocation = first.createInvocation(subjectToTransform, dependenciesResolver, context);
        return invocation.flatMap(intermediate -> second.createInvocation(intermediate, dependenciesResolver, context));
    }

    @Override
    public boolean requiresDependencies() {
        return first.requiresDependencies() || second.requiresDependencies();
    }

    @Override
    public String getDisplayName() {
        return first.getDisplayName() + " -> " + second.getDisplayName();
    }

    @Override
    public void visitTransformationSteps(Action<? super TransformationStep> action) {
        first.visitTransformationSteps(action);
        second.visitTransformationSteps(action);
    }
}
