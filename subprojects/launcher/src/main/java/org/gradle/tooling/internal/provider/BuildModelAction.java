/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.tooling.internal.provider;

import org.gradle.api.internal.StartParameterInternal;
import org.gradle.tooling.internal.protocol.ModelIdentifier;
import org.gradle.internal.build.event.BuildEventSubscriptions;

public class BuildModelAction extends SubscribableBuildAction {
    private final StartParameterInternal startParameter;
    private final String modelName;
    private final boolean runTasks;

    public BuildModelAction(StartParameterInternal startParameter, String modelName, boolean runTasks, BuildEventSubscriptions clientSubscriptions) {
        super(clientSubscriptions);
        this.startParameter = startParameter;
        this.modelName = modelName;
        this.runTasks = runTasks;
    }

    @Override
    public StartParameterInternal getStartParameter() {
        return startParameter;
    }

    public String getModelName() {
        return modelName;
    }

    public boolean isRunTasks() {
        return runTasks;
    }

    public boolean isModelRequest() {
        return !ModelIdentifier.NULL_MODEL.equals(modelName);
    }

}
