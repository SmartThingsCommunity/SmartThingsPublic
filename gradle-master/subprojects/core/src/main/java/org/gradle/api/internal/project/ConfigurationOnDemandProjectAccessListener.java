/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.internal.project;

import org.gradle.initialization.ProjectAccessListener;

public class ConfigurationOnDemandProjectAccessListener implements ProjectAccessListener {

    @Override
    public void beforeRequestingTaskByPath(ProjectInternal targetProject) {
        evaluateProjectAndDiscoverTasks(targetProject);
    }

    @Override
    public void beforeResolvingProjectDependency(ProjectInternal targetProject) {
        evaluateProjectAndDiscoverTasks(targetProject);
    }

    @Override
    public void onProjectAccess(String invocationDescription, Object invocationSource) {
        // NOOP
    }

    private synchronized void evaluateProjectAndDiscoverTasks(final ProjectInternal targetProject) {
        targetProject.evaluate();
        targetProject.getTasks().discoverTasks();
    }
}
