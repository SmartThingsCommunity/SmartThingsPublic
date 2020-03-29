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

package org.gradle.internal.execution.steps;

import org.gradle.internal.Try;
import org.gradle.internal.execution.Context;
import org.gradle.internal.execution.Result;
import org.gradle.internal.execution.Step;

public class CatchExceptionStep<C extends Context> implements Step<C, Result> {
    private final Step<? super C, ? extends Result> delegate;

    public CatchExceptionStep(Step<C, ? extends Result> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Result execute(C context) {
        try {
            return delegate.execute(context);
        } catch (Throwable failure) {
            return () -> Try.failure(failure);
        }
    }
}
