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

package org.gradle.kotlin.dsl.precompile

import org.gradle.api.internal.ProcessOperations
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.serviceOf


/**
 * Legacy script template definition for precompiled Kotlin script targeting [Gradle] instances.
 *
 * @see PrecompiledProjectScript
 */
@Deprecated("Kept for compatibility with precompiled script plugins published with Gradle versions prior to 6.0")
open class PrecompiledInitScript(target: Gradle) : InitScriptApi(target) {

    override val fileOperations by lazy { fileOperationsFor(delegate, null) }

    override val processOperations by lazy { delegate.serviceOf<ProcessOperations>() }
}
