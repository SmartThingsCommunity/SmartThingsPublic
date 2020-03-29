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

package org.gradle.internal.component;

import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvedVariantSet;
import org.gradle.internal.exceptions.Contextual;

@Contextual
public class VariantSelectionException extends RuntimeException {
    public VariantSelectionException(String message) {
        super(message);
    }

    public VariantSelectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static VariantSelectionException selectionFailed(ResolvedVariantSet producer, Throwable failure) {
        return new VariantSelectionException(String.format("Could not select a variant of %s that matches the consumer attributes.", producer.asDescribable().getDisplayName()), failure);
    }
}
