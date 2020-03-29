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

package org.gradle.kotlin.dsl

import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyConstraintHandler
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.support.delegates.DependencyHandlerDelegate


/**
 * Receiver for `dependencies` block providing convenient utilities for configuring dependencies.
 *
 * @see [DependencyHandler]
 */
open class DependencyHandlerScope
private constructor(
    val dependencies: DependencyHandler
) : DependencyHandlerDelegate() {

    companion object {
        fun of(dependencies: DependencyHandler): DependencyHandlerScope =
            DependencyHandlerScope(dependencies)
    }

    override val delegate: DependencyHandler
        get() = dependencies

    @Deprecated(replaceWith = ReplaceWith("constraints"), message = "This method shouldn't be called because the most specific variant should be preferred by the Kotlin compiler", level = DeprecationLevel.HIDDEN)
    override fun constraints(configureAction: Action<in DependencyConstraintHandler>) {
        super.constraints(configureAction)
    }

    /**
     * Configures dependency constraint for this project.
     *
     * @param configureAction the action to use to configure module metadata
     *
     * @since 6.3
     */
    @Incubating
    fun constraints(configureAction: DependencyConstraintHandlerScope.() -> Unit) {
        super.constraints { t -> configureAction(DependencyConstraintHandlerScope.of(t)) }
    }

    /**
     * Adds a dependency to the given configuration.
     *
     * @param dependencyNotation notation for the dependency to be added.
     * @return The dependency.
     * @see [DependencyHandler.add]
     */
    operator fun String.invoke(dependencyNotation: Any): Dependency? =
        dependencies.add(this, dependencyNotation)

    /**
     * Adds a dependency to the given configuration.
     *
     * @param dependencyNotation notation for the dependency to be added.
     * @param dependencyConfiguration expression to use to configure the dependency.
     * @return The dependency.
     * @see [DependencyHandler.add]
     */
    inline operator fun String.invoke(dependencyNotation: String, dependencyConfiguration: ExternalModuleDependency.() -> Unit): ExternalModuleDependency =
        dependencies.add(this, dependencyNotation, dependencyConfiguration)

    /**
     * Adds a dependency to the given configuration.
     *
     * @param group the group of the module to be added as a dependency.
     * @param name the name of the module to be added as a dependency.
     * @param version the optional version of the module to be added as a dependency.
     * @param configuration the optional configuration of the module to be added as a dependency.
     * @param classifier the optional classifier of the module artifact to be added as a dependency.
     * @param ext the optional extension of the module artifact to be added as a dependency.
     * @return The dependency.
     *
     * @see [DependencyHandler.add]
     */
    operator fun String.invoke(
        group: String,
        name: String,
        version: String? = null,
        configuration: String? = null,
        classifier: String? = null,
        ext: String? = null
    ): ExternalModuleDependency =
        dependencies.create(group, name, version, configuration, classifier, ext).apply { add(this@invoke, this) }

    /**
     * Adds a dependency to the given configuration.
     *
     * @param group the group of the module to be added as a dependency.
     * @param name the name of the module to be added as a dependency.
     * @param version the optional version of the module to be added as a dependency.
     * @param configuration the optional configuration of the module to be added as a dependency.
     * @param classifier the optional classifier of the module artifact to be added as a dependency.
     * @param ext the optional extension of the module artifact to be added as a dependency.
     * @param dependencyConfiguration expression to use to configure the dependency.
     * @return The dependency.
     *
     * @see [DependencyHandler.create]
     * @see [DependencyHandler.add]
     */
    inline operator fun String.invoke(
        group: String,
        name: String,
        version: String? = null,
        configuration: String? = null,
        classifier: String? = null,
        ext: String? = null,
        dependencyConfiguration: ExternalModuleDependency.() -> Unit
    ): ExternalModuleDependency =
        dependencies.add(this, create(group, name, version, configuration, classifier, ext), dependencyConfiguration)

    /**
     * Adds a dependency to the given configuration.
     *
     * @param dependency dependency to be added.
     * @param dependencyConfiguration expression to use to configure the dependency.
     * @return The dependency.
     *
     * @see [DependencyHandler.add]
     */
    inline operator fun <T : ModuleDependency> String.invoke(dependency: T, dependencyConfiguration: T.() -> Unit): T =
        dependencies.add(this, dependency, dependencyConfiguration)

    /**
     * Adds a dependency to the given configuration.
     *
     * @param dependencyNotation notation for the dependency to be added.
     * @return The dependency.
     * @see [DependencyHandler.add]
     */
    operator fun Configuration.invoke(dependencyNotation: Any): Dependency? =
        add(name, dependencyNotation)

    /**
     * Adds a dependency to the given configuration.
     *
     * @param dependencyNotation notation for the dependency to be added.
     * @param dependencyConfiguration expression to use to configure the dependency.
     * @return The dependency.
     * @see [DependencyHandler.add]
     */
    inline operator fun Configuration.invoke(dependencyNotation: String, dependencyConfiguration: ExternalModuleDependency.() -> Unit): ExternalModuleDependency =
        add(name, dependencyNotation, dependencyConfiguration)

    /**
     * Adds a dependency to the given configuration.
     *
     * @param group the group of the module to be added as a dependency.
     * @param name the name of the module to be added as a dependency.
     * @param version the optional version of the module to be added as a dependency.
     * @param configuration the optional configuration of the module to be added as a dependency.
     * @param classifier the optional classifier of the module artifact to be added as a dependency.
     * @param ext the optional extension of the module artifact to be added as a dependency.
     * @return The dependency.
     *
     * @see [DependencyHandler.add]
     */
    operator fun Configuration.invoke(
        group: String,
        name: String,
        version: String? = null,
        configuration: String? = null,
        classifier: String? = null,
        ext: String? = null
    ): ExternalModuleDependency =
        create(group, name, version, configuration, classifier, ext).apply { add(this@invoke.name, this) }

    /**
     * Adds a dependency to the given configuration.
     *
     * @param group the group of the module to be added as a dependency.
     * @param name the name of the module to be added as a dependency.
     * @param version the optional version of the module to be added as a dependency.
     * @param configuration the optional configuration of the module to be added as a dependency.
     * @param classifier the optional classifier of the module artifact to be added as a dependency.
     * @param ext the optional extension of the module artifact to be added as a dependency.
     * @param dependencyConfiguration expression to use to configure the dependency.
     * @return The dependency.
     *
     * @see [DependencyHandler.create]
     * @see [DependencyHandler.add]
     */
    inline operator fun Configuration.invoke(
        group: String,
        name: String,
        version: String? = null,
        configuration: String? = null,
        classifier: String? = null,
        ext: String? = null,
        dependencyConfiguration: ExternalModuleDependency.() -> Unit
    ): ExternalModuleDependency =
        add(this.name, create(group, name, version, configuration, classifier, ext), dependencyConfiguration)

    /**
     * Adds a dependency to the given configuration.
     *
     * @param dependency dependency to be added.
     * @param dependencyConfiguration expression to use to configure the dependency.
     * @return The dependency.
     *
     * @see [DependencyHandler.add]
     */
    inline operator fun <T : ModuleDependency> Configuration.invoke(dependency: T, dependencyConfiguration: T.() -> Unit): T =
        add(name, dependency, dependencyConfiguration)

    /**
     * Configures the dependencies.
     */
    inline operator fun invoke(configuration: DependencyHandlerScope.() -> Unit) =
        this.configuration()
}
