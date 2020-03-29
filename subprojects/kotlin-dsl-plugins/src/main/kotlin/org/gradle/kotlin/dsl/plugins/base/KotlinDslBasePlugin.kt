/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.kotlin.dsl.plugins.base

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.kotlin.dsl.plugins.dsl.KotlinDslCompilerPlugins
import org.gradle.kotlin.dsl.plugins.dsl.KotlinDslPluginOptions
import org.gradle.kotlin.dsl.plugins.embedded.EmbeddedKotlinPlugin
import org.gradle.kotlin.dsl.plugins.embedded.kotlinArtifactConfigurationNames

import org.gradle.kotlin.dsl.*


/**
 * The `kotlin-dsl-base` plugin.
 *
 * - Applies the `embedded-kotlin` plugin
 * - Adds the `gradleKotlinDsl()` dependency to the `compileOnly` and `testImplementation` configurations
 * - Configures the Kotlin DSL compiler plugins
 *
 * You can use the `kotlinDslPluginOptions` extension of type [KotlinDslPluginOptions] to configure the plugin.
 *
 * @see KotlinDslPluginOptions
 * @see org.gradle.kotlin.dsl.plugins.embedded.EmbeddedKotlinPlugin
 */
class KotlinDslBasePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        apply<EmbeddedKotlinPlugin>()
        createOptionsExtension()
        apply<KotlinDslCompilerPlugins>()
        addGradleKotlinDslDependencyTo(kotlinArtifactConfigurationNames)
    }

    private
    fun Project.createOptionsExtension() =
        extensions.add("kotlinDslPluginOptions", KotlinDslPluginOptions(objects))

    private
    fun Project.addGradleKotlinDslDependencyTo(configurations: List<String>) =
        configurations.forEach {
            dependencies.add(it, gradleKotlinDsl())
        }
}
