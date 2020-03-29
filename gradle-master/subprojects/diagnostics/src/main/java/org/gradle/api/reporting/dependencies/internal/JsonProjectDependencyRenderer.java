/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.reporting.dependencies.internal;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import groovy.json.JsonBuilder;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionComparator;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.UnresolvableConfigurationResult;
import org.gradle.api.tasks.diagnostics.internal.insight.DependencyInsightReporter;
import org.gradle.internal.deprecation.DeprecatableConfiguration;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GradleVersion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renderer that emits a JSON tree containing the HTML dependency report structure for a given project. The structure is the following:
 *
 * <pre>
 *     {
 *          "gradleVersion" : "...",
 *          "generationDate" : "...",
 *          "project" : {
 *               "name" : "...",
 *               "description : "...", (optional)
 *               "configurations" : [
 *                   "name" : "...",
 *                   "description" : "...", (optional)
 *                   "dependencies" : [
 *                       {
 *                           "module" : "group:name"
 *                           "name" : "...",
 *                           "resolvable" : true|false,
 *                           "alreadyRendered" : true|false
 *                           "hasConflict" : true|false
 *                           "children" : [
 *                               same array as configurations.dependencies.children
 *                           ]
 *                       },
 *                       ...
 *                   ],
 *                   "moduleInsights : [
 *                       {
 *                           "module" : "group:name"
 *                           "insight" : [
 *                               {
 *                                   "name" : "...",
 *                                   "description" : "...",
 *                                   "resolvable" : true|false,
 *                                   "hasConflict" : true|false,
 *                                   "children": [
 *                                       {
 *                                           "name" : "...",
 *                                           "resolvable" : "...",
 *                                           "hasConflict" : true|false,
 *                                           "alreadyRendered" : true|false
 *                                           "isLeaf" : true|false
 *                                           "children" : [
 *                                               same array as configurations.moduleInsights.insight.children
 *                                           ]
 *                                       },
 *                                       ...
 *                                   ]
 *                               },
 *                               ...
 *                           ]
 *                       }
 *                       ,
 *                       ...
 *                   ]
 *               ]
 *          }
 *      }
 * </pre>
 */
public class JsonProjectDependencyRenderer {
    public JsonProjectDependencyRenderer(VersionSelectorScheme versionSelectorScheme, VersionComparator versionComparator, VersionParser versionParser) {
        this.versionSelectorScheme = versionSelectorScheme;
        this.versionComparator = versionComparator;
        this.versionParser = versionParser;
    }

    /**
     * Generates the project dependency report structure
     *
     * @param project the project for which the report must be generated
     * @return the generated JSON, as a String
     */
    public String render(Project project) {
        JsonBuilder json = new JsonBuilder();
        renderProject(project, json);
        return json.toString();
    }

    // Historic note: this class still uses the Groovy JsonBuilder, as it was originally developed as a Groovy class.
    private void renderProject(Project project, JsonBuilder json) {

        Map<String, Object> overall = Maps.newLinkedHashMap();
        overall.put("gradleVersion", GradleVersion.current().toString());
        overall.put("generationDate", new Date().toString());

        Map<String, Object> projectOut = Maps.newLinkedHashMap();
        projectOut.put("name", project.getName());
        projectOut.put("description", project.getDescription());
        projectOut.put("configurations", createConfigurations(project));
        overall.put("project", projectOut);

        json.call(overall);
    }

    private List<Configuration> getNonDeprecatedConfigurations(Project project) {
        List<Configuration> filteredConfigurations = new ArrayList<Configuration>();
        for (Configuration configuration : project.getConfigurations()) {
            if (!((DeprecatableConfiguration) configuration).isFullyDeprecated()) {
                filteredConfigurations.add(configuration);
            }
        }
        return filteredConfigurations;
    }

    private boolean canBeResolved(Configuration configuration) {
        boolean isDeprecatedForResolving = ((DeprecatableConfiguration) configuration).getResolutionAlternatives() != null;
        return configuration.isCanBeResolved() && !isDeprecatedForResolving;
    }

