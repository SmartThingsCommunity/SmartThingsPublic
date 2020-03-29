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

package org.gradle.kotlin.dsl.accessors.tasks

import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.accessors.ConfigurationEntry

import org.gradle.kotlin.dsl.accessors.TypedProjectSchema
import org.gradle.kotlin.dsl.accessors.entry
import org.gradle.kotlin.dsl.fixtures.standardOutputOf

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Test


class PrintAccessorsTest {

    @Test
    fun `prints accessors for all schema entries`() {

        assertThat(
            standardOutputOf {
                printAccessorsFor(
                    TypedProjectSchema(
                        extensions = listOf(
                            entry<Project, ExtraPropertiesExtension>("extra")
                        ),
                        conventions = listOf(
                            entry<Project, ApplicationPluginConvention>("application")
                        ),
                        tasks = listOf(
                            entry<TaskContainer, Delete>("delete")
                        ),
                        configurations = listOf(
                            ConfigurationEntry("api"),
                            ConfigurationEntry("compile", listOf("api", "implementation"))
                        ),
                        containerElements = listOf(
                            entry<SourceSetContainer, SourceSet>("main")
                        )
                    )
                )
            }.withoutTrailingWhitespace(),
            equalTo(
                textFromResource("PrintAccessors-expected-output.txt")
            )
        )
    }

    private
    fun textFromResource(named: String) =
        javaClass.getResource(named).readText()

    private
    fun String.withoutTrailingWhitespace() =
        lineSequence().map { it.trimEnd() }.joinToString("\n")
}
