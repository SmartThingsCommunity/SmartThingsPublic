/*
 * Copyright 2019 the original author or authors.
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

import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.internal.tasks.properties.InspectionScheme;
import org.gradle.api.internal.tasks.properties.TypeMetadataStore;
import org.gradle.api.internal.tasks.properties.TypeScheme;
import org.gradle.internal.instantiation.InstantiationScheme;

public class ArtifactTransformParameterScheme implements TypeScheme {
    private final InstantiationScheme instantiationScheme;
    private final InspectionScheme inspectionScheme;

    public ArtifactTransformParameterScheme(InstantiationScheme instantiationScheme, InspectionScheme inspectionScheme) {
        this.instantiationScheme = instantiationScheme;
        this.inspectionScheme = inspectionScheme;
    }

    @Override
    public TypeMetadataStore getMetadataStore() {
        return inspectionScheme.getMetadataStore();
    }

    @Override
    public boolean appliesTo(Class<?> type) {
        return TransformParameters.class.isAssignableFrom(type);
    }

    public InstantiationScheme getInstantiationScheme() {
        return instantiationScheme;
    }

    public InspectionScheme getInspectionScheme() {
        return inspectionScheme;
    }
}
