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

package org.gradle.launcher.cli.converter;

import org.gradle.internal.buildoption.BuildOption;
import org.gradle.launcher.daemon.configuration.DaemonBuildOptions;
import org.gradle.launcher.daemon.configuration.DaemonParameters;

import java.util.List;
import java.util.Map;

public class PropertiesToDaemonParametersConverter {
    private List<BuildOption<DaemonParameters>> buildOptions = DaemonBuildOptions.get();

    public void convert(Map<String, String> properties, DaemonParameters target) {
        for (BuildOption<DaemonParameters> option : buildOptions) {
            option.applyFromProperty(properties, target);
        }
    }
}