    private List<Map> createConfigurations(Project project) {
        Iterable<Configuration> configurations = getNonDeprecatedConfigurations(project);
        return CollectionUtils.collect(configurations, new Transformer<Map, Configuration>() {
            @Override
            public Map transform(Configuration configuration) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(4);
                map.put("name", configuration.getName());
                map.put("description", configuration.getDescription());
                map.put("dependencies", createDependencies(configuration));
                map.put("moduleInsights", createModuleInsights(configuration));
                return map;
            }

        });
    }

    private List createDependencies(Configuration configuration) {
        if (canBeResolved(configuration)) {
            ResolutionResult result = configuration.getIncoming().getResolutionResult();
            RenderableDependency root = new RenderableModuleResult(result.getRoot());
            return createDependencyChildren(root, new HashSet<Object>());
        } else {
            return createDependencyChildren(new UnresolvableConfigurationResult(configuration), new HashSet<Object>());
        }
    }

    private List createDependencyChildren(RenderableDependency dependency, final Set<Object> visited) {
        Iterable<? extends RenderableDependency> children = dependency.getChildren();
        return CollectionUtils.collect(children, new Transformer<Map, RenderableDependency>() {
            @Override
            public Map transform(RenderableDependency childDependency) {
                boolean alreadyVisited = !visited.add(childDependency.getId());
                boolean alreadyRendered = alreadyVisited && !childDependency.getChildren().isEmpty();
                String name = replaceArrow(childDependency.getName());
                boolean hasConflict = !name.equals(childDependency.getName());
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(6);
                ModuleIdentifier moduleIdentifier = getModuleIdentifier(childDependency);
                map.put("module", moduleIdentifier == null ? null : moduleIdentifier.toString());
                map.put("name", name);
                map.put("resolvable", childDependency.getResolutionState());
                map.put("hasConflict", hasConflict);
                map.put("alreadyRendered", alreadyRendered);
                map.put("children", Collections.emptyList());
                if (!alreadyRendered) {
                    map.put("children", createDependencyChildren(childDependency, visited));
                }
                return map;
            }
        });
    }

    private ModuleIdentifier getModuleIdentifier(RenderableDependency renderableDependency) {
        if (renderableDependency.getId() instanceof ModuleComponentIdentifier) {
            ModuleComponentIdentifier id = (ModuleComponentIdentifier) renderableDependency.getId();
            return id.getModuleIdentifier();
        }
        return null;
    }

    private List createModuleInsights(final Configuration configuration) {
        Iterable<ModuleIdentifier> modules = collectModules(configuration);
        return CollectionUtils.collect(modules, new Transformer<Object, ModuleIdentifier>() {
            @Override
            public Object transform(ModuleIdentifier moduleIdentifier) {
                return createModuleInsight(moduleIdentifier, configuration);
            }
        });
    }

    private Set<ModuleIdentifier> collectModules(Configuration configuration) {
        RenderableDependency root;
        if (canBeResolved(configuration)) {
            ResolutionResult result = configuration.getIncoming().getResolutionResult();
            root = new RenderableModuleResult(result.getRoot());
        } else {
            root = new UnresolvableConfigurationResult(configuration);
        }
        Set<ModuleIdentifier> modules = Sets.newHashSet();
        Set<ComponentIdentifier> visited = Sets.newHashSet();
        populateModulesWithChildDependencies(root, visited, modules);
        return modules;
    }

    private void populateModulesWithChildDependencies(RenderableDependency dependency, Set<ComponentIdentifier> visited, Set<ModuleIdentifier> modules) {
        for (RenderableDependency childDependency : dependency.getChildren()) {
            ModuleIdentifier moduleId = getModuleIdentifier(childDependency);
            if (moduleId == null) {
                continue;
            }
            modules.add(moduleId);
            boolean alreadyVisited = !visited.add((ComponentIdentifier) childDependency.getId());
            if (!alreadyVisited) {
                populateModulesWithChildDependencies(childDependency, visited, modules);
            }
        }
    }

    private Map createModuleInsight(ModuleIdentifier module, Configuration configuration) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(2);
        map.put("module", module.toString());
        map.put("insight", createInsight(module, configuration));
        return map;
    }

    private List createInsight(ModuleIdentifier module, final Configuration configuration) {
        final Spec<DependencyResult> dependencySpec = new StrictDependencyResultSpec(module);

        ResolutionResult result = configuration.getIncoming().getResolutionResult();
        final Set<DependencyResult> selectedDependencies = new LinkedHashSet<DependencyResult>();

        result.allDependencies(new Action<DependencyResult>() {
            @Override
            public void execute(DependencyResult it) {
                if (dependencySpec.isSatisfiedBy(it)) {
                    selectedDependencies.add(it);
                }
            }
        });

        Collection<RenderableDependency> sortedDeps = new DependencyInsightReporter(versionSelectorScheme, versionComparator, versionParser).convertToRenderableItems(selectedDependencies, false);
        return CollectionUtils.collect(sortedDeps, new Transformer<Object, RenderableDependency>() {
            @Override
            public Object transform(RenderableDependency dependency) {
                String name = replaceArrow(dependency.getName());
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(5);
                map.put("name", replaceArrow(dependency.getName()));
                map.put("description", dependency.getDescription());
                map.put("resolvable", dependency.getResolutionState());
                map.put("hasConflict", !name.equals(dependency.getName()));
                map.put("children", createInsightDependencyChildren(dependency, new HashSet<Object>(), configuration));
                return map;
            }
        });
    }

    private List createInsightDependencyChildren(RenderableDependency dependency, final Set<Object> visited, final Configuration configuration) {
        Iterable<? extends RenderableDependency> children = dependency.getChildren();
        return CollectionUtils.collect(children, new Transformer<Object, RenderableDependency>() {
            @Override
            public Object transform(RenderableDependency childDependency) {
                boolean alreadyVisited = !visited.add(childDependency.getId());
                boolean leaf = childDependency.getChildren().isEmpty();
                boolean alreadyRendered = alreadyVisited && !leaf;
                String childName = replaceArrow(childDependency.getName());
                boolean hasConflict = !childName.equals(childDependency.getName());
                String name = leaf ? configuration.getName() : childName;

                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(6);
                map.put("name", name);
                map.put("resolvable", childDependency.getResolutionState());
                map.put("hasConflict", hasConflict);
                map.put("alreadyRendered", alreadyRendered);
                map.put("isLeaf", leaf);
                map.put("children", Collections.emptyList());
                if (!alreadyRendered) {
                    map.put("children", createInsightDependencyChildren(childDependency, visited, configuration));
                }
                return map;
            }
        });
    }

    private String replaceArrow(String name) {
        return name.replace(" -> ", " \u27A1 ");
    }

    private final VersionSelectorScheme versionSelectorScheme;
    private final VersionComparator versionComparator;
    private final VersionParser versionParser;
}
