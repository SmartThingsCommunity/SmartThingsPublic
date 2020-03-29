/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.kotlin.dsl.support

import org.gradle.internal.io.NullOutputStream

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys

import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler.compileBunchOfSources
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot

import org.jetbrains.kotlin.codegen.CompilationException

import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer.dispose
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer.newDisposable
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar

import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys.JVM_TARGET
import org.jetbrains.kotlin.config.JVMConfigurationKeys.OUTPUT_DIRECTORY
import org.jetbrains.kotlin.config.JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY
import org.jetbrains.kotlin.config.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl

import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor.Companion.registerExtension

import org.jetbrains.kotlin.name.NameUtils

import org.jetbrains.kotlin.samWithReceiver.CliSamWithReceiverComponentContributor

import org.jetbrains.kotlin.scripting.compiler.plugin.ScriptingCompilerConfigurationComponentRegistrar
import org.jetbrains.kotlin.scripting.configuration.ScriptingConfigurationKeys.SCRIPT_DEFINITIONS
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition

import org.jetbrains.kotlin.utils.PathUtil
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult

import org.slf4j.Logger

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

import kotlin.reflect.KClass

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration


fun compileKotlinScriptModuleTo(
    outputDirectory: File,
    moduleName: String,
    scriptFiles: Collection<String>,
    scriptDef: ScriptDefinition,
    classPath: Iterable<File>,
    logger: Logger,
    pathTranslation: (String) -> String
) = compileKotlinScriptModuleTo(
    outputDirectory,
    moduleName,
    scriptFiles,
    scriptDef,
    classPath,
    LoggingMessageCollector(logger, pathTranslation)
)


fun scriptDefinitionFromTemplate(
    template: KClass<out Any>,
    implicitImports: List<String>,
    implicitReceiver: KClass<*>? = null
): ScriptDefinition {
    val hostConfiguration = defaultJvmScriptingHostConfiguration
    return ScriptDefinition.FromConfigurations(
        hostConfiguration = hostConfiguration,
        compilationConfiguration = ScriptCompilationConfiguration {
            baseClass(template)
            defaultImports(implicitImports)
            hostConfiguration(hostConfiguration)
            implicitReceiver?.let {
                implicitReceivers(it)
            }
        },
        evaluationConfiguration = null
    )
}


internal
fun compileKotlinScriptToDirectory(
    outputDirectory: File,
    scriptFile: File,
    scriptDef: ScriptDefinition,
    classPath: List<File>,
    messageCollector: LoggingMessageCollector
): String {

    compileKotlinScriptModuleTo(
        outputDirectory,
        "buildscript",
        listOf(scriptFile.path),
        scriptDef,
        classPath,
        messageCollector
    )

    return NameUtils.getScriptNameForFile(scriptFile.name).asString()
}


private
fun compileKotlinScriptModuleTo(
    outputDirectory: File,
    moduleName: String,
    scriptFiles: Collection<String>,
    scriptDef: ScriptDefinition,
    classPath: Iterable<File>,
    messageCollector: LoggingMessageCollector
) {
    withRootDisposable {

        withCompilationExceptionHandler(messageCollector) {

            val configuration = compilerConfigurationFor(messageCollector).apply {
                put(RETAIN_OUTPUT_IN_MEMORY, false)
                put(OUTPUT_DIRECTORY, outputDirectory)
                setModuleName(moduleName)
                addScriptingCompilerComponents()
                addScriptDefinition(scriptDef)
                scriptFiles.forEach { addKotlinSourceRoot(it) }
                classPath.forEach { addJvmClasspathRoot(it) }
            }
            val environment = kotlinCoreEnvironmentFor(configuration).apply {
                HasImplicitReceiverCompilerPlugin.apply(project)
            }

            compileBunchOfSources(environment)
                || throw ScriptCompilationException(messageCollector.errors)
        }
    }
}


private
object HasImplicitReceiverCompilerPlugin {

    fun apply(project: Project) {
        registerExtension(project, samWithReceiverComponentContributor)
    }

    val samWithReceiverComponentContributor = CliSamWithReceiverComponentContributor(
        listOf("org.gradle.api.HasImplicitReceiver")
    )
}


