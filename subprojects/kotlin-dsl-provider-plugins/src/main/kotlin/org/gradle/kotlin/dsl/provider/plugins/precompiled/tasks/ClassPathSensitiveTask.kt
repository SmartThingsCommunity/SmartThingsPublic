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

package org.gradle.kotlin.dsl.provider.plugins.precompiled.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal

import org.gradle.internal.classpath.ClassPath
import org.gradle.internal.hash.HashCode

import org.gradle.kotlin.dsl.provider.plugins.precompiled.HashedClassPath


abstract class ClassPathSensitiveTask : DefaultTask() {

    @get:Internal
    internal
    lateinit var hashedClassPath: HashedClassPath

    @get:Classpath
    val classPathFiles: FileCollection
        get() = hashedClassPath.classPathFiles

    @get:Internal
    protected
    val classPath: ClassPath
        get() = hashedClassPath.classPath

    @get:Internal
    protected
    val classPathHash: HashCode
        get() = hashedClassPath.hash
}
