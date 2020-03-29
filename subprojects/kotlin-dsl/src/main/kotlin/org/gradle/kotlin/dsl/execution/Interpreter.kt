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

package org.gradle.kotlin.dsl.execution

import com.google.common.annotations.VisibleForTesting
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.internal.initialization.ClassLoaderScope
import org.gradle.api.invocation.Gradle
import org.gradle.groovy.scripts.ScriptSource
import org.gradle.internal.classpath.ClassPath
import org.gradle.internal.exceptions.LocationAwareException
import org.gradle.internal.hash.HashCode
import org.gradle.internal.service.ServiceRegistry
import org.gradle.kotlin.dsl.accessors.projectAccessorsClassPath
import org.gradle.kotlin.dsl.support.KotlinScriptHost
import org.gradle.kotlin.dsl.support.ScriptCompilationException
import org.gradle.kotlin.dsl.support.loggerFor
import org.gradle.kotlin.dsl.support.serviceRegistryOf
import org.gradle.kotlin.dsl.support.unsafeLazy
import org.gradle.plugin.management.internal.PluginRequests
import java.io.File
import java.lang.reflect.InvocationTargetException


/**
 * An optimised interpreter for the Kotlin DSL based on the idea of
 * [partial evaluation](https://en.wikipedia.org/wiki/Partial_evaluation).
 *
 * Instead of interpreting a given Kotlin DSL script directly, the interpreter emits a
 * specialized program that captures the optimal execution procedure for the particular
 * combination of script structure (does it contain a `buildscript` block? a `plugins` block?
 * a script body? etc), target object and context (top-level or not).
 *
 * The specialized program is then cached via a cheap cache key based on the original,
 * unprocessed contents of the script, the target object type and parent `ClassLoader`.
 *
 * Because each program is specialized to a given script structure, a lot of work is
 * avoided. For example, a top-level script containing a `plugins` block but no body
 * can be compiled down to a specialized program that instantiates the precompiled
 * `plugins` block class directly - without reflection - and does nothing else. The
 * same strategy can be used for a **script plugin** (a non top-level script) with a
 * body but no `buildscript` block since the classpath is completely determined at
 * the time the specialized program is emitted.
 *
 * @see Program
 * @see PartialEvaluator
 * @see ResidualProgram
 * @see ResidualProgramCompiler
 */
class Interpreter(val host: Host) {

    interface Host {

        fun cachedClassFor(
            programId: ProgramId
        ): CompiledScript?

        fun cache(
            specializedProgram: CompiledScript,
            programId: ProgramId
        )

        fun cachedDirFor(
            scriptHost: KotlinScriptHost<*>,
            templateId: String,
            sourceHash: HashCode,
            parentClassLoader: ClassLoader,
            accessorsClassPath: ClassPath?,
            initializer: (File) -> Unit
        ): File

        fun startCompilerOperation(
            description: String
        ): AutoCloseable

        fun compilationClassPathOf(
            classLoaderScope: ClassLoaderScope
        ): ClassPath

        /**
         * Provides an additional [ClassPath] to be used in the compilation of a top-level [Project] script
         * `plugins` block.
         *
         * The [ClassPath] is assumed not to influence the cache key of the script by itself as it should
         * already be implied by [ProgramId.parentClassLoader].
         */
        fun pluginAccessorsFor(
            scriptHost: KotlinScriptHost<*>
        ): ClassPath

        fun loadClassInChildScopeOf(
            classLoaderScope: ClassLoaderScope,
            childScopeId: String,
            location: File,
            className: String,
            accessorsClassPath: ClassPath?
        ): CompiledScript

        fun applyPluginsTo(
            scriptHost: KotlinScriptHost<*>,
            pluginRequests: PluginRequests
        )

        fun applyBasePluginsTo(project: Project)

        fun setupEmbeddedKotlinFor(scriptHost: KotlinScriptHost<*>)

        fun closeTargetScopeOf(scriptHost: KotlinScriptHost<*>)

        fun hashOf(classPath: ClassPath): HashCode

        fun runCompileBuildOperation(scriptPath: String, stage: String, action: () -> String): String

        val implicitImports: List<String>

        fun serviceRegistryFor(programTarget: ProgramTarget, target: Any): ServiceRegistry = when (programTarget) {
            ProgramTarget.Project -> serviceRegistryOf(target as Project)
            ProgramTarget.Settings -> serviceRegistryOf(target as Settings)
            ProgramTarget.Gradle -> serviceRegistryOf(target as Gradle)
        }
    }

