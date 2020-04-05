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

package org.gradle.binarycompatibility

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure

import org.hamcrest.CoreMatchers

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import java.io.File
import java.nio.file.Files

import org.gradle.kotlin.dsl.*


abstract class AbstractBinaryCompatibilityTest {

    @get:Rule
    val tmpDir = TemporaryFolder()

    private
    val rootDir: File
        get() = tmpDir.root

    internal
    fun checkBinaryCompatibleKotlin(v1: String = "", v2: String, block: CheckResult.() -> Unit = {}): CheckResult =
        runKotlinBinaryCompatibilityCheck(v1, v2) {
            assertBinaryCompatible()
            block()
        }

    internal
    fun checkNotBinaryCompatibleKotlin(v1: String = "", v2: String, block: CheckResult.() -> Unit = {}): CheckResult =
        runKotlinBinaryCompatibilityCheck(v1, v2) {
            assertNotBinaryCompatible()
            block()
        }

    internal
    fun checkBinaryCompatibleJava(v1: String = "", v2: String, block: CheckResult.() -> Unit = {}): CheckResult =
        runJavaBinaryCompatibilityCheck(v1, v2) {
            assertBinaryCompatible()
            block()
        }

    internal
    fun checkNotBinaryCompatibleJava(v1: String = "", v2: String, block: CheckResult.() -> Unit = {}): CheckResult =
        runJavaBinaryCompatibilityCheck(v1, v2) {
            assertNotBinaryCompatible()
            block()
        }

    internal
    fun checkBinaryCompatible(v1: File.() -> Unit = {}, v2: File.() -> Unit = {}, block: CheckResult.() -> Unit = {}): CheckResult =
        runBinaryCompatibilityCheck(v1, v2) {
            assertBinaryCompatible()
            block()
        }

    internal
    fun checkNotBinaryCompatible(v1: File.() -> Unit = {}, v2: File.() -> Unit = {}, block: CheckResult.() -> Unit = {}): CheckResult =
        runBinaryCompatibilityCheck(v1, v2) {
            assertNotBinaryCompatible()
            block()
        }

    private
    fun CheckResult.assertBinaryCompatible() {
        assertTrue(richReport.toAssertionMessage("Expected to be compatible but the check failed"), isBinaryCompatible)
    }

    private
    fun CheckResult.assertNotBinaryCompatible() {
        assertFalse(richReport.toAssertionMessage("Expected to be breaking but the check passed"), isBinaryCompatible)
    }

    private
    fun RichReport.toAssertionMessage(message: String) =
        if (isEmpty) "$message with an empty report"
        else "$message\n${toText().prependIndent("    ")}"

    private
    fun runKotlinBinaryCompatibilityCheck(v1: String, v2: String, block: CheckResult.() -> Unit = {}): CheckResult =
        runBinaryCompatibilityCheck(
            v1 = {
                withFile("kotlin/com/example/Source.kt", """
                    package com.example

                    import org.gradle.api.Incubating
                    import javax.annotation.Nullable

                    $v1
                """)
            },
            v2 = {
                withFile("kotlin/com/example/Source.kt", """
                    package com.example

                    import org.gradle.api.Incubating
                    import javax.annotation.Nullable

                    $v2
                """)
            },
            block = block
        )

    private
    fun runJavaBinaryCompatibilityCheck(v1: String, v2: String, block: CheckResult.() -> Unit = {}): CheckResult =
        runBinaryCompatibilityCheck(
            v1 = {
                withFile("java/com/example/Source.java", """
                    package com.example;

                    import org.gradle.api.Incubating;
                    import javax.annotation.Nullable;

                    $v1
                """)
            },
            v2 = {
                withFile("java/com/example/Source.java", """
                    package com.example;

                    import org.gradle.api.Incubating;
                    import javax.annotation.Nullable;

                    $v2
                """)
            },
            block = block
        )

