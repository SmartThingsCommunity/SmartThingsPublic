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

package org.gradle.kotlin.dsl.plugins.precompiled

import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution

import org.gradle.kotlin.dsl.fixtures.FoldersDsl
import org.gradle.kotlin.dsl.fixtures.bytecode.InternalName
import org.gradle.kotlin.dsl.fixtures.bytecode.RETURN
import org.gradle.kotlin.dsl.fixtures.bytecode.internalName
import org.gradle.kotlin.dsl.fixtures.bytecode.publicClass
import org.gradle.kotlin.dsl.fixtures.bytecode.publicDefaultConstructor
import org.gradle.kotlin.dsl.fixtures.bytecode.publicMethod
import org.gradle.kotlin.dsl.fixtures.containsMultiLineString
import org.gradle.kotlin.dsl.fixtures.normalisedPath
import org.gradle.kotlin.dsl.fixtures.pluginDescriptorEntryFor
import org.gradle.kotlin.dsl.support.zipTo

import org.gradle.test.fixtures.file.LeaksFileHandles
import org.gradle.util.TextUtil.replaceLineSeparatorsOf

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.psi.psiUtil.toVisibility
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierType
import org.junit.Assert.assertTrue

import org.junit.Test

import java.io.File


@LeaksFileHandles("Kotlin Compiler Daemon working directory")
class PrecompiledScriptPluginAccessorsTest : AbstractPrecompiledScriptPluginTest() {

