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

package org.gradle.plugin.management;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.internal.HasInternalProtocol;
import org.gradle.plugin.use.PluginDependenciesSpec;

/**
 * Configures how plugins are resolved.
 *
 * @since 3.5
 */
@HasInternalProtocol
public interface PluginManagementSpec {

    /**
     * Defines the plugin repositories to use.
     */
    void repositories(Action<? super RepositoryHandler> repositoriesAction);

    /**
     * The plugin repositories to use.
     */
    RepositoryHandler getRepositories();

    /**
     * Configure the plugin resolution strategy.
     */
    void resolutionStrategy(Action<? super PluginResolutionStrategy> action);

    /**
     * The plugin resolution strategy.
     */
    PluginResolutionStrategy getResolutionStrategy();

    /**
     * Configure the default plugin versions.
     * @since 5.6
     */
    @Incubating
    void plugins(Action<? super PluginDependenciesSpec> action);

    /**
     * The Plugin dependencies, permitting default plugin versions to be configured.
     * @since 5.6
     */
    @Incubating
    PluginDependenciesSpec getPlugins();

}