internal
fun compileToDirectory(
    outputDirectory: File,
    moduleName: String,
    sourceFiles: Iterable<File>,
    logger: Logger,
    classPath: Iterable<File>
): Boolean {

    withRootDisposable {
        withMessageCollectorFor(logger) { messageCollector ->
            val configuration = compilerConfigurationFor(messageCollector).apply {
                addKotlinSourceRoots(sourceFiles.map { it.canonicalPath })
                put(OUTPUT_DIRECTORY, outputDirectory)
                setModuleName(moduleName)
                classPath.forEach { addJvmClasspathRoot(it) }
                addJvmClasspathRoot(kotlinStdlibJar)
            }
            val environment = kotlinCoreEnvironmentFor(configuration)
            return compileBunchOfSources(environment)
        }
    }
}


private
val kotlinStdlibJar: File
    get() = PathUtil.getResourcePathForClass(Unit::class.java)


private
inline fun <T> withRootDisposable(action: Disposable.() -> T): T {
    val rootDisposable = newDisposable()
    try {
        return action(rootDisposable)
    } finally {
        dispose(rootDisposable)
    }
}


private
inline fun <T> withMessageCollectorFor(log: Logger, action: (MessageCollector) -> T): T {
    val messageCollector = messageCollectorFor(log)
    withCompilationExceptionHandler(messageCollector) {
        return action(messageCollector)
    }
}


private
inline fun <T> withCompilationExceptionHandler(messageCollector: LoggingMessageCollector, action: () -> T): T {
    try {
        val log = messageCollector.log
        return when {
            log.isDebugEnabled -> {
                loggingOutputTo(log::debug) { action() }
            }
            else -> {
                ignoringOutputOf { action() }
            }
        }
    } catch (ex: CompilationException) {
        messageCollector.report(
            CompilerMessageSeverity.EXCEPTION,
            ex.localizedMessage,
            MessageUtil.psiElementToMessageLocation(ex.element))

        throw IllegalStateException("Internal compiler error: ${ex.localizedMessage}", ex)
    }
}


private
inline fun <T> loggingOutputTo(noinline log: (String) -> Unit, action: () -> T): T =
    redirectingOutputTo({ LoggingOutputStream(log) }, action)


private
inline fun <T> ignoringOutputOf(action: () -> T): T =
    redirectingOutputTo({ NullOutputStream.INSTANCE }, action)


private
inline fun <T> redirectingOutputTo(noinline outputStream: () -> OutputStream, action: () -> T): T =
    redirecting(System.err, System::setErr, outputStream()) {
        redirecting(System.out, System::setOut, outputStream()) {
            action()
        }
    }


private
inline fun <T> redirecting(
    stream: PrintStream,
    set: (PrintStream) -> Unit,
    to: OutputStream,
    action: () -> T
): T = try {
    set(PrintStream(to, true))
    action()
} finally {
    set(stream)
    to.flush()
}


private
class LoggingOutputStream(val log: (String) -> Unit) : OutputStream() {

    private
    val buffer = ByteArrayOutputStream()

    override fun write(b: Int) = buffer.write(b)

    override fun write(b: ByteArray, off: Int, len: Int) = buffer.write(b, off, len)

    override fun flush() {
        buffer.run {
            val string = toString("utf8")
            if (string.isNotBlank()) {
                log(string)
            }
            reset()
        }
    }

    override fun close() {
        flush()
    }
}


private
fun compilerConfigurationFor(messageCollector: MessageCollector): CompilerConfiguration =
    CompilerConfiguration().apply {
        put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
        put(JVM_TARGET, JVM_1_8)
        put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, gradleKotlinDslLanguageVersionSettings)
    }


private
val gradleKotlinDslLanguageVersionSettings = LanguageVersionSettingsImpl(
    languageVersion = LanguageVersion.KOTLIN_1_3,
    apiVersion = ApiVersion.KOTLIN_1_3,
    specificFeatures = mapOf(
        LanguageFeature.NewInference to LanguageFeature.State.ENABLED,
        LanguageFeature.SamConversionForKotlinFunctions to LanguageFeature.State.ENABLED,
        LanguageFeature.SamConversionPerArgument to LanguageFeature.State.ENABLED,
        LanguageFeature.ReferencesToSyntheticJavaProperties to LanguageFeature.State.ENABLED
    ),
    analysisFlags = mapOf(
        AnalysisFlags.skipMetadataVersionCheck to true
    )
)


