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

package org.gradle.gradlebuild.docs;

import groovy.lang.Closure;
import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RelativePath;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.build.docs.dsl.source.GenerateApiMapping;
import org.gradle.build.docs.dsl.source.GenerateDefaultImports;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradleUserManualPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        ObjectFactory objects = project.getObjects();

        GradleDocumentationExtension extension = project.getExtensions().getByType(GradleDocumentationExtension.class);
        generateDefaultImports(project, tasks, extension);
        generateUserManual(project, tasks, objects, layout, extension);

        checkXrefLinksInUserManualAreValid(layout, tasks, extension);
    }

    // TODO: This doesn't really make sense to be part of the user manual generation, but it's so tied up into it
    // it's left here for a future project.
    private void generateDefaultImports(Project project, TaskContainer tasks, GradleDocumentationExtension extension) {
        // TODO: This should be configured via the extension vs hardcoded in the plugin
        List<String> excludedPackages = new ArrayList<>();
        // These are part of the API, but not the DSL
        excludedPackages.add("org.gradle.tooling.**");
        excludedPackages.add("org.gradle.testfixtures.**");

        // Tweak the imports due to some inconsistencies introduced before we automated the default-imports generation
        excludedPackages.add("org.gradle.plugins.ide.eclipse.model");
        excludedPackages.add("org.gradle.plugins.ide.idea.model");
        excludedPackages.add("org.gradle.api.tasks.testing.logging");

        // TODO - rename some incubating types to remove collisions and then remove these exclusions
        excludedPackages.add("org.gradle.plugins.binaries.model");

        // Exclude classes that were moved in a different package but the deprecated ones are not removed yet
        excludedPackages.add("org.gradle.platform.base.test");

        Provider<Directory> generatedDirectory = extension.getUserManual().getStagingRoot().dir("generated");

        TaskProvider<GenerateApiMapping> apiMapping = tasks.register("apiMapping", GenerateApiMapping.class, task -> {
            task.getMetaDataFile().convention(extension.getDslReference().getGeneratedMetaDataFile());
            task.getMappingDestFile().convention(generatedDirectory.map(dir -> dir.file("api-mapping.txt")));
            task.getExcludedPackages().convention(excludedPackages);
        });
        TaskProvider<GenerateDefaultImports> defaultImports = tasks.register("defaultImports", GenerateDefaultImports.class, task -> {
            task.getMetaDataFile().convention(extension.getDslReference().getGeneratedMetaDataFile());
            task.getImportsDestFile().convention(generatedDirectory.map(dir -> dir.file("default-imports.txt")));
            task.getExcludedPackages().convention(excludedPackages);
        });
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        sourceSets.getByName("main", main -> {
            main.getOutput().dir(Collections.singletonMap("builtBy", Arrays.asList(apiMapping, defaultImports)), generatedDirectory);
        });

        extension.getUserManual().getResources().from(apiMapping);
        extension.getUserManual().getResources().from(defaultImports);
    }

    private void generateUserManual(Project project, TaskContainer tasks, ObjectFactory objects, ProjectLayout layout, GradleDocumentationExtension extension) {
        tasks.withType(AsciidoctorTask.class).configureEach(task -> {
            if (task.getName().equals("asciidoctor")) {
                // ignore this task
                task.setEnabled(false);
                return;
            }

            task.setSeparateOutputDirs(false);
            task.backends("html5");

            // TODO: Break the paths assumed here
            TaskInputs inputs = task.getInputs();
            inputs.files(extension.getCssFiles())
                    .withPropertyName("manual")
                    .withPathSensitivity(PathSensitivity.RELATIVE);
            inputs.dir("src/main/resources")
                    .withPropertyName("resources")
                    .withPathSensitivity(PathSensitivity.RELATIVE);
            inputs.dir(extension.getUserManual().getSnippets())
                    .withPropertyName("snippets")
                    .withPathSensitivity(PathSensitivity.RELATIVE);

            // TODO: Break the paths assumed here
            Map<String, Object> attributes = new HashMap<>();
            // TODO: This breaks the provider
            attributes.put("stylesdir", extension.getUserManual().getStagedDocumentation().dir("css").get().getAsFile().getAbsolutePath());
            attributes.put("stylesheet", "manual.css");
            attributes.put("doctype", "book");
            attributes.put("imagesdir", "img");
            attributes.put("nofooter", true);
            attributes.put("sectanchors", true);
            attributes.put("sectlinks", true);
            attributes.put("linkattrs", true);
            attributes.put("reproducible", "");
            attributes.put("docinfo", "");
            attributes.put("lang", "en-US");
            attributes.put("encoding", "utf-8");
            attributes.put("idprefix", "");
            attributes.put("website", "https://gradle.org");
            // TODO: This breaks the provider
            attributes.put("javaApi", extension.getJavadocs().getJavaApi().get().toString());
            attributes.put("jdkDownloadUrl", "https://jdk.java.net/");
            // TODO: This is coupled to extension.getJavadocs().getJavaApi()
            attributes.put("javadocReferenceUrl", "https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html");
            // TODO: This is coupled to extension.getJavadocs().getJavaApi()
            attributes.put("minJdkVersion", "8");

            attributes.put("antManual", "https://ant.apache.org/manual");
            attributes.put("docsUrl", "https://docs.gradle.org");
            attributes.put("guidesUrl", "https://guides.gradle.org");

            // TODO: This breaks if the version is changed later.
            attributes.put("gradleVersion", project.getVersion().toString());
            attributes.put("samplesPath", "snippets");
            task.attributes(attributes);
        });

        TaskProvider<GenerateDocInfo> generateDocinfo = tasks.register("generateDocInfo", GenerateDocInfo.class, task -> {
            task.getDocumentationFiles().from(extension.getUserManual().getRoot());
            task.getDocumentationRoot().convention(extension.getUserManual().getRoot());
            task.getDestinationDirectory().convention(layout.getBuildDirectory().dir("tmp/" + task.getName()));
        });

        TaskProvider<Sync> userguideFlattenSources = tasks.register("stageUserguideSource", Sync.class, task -> {
            task.setDuplicatesStrategy(DuplicatesStrategy.FAIL);

            // TODO: This doesn't allow adoc files to be generated?
            task.from(extension.getUserManual().getRoot(), sub -> {
                sub.include("**/*.adoc");
                // Flatten adocs into a single directory
                sub.eachFile(fcd -> fcd.setRelativePath(RelativePath.parse(true, fcd.getName())));
            });

            task.from(extension.getUserManual().getSnippets(), sub -> sub.into("snippets"));
            task.from(extension.getCssFiles(), sub -> sub.into("css"));
            task.from(extension.getUserManual().getRoot().dir("img"), sub -> {
                sub.include("**/*.png", "**/*.gif", "**/*.jpg");
                sub.into("img");
            });
            task.from(extension.getUserManual().getResources());

            task.from(generateDocinfo);

            // TODO: This should be available on a Copy task.
            DirectoryProperty flattenedAsciidocDirectory = project.getObjects().directoryProperty();
            flattenedAsciidocDirectory.set(extension.getUserManual().getStagingRoot().dir("raw"));
            task.getOutputs().dir(flattenedAsciidocDirectory);
            task.getExtensions().getExtraProperties().set("destinationDirectory", flattenedAsciidocDirectory);
            task.into(flattenedAsciidocDirectory);
        });

        TaskProvider<AsciidoctorTask> userguideSinglePage = tasks.register("userguideSinglePage", AsciidoctorTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Generates single-page user manual.");
            task.dependsOn(extension.getUserManual().getStagedDocumentation());
            task.onlyIf(t -> !extension.getQuickFeedback().get());

            task.sources(new Closure(null) {
                public Object doCall(Object ignore) {
                    ((PatternSet)this.getDelegate()).include("userguide_single.adoc");
                    return null;
                }
            });
            task.backends("pdf");

            // TODO: This breaks the provider
            task.setSourceDir(extension.getUserManual().getStagedDocumentation().get().getAsFile());

            // TODO: This breaks the provider
            task.setOutputDir(extension.getUserManual().getStagingRoot().dir("render-single").get().getAsFile());

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("source-highlighter", "coderay");
            attributes.put("toc", "macro");
            attributes.put("toclevels", 2);

            // TODO: This breaks if version is changed later
            attributes.put("groovyDslPath", "https://docs.gradle.org/" + project.getVersion() + "/dsl");
            attributes.put("javadocPath", "https://docs.gradle.org/" + project.getVersion() + "/javadoc");
            attributes.put("kotlinDslPath", "https://gradle.github.io/kotlin-dsl-docs/api");
            // Used by SampleIncludeProcessor from `gradle/dotorg-docs`
            // TODO: This breaks the provider
            attributes.put("samples-dir", extension.getUserManual().getStagedDocumentation().get().getAsFile()); // TODO:
            task.attributes(attributes);
        });

        TaskProvider<AsciidoctorTask> userguideMultiPage = tasks.register("userguideMultiPage", AsciidoctorTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Generates multi-page user manual.");
            task.dependsOn(extension.getUserManual().getStagedDocumentation());

            task.sources(new Closure(null) {
                public Object doCall(Object ignore) {
                    ((PatternSet)this.getDelegate()).include("**/*.adoc");
                    ((PatternSet)this.getDelegate()).exclude("javaProject*Layout.adoc");
                    ((PatternSet)this.getDelegate()).exclude("userguide_single.adoc");
                    ((PatternSet)this.getDelegate()).exclude("snippets/**/*.adoc");
                    return null;
                }
            });
            // TODO: This breaks the provider
            task.setSourceDir(extension.getUserManual().getStagedDocumentation().get().getAsFile());
            // TODO: This breaks the provider
            task.setOutputDir(extension.getUserManual().getStagingRoot().dir("render-multi").get().getAsFile());

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("icons", "font");
            attributes.put("source-highlighter", "prettify");
            attributes.put("toc", "auto");
            attributes.put("toclevels", 1);
            attributes.put("toc-title", "Contents");
            attributes.put("groovyDslPath", "../dsl");
            attributes.put("javadocPath", "../javadoc");
            attributes.put("kotlinDslPath", "https://gradle.github.io/kotlin-dsl-docs/api");
            // Used by SampleIncludeProcessor from `gradle/dotorg-docs`
            // TODO: This breaks the provider
            attributes.put("samples-dir", extension.getUserManual().getStagedDocumentation().get().getAsFile()); // TODO:
            task.attributes(attributes);
        });

        // Avoid overlapping outputs by copying exactly what we want from other intermediate tasks
        TaskProvider<Sync> userguide = tasks.register("userguide", Sync.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Stages rendered user manual documentation.");

            task.from(userguideSinglePage);
            task.from(userguideMultiPage);
            task.into(extension.getUserManual().getStagingRoot().dir("final"));
            // TODO: Eliminate this duplication with the flatten task
            task.from(extension.getUserManual().getRoot().dir("img"), sub -> {
                sub.include("**/*.png", "**/*.gif", "**/*.jpg");
                sub.into("img");
            });

            task.rename("userguide_single.pdf", "userguide.pdf");
        });

        extension.userManual(userManual -> {
            userManual.getRoot().convention(extension.getSourceRoot().dir("userguide"));
            userManual.getStagingRoot().convention(extension.getStagingRoot().dir("usermanual"));
            // TODO: These should be generated too
            userManual.getSnippets().convention(layout.getProjectDirectory().dir("src/snippets"));
            userManual.getStagedDocumentation().convention(userguideFlattenSources.flatMap(task -> (DirectoryProperty) task.getExtensions().getExtraProperties().get("destinationDirectory")));
            userManual.getRenderedDocumentation().from(userguide);
        });
    }

    private void checkXrefLinksInUserManualAreValid(ProjectLayout layout, TaskContainer tasks, GradleDocumentationExtension extension) {
        TaskProvider<FindBrokenInternalLinks> checkDeadInternalLinks = tasks.register("checkDeadInternalLinks", FindBrokenInternalLinks.class, task -> {
            task.getReportFile().convention(layout.getBuildDirectory().file("reports/dead-internal-links.txt"));
            task.getDocumentationRoot().convention(extension.getUserManual().getStagedDocumentation());
        });

        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME, task -> task.dependsOn(checkDeadInternalLinks));
    }
}
