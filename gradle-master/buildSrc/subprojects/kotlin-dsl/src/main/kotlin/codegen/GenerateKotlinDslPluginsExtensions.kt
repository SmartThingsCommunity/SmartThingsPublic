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

package codegen

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

import java.io.File


@Suppress("unused")
abstract class GenerateKotlinDslPluginsExtensions : CodeGenerationTask() {

    @get:Input
    abstract val kotlinDslPluginsVersion: Property<Any>

    override fun File.writeFiles() {
        writeFile(
            "org/gradle/kotlin/dsl/plugins/Version.kt",
            """$licenseHeader
package org.gradle.kotlin.dsl.plugins


internal
val appliedKotlinDslPluginsVersion = "${kotlinDslPluginsVersion.get()}"
""")
    }
}
