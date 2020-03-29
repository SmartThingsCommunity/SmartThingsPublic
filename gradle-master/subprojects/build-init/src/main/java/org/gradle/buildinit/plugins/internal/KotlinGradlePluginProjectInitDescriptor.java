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

public class KotlinGradlePluginProjectInitDescriptor extends JvmGradlePluginProjectInitDescriptor {
    private final TemplateLibraryVersionProvider libraryVersionProvider;

    public KotlinGradlePluginProjectInitDescriptor(TemplateLibraryVersionProvider libraryVersionProvider, DocumentationRegistry documentationRegistry) {
        super(documentationRegistry);
        this.libraryVersionProvider = libraryVersionProvider;
    }

    @Override
    public String getId() {
        return "kotlin-gradle-plugin";
    }

    @Override
    public Language getLanguage() {
        return Language.KOTLIN;
    }

    @Override
    public BuildInitTestFramework getDefaultTestFramework() {
        return BuildInitTestFramework.KOTLINTEST;
    }

    @Override
    public Set<BuildInitTestFramework> getTestFrameworks() {
        return ImmutableSet.of(BuildInitTestFramework.KOTLINTEST);
    }

    @Override
    public void generate(InitSettings settings, BuildScriptBuilder buildScriptBuilder, TemplateFactory templateFactory) {
        super.generate(settings, buildScriptBuilder, templateFactory);

        new KotlinProjectInitDescriptor(libraryVersionProvider).generate(buildScriptBuilder);
    }

    @Override
    protected TemplateOperation sourceTemplate(InitSettings settings, TemplateFactory templateFactory, String pluginId, String pluginClassName) {
        return templateFactory.fromSourceTemplate("plugin/kotlin/Plugin.kt.template", t -> {
            t.sourceSet("main");
            t.className(pluginClassName);
            t.binding("pluginId", pluginId);
        });
    }

    @Override
    protected TemplateOperation testTemplate(InitSettings settings, TemplateFactory templateFactory, String pluginId, String testClassName) {
        return templateFactory.fromSourceTemplate("plugin/kotlin/kotlintest/PluginTest.kt.template", t -> {
            t.sourceSet("test");
            t.className(testClassName);
            t.binding("pluginId", pluginId);
        });
    }

    @Override
    protected TemplateOperation functionalTestTemplate(InitSettings settings, TemplateFactory templateFactory, String pluginId, String testClassName) {
        return templateFactory.fromSourceTemplate("plugin/kotlin/kotlintest/PluginFunctionalTest.kt.template", t -> {
            t.sourceSet("functionalTest");
            t.className(testClassName);
            t.binding("pluginId", pluginId);
        });
    }
}
