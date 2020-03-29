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
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import org.gradle.kotlin.dsl.provider.plugins.precompiled.HashedClassPath

import org.gradle.kotlin.dsl.support.CompiledKotlinPluginsBlock
import org.gradle.kotlin.dsl.support.ImplicitImports
import org.gradle.kotlin.dsl.support.compileKotlinScriptModuleTo
import org.gradle.kotlin.dsl.support.scriptDefinitionFromTemplate

import javax.inject.Inject


@CacheableTask
abstract class CompilePrecompiledScriptPluginPlugins @Inject constructor(

    private
    val implicitImports: ImplicitImports

) : DefaultTask(), SharedAccessorsPackageAware {

    @get:Internal
    internal
    lateinit var hashedClassPath: HashedClassPath

    @get:Classpath
    val classPathFiles: FileCollection
        get() = hashedClassPath.classPathFiles

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceFiles: SourceDirectorySet = project.objects.sourceDirectorySet(
        "precompiled-script-plugin-plugins",
        "Precompiled script plugin plugins"
    )

    fun sourceDir(dir: Provider<Directory>) {
        sourceFiles.srcDir(dir)
    }

    @TaskAction
    fun compile() {
        outputDir.withOutputDirectory { outputDir ->
            val scriptFiles = sourceFiles.map { it.path }
            if (scriptFiles.isNotEmpty())
                compileKotlinScriptModuleTo(
                    outputDir,
                    sourceFiles.name,
                    scriptFiles,
                    scriptDefinitionFromTemplate(
                        CompiledKotlinPluginsBlock::class,
                        implicitImportsForPrecompiledScriptPlugins(implicitImports)
                    ),
                    classPathFiles,
                    logger,
                    { it } // TODO: translate paths
                )
        }
    }
}
