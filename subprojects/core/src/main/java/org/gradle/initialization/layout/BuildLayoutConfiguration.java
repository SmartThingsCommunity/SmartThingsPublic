/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.initialization.layout;

import org.gradle.StartParameter;
import org.gradle.TaskExecutionRequest;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.internal.scan.UsedByScanPlugin;

import java.io.File;

/**
 * Configuration which affects the (static) layout of a build.
 */
@UsedByScanPlugin
public class BuildLayoutConfiguration {
    private File currentDir;
    private boolean searchUpwards;
    private final File settingsFile;
    private final boolean useEmptySettings;

    public BuildLayoutConfiguration(StartParameter startParameter) {
        currentDir = startParameter.getCurrentDir();
        searchUpwards = ((StartParameterInternal)startParameter).isSearchUpwardsWithoutDeprecationWarning() && !isInitTaskRequested(startParameter);
        settingsFile = startParameter.getSettingsFile();
        useEmptySettings = ((StartParameterInternal)startParameter).isUseEmptySettingsWithoutDeprecationWarning();
    }

    private boolean isInitTaskRequested(StartParameter startParameter) {
        if (startParameter.getTaskNames().contains("init")) {
            return true;
        }
        for (TaskExecutionRequest request : startParameter.getTaskRequests()) {
            if (request.getArgs().contains("init")) {
                return true;
            }
        }
        return false;
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public boolean isSearchUpwards() {
        return searchUpwards;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public boolean isUseEmptySettings() {
        return useEmptySettings;
    }
}