private
fun CompilerConfiguration.setModuleName(name: String) {
    put(CommonConfigurationKeys.MODULE_NAME, name)
}


private
fun CompilerConfiguration.addScriptingCompilerComponents() {
    add(
        ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS,
        ScriptingCompilerConfigurationComponentRegistrar()
    )
}


private
fun CompilerConfiguration.addScriptDefinition(scriptDef: ScriptDefinition) {
    add(SCRIPT_DEFINITIONS, scriptDef)
}


private
fun Disposable.kotlinCoreEnvironmentFor(configuration: CompilerConfiguration): KotlinCoreEnvironment {
    org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback()
    return KotlinCoreEnvironment.createForProduction(this, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
}


internal
fun messageCollectorFor(log: Logger, pathTranslation: (String) -> String = { it }): LoggingMessageCollector =
    LoggingMessageCollector(log, pathTranslation)


internal
data class ScriptCompilationError(val message: String, val location: CompilerMessageLocation?)


internal
data class ScriptCompilationException(val errors: List<ScriptCompilationError>) : RuntimeException() {

    init {
        require(errors.isNotEmpty())
    }

    val firstErrorLine
        get() = errors.firstNotNullResult { it.location?.line }

    override val message: String
        get() = (
            listOf("Script compilation $errorPlural:")
                + indentedErrorMessages()
                + "${errors.size} $errorPlural")
            .joinToString("\n\n")

    private
    fun indentedErrorMessages() =
        errors.asSequence().map(::errorMessage).map(::prependIndent).toList()

    private
    fun errorMessage(error: ScriptCompilationError): String =
        error.location?.let { location ->
            errorAt(location, error.message)
        } ?: error.message

    private
    fun errorAt(location: CompilerMessageLocation, message: String): String {
        val columnIndent = " ".repeat(5 + maxLineNumberStringLength + 1 + location.column)
        return "Line ${lineNumber(location)}: ${location.lineContent}\n" +
            "^ $message".lines().joinToString(
                prefix = columnIndent,
                separator = "\n$columnIndent  $indent")
    }

    private
    fun lineNumber(location: CompilerMessageLocation) =
        location.line.toString().padStart(maxLineNumberStringLength, '0')

    private
    fun prependIndent(it: String) = it.prependIndent(indent)

    private
    val errorPlural
        get() = if (errors.size > 1) "errors" else "error"

    private
    val maxLineNumberStringLength: Int by lazy {
        errors.mapNotNull { it.location?.line }.max().toString().length
    }
}


private
const val indent = "  "


internal
class LoggingMessageCollector(
    internal val log: Logger,
    private val pathTranslation: (String) -> String
) : MessageCollector {

    val errors = arrayListOf<ScriptCompilationError>()

    override fun hasErrors() = errors.isNotEmpty()

    override fun clear() = errors.clear()

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {

        fun msg() =
            location?.run {
                path.let(pathTranslation).let { path ->
                    when {
                        line >= 0 && column >= 0 -> compilerMessageFor(path, line, column, message)
                        else -> "$path: $message"
                    }
                }
            } ?: message

        fun taggedMsg() =
            "${severity.presentableName[0]}: ${msg()}"

        when (severity) {
            CompilerMessageSeverity.ERROR, CompilerMessageSeverity.EXCEPTION -> {
                errors += ScriptCompilationError(message, location)
                log.error { taggedMsg() }
            }
            in CompilerMessageSeverity.VERBOSE -> log.trace { msg() }
            CompilerMessageSeverity.STRONG_WARNING -> log.info { taggedMsg() }
            CompilerMessageSeverity.WARNING -> log.info { taggedMsg() }
            CompilerMessageSeverity.INFO -> log.info { msg() }
            else -> log.debug { taggedMsg() }
        }
    }
}


internal
fun compilerMessageFor(path: String, line: Int, column: Int, message: String) =
    "$path:$line:$column: $message"