    fun eval(
        target: Any,
        scriptSource: ScriptSource,
        sourceHash: HashCode,
        scriptHandler: ScriptHandler,
        targetScope: ClassLoaderScope,
        baseScope: ClassLoaderScope,
        topLevelScript: Boolean,
        options: EvalOptions = defaultEvalOptions
    ) {

        val programKind =
            if (topLevelScript) ProgramKind.TopLevel
            else ProgramKind.ScriptPlugin

        val programTarget =
            programTargetFor(target)

        val templateId =
            templateIdFor(programTarget, programKind, "stage1")

        val parentClassLoader =
            baseScope.exportClassLoader

        val programId =
            ProgramId(templateId, sourceHash, parentClassLoader)

        val cachedProgram =
            host.cachedClassFor(programId)

        val scriptHost =
            scriptHostFor(programTarget, target, scriptSource, scriptHandler, targetScope, baseScope)

        val programHost =
            programHostFor(options)

        if (cachedProgram != null) {
            programHost.eval(cachedProgram.programFor, scriptHost)
            return
        }

        val specializedProgram =
            emitSpecializedProgramFor(
                scriptHost,
                scriptSource,
                sourceHash,
                templateId,
                parentClassLoader,
                targetScope,
                baseScope,
                programKind,
                programTarget
            )

        host.cache(
            specializedProgram,
            programId
        )

        programHost.eval(specializedProgram.programFor, scriptHost)
    }

    private
    fun programTargetFor(target: Any): ProgramTarget =
        when (target) {
            is Settings -> ProgramTarget.Settings
            is Project -> ProgramTarget.Project
            is Gradle -> ProgramTarget.Gradle
            else -> throw IllegalArgumentException("Unsupported target: $target")
        }

    private
    fun scriptHostFor(
        programTarget: ProgramTarget,
        target: Any,
        scriptSource: ScriptSource,
        scriptHandler: ScriptHandler,
        targetScope: ClassLoaderScope,
        baseScope: ClassLoaderScope
    ) =
        KotlinScriptHost(
            target,
            scriptSource,
            scriptHandler,
            targetScope,
            baseScope,
            host.serviceRegistryFor(programTarget, target))

    private
    fun programHostFor(options: EvalOptions) =
        if (EvalOption.SkipBody in options) FirstStageOnlyProgramHost() else defaultProgramHost

    private
    fun emitSpecializedProgramFor(
        scriptHost: KotlinScriptHost<Any>,
        scriptSource: ScriptSource,
        sourceHash: HashCode,
        templateId: String,
        parentClassLoader: ClassLoader,
        targetScope: ClassLoaderScope,
        baseScope: ClassLoaderScope,
        programKind: ProgramKind,
        programTarget: ProgramTarget
    ): CompiledScript {

        val pluginAccessorsClassPath by unsafeLazy {
            // TODO: consider computing plugin accessors only when there's a plugins block
            if (requiresAccessors(programTarget, programKind)) host.pluginAccessorsFor(scriptHost)
            else null
        }

        val scriptPath =
            scriptHost.fileName

        val cachedDir =
            host.cachedDirFor(
                scriptHost,
                templateId,
                sourceHash,
                parentClassLoader,
                null
            ) { cachedDir ->

                startCompilerOperationFor(scriptSource, templateId).use {

                    val outputDir =
                        stage1SubDirOf(cachedDir).apply { mkdir() }

                    val sourceText =
                        scriptSource.resource!!.text

                    val programSource =
                        ProgramSource(scriptPath, sourceText)

                    val program =
                        ProgramParser.parse(programSource, programKind, programTarget)

                    val residualProgram = program.map(
                        PartialEvaluator(programKind, programTarget)::reduce
                    )

                    scriptSource.withLocationAwareExceptionHandling {
                        ResidualProgramCompiler(
                            outputDir = outputDir,
                            classPath = host.compilationClassPathOf(targetScope.parent),
                            originalSourceHash = sourceHash,
                            programKind = programKind,
                            programTarget = programTarget,
                            implicitImports = host.implicitImports,
                            logger = interpreterLogger,
                            compileBuildOperationRunner = host::runCompileBuildOperation,
                            pluginAccessorsClassPath = pluginAccessorsClassPath ?: ClassPath.EMPTY,
                            packageName = residualProgram.packageName
                        ).compile(residualProgram.document)
                    }
                }
            }

        val classesDir =
            stage1SubDirOf(cachedDir)

        return loadClassInChildScopeOf(
            baseScope,
            scriptPath,
            classesDir,
            templateId,
            pluginAccessorsClassPath,
            scriptSource
        )
    }

