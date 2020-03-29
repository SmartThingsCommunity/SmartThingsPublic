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

package org.gradle.api.tasks

import org.gradle.initialization.StartParameterBuildOptions.BuildCacheDebugLoggingOption
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.DirectoryBuildCacheFixture
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.integtests.fixtures.executer.GradleContextualExecuter
import org.gradle.internal.jvm.Jvm
import org.gradle.test.fixtures.file.TestFile
import org.gradle.util.TextUtil
import spock.lang.IgnoreIf
import spock.lang.Unroll

class CachedTaskExecutionIntegrationTest extends AbstractIntegrationSpec implements DirectoryBuildCacheFixture {
    public static final String ORIGINAL_HELLO_WORLD = """
            public class Hello {
                public static void main(String... args) {
                    System.out.println("Hello World!");
                }
            }
        """
    public static final String CHANGED_HELLO_WORLD = """
            public class Hello {
                public static void main(String... args) {
                    System.out.println("Hello World with Changes!");
                }
            }
        """

    def setup() {
        setupProjectInDirectory(testDirectory)
    }

    private static void setupProjectInDirectory(TestFile dir, String sourceDir = "main") {
        dir.file("build.gradle") << """
            apply plugin: "java"
        """

        dir.file("src/$sourceDir/java/Hello.java") << ORIGINAL_HELLO_WORLD
        dir.file("src/$sourceDir/resources/resource.properties") << """
            test=true
        """

        if (sourceDir != "main") {
            dir.file("build.gradle") << """
                sourceSets {
                    main {
                        java {
                            srcDir "src/$sourceDir/java"
                        }
                        resources {
                            srcDir "src/$sourceDir/resources"
                        }
                    }
                }
            """
        }
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "no task is re-executed when inputs are unchanged"() {
        when:
        withBuildCache().run "jar"
        then:
        noneSkipped()

        expect:
        withBuildCache().run "clean"

        when:
        withBuildCache().run "jar"
        then:
        skipped ":compileJava"
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "cached tasks are executed with #rerunMethod"() {
        expect:
        cacheDir.listFiles() as List == []
        buildFile << """
            tasks.withType(JavaCompile).configureEach { it.outputs.upToDateWhen { project.findProperty("upToDateWhenFalse") == null } }
        """

        when:
        withBuildCache().run "jar"
        def originalCacheContents = listCacheFiles()
        def originalModificationTimes = originalCacheContents.collect { file -> TestFile.makeOlder(file); file.lastModified() }
        then:
        noneSkipped()
        originalCacheContents.size() > 0

        expect:
        withBuildCache().run "clean"

        when:
        withBuildCache().run "jar", rerunMethod
        def updatedCacheContents = listCacheFiles()
        def updatedModificationTimes = updatedCacheContents*.lastModified()
        then:
        executedAndNotSkipped ":compileJava", ":jar"
        updatedCacheContents == originalCacheContents
        originalModificationTimes.size().times { i ->
            assert originalModificationTimes[i] < updatedModificationTimes[i]
        }

        where:
        rerunMethod << ["--rerun-tasks", "-PupToDateWhenFalse=true"]
    }

    @ToBeFixedForInstantExecution
    def "task results don't get stored when pushing is disabled"() {
        settingsFile << """
            buildCache {
                local {
                    push = false
                }
            }
        """

        when:
        withBuildCache().run "jar"
        then:
        noneSkipped()

        expect:
        withBuildCache().run "clean"

        when:
        withBuildCache().run "jar"
        then:
        executedAndNotSkipped ":compileJava", ":jar"
    }

    @ToBeFixedForInstantExecution
    def "outputs are correctly loaded from cache"() {
        buildFile << """
            apply plugin: "application"
            mainClassName = "Hello"
        """
        withBuildCache().run "run"
        withBuildCache().run "clean"
        expect:
        withBuildCache().run "run"
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "tasks get cached when source code changes without changing the compiled output"() {
        when:
        withBuildCache().run "assemble"
        then:
        noneSkipped()

        file("src/main/java/Hello.java") << """
            // Change to source file without compiled result change
        """
        withBuildCache().run "clean"

        when:
        withBuildCache().run "assemble"
        then:
        executedAndNotSkipped ":compileJava"
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "tasks get cached when source code changes back to previous state"() {
        expect:
        withBuildCache().run "jar" assertTaskNotSkipped ":compileJava" assertTaskNotSkipped ":jar"

        when:
        file("src/main/java/Hello.java").text = CHANGED_HELLO_WORLD
        then:
        withBuildCache().run "jar" assertTaskNotSkipped ":compileJava" assertTaskNotSkipped ":jar"

        when:
        file("src/main/java/Hello.java").text = ORIGINAL_HELLO_WORLD
        then:
        withBuildCache().run "jar"
        result.assertTaskSkipped ":compileJava"
    }

    def "clean doesn't get cached"() {
        withBuildCache().run "assemble"
        withBuildCache().run "clean"
        withBuildCache().run "assemble"
        when:
        withBuildCache().run "clean"
        then:
        executedAndNotSkipped ":clean"
    }

    def "task gets loaded from cache when it is executed from a different directory"() {
        // Compile Java in a different copy of the project
        def remoteProjectDir = file("remote-project")
        setupProjectInDirectory(remoteProjectDir)

        when:
        executer.inDirectory(remoteProjectDir)
        remoteProjectDir.file("settings.gradle") << """
            buildCache {
                local {
                    directory = '${cacheDir.absoluteFile.toURI()}'
                }
            }
        """
        withBuildCache().run "compileJava"
        then:
        noneSkipped()
        remoteProjectDir.file("build/classes/java/main/Hello.class").exists()

        // Remove the project completely
        remoteProjectDir.deleteDir()

        when:
        withBuildCache().run "compileJava"
        then:
        skipped ":compileJava"
        javaClassFile("Hello.class").exists()
    }

    def "compile task gets loaded from cache when source is moved to another directory"() {
        def remoteProjectDir = file("remote-project")
        setupProjectInDirectory(remoteProjectDir, "other-than-main")

        when:
        executer.inDirectory(remoteProjectDir)
        remoteProjectDir.file("settings.gradle") << """
            buildCache {
                local {
                    directory = '${cacheDir.absoluteFile.toURI()}'
                }
            }
        """
        withBuildCache().run "compileJava"
        then:
        noneSkipped()
        remoteProjectDir.file("build/classes/java/main/Hello.class").exists()

        remoteProjectDir.deleteDir()

        when:
        withBuildCache().run "compileJava"
        then:
        skipped ":compileJava"
        javaClassFile("Hello.class").exists()
    }

    def "error message contains spec which failed to evaluate"() {
        given:
        buildFile << """
            task adHocTask {
                inputs.property("input") { true }
                outputs.file("someFile")
                outputs.cacheIf("on CI") { throw new RuntimeException() }
                doLast {
                    println "Success"
                }
            }
        """

        when:
        withBuildCache().fails 'adHocTask'

        then:
        failure.assertHasDescription("Execution failed for task ':adHocTask'.")
        failure.assertHasCause("Could not evaluate spec for 'on CI'.")
    }

    @IgnoreIf({ GradleContextualExecuter.parallel })
    @ToBeFixedForInstantExecution
    def "can load twice from the cache with no changes"() {
        given:
        buildFile << """
            apply plugin: "application"
            mainClassName = "Hello"
        """

        when:
        withBuildCache().run 'clean', 'run'

        then:
        executedAndNotSkipped ':compileJava'

        when:
        withBuildCache().run 'clean', 'run'

        then:
        skipped ':compileJava'

        when:
        withBuildCache().run 'clean', 'run'

        then:
        skipped ':compileJava'
    }

    @ToBeFixedForInstantExecution
    def "outputs loaded from the cache are snapshotted as outputs"() {
        buildFile << """
            apply plugin: 'base'

            task createOutput {
                def outputFile = file('build/output.txt')
                def inputFile = file('input.txt')
                inputs.file inputFile
                inputs.file 'unrelated-input.txt'
                outputs.file outputFile
                outputs.cacheIf { true }
                doLast {
                    if (!outputFile.exists() || outputFile.text != inputFile.text) {
                        outputFile.parentFile.mkdirs()
                        outputFile.text = file('input.txt').text
                    }
                }
            }
        """.stripIndent()

        def inputFile = file('input.txt')
        inputFile.text = "input text"
        def unrelatedInputFile = file('unrelated-input.txt')
        unrelatedInputFile.text = "not part of the input"
        def outputFile = file('build/output.txt')

        when:
        def taskPath = ':createOutput'
        withBuildCache().run taskPath

        then:
        executedAndNotSkipped taskPath
        outputFile.text == "input text"

        when:
        succeeds 'clean'
        withBuildCache().run taskPath

        then:
        skipped taskPath
        outputFile.text == "input text"

        when:
        unrelatedInputFile.text = "changed input"
        succeeds taskPath

        then:
        executedAndNotSkipped taskPath
        outputFile.text == "input text"

        when:
        outputFile.text = "that should not be the output"
        succeeds taskPath

        then:
        // If the output wouldn't have been captured then the task would be up to date
        executedAndNotSkipped taskPath
        outputFile.text == "input text"
    }

    @ToBeFixedForInstantExecution
    def "no caching happens when local cache is disabled"() {
        settingsFile << """
            buildCache {
                local {
                    enabled = false
                }
            }
        """
        when:
        withBuildCache().run "jar"
        then:
        noneSkipped()

        expect:
        withBuildCache().run "clean"

        when:
        withBuildCache().run "jar"
        then:
        noneSkipped()
    }

    @ToBeFixedForInstantExecution
    def "task with custom actions gets logged"() {
        when:
        withBuildCache().run "compileJava", "--info"
        then:
        noneSkipped()
        !output.contains("Custom actions are attached to task ':compileJava'.")

        expect:
        withBuildCache().run "clean"

        when:
        buildFile << """
            compileJava.doFirst { println "Custom action" }
        """
        withBuildCache().run "compileJava", "--info"
        then:
        noneSkipped()
        output.contains("Custom actions are attached to task ':compileJava'.")
    }

    def "input hashes are reported if build cache debugging is enabled"() {
        when:
        buildFile << """
            compileJava.doFirst { }
        """.stripIndent()
        withBuildCache().run "compileJava", "-D${BuildCacheDebugLoggingOption.GRADLE_PROPERTY}=true"

        then:
        noneSkipped()
        outputContains("Appending implementation to build cache key:")
        outputContains("Appending additional implementation to build cache key:")
        outputContains("Appending input value fingerprint for 'options.fork'")
        outputContains("Appending input file fingerprints for 'classpath'")
        def sourcesDebugLogging = "Appending input file fingerprints for 'stableSources' to build cache key: "
        outputContains(sourcesDebugLogging)
        outputContains("Build cache key for task ':compileJava' is ")
        outputContains("Appending output property name to build cache key: destinationDir")

        def stableInputsFingerprintLog = result.getOutputLineThatContains(sourcesDebugLogging)
        stableInputsFingerprintLog.contains("RELATIVE_PATH{${testDirectory.absolutePath}")
        stableInputsFingerprintLog.contains("java=IGNORED / DIR")
        stableInputsFingerprintLog.contains("Hello.java='Hello.java' / ")
    }

    def "only the build cache key is reported at the info level"() {
        when:
        buildFile << """
            compileJava.doFirst { }
        """.stripIndent()
        withBuildCache().run "compileJava", "--info"

        then:
        noneSkipped()
        output.contains("Build cache key for task ':compileJava' is ")
        !output.contains("Appending implementation to build cache key:")
        !output.contains("Appending input value fingerprint for")
        !output.contains("Appending input file fingerprints for 'classpath'")
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "compileJava is not cached if forked executable is used"() {
        buildFile << """
            compileJava.options.fork = true
            compileJava.options.forkOptions.executable = "${TextUtil.escapeString(Jvm.current().getExecutable("javac"))}"
        """

        when:
        withBuildCache().run "compileJava", "--info"
        then:
        noneSkipped()
        output.contains "Caching disabled for task ':compileJava' because:\n" +
            "  'Forking compiler via ForkOptions.executable' satisfied"

        expect:
        succeeds "clean"

        when:
        withBuildCache().run "compileJava"
        then:
        noneSkipped()
    }

    @ToBeFixedForInstantExecution
    def "order of resources on classpath does not affect how we calculate the cache key"() {
        buildFile << """
            apply plugin: 'base'

            @CacheableTask
            class CustomTask extends DefaultTask {
                @OutputFile File outputFile = new File(temporaryDir, "output.txt")
                @Classpath FileCollection classpath = project.fileTree("resources")

                @TaskAction void generate() {
                    outputFile.text = "done"
                }
            }

            task cacheable(type: CustomTask)
        """

        def resources = file("resources")
        resources.mkdirs()

        def make = { String resource ->
            resources.file(resource).text = "content"
        }
        def gradleUserHome = file("gradle-user-home")
        executer.withGradleUserHomeDir(gradleUserHome)

        // Make A then B and populate cache
        expect:
        make("A")
        succeeds("cacheable")
        make("B")
        // populate the cache
        withBuildCache().run("cacheable")

        when:
        gradleUserHome.deleteDir() // nuke the file snapshot cache
        resources.deleteDir().mkdirs()
        succeeds("clean")
        and:
        // Building with the resources seen in the opposite order
        // shouldn't make a difference
        make("B")
        succeeds("cacheable")
        make("A")
        withBuildCache().run("cacheable")
        then:
        result.assertTaskSkipped(":cacheable")
    }
}
