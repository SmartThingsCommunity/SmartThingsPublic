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

package org.gradle.plugins.javascript.base;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.internal.deprecation.DeprecationLogger;

public class JavaScriptBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        DeprecationLogger.deprecatePlugin("org.gradle.javascript-base")
            .willBeRemovedInGradle7()
            .withUpgradeGuideSection(5, "deprecated_plugins")
            .nagUser();
        project.getPluginManager().apply(BasePlugin.class);
        project.getExtensions().create(JavaScriptExtension.NAME, JavaScriptExtension.class);
        ((ExtensionAware) project.getRepositories()).getExtensions().create(
            JavaScriptRepositoriesExtension.NAME,
            JavaScriptRepositoriesExtension.class,
            project.getRepositories());
    }
}