    private
    fun stage1SubDirOf(cachedDir: File) =
        cachedDir.resolve("stage-1")

    private
    fun loadClassInChildScopeOf(
        baseScope: ClassLoaderScope,
        scriptPath: String,
        classesDir: File,
        scriptTemplateId: String,
        accessorsClassPath: ClassPath?,
        scriptSource: ScriptSource
    ): CompiledScript {

        logClassLoadingOf(scriptTemplateId, scriptSource)

        return host.loadClassInChildScopeOf(
            baseScope,
            childScopeId = classLoaderScopeIdFor(scriptPath, scriptTemplateId),
            accessorsClassPath = accessorsClassPath,
            location = classesDir,
            className = "Program"
        )
    }

    private
    val defaultProgramHost = ProgramHost()

    private
    inner class FirstStageOnlyProgramHost : ProgramHost() {

        override fun evaluateSecondStageOf(
            program: ExecutableProgram.StagedProgram,
            scriptHost: KotlinScriptHost<*>,
            scriptTemplateId: String,
            sourceHash: HashCode,
            accessorsClassPath: ClassPath?
        ) = Unit
    }

    private
    open inner class ProgramHost : ExecutableProgram.Host {

        override fun setupEmbeddedKotlinFor(scriptHost: KotlinScriptHost<*>) {
            host.setupEmbeddedKotlinFor(scriptHost)
        }

        override fun applyPluginsTo(scriptHost: KotlinScriptHost<*>, pluginRequests: PluginRequests) {
            host.applyPluginsTo(scriptHost, pluginRequests)
        }

        override fun applyBasePluginsTo(project: Project) {
            host.applyBasePluginsTo(project)
        }

        override fun handleScriptException(
            exception: Throwable,
            scriptClass: Class<*>,
            scriptHost: KotlinScriptHost<*>
        ) {
            locationAwareExceptionHandlingFor(exception, scriptClass, scriptHost.scriptSource)
        }

        override fun closeTargetScopeOf(scriptHost: KotlinScriptHost<*>) {
            host.closeTargetScopeOf(scriptHost)
        }

        override fun evaluateSecondStageOf(
            program: ExecutableProgram.StagedProgram,
            scriptHost: KotlinScriptHost<*>,
            scriptTemplateId: String,
            sourceHash: HashCode,
            accessorsClassPath: ClassPath?
        ) {
            val targetScope =
                scriptHost.targetScope

            val parentClassLoader =
                targetScope.exportClassLoader

            val classPathHash: HashCode? =
                accessorsClassPath?.let { host.hashOf(it) }

            val programId =
                ProgramId(scriptTemplateId, sourceHash, parentClassLoader, classPathHash)

            val cachedProgram =
                host.cachedClassFor(programId)

            if (cachedProgram != null) {
                eval(cachedProgram.programFor, scriptHost)
                return
            }

            val specializedProgram =
                program.loadSecondStageFor(
                    this,
                    scriptHost,
                    scriptTemplateId,
                    sourceHash,
                    accessorsClassPath)

            host.cache(
                specializedProgram,
                programId)

            eval(specializedProgram.programFor, scriptHost)
        }

        override fun accessorsClassPathFor(scriptHost: KotlinScriptHost<*>) =
            projectAccessorsClassPath(
                scriptHost.target as Project,
                host.compilationClassPathOf(scriptHost.targetScope)
            ).bin

        override fun compileSecondStageOf(
            program: ExecutableProgram.StagedProgram,
            scriptHost: KotlinScriptHost<*>,
            scriptTemplateId: String,
            sourceHash: HashCode,
            programKind: ProgramKind,
            programTarget: ProgramTarget,
            accessorsClassPath: ClassPath?
        ): CompiledScript {

            val originalScriptPath =
                scriptHost.fileName

            val targetScope =
                scriptHost.targetScope

            val parentClassLoader =
                targetScope.exportClassLoader

            val scriptSource =
                scriptHost.scriptSource

            val cacheDir =
                host.cachedDirFor(
                    scriptHost,
                    scriptTemplateId,
                    sourceHash,
                    parentClassLoader,
                    accessorsClassPath
                ) { outputDir ->

                    startCompilerOperationFor(scriptSource, scriptTemplateId).use {

                        val targetScopeClassPath =
                            host.compilationClassPathOf(targetScope)

                        val compilationClassPath =
                            accessorsClassPath?.let {
                                targetScopeClassPath + it
                            } ?: targetScopeClassPath

                        scriptSource.withLocationAwareExceptionHandling {

                            withTemporaryScriptFileFor(originalScriptPath, program.secondStageScriptText) { scriptFile ->

                                ResidualProgramCompiler(
                                    outputDir,
                                    compilationClassPath,
                                    sourceHash,
                                    programKind,
                                    programTarget,
                                    host.implicitImports,
                                    interpreterLogger,
                                    host::runCompileBuildOperation
                                ).emitStage2ProgramFor(
                                    scriptFile,
                                    originalScriptPath
                                )
                            }
                        }
                    }
                }

            return loadClassInChildScopeOf(
                targetScope,
                originalScriptPath,
                cacheDir,
                scriptTemplateId,
                accessorsClassPath,
                scriptSource)
        }

        open fun eval(specializedProgram: Class<*>, scriptHost: KotlinScriptHost<*>) {
            withContextClassLoader(specializedProgram.classLoader) {
                instantiate(specializedProgram)
                    .execute(this, scriptHost)
            }
        }

        private
        fun instantiate(specializedProgram: Class<*>) =
            specializedProgram.getDeclaredConstructor().newInstance() as ExecutableProgram
    }

