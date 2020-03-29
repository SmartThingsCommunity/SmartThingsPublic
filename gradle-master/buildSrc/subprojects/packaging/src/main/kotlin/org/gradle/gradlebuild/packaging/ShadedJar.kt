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

package org.gradle.gradlebuild.packaging

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.ArrayDeque
import java.util.Queue
import java.util.jar.JarFile
import java.util.jar.JarOutputStream


open class ShadedJar : DefaultTask() {
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    val relocatedClassesConfiguration = project.files()
    @PathSensitive(PathSensitivity.NONE)
    @InputFiles
    val classTreesConfiguration = project.files()
    @PathSensitive(PathSensitivity.NONE)
    @InputFiles
    val entryPointsConfiguration = project.files()
    @PathSensitive(PathSensitivity.NONE)
    @InputFiles
    val manifests = project.files()

    /**
     * The build receipt properties file.
     *
     * The file will be included in the shaded jar under {@code /org/gradle/build-receipt.properties}.
     */
    @PathSensitive(PathSensitivity.NONE)
    @InputFile
    val buildReceiptFile: RegularFileProperty = project.objects.fileProperty()

    /**
      * The output Jar file.
      */
    @OutputFile
    val jarFile = project.objects.fileProperty()

    @TaskAction
    fun shade() {
        val entryPoints = readEntryPoints()
        val classTrees = buildClassTrees(readClassTrees())

        val classesToInclude = mutableSetOf<String>()

        val queue: Queue<String> = ArrayDeque()
        queue.addAll(entryPoints)
        while (!queue.isEmpty()) {
            val className = queue.remove()
            if (classesToInclude.add(className)) {
                queue.addAll(classTrees.getOrDefault(className, emptySet()))
            }
        }

        JarOutputStream(BufferedOutputStream(FileOutputStream(jarFile.get().asFile))).use { jarOutputStream ->
            if (!manifests.isEmpty) {
                jarOutputStream.addJarEntry(JarFile.MANIFEST_NAME, manifests.first())
            }
            jarOutputStream.addJarEntry("org/gradle/build-receipt.properties", buildReceiptFile.get().asFile)
            relocatedClassesConfiguration.files.forEach { classesDir ->
                val classesDirPath = classesDir.toPath()
                classesDir.walk().filter {
                    val relativePath = classesDirPath.relativePath(it)
                    classesToInclude.contains(relativePath)
                }.forEach {
                    val relativePath = classesDirPath.relativePath(it)
                    jarOutputStream.addJarEntry(relativePath, it)
                }
            }
        }
    }

    private
    fun readEntryPoints() = entryPointsConfiguration.files.flatMap { readJson<List<String>>(it) }

    private
    fun readClassTrees() = classTreesConfiguration.files.map { readJson<Map<String, List<String>>>(it) }

    private
    fun Path.relativePath(other: File) = relativize(other.toPath()).toString().replace(File.separatorChar, '/')

    private
    inline fun <reified T> readJson(file: File) =
        file.bufferedReader().use { reader ->
            Gson().fromJson<T>(reader, object : TypeToken<T>() {}.type)!!
        }
}


internal
fun buildClassTrees(individualClassTrees: List<Map<String, List<String>>>): Map<String, Set<String>> =
    individualClassTrees.flatMap { it.entries }
        .groupingBy { it.key }
        .aggregate { _, accumulator: Set<String>?, element: Map.Entry<String, List<String>>, first ->
            if (first) {
                element.value.toSet()
            } else {
                accumulator!!.union(element.value)
            }
        }
