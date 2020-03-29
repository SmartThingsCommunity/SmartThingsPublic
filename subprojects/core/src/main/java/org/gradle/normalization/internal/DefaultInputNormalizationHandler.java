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

package org.gradle.normalization.internal;

import org.gradle.api.Action;
import org.gradle.normalization.RuntimeClasspathNormalization;

public class DefaultInputNormalizationHandler implements InputNormalizationHandlerInternal {
    private final RuntimeClasspathNormalizationInternal runtimeClasspathNormalization;

    public DefaultInputNormalizationHandler(RuntimeClasspathNormalizationInternal runtimeClasspathNormalization) {
        this.runtimeClasspathNormalization = runtimeClasspathNormalization;
    }

    @Override
    public RuntimeClasspathNormalizationInternal getRuntimeClasspath() {
        return runtimeClasspathNormalization;
    }

    @Override
    public void runtimeClasspath(Action<? super RuntimeClasspathNormalization> configuration) {
        configuration.execute(getRuntimeClasspath());
    }
}