    private
    fun startCompilerOperationFor(scriptSource: ScriptSource, scriptTemplateId: String): AutoCloseable {
        logCompilationOf(scriptTemplateId, scriptSource)
        return host.startCompilerOperation(scriptSource.displayName)
    }
}


@VisibleForTesting
fun templateIdFor(programTarget: ProgramTarget, programKind: ProgramKind, stage: String): String =
    programTarget.name + "/" + programKind.name + "/" + stage


private
fun classLoaderScopeIdFor(scriptPath: String, stage: String) =
    "kotlin-dsl:$scriptPath:$stage"


private
fun locationAwareExceptionHandlingFor(e: Throwable, scriptClass: Class<*>, scriptSource: ScriptSource): Nothing {
    val targetException = maybeUnwrapInvocationTargetException(e)
    val locationAware = locationAwareExceptionFor(targetException, scriptClass, scriptSource)
    throw locationAware ?: targetException
}


private
fun locationAwareExceptionFor(
    original: Throwable,
    scriptClass: Class<*>,
    scriptSource: ScriptSource
): LocationAwareException? {

    val scriptClassName = scriptClass.name
    val scriptClassNameInnerPrefix = "$scriptClassName$"

    fun scriptStackTraceElement(element: StackTraceElement) =
        element.className?.run {
            equals(scriptClassName) || startsWith(scriptClassNameInnerPrefix)
        } == true

    tailrec fun inferLocationFrom(exception: Throwable): LocationAwareException? {

        if (exception is LocationAwareException) {
            return exception
        }

        exception.stackTrace.find(::scriptStackTraceElement)?.run {
            return LocationAwareException(original, scriptSource, lineNumber.takeIf { it >= 0 })
        }

        val cause = exception.cause ?: return null
        return inferLocationFrom(cause)
    }

    return inferLocationFrom(original)
}


private
inline fun <T> ScriptSource.withLocationAwareExceptionHandling(action: () -> T): T =
    try {
        action()
    } catch (e: ScriptCompilationException) {
        throw LocationAwareException(e, this, e.firstErrorLine)
    }


private
fun maybeUnwrapInvocationTargetException(e: Throwable) =
    if (e is InvocationTargetException) e.targetException
    else e


private
inline fun withContextClassLoader(classLoader: ClassLoader, block: () -> Unit) {
    val currentThread = Thread.currentThread()
    val previous = currentThread.contextClassLoader
    try {
        currentThread.contextClassLoader = classLoader
        block()
    } finally {
        currentThread.contextClassLoader = previous
    }
}


private
fun logCompilationOf(templateId: String, source: ScriptSource) {
    interpreterLogger.debug("Compiling $templateId from ${source.displayName}")
}


private
fun logClassLoadingOf(templateId: String, source: ScriptSource) {
    interpreterLogger.debug("Loading $templateId from ${source.displayName}")
}


internal
val interpreterLogger = loggerFor<Interpreter>()
