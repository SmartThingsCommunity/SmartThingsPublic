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
package org.gradle.gradlebuild.test.integrationtests

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.junit.JUnitOptions
import org.gradle.kotlin.dsl.*


class IntegrationTestsPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val sourceSet = addSourceSet(TestType.INTEGRATION)
        addDependenciesAndConfigurations(TestType.INTEGRATION)
        createTasks(sourceSet, TestType.INTEGRATION)
        configureIde(TestType.INTEGRATION)

        createTestTask("integMultiVersionTest", "forking", sourceSet, TestType.INTEGRATION, Action {
            // This test task runs only multi-version tests and is intended to be used in the late pipeline to sweep up versions not previously tested
            (options as JUnitOptions).includeCategories("org.gradle.integtests.fixtures.ContextualMultiVersionTest")
        })

        // TODO Model as an extension object. The name is also misleading, as this applies to integration tests as well as cross version tests.
        @Suppress("unused_variable")
        val integTestTasks by extra { tasks.withType<IntegrationTest>() }
    }
}
