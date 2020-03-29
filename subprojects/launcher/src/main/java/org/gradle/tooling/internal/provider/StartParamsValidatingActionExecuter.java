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

package org.gradle.tooling.internal.provider;

import org.gradle.StartParameter;
import org.gradle.initialization.BuildRequestContext;
import org.gradle.internal.invocation.BuildAction;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.launcher.exec.BuildActionExecuter;
import org.gradle.launcher.exec.BuildActionParameters;
import org.gradle.launcher.exec.BuildActionResult;

import java.io.File;

/**
 * Validates certain aspects of the start parameters, prior to starting a session using the parameters.
 */
public class StartParamsValidatingActionExecuter implements BuildActionExecuter<BuildActionParameters> {
    private final BuildActionExecuter<BuildActionParameters> delegate;

    public StartParamsValidatingActionExecuter(BuildActionExecuter<BuildActionParameters> delegate) {
        this.delegate = delegate;
    }

    @Override
    public BuildActionResult execute(BuildAction action, BuildRequestContext requestContext, BuildActionParameters actionParameters, ServiceRegistry contextServices) {
        StartParameter startParameter = action.getStartParameter();
        if (startParameter.getBuildFile() != null) {
            validateIsFileAndExists(startParameter.getBuildFile(), "build file");
        }
        if (startParameter.getProjectDir() != null) {
            if (!startParameter.getProjectDir().isDirectory()) {
                if (!startParameter.getProjectDir().exists()) {
                    throw new IllegalArgumentException(String.format("The specified project directory '%s' does not exist.", startParameter.getProjectDir()));
                }
                throw new IllegalArgumentException(String.format("The specified project directory '%s' is not a directory.", startParameter.getProjectDir()));
            }
        }
        if (startParameter.getSettingsFile() != null) {
            validateIsFileAndExists(startParameter.getSettingsFile(), "settings file");
        }
        for (File initScript : startParameter.getInitScripts()) {
            validateIsFileAndExists(initScript, "initialization script");
        }

        return delegate.execute(action, requestContext, actionParameters, contextServices);
    }

    private static void validateIsFileAndExists(File file, String fileType) {
        if (!file.isFile()) {
            if (!file.exists()) {
                throw new IllegalArgumentException(String.format("The specified %s '%s' does not exist.", fileType, file));
            }
            throw new IllegalArgumentException(String.format("The specified %s '%s' is not a file.", fileType, file));
        }
    }
}
