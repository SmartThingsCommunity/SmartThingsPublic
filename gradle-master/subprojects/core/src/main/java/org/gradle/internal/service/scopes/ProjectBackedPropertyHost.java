/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.internal.service.scopes;

import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.provider.PropertyHost;

import javax.annotation.Nullable;

class ProjectBackedPropertyHost implements PropertyHost {
    private final ProjectInternal project;

    public ProjectBackedPropertyHost(ProjectInternal project) {
        this.project = project;
    }

    @Nullable
    @Override
    public String beforeRead() {
        if (!project.getState().getExecuted()) {
            // Relies on the odd semantics of getExecuted() to allow access during afterEvaluate but not before
            return "configuration of " + project.getDisplayName() + " has not finished yet";
        } else {
            return null;
        }
    }
}
