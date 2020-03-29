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

package org.gradle.kotlin.dsl.plugins.precompiled

import org.gradle.integtests.fixtures.ToBeFixedForVfsRetention
import org.gradle.kotlin.dsl.fixtures.AbstractPluginTest
import org.gradle.kotlin.dsl.fixtures.classLoaderFor
import org.gradle.util.TestPrecondition

import org.junit.Before


@ToBeFixedForVfsRetention(
    because = "https://github.com/gradle/gradle/issues/12184",
    failsOnlyIf = TestPrecondition.WINDOWS
)
open class AbstractPrecompiledScriptPluginTest : AbstractPluginTest() {

    @Before
    fun setupPluginTest() {
        requireGradleDistributionOnEmbeddedExecuter()
        executer.beforeExecute {
            // Ignore stacktraces when the Kotlin daemon fails
            // See https://github.com/gradle/gradle-private/issues/2936
            it.withStackTraceChecksDisabled()
        }
    }

    protected
    inline fun <reified T> instantiatePrecompiledScriptOf(target: T, className: String): Any =
        loadCompiledKotlinClass(className)
            .getConstructor(T::class.java, T::class.java)
            .newInstance(target, target)

    protected
    fun loadCompiledKotlinClass(className: String): Class<*> =
        classLoaderFor(existing("build/classes/kotlin/main"))
            .loadClass(className)
}
