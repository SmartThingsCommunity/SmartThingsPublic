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

package org.gradle.buildinit.plugins.internal;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.buildinit.plugins.internal.modifiers.BuildInitTestFramework;
import org.gradle.buildinit.plugins.internal.modifiers.Language;

import java.util.Set;

public class JavaGradlePluginProjectInitDescriptor extends JvmGradlePluginProjectInitDescriptor {
    private final TemplateLibraryVersionProvider libraryVersionProvider;

    public JavaGradlePluginProjectInitDescriptor(TemplateLibraryVersionProvider libraryVersionProvider, DocumentationRegistry documentationRegistry) {
        super(documentationRegistry);
        this.libraryVersionProvider = libraryVersionProvider;
    }

    @Override
    public String getId() {
        return "java-gradle-plugin";
    }

    @Override
    public Language getLanguage() {
        return Language.JAVA;
    }

    @Override
    public BuildInitTestFramework getDefaultTestFramework() {
        return BuildInitTestFramework.JUNIT;
    }

    @Override
    public Set<BuildInitTestFramework> getTestFrameworks() {
        return ImmutableSet.of(BuildInitTestFramework.JUNIT);
    }

    @Override
    public void generate(InitSettings settings, BuildScriptBuilder buildScriptBuilder, TemplateFactory templateFactory) {
        super.generate(settings, buildScriptBuilder, templateFactory);
        buildScriptBuilder.testImplementationDependency("Use JUnit test framework for unit tests", "junit:junit:" + libraryVersionProvider.getVersion("junit"));
    }

    @Override
    protected TemplateOperation sourceTemplate(InitSettings settings, TemplateFactory templateFactory, String pluginId, String pluginClassName) {
        return templateFactory.fromSourceTemplate("plugin/java/Plugin.java.template", t -> {
            t.sourceSet("main");
            t.className(pluginClassName);
            t.binding("pluginId", pluginId);
        });
    }

    @Override
    protected TemplateOperation testTemplate(InitSettings settings, TemplateFactory templateFactory, String pluginId, String testClassName) {
        return templateFactory.fromSourceTemplate("plugin/java/junit/PluginTest.java.template", t -> {
            t.sourceSet("test");
            t.className(testClassName);
            t.binding("pluginId", pluginId);
        });
    }

    @Override
    protected TemplateOperation functionalTestTemplate(InitSettings settings, TemplateFactory templateFactory, String pluginId, String testClassName) {
        return templateFactory.fromSourceTemplate("plugin/java/junit/PluginFunctionalTest.java.template", t -> {
            t.sourceSet("functionalTest");
            t.className(testClassName);
            t.binding("pluginId", pluginId);
        });
    }
}
