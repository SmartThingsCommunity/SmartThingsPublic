/*
 * Copyright 2016 the original author or authors.
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

import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.internal.component.local.model.LocalComponentMetadata;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A provider of dependency resolution metadata for locally produced components.
 * These components may be produced within the same project, another project in the same multi-project build,
 * or in another build within a composite.
 *
 * <p>In general, you should be using {@link LocalComponentRegistry} instead of this type.</p>
 */
@ThreadSafe
public interface LocalComponentProvider {
    /**
     * @return The component metadata for the supplied identifier.
     */
    @Nullable
    LocalComponentMetadata getComponent(ProjectComponentIdentifier projectIdentifier);
}
