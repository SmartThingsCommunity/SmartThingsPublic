/*
 * Copyright 2008 the original author or authors.
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
package org.gradle.api.tasks.diagnostics.internal.dependencies;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.tasks.diagnostics.internal.DependencyReportRenderer;
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;
import org.gradle.api.tasks.diagnostics.internal.graph.DependencyGraphsRenderer;
import org.gradle.api.tasks.diagnostics.internal.graph.NodeRenderer;
import org.gradle.api.tasks.diagnostics.internal.graph.SimpleNodeRenderer;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.UnresolvableConfigurationResult;
import org.gradle.initialization.StartParameterBuildOptions;
import org.gradle.internal.deprecation.DeprecatableConfiguration;
import org.gradle.internal.graph.GraphRenderer;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.util.GUtil;

import java.io.IOException;
import java.util.Collections;

import static org.gradle.internal.logging.text.StyledTextOutput.Style.Description;
import static org.gradle.internal.logging.text.StyledTextOutput.Style.Identifier;
import static org.gradle.internal.logging.text.StyledTextOutput.Style.Info;
import static org.gradle.internal.logging.text.StyledTextOutput.Style.UserInput;

/**
 * Simple dependency graph renderer that emits an ASCII tree.
 */
public class AsciiDependencyReportRenderer extends TextReportRenderer implements DependencyReportRenderer {
    private final ConfigurationAction configurationAction = new ConfigurationAction();
    private boolean hasConfigs;
    private GraphRenderer renderer;

    DependencyGraphsRenderer dependencyGraphRenderer;

    @Override
    public void startProject(Project project) {
        super.startProject(project);
        prepareVisit();
    }

    void prepareVisit() {
        hasConfigs = false;
        renderer = new GraphRenderer(getTextOutput());
        dependencyGraphRenderer = new DependencyGraphsRenderer(getTextOutput(), renderer, NodeRenderer.NO_OP, new SimpleNodeRenderer());
    }

    @Override
    public void completeProject(Project project) {
        if (!hasConfigs) {
            getTextOutput().withStyle(Info).println("No configurations");
        }
        super.completeProject(project);
    }

    @Override
    public void startConfiguration(final Configuration configuration) {
        if (hasConfigs) {
            getTextOutput().println();
        }
        hasConfigs = true;
        configurationAction.setConfiguration(configuration);
        renderer.visit(configurationAction, true);

    }

    private String getDescription(Configuration configuration) {
        return GUtil.isTrue(configuration.getDescription()) ? " - " + configuration.getDescription() : "";
    }

    @Override
    public void completeConfiguration(Configuration configuration) {}

    @Override
    public void render(Configuration configuration) throws IOException {
        if (canBeResolved(configuration)) {
            ResolutionResult result = configuration.getIncoming().getResolutionResult();
            RenderableDependency root = new RenderableModuleResult(result.getRoot());
            renderNow(root);
        } else {
            renderNow(new UnresolvableConfigurationResult(configuration));
        }
    }

    private boolean canBeResolved(Configuration configuration) {
        boolean isDeprecatedForResolving = ((DeprecatableConfiguration) configuration).getResolutionAlternatives() != null;
        return configuration.isCanBeResolved() && !isDeprecatedForResolving;
    }

    void renderNow(RenderableDependency root) {
        if (root.getChildren().isEmpty()) {
            getTextOutput().withStyle(Info).text("No dependencies");
            getTextOutput().println();
            return;
        }

        dependencyGraphRenderer.render(Collections.singletonList(root));
    }

    @Override
    public void complete() {
        if (dependencyGraphRenderer != null) {
            dependencyGraphRenderer.complete();
        }

        getTextOutput().println();
        getTextOutput().text("A web-based, searchable dependency report is available by adding the ");
        getTextOutput().withStyle(UserInput).format("--%s", StartParameterBuildOptions.BuildScanOption.LONG_OPTION);
        getTextOutput().println(" option.");

        super.complete();
    }

    private class ConfigurationAction implements Action<StyledTextOutput> {
        private Configuration configuration;

        @Override
        public void execute(StyledTextOutput styledTextOutput) {
            getTextOutput().withStyle(Identifier).text(configuration.getName());
            getTextOutput().withStyle(Description).text(getDescription(configuration));
            if (!canBeResolved(configuration)) {
                getTextOutput().withStyle(Info).text(" (n)");
            }
        }

        public void setConfiguration(Configuration configuration) {
            this.configuration = configuration;
        }
    }
}