    @ToBeFixedForInstantExecution
    @Test
    fun `can use type-safe accessors for applied plugins with CRLF line separators`() {

        withKotlinDslPlugin()

        withPrecompiledKotlinScript("my-java-library.gradle.kts",
            replaceLineSeparatorsOf(
                """
                plugins { java }

                java { }

                tasks.compileJava { }
                """,
                "\r\n"
            )
        )

        compileKotlin()
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `fails the build with help message for plugin spec with version`() {

        withDefaultSettings().appendText("""
            rootProject.name = "invalid-plugin"
        """)

        withKotlinDslPlugin()

        withPrecompiledKotlinScript("invalid-plugin.gradle.kts", """
            plugins {
                id("a.plugin") version "1.0"
            }
        """)

        assertThat(
            buildFailureOutput("assemble"),
            containsMultiLineString("""
                Invalid plugin request [id: 'a.plugin', version: '1.0']. Plugin requests from precompiled scripts must not include a version number. Please remove the version from the offending request and make sure the module containing the requested plugin 'a.plugin' is an implementation dependency of root project 'invalid-plugin'.
            """)
        )
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can use type-safe accessors with same name but different meaning in sibling plugins`() {

        val externalPlugins = withExternalPlugins()

        withFolders {
            "buildSrc" {
                withDefaultSettingsIn(relativePath)
                withKotlinDslPlugin().appendText(
                    implementationDependencyOn(externalPlugins)
                )

                withFile("src/main/kotlin/local-app.gradle.kts", """
                    plugins { `external-app` }
                    println("*using " + external.name + " from local-app in " + project.name + "*")
                """)

                withFile("src/main/kotlin/local-lib.gradle.kts", """
                    plugins { `external-lib` }
                    println("*using " + external.name + " from local-lib in " + project.name + "*")
                """)
            }
        }

        withDefaultSettings().appendText("""
            include("foo")
            include("bar")
        """)

        withFolders {
            "foo" {
                withFile("build.gradle.kts", """
                    plugins { `local-app` }
                """)
            }
            "bar" {
                withFile("build.gradle.kts", """
                    plugins { `local-lib` }
                """)
            }
        }

        assertThat(
            build("tasks").output,
            allOf(
                containsString("*using app from local-app in foo*"),
                containsString("*using lib from local-lib in bar*")
            )
        )
    }

    private
    fun implementationDependencyOn(file: File): String = """
        dependencies {
            implementation(files("${file.normalisedPath}"))
        }
    """

    @Test
    @ToBeFixedForInstantExecution
    fun `can use type-safe accessors for the Kotlin Gradle plugin extensions`() {

        withKotlinDslPlugin().appendText("""
            dependencies {
                implementation(kotlin("gradle-plugin"))
            }
        """)

        withPrecompiledKotlinScript("kotlin-library.gradle.kts", """

            plugins { kotlin("jvm") }

            kotlin { }

            tasks.compileKotlin { kotlinOptions { } }

        """)

        build("generatePrecompiledScriptPluginAccessors")

        compileKotlin()
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can use type-safe accessors for plugins applied by sibling plugin`() {

        withKotlinDslPlugin()

        withPrecompiledKotlinScript("my-java-library.gradle.kts", """
            plugins { java }
        """)

        withPrecompiledKotlinScript("my-java-module.gradle.kts", """
            plugins { id("my-java-library") }

            java { }

            tasks.compileJava { }
        """)

        compileKotlin()
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can use type-safe accessors from scripts with same name but different ids`() {

        val externalPlugins = withExternalPlugins()

        withKotlinDslPlugin()
        withKotlinBuildSrc()
        withFolders {
            "buildSrc" {
                existing("build.gradle.kts").appendText(
                    implementationDependencyOn(externalPlugins)
                )
                "src/main/kotlin" {
                    withFile("app/model.gradle.kts", """
                        package app
                        plugins { `external-app` }
                        println("*using " + external.name + " from app/model in " + project.name + "*")
                    """)
                    withFile("lib/model.gradle.kts", """
                        package lib
                        plugins { `external-lib` }
                        println("*using " + external.name + " from lib/model in " + project.name + "*")
                    """)
                }
            }
        }

        withDefaultSettings().appendText("""
            include("lib")
            include("app")
        """)

        withFolders {
            "lib" {
                withFile("build.gradle.kts", """
                    plugins { lib.model }
                """)
            }
            "app" {
                withFile("build.gradle.kts", """
                    plugins { app.model }
                """)
            }
        }

        assertThat(
            build("tasks").output,
            allOf(
                containsString("*using app from app/model in app*"),
                containsString("*using lib from lib/model in lib*")
            )
        )
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can apply sibling plugin whether it has a plugins block or not`() {

        withKotlinDslPlugin()

        withPrecompiledKotlinScript("no-plugins.gradle.kts", "")
        withPrecompiledKotlinScript("java-plugin.gradle.kts", """
            plugins { java }
        """)

        withPrecompiledKotlinScript("plugins.gradle.kts", """
            plugins {
                id("no-plugins")
                id("java-plugin")
            }

            java { }

            tasks.compileJava { }
        """)

        compileKotlin()
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can apply sibling plugin from another package`() {

        withKotlinDslPlugin()

        withPrecompiledKotlinScript("my/java-plugin.gradle.kts", """
            package my
            plugins { java }
        """)

        withPrecompiledKotlinScript("plugins.gradle.kts", """
            plugins { id("my.java-plugin") }

            java { }

            tasks.compileJava { }
        """)

        compileKotlin()
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `generated type-safe accessors are internal`() {

        givenPrecompiledKotlinScript("java-project.gradle.kts", """
            plugins { java }
        """)

        val generatedSourceFiles =
            existing("build/generated-sources")
                .walkTopDown()
                .filter { it.isFile }
                .toList()

        assertTrue(
            generatedSourceFiles.isNotEmpty()
        )

        data class Declaration(
            val packageName: String,
            val name: String,
            val visibility: Visibility?
        )

        val generatedAccessors =
            KotlinParser.run {
                withProject {
                    generatedSourceFiles.flatMap { file ->
                        parse(file.name, file.readText()).run {
                            val packageName = packageFqName.asString()
                            declarations.map { declaration ->
                                Declaration(
                                    packageName,
                                    declaration.name!!,
                                    declaration.visibilityModifierType()?.toVisibility()
                                )
                            }
                        }
                    }
                }
            }

        assertThat(
            "Only the generated Gradle Plugin wrapper is not internal",
            generatedAccessors.filterNot { it.visibility == Visibilities.INTERNAL },
            equalTo(
                listOf(
                    Declaration(
                        "",
                        "JavaProjectPlugin",
                        null
                    )
                )
            )
        )
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can use core plugin spec builders`() {

        givenPrecompiledKotlinScript("java-project.gradle.kts", """

            plugins {
                java
            }

        """)

        val (project, pluginManager) = projectAndPluginManagerMocks()

        instantiatePrecompiledScriptOf(
            project,
            "Java_project_gradle"
        )

        inOrder(pluginManager) {
            verify(pluginManager).apply("org.gradle.java")
            verifyNoMoreInteractions()
        }
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can use plugin spec builders for plugins in the implementation classpath`() {

        // given:
        val pluginId = "my.plugin"
        val pluginJar = jarForPlugin(pluginId, "MyPlugin")

        withPrecompiledScriptApplying(pluginId, pluginJar)
        assertPrecompiledScriptPluginApplies(
            pluginId,
            "Plugin_gradle"
        )
    }

    private
    fun assertPrecompiledScriptPluginApplies(pluginId: String, precompiledScriptClassName: String) {

        compileKotlin()

        val (project, pluginManager) = projectAndPluginManagerMocks()

        instantiatePrecompiledScriptOf(
            project,
            precompiledScriptClassName
        )

        inOrder(pluginManager) {
            verify(pluginManager).apply(pluginId)
            verifyNoMoreInteractions()
        }
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can use plugin specs with jruby-gradle-plugin`() {

        withKotlinDslPlugin().appendText("""
            dependencies {
                implementation("com.github.jruby-gradle:jruby-gradle-plugin:1.4.0")
            }
        """)

        withPrecompiledKotlinScript("plugin.gradle.kts", """
            plugins {
                com.github.`jruby-gradle`.base
            }
        """)

        assertPrecompiledScriptPluginApplies(
            "com.github.jruby-gradle.base",
            "Plugin_gradle"
        )
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `plugin application errors are reported but don't cause the build to fail`() {

        // given:
        val pluginId = "invalid.plugin"
        val pluginJar = jarWithInvalidPlugin(pluginId, "InvalidPlugin")

        withPrecompiledScriptApplying(pluginId, pluginJar)

        gradleExecuterFor(arrayOf("classes")).withStackTraceChecksDisabled().run().apply {
            assertOutputContains("An exception occurred applying plugin request [id: '$pluginId']")
            assertOutputContains("'InvalidPlugin' is neither a plugin or a rule source and cannot be applied.")
        }
    }

    private
    fun withPrecompiledScriptApplying(pluginId: String, pluginJar: File) {

        withKotlinDslPlugin().appendText("""

            dependencies {
                implementation(files("${pluginJar.normalisedPath}"))
            }

        """)

        withPrecompiledKotlinScript("plugin.gradle.kts", """

            plugins { $pluginId }

        """)
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can use plugin spec builders in multi-project builds with local and external plugins`() {

        testPluginSpecBuildersInMultiProjectBuildWithPluginsFromPackage(null)
    }

    @Test
    @ToBeFixedForInstantExecution
    fun `can use plugin spec builders in multi-project builds with local and external plugins sharing package name`() {

        testPluginSpecBuildersInMultiProjectBuildWithPluginsFromPackage("p")
    }

    private
    fun testPluginSpecBuildersInMultiProjectBuildWithPluginsFromPackage(packageName: String?) {

        val packageDeclaration = packageName?.let { "package $it" } ?: ""
        val packageQualifier = packageName?.let { "$it." } ?: ""

        withProjectRoot(newDir("external-plugins")) {
            withFolders {
                "external-foo" {
                    withKotlinDslPlugin()
                    withFile("src/main/kotlin/external-foo.gradle.kts", """
                        $packageDeclaration
                        println("*external-foo applied*")
                    """)
                }
                "external-bar" {
                    withKotlinDslPlugin()
                    withFile("src/main/kotlin/external-bar.gradle.kts", """
                        $packageDeclaration
                        println("*external-bar applied*")
                    """)
                }
                withDefaultSettingsIn(relativePath).appendText("""
                    include("external-foo", "external-bar")
                """)
            }
            build("assemble")
        }

        val externalFoo = existing("external-plugins/external-foo/build/libs/external-foo.jar")
        val externalBar = existing("external-plugins/external-bar/build/libs/external-bar.jar")

        withFolders {
            "buildSrc" {
                "local-foo" {
                    withFile("src/main/kotlin/local-foo.gradle.kts", """
                        $packageDeclaration
                        plugins { $packageQualifier`external-foo` }
                    """)
                    withKotlinDslPlugin().appendText("""
                        dependencies {
                            implementation(files("${externalFoo.normalisedPath}"))
                        }
                    """)
                }
                "local-bar" {
                    withFile("src/main/kotlin/local-bar.gradle.kts", """
                        $packageDeclaration
                        plugins { $packageQualifier`external-bar` }
                    """)
                    withKotlinDslPlugin().appendText("""
                        dependencies {
                            implementation(files("${externalBar.normalisedPath}"))
                        }
                    """)
                }
                withDefaultSettingsIn(relativePath).appendText("""
                    include("local-foo", "local-bar")
                """)
                withFile("build.gradle.kts", """
                    dependencies {
                        subprojects.forEach {
                            runtimeOnly(project(it.path))
                        }
                    }
                """)
            }
        }
        withBuildScript("""
            plugins {
                $packageQualifier`local-foo`
                $packageQualifier`local-bar`
            }
        """)

        assertThat(
            build("tasks").output,
            allOf(
                containsString("*external-foo applied*"),
                containsString("*external-bar applied*")
            )
        )
    }

    private
    fun withExternalPlugins(): File =
        withProjectRoot(newDir("external-plugins")) {
            withDefaultSettings()
            withKotlinDslPlugin()
            withFolders {
                "src/main/kotlin" {
                    "extensions" {
                        withFile("Extensions.kt", """
                            open class App { var name: String = "app" }
                            open class Lib { var name: String = "lib" }
                        """)
                    }
                    withFile("external-app.gradle.kts", """
                        extensions.create("external", App::class)
                    """)
                    withFile("external-lib.gradle.kts", """
                        extensions.create("external", Lib::class)
                    """)
                }
            }
            build("assemble")
            existing("build/libs/external-plugins.jar")
        }

    private
    fun FoldersDsl.withKotlinDslPlugin(): File =
        withKotlinDslPluginIn(relativePath)

    private
    val FoldersDsl.relativePath
        get() = root.relativeTo(projectRoot).path

    private
    fun jarWithInvalidPlugin(id: String, implClass: String): File =
        pluginJarWith(
            pluginDescriptorEntryFor(id, implClass),
            "$implClass.class" to publicClass(InternalName(implClass))
        )

    private
    fun jarForPlugin(id: String, implClass: String): File =
        pluginJarWith(
            pluginDescriptorEntryFor(id, implClass),
            "$implClass.class" to emptyPluginClassNamed(implClass)
        )

    private
    fun emptyPluginClassNamed(implClass: String): ByteArray =
        publicClass(InternalName(implClass), interfaces = listOf(Plugin::class.internalName)) {
            publicDefaultConstructor()
            publicMethod("apply", "(Ljava/lang/Object;)V") {
                RETURN()
            }
        }

    private
    fun pluginJarWith(vararg entries: Pair<String, ByteArray>): File =
        newFile("my.plugin.jar").also { file ->
            zipTo(file, entries.asSequence())
        }

    private
    fun projectAndPluginManagerMocks(): Pair<Project, PluginManager> {
        val pluginManager = mock<PluginManager>()
        val project = mock<Project> {
            on { getPluginManager() } doReturn pluginManager
            on { project } doAnswer { it.mock as Project }
        }
        return Pair(project, pluginManager)
    }
}
