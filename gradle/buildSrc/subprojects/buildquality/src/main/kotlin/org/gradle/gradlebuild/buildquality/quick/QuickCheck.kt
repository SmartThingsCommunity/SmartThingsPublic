/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.gradlebuild.buildquality.quick

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.cache.internal.GeneratedGradleJarCache
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.experiments.plugins.GradleKotlinDslKtlintConventionPlugin
import org.gradle.kotlin.dsl.support.serviceOf
import java.util.concurrent.Callable


class QuickCheckPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("quickCheck", QuickCheckTask::class.java)
    }
}


abstract class QuickCheckTask() : DefaultTask() {
    @TaskAction
    fun execute() {
        if (System.getenv("IGNORE_QUICK_CHECK") != null) {
            return
        }
        val changedFiles = getChangedFiles()

        project.configurations.create("quickCheck")
        val checks = Check.values().filter { it.filter(changedFiles).isNotEmpty() }
        checks.forEach { it.addDependencies(project) }
        checks.forEach { it.runCheck(project, it.filter(changedFiles)) }
    }

    //    A       buildSrc/subprojects/buildquality/src/main/kotlin/org/gradle/gradlebuild/buildquality/quick/QuickCheck.kt
    //    M       gradle.properties
    //    D       gradle/Check.java
    private
    fun getChangedFiles(): List<String> =
        project.execAndGetStdout("git", "diff", "--cached", "--name-status", "HEAD")
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("D") }
            .map { line -> line.replaceFirst("^[A-Z]+\\s+".toRegex(), "") }
}


private
val rulesetChecksum by lazy {
    GradleKotlinDslKtlintConventionPlugin::class.java.getResource("gradle-kotlin-dsl-ruleset.md5").readText()
}


private
val rulesetJar by lazy {
    GradleKotlinDslKtlintConventionPlugin::class.java.getResource("gradle-kotlin-dsl-ruleset.jar")
}


enum class Check(private val extension: String) {
    JAVA(".java") {
        override fun runCheck(project: Project, filesToBeChecked: List<String>) {
            project.javaexec {
                main = "com.puppycrawl.tools.checkstyle.Main"
                args("-c")
                args("config/checkstyle/checkstyle.xml")
                filesToBeChecked.forEach { args(it) }
                jvmArgs("-Dconfig_loc=config/checkstyle")
                classpath = project.configurations["quickCheck"]
                systemProperty("config_location", "config/checkstyle")
            }
        }

        override fun addDependencies(project: Project) {
            project.dependencies.add("quickCheck", "com.puppycrawl.tools:checkstyle:8.28")
        }
    },
    GROOVY(".groovy") {
        override fun runCheck(project: Project, filesToBeChecked: List<String>) {
            val groovyDirs = groupByDir(filesToBeChecked).keys
            groovyDirs.forEach { groovyDir ->
                val rulesets = if (groovyDir.contains("integTest")) "config/codenarc.xml" else "config/codenarc-integtests.xml"
                project.javaexec {
                    main = "org.codenarc.CodeNarc"
                    args("-basedir=$groovyDir")
                    args("-rulesetfiles=${project.file(rulesets).toURI()}")
                    args("-report=console")
                    args("-maxPriority1Violations=0")
                    args("-maxPriority2Violations=0")
                    args("-maxPriority3Violations=0")
                    classpath = project.configurations["quickCheck"]
                }
            }
        }

        override fun addDependencies(project: Project) {
            val ruleClass = Class.forName("org.gradle.gradlebuild.buildquality.codenarc.IntegrationTestFixturesRule", false, this.javaClass.classLoader)
            project.dependencies.add("quickCheck", project.dependencies.localGroovy())
            project.dependencies.add("quickCheck", project.dependencies.embeddedKotlin("stdlib"))
            project.dependencies.add("quickCheck", "org.slf4j:slf4j-api:1.7.28")
            project.dependencies.add("quickCheck", project.files(ruleClass.protectionDomain!!.codeSource!!.location))
            project.dependencies.add("quickCheck", "org.codenarc:CodeNarc:1.5") {
                isTransitive = false
            }
        }

        private
        fun groupByDir(files: List<String>): Map<String, List<String>> = files.groupBy { filePath ->
            val filePathArray = filePath.split("/")
            filePathArray.subList(0, filePathArray.size - 1).joinToString("/")
        }
    },
    KOTLIN(".kt") {
        override fun runCheck(project: Project, filesToBeChecked: List<String>) {
            val nonTeamCityKtFiles = filesToBeChecked.filter { !it.startsWith(".teamcity") }
            if (nonTeamCityKtFiles.isEmpty()) {
                println("Only .teamcity kt file changes found, skip.")
                return
            }

            project.javaexec {
                main = "com.github.shyiko.ktlint.Main"
                nonTeamCityKtFiles.forEach { args(it) }
                args("--reporter=plain")
                args("--color")
                classpath = project.configurations["quickCheck"]
            }
        }

        override fun addDependencies(project: Project) {
            project.dependencies.add("quickCheck", project.files(project.gradleKotlinDslKtlintRulesetJar()))
            project.dependencies.add("quickCheck", "com.github.shyiko:ktlint:0.30.0") {
                exclude(group = "com.github.shyiko.ktlint", module = "ktlint-ruleset-standard")
            }
        }

        private
        fun Project.gradleKotlinDslKtlintRulesetJar() = Callable {
            serviceOf<GeneratedGradleJarCache>().get("ktlint-convention-ruleset-$rulesetChecksum") {
                outputStream().use { it.write(rulesetJar.readBytes()) }
            }
        }
    };


    abstract fun runCheck(project: Project, filesToBeChecked: List<String>)
    abstract fun addDependencies(project: Project)

    fun filter(changedFiles: List<String>) = changedFiles.filter { it.endsWith(extension) }
}
