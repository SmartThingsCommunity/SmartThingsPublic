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

package org.gradle.buildinit.plugins.internal;

import org.gradle.api.internal.DocumentationRegistry;

public class SimpleGlobalFilesBuildSettingsDescriptor implements BuildContentGenerator {
    private final DocumentationRegistry documentationRegistry;
    private final BuildScriptBuilderFactory scriptBuilderFactory;

    public SimpleGlobalFilesBuildSettingsDescriptor(BuildScriptBuilderFactory scriptBuilderFactory, DocumentationRegistry documentationRegistry) {
        this.scriptBuilderFactory = scriptBuilderFactory;
        this.documentationRegistry = documentationRegistry;
    }

    @Override
    public void generate(InitSettings settings) {
        scriptBuilderFactory.script(settings.getDsl(), "settings")
            .fileComment(
                "The settings file is used to specify which projects to include in your build.\n\n"
                    + "Detailed information about configuring a multi-project build in Gradle can be found\n"
                    + "in the user manual at " + documentationRegistry.getDocumentationFor("multi_project_builds"))
            .propertyAssignment(null, "rootProject.name", settings.getProjectName())
            .create()
            .generate();
    }
}