    /**
     * Runs the binary compatibility check against two source trees.
     *
     * The fixture build supports both Java and Kotlin sources.
     *
     * @param v1 sources producer for V1, receiver is the `src/main` directory
     * @param v2 sources producer for V2, receiver is the `src/main` directory
     * @param block convenience block invoked on the result
     * @return the check result
     */
    private
    fun runBinaryCompatibilityCheck(v1: File.() -> Unit, v2: File.() -> Unit, block: CheckResult.() -> Unit = {}): CheckResult {

        val inputBuildDir = rootDir.withUniqueDirectory("input-build").apply {

            withSettings("""include("v1", "v2", "binaryCompatibility")""")
            withBuildScript("""
                    plugins {
                        base
                        kotlin("jvm") version "$embeddedKotlinVersion" apply false
                    }
                    subprojects {
                        apply(plugin = "kotlin")
                        base.archivesBaseName = "api-module"
                        repositories {
                            jcenter()
                        }
                        dependencies {
                            "implementation"(gradleApi())
                            "implementation"(kotlin("stdlib"))
                        }
                    }
                    project(":v1") {
                        version = "1.0"
                    }
                    project(":v2") {
                        version = "2.0"
                    }
                """)
            withDirectory("v1/src/main").v1()
            withDirectory("v2/src/main").v2()
            withDirectory("binaryCompatibility").apply {
                withBuildScript("""
                    import japicmp.model.JApiChangeStatus
                    import me.champeau.gradle.japicmp.JapicmpTask
                    import org.gradle.binarycompatibility.*
                    import org.gradle.binarycompatibility.filters.*

                    tasks.register<JapicmpTask>("checkBinaryCompatibility") {

                        dependsOn(":v1:jar", ":v2:jar")

                        val v1 = rootProject.project(":v1")
                        val v1Jar = v1.tasks.named("jar")
                        val v2 = rootProject.project(":v2")
                        val v2Jar = v2.tasks.named("jar")

                        oldArchives = files(v1Jar)
                        oldClasspath = files(v1.configurations.named("runtimeClasspath"), v1Jar)

                        newArchives = files(v2Jar)
                        newClasspath = files(v2.configurations.named("runtimeClasspath"), v2Jar)

                        isOnlyModified = false
                        isFailOnModification = false // we rely on the rich report to fail

                        txtOutputFile = file("build/japi-report.txt")

                        richReport {

                            title = "Gradle Binary Compatibility Check"
                            destinationDir = file("build/japi")
                            reportName = "japi.html"

                            includedClasses = listOf(".*")
                            excludedClasses = emptyList()

                        }

                        BinaryCompatibilityHelper.setupJApiCmpRichReportRules(
                            this,
                            AcceptedApiChanges.parse("{acceptedApiChanges:[]}"),
                            setOf(rootProject.file("v2/src/main/kotlin")),
                            "2.0"
                        )
                    }
                """)
            }
        }

        val runner = GradleRunner.create()
            .withProjectDir(inputBuildDir)
            .withPluginClasspath()
            .withArguments(":binaryCompatibility:checkBinaryCompatibility", "-s")

        val (buildResult, failure) = try {
            runner.build()!! to null
        } catch (ex: UnexpectedBuildFailure) {
            ex.buildResult!! to ex
        }

        println(buildResult.output)

        val richReportFile = inputBuildDir.resolve("binaryCompatibility/build/japi/japi.html").apply {
            assertTrue("Rich report file exists", isFile)
        }

        return CheckResult(failure, scrapeRichReport(richReportFile), buildResult).apply {
            println(richReport.toText())
            block()
        }
    }

    internal
    data class CheckResult(
        val checkFailure: UnexpectedBuildFailure?,
        val richReport: RichReport,
        val buildResult: BuildResult
    ) {

        val isBinaryCompatible = checkFailure == null

        fun assertEmptyReport() {
            assertHasNoError()
            assertHasNoWarning()
            assertHasNoInformation()
        }

        fun assertHasNoError() {
            assertTrue("Has no error (${richReport.errors})", richReport.errors.isEmpty())
        }

        fun assertHasNoWarning() {
            assertTrue("Has no warning (${richReport.warnings})", richReport.warnings.isEmpty())
        }

        fun assertHasNoInformation() {
            assertTrue("Has no information (${richReport.information})", richReport.information.isEmpty())
        }

        fun assertHasErrors(vararg errors: String) {
            assertThat("Has errors", richReport.errors.map { it.message }, CoreMatchers.equalTo(errors.toList()))
        }

        fun assertHasWarnings(vararg warnings: String) {
            assertThat("Has warnings", richReport.warnings.map { it.message }, CoreMatchers.equalTo(warnings.toList()))
        }

        fun assertHasInformation(vararg information: String) {
            assertThat("Has information", richReport.information.map { it.message }, CoreMatchers.equalTo(information.toList()))
        }

        fun assertHasErrors(vararg errors: List<String>) {
            assertHasErrors(*errors.toList().flatten().toTypedArray())
        }

        fun assertHasErrors(vararg errorWithDetail: Pair<String, List<String>>) {
            assertThat("Has errors", richReport.errors, CoreMatchers.equalTo(errorWithDetail.map { ReportMessage(it.first, it.second) }))
        }

        fun newApi(thing: String, desc: String): String =
            "$thing ${describe(thing, desc)}: New public API in 2.0 (@Incubating)"

        fun added(thing: String, desc: String): List<String> =
            listOf(
                "$thing ${describe(thing, desc)}: Is not annotated with @Incubating.",
                "$thing ${describe(thing, desc)}: Is not annotated with @since 2.0."
            )

        fun removed(thing: String, desc: String): Pair<String, List<String>> =
            "$thing ${describe(thing, desc)}: Is not binary compatible." to listOf("$thing has been removed")

        private
        fun describe(thing: String, desc: String) =
            if (thing == "Field") desc else "com.example.$desc"
    }

    protected
    fun File.withFile(path: String, text: String = ""): File =
        resolve(path).apply {
            parentFile.mkdirs()
            writeText(text.trimIndent())
        }

    private
    fun File.withUniqueDirectory(prefixPath: String): File =
        Files.createTempDirectory(
            withDirectory(prefixPath.substringBeforeLast("/")).toPath(),
            prefixPath.substringAfterLast("/")
        ).toFile()

    private
    fun File.withDirectory(path: String): File =
        resolve(path).apply {
            mkdirs()
        }

    private
    fun File.withSettings(text: String = ""): File =
        withFile("settings.gradle.kts", text)

    private
    fun File.withBuildScript(text: String = ""): File =
        withFile("build.gradle.kts", text)
}
