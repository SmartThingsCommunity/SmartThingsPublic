/*
 * Copyright 2017 the original author or authors.
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
package org.gradle.kotlin.dsl.plugins.embedded

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import org.gradle.api.plugins.JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME

import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

import org.gradle.kotlin.dsl.embeddedKotlinVersion
import org.gradle.kotlin.dsl.support.EmbeddedKotlinProvider

import javax.inject.Inject


/**
 * The `embedded-kotlin` plugin.
 *
 * Applies the `org.jetbrains.kotlin.jvm` plugin,
 * adds compile only and test implementation dependencies on `kotlin-stdlib` and `kotlin-reflect`.
 */
class EmbeddedKotlinPlugin @Inject internal constructor(
    private val embeddedKotlin: EmbeddedKotlinProvider
) : Plugin<Project> {

    override fun apply(project: Project) {
        project.run {

            plugins.apply(KotlinPluginWrapper::class.java)

            logger.warnOnDifferentKotlinVersion(getKotlinPluginVersion())

            val embeddedKotlinConfiguration = configurations.create("embeddedKotlin")
            embeddedKotlin.addDependenciesTo(
                dependencies,
                embeddedKotlinConfiguration.name,
                "stdlib-jdk8", "reflect")

            kotlinArtifactConfigurationNames.forEach {
                configurations.getByName(it).extendsFrom(embeddedKotlinConfiguration)
            }
        }
    }
}


fun Logger.warnOnDifferentKotlinVersion(kotlinVersion: String?) {
    if (kotlinVersion != embeddedKotlinVersion) {
        warn(
            """
                WARNING: Unsupported Kotlin plugin version.
                The `embedded-kotlin` and `kotlin-dsl` plugins rely on features of Kotlin `{}` that might work differently than in the requested version `{}`.
            """.trimIndent(),
            embeddedKotlinVersion,
            kotlinVersion
        )
    }
}


internal
val kotlinArtifactConfigurationNames =
    listOf(COMPILE_ONLY_CONFIGURATION_NAME, TEST_IMPLEMENTATION_CONFIGURATION_NAME)
