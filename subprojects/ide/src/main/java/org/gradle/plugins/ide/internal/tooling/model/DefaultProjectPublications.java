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

package org.gradle.plugins.ide.internal.tooling.model;

import org.gradle.tooling.internal.gradle.DefaultProjectIdentifier;
import org.gradle.tooling.internal.gradle.GradleProjectIdentity;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class DefaultProjectPublications implements Serializable, GradleProjectIdentity {
    private List<DefaultGradlePublication> publications;
    private DefaultProjectIdentifier projectIdentifier;

    public List<DefaultGradlePublication> getPublications() {
        return publications;
    }

    public DefaultProjectPublications setPublications(List<DefaultGradlePublication> publications) {
        this.publications = publications;
        return this;
    }

    public DefaultProjectIdentifier getProjectIdentifier() {
        return projectIdentifier;
    }

    @Override
    public String getProjectPath() {
        return projectIdentifier.getProjectPath();
    }

    @Override
    public File getRootDir() {
        return projectIdentifier.getBuildIdentifier().getRootDir();
    }

    public DefaultProjectPublications setProjectIdentifier(DefaultProjectIdentifier projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
        return this;
    }
}
