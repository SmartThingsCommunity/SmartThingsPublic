/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.integtests.resolve.transform

import org.gradle.api.internal.artifacts.ivyservice.CacheLayout
import org.gradle.api.internal.artifacts.transform.DefaultTransformationWorkspace
import org.gradle.cache.internal.LeastRecentlyUsedCacheCleanup
import org.gradle.integtests.fixtures.AbstractHttpDependencyResolutionTest
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.integtests.fixtures.cache.FileAccessTimeJournalFixture
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.server.http.BlockingHttpServer
import org.junit.Rule
import spock.lang.Unroll

import java.util.regex.Pattern

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS
import static org.gradle.test.fixtures.ConcurrentTestUtil.poll

class ArtifactTransformCachingIntegrationTest extends AbstractHttpDependencyResolutionTest implements FileAccessTimeJournalFixture {
    private final static long MAX_CACHE_AGE_IN_DAYS = LeastRecentlyUsedCacheCleanup.DEFAULT_MAX_AGE_IN_DAYS_FOR_RECREATABLE_CACHE_ENTRIES

    @Rule BlockingHttpServer blockingHttpServer = new BlockingHttpServer()

    def setup() {
        settingsFile << """
            rootProject.name = 'root'
            include 'lib'
            include 'util'
            include 'app'
        """
        buildFile << resolveTask << """
            import org.gradle.api.artifacts.transform.TransformParameters
        """
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "transform is applied to each file once per build"() {
        given:
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform() << withJarTasks() << withLibJarDependency("lib3.jar")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [lib1.jar.txt, lib2.jar.txt, lib3.jar.txt]") == 2
        output.count("ids: [lib1.jar.txt (project :lib), lib2.jar.txt (project :lib), lib3.jar.txt (lib3.jar)]") == 2
        output.count("components: [project :lib, project :lib, lib3.jar]") == 2

        output.count("Transformed") == 3
        isTransformed("lib1.jar", "lib1.jar.txt")
        isTransformed("lib2.jar", "lib2.jar.txt")
        isTransformed("lib3.jar", "lib3.jar.txt")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [lib1.jar.txt, lib2.jar.txt, lib3.jar.txt]") == 2

        output.count("Transformed") == 0
    }

    def "task cannot write into transform directory"() {
        def forbiddenPath = ".transforms/not-allowed.txt"

        buildFile << """
            subprojects {
                task badTask {
                    outputs.file { project.layout.buildDirectory.file("${forbiddenPath}") }
                    doLast { }
                }
            }
        """

        when:
        fails "badTask", "--continue"
        then:
        ['lib', 'app', 'util'].each {
            failure.assertHasDescription("A problem was found with the configuration of task ':${it}:badTask' (type 'DefaultTask').")
            failure.assertHasCause("The output ${file("${it}/build/${forbiddenPath}")} must not be in a reserved location.")
        }
    }

    def "scheduled transformation is invoked before consuming task is executed"() {
        given:
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform() << withJarTasks()

        when:
        succeeds ":util:resolve"

        def transformationPosition1 = output.indexOf("> Transform artifact lib1.jar (project :lib) with FileSizer")
        def transformationPosition2 = output.indexOf("> Transform artifact lib2.jar (project :lib) with FileSizer")
        def taskPosition = output.indexOf("> Task :util:resolve")

        then:
        transformationPosition1 >= 0
        transformationPosition2 >= 0
        taskPosition >= 0
        transformationPosition1 < taskPosition
        transformationPosition2 < taskPosition
    }

    def "scheduled transformation is only invoked once per subject"() {
        given:
        settingsFile << """
            include 'util2'
        """
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform() << withJarTasks()
        buildFile << """
            project(':util2') {
                dependencies {
                    compile project(':lib')
                }
            }
        """

        when:
        succeeds ":util:resolve", ":util2:resolve"

        then:
        output.count("> Transform artifact lib1.jar (project :lib) with FileSizer") == 1
        output.count("> Transform artifact lib2.jar (project :lib) with FileSizer") == 1
    }

    def "scheduled chained transformation is only invoked once per subject"() {
        given:
        settingsFile << """
            include 'app1'
            include 'app2'
        """
        buildFile << """
            def color = Attribute.of('color', String)

            allprojects {
                dependencies {
                    attributesSchema {
                        attribute(color)
                    }
                }
                configurations {
                    compile {
                        attributes.attribute color, 'blue'
                    }
                }
                task resolveRed(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(color, 'red') }
                    }.artifacts
                }
                task resolveYellow(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(color, 'yellow') }
                    }.artifacts
                }
            }

            configure([project(':app1'), project(':app2')]) {

                dependencies {
                    compile project(':lib')

                    registerTransform(MakeBlueToGreenThings) {
                        from.attribute(Attribute.of('color', String), "blue")
                        to.attribute(Attribute.of('color', String), "green")
                    }
                    registerTransform(MakeGreenToRedThings) {
                        from.attribute(Attribute.of('color', String), "green")
                        to.attribute(Attribute.of('color', String), "red")
                    }
                    registerTransform(MakeGreenToYellowThings) {
                        from.attribute(Attribute.of('color', String), "green")
                        to.attribute(Attribute.of('color', String), "yellow")
                    }
                }
            }

            abstract class MakeThingsColored implements TransformAction<TransformParameters.None> {
                private final String targetColor

                MakeThingsColored(String targetColor) {
                    this.targetColor = targetColor
                }

                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                void transform(TransformOutputs outputs) {
                    def input = inputArtifact.get().asFile
                    def output = outputs.file(input.name + ".\${targetColor}")
                    assert output.parentFile.directory && output.parentFile.list().length == 0
                    println "Transforming \${input.name} to \${output.name}"
                    println "Input exists: \${input.exists()}"
                    output.text = String.valueOf(input.length())
                }
            }

            abstract class MakeGreenToRedThings extends MakeThingsColored {
                MakeGreenToRedThings() {
                    super('red')
                }
            }

            abstract class MakeGreenToYellowThings extends MakeThingsColored {
                MakeGreenToYellowThings() {
                    super('yellow')
                }
            }

            abstract class MakeBlueToGreenThings extends MakeThingsColored {
                MakeBlueToGreenThings() {
                    super('green')
                }
            }
        """ << withJarTasks()

        when:
        run ":app1:resolveRed", ":app2:resolveYellow"

        then:
        output.count("> Transform artifact lib1.jar (project :lib) with MakeBlueToGreenThings") == 1
        output.count("> Transform artifact lib2.jar (project :lib) with MakeBlueToGreenThings") == 1
        output.count("> Transform artifact lib1.jar (project :lib) with MakeGreenToYellowThings") == 1
        output.count("> Transform artifact lib2.jar (project :lib) with MakeGreenToYellowThings") == 1
        output.count("> Transform artifact lib1.jar (project :lib) with MakeGreenToRedThings") == 1
        output.count("> Transform artifact lib2.jar (project :lib) with MakeGreenToRedThings") == 1
    }

    def "executes transform immediately when required during task graph building"() {
        buildFile << declareAttributes() << withJarTasks() << """
            import org.gradle.api.artifacts.transform.TransformParameters

            abstract class MakeGreen implements TransformAction<TransformParameters.None> {
                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                @Override
                void transform(TransformOutputs outputs) {
                    outputs.file(inputArtifact.get().asFile.name + ".green").text = "very green"
                }
            }

            project(':util') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':app') {
                dependencies {
                    compile project(':util')

                    registerTransform(MakeGreen) {
                        from.attribute(artifactType, 'jar')
                        to.attribute(artifactType, 'green')
                    }
                }
                configurations {
                    green {
                        extendsFrom(compile)
                        canBeResolved = true
                        canBeConsumed = false
                        attributes {
                            attribute(artifactType, 'green')
                        }
                    }
                }

                tasks.register("resolveAtConfigurationTime").configure {
                    inputs.files(configurations.green)
                    configurations.green.each { println it }
                    doLast { }
                }
                tasks.register("declareTransformAsInput").configure {
                    inputs.files(configurations.green)
                    doLast {
                        configurations.green.each { println it }
                    }
                }

                tasks.register("withDependency").configure {
                    dependsOn("resolveAtConfigurationTime")
                }
                tasks.register("toBeFinalized").configure {
                    // We require the task via a finalizer, so the transform node is in UNKNOWN state.
                    finalizedBy("declareTransformAsInput")
                }
            }
        """

        when:
        run(":app:toBeFinalized", "withDependency", "--info")
        then:
        output.count("Transforming artifact lib1.jar (project :lib) with MakeGreen") == 2
        output.count("Transforming artifact lib2.jar (project :lib) with MakeGreen") == 2
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "each file is transformed once per set of configuration parameters"() {
        given:
        buildFile << declareAttributes() << withJarTasks() << withLibJarDependency("lib3.jar") << """
            abstract class TransformWithMultipleTargets implements TransformAction<Parameters> {
                interface Parameters extends TransformParameters {
                    @Input
                    Property<String> getTarget()
                }

                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                void transform(TransformOutputs outputs) {
                    def input = inputArtifact.get().asFile
                    assert input.exists()
                    def output = outputs.file(input.name + ".\${parameters.target.get()}")
                    def outputDirectory = output.parentFile
                    assert outputDirectory.directory && outputDirectory.list().length == 0
                    if (parameters.target.get() == "size") {
                        output.text = String.valueOf(input.length())
                    } else if (parameters.target.get() == "hash") {
                        output.text = 'hash'
                    }
                    println "Transformed \$input.name to \$output.name into \$outputDirectory"
                }
            }

            allprojects {
                dependencies {
                    registerTransform(TransformWithMultipleTargets) {
                        from.attribute(artifactType, 'jar')
                        to.attribute(artifactType, 'size')
                        parameters {
                            target = 'size'
                        }
                    }
                    registerTransform(TransformWithMultipleTargets) {
                        from.attribute(artifactType, 'jar')
                        to.attribute(artifactType, 'hash')
                        parameters {
                            target = 'hash'
                        }
                    }
                }
                task resolveSize(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'size') }
                    }.artifacts
                    identifier = "1"
                }
                task resolveHash(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'hash') }
                    }.artifacts
                    identifier = "2"
                }
                task resolve {
                    dependsOn(resolveHash, resolveSize)
                }
            }

            project(':util') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':app') {
                dependencies {
                    compile project(':util')
                }
            }
        """

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files 1: [lib1.jar.size, lib2.jar.size, lib3.jar.size]") == 2
        output.count("ids 1: [lib1.jar.size (project :lib), lib2.jar.size (project :lib), lib3.jar.size (lib3.jar)]") == 2
        output.count("components 1: [project :lib, project :lib, lib3.jar]") == 2
        output.count("files 2: [lib1.jar.hash, lib2.jar.hash, lib3.jar.hash]") == 2
        output.count("ids 2: [lib1.jar.hash (project :lib), lib2.jar.hash (project :lib), lib3.jar.hash (lib3.jar)]") == 2
        output.count("components 2: [project :lib, project :lib, lib3.jar]") == 2

        output.count("Transformed") == 6
        isTransformed("lib1.jar", "lib1.jar.size")
        isTransformed("lib2.jar", "lib2.jar.size")
        isTransformed("lib3.jar", "lib3.jar.size")
        isTransformed("lib1.jar", "lib1.jar.hash")
        isTransformed("lib2.jar", "lib2.jar.hash")
        isTransformed("lib3.jar", "lib3.jar.hash")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files 1: [lib1.jar.size, lib2.jar.size, lib3.jar.size]") == 2

        output.count("Transformed") == 0
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "can use custom type that does not implement equals() for transform configuration"() {
        given:
        buildFile << declareAttributes() << withJarTasks() << """
            class CustomType implements Serializable {
                String value
            }

            abstract class TransformWithMultipleTargets implements TransformAction<Parameters> {

                interface Parameters extends TransformParameters {
                    @Input
                    CustomType getTarget()
                    void setTarget(CustomType target)
                }

                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                void transform(TransformOutputs outputs) {
                    def input = inputArtifact.get().asFile
                    def output = outputs.file(input.name + ".\${parameters.target.value}")
                    def outputDirectory = output.parentFile
                    if (parameters.target.value == "size") {
                        output.text = String.valueOf(input.length())
                    }
                    if (parameters.target.value == "hash") {
                        output.text = 'hash'
                    }
                    println "Transformed \$input.name to \$output.name into \$outputDirectory"
                }
            }

            allprojects {
                dependencies {
                    registerTransform(TransformWithMultipleTargets) {
                        from.attribute(artifactType, 'jar')
                        to.attribute(artifactType, 'size')
                        parameters {
                            target = new CustomType(value: 'size')
                        }
                    }
                    registerTransform(TransformWithMultipleTargets) {
                        from.attribute(artifactType, 'jar')
                        to.attribute(artifactType, 'hash')
                        parameters {
                            target = new CustomType(value: 'hash')
                        }
                    }
                }
                task resolveSize(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'size') }
                    }.artifacts
                    identifier = "1"
                }

                task resolveHash(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'hash') }
                    }.artifacts
                    identifier = "2"
                }
                task resolve {
                    dependsOn(resolveSize, resolveHash)
                }
            }

            project(':util') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':app') {
                dependencies {
                    compile project(':util')
                }
            }
        """

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files 1: [lib1.jar.size, lib2.jar.size]") == 2
        output.count("files 2: [lib1.jar.hash, lib2.jar.hash]") == 2

        output.count("Transformed") == 4
        isTransformed("lib1.jar", "lib1.jar.size")
        isTransformed("lib2.jar", "lib2.jar.size")
        isTransformed("lib1.jar", "lib1.jar.hash")
        isTransformed("lib2.jar", "lib2.jar.hash")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files 1: [lib1.jar.size, lib2.jar.size]") == 2

        output.count("Transformed") == 0
    }

    @Unroll
    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "can use configuration parameter of type #type"() {
        given:
        buildFile << declareAttributes() << withJarTasks() << """
            abstract class TransformWithMultipleTargets implements TransformAction<Parameters> {

                interface Parameters extends TransformParameters {
                    @Input
                    $type getTarget()
                    void setTarget($type target)
                }

                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                void transform(TransformOutputs outputs) {
                    def input = inputArtifact.get().asFile
                    assert input.exists()
                    def output = outputs.file(input.name + ".value")
                    println "Transformed \$input.name to \$output.name into \$output.parentFile"
                    output.text = String.valueOf(input.length()) + String.valueOf(parameters.target)
                }
            }

            allprojects {
                dependencies {
                    registerTransform(TransformWithMultipleTargets) {
                        from.attribute(artifactType, 'jar')
                        to.attribute(artifactType, 'value')
                        parameters {
                            target = $value
                        }
                    }
                }
                task resolve1(type: Resolve) {
                    identifier = "1"
                }
                task resolve2(type: Resolve) {
                    identifier = "2"
                }
                configure([resolve1, resolve2]) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'value') }
                    }.artifacts
                }
                task resolve {
                    dependsOn(resolve1, resolve2)
                }
            }

            project(':util') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':app') {
                dependencies {
                    compile project(':util')
                }
            }
        """

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files 1: [lib1.jar.value, lib2.jar.value]") == 2
        output.count("files 2: [lib1.jar.value, lib2.jar.value]") == 2

        output.count("Transformed") == 2
        isTransformed("lib1.jar", "lib1.jar.value")
        isTransformed("lib2.jar", "lib2.jar.value")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files 1: [lib1.jar.value, lib2.jar.value]") == 2

        output.count("Transformed") == 0

        where:
        type           | value
        "boolean"      | "true"
        "int"          | "123"
        "List<Object>" | "[123, 'abc']"
        "Named"        | "objects.named(Named, 'abc')"
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "each file is transformed once per transform class"() {
        given:
        buildFile << declareAttributes() << withJarTasks() << withLibJarDependency("lib3.jar") << """
            abstract class Sizer implements TransformAction<Parameters> {
                interface Parameters extends TransformParameters {
                    @Input
                    Property<String> getTarget()
                }

                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                void transform(TransformOutputs outputs) {
                    def input = inputArtifact.get().asFile
                    assert input.exists()
                    def output = outputs.file(input.name + ".size")
                    def outputDirectory = output.parentFile
                    assert outputDirectory.directory && outputDirectory.list().length == 0
                    println "Transformed \$input.name to \$output.name into \$outputDirectory"
                    output.text = String.valueOf(input.length())
                }
            }
            abstract class Hasher implements TransformAction<Parameters> {
                interface Parameters extends TransformParameters {
                    @Input
                    Property<String> getTarget()
                }

                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                void transform(TransformOutputs outputs) {
                    def input = inputArtifact.get().asFile
                    assert input.exists()
                    def output = outputs.file(input.name + ".hash")
                    def outputDirectory = output.parentFile
                    println "Transformed \$input.name to \$output.name into \$outputDirectory"
                    output.text = 'hash'
                }
            }

            allprojects {
                dependencies {
                    registerTransform(Sizer) {
                        from.attribute(artifactType, 'jar')
                        to.attribute(artifactType, 'size')
                        parameters {
                            target = 'size'
                        }
                    }
                    registerTransform(Hasher) {
                        from.attribute(artifactType, 'jar')
                        to.attribute(artifactType, 'hash')
                        parameters {
                            target = 'hash'
                        }
                    }
                }
                task resolveSize(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'size') }
                    }.artifacts
                    identifier = "1"
                }
                task resolveHash(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'hash') }
                    }.artifacts
                    identifier = "2"
                }
                task resolve {
                    dependsOn(resolveSize, resolveHash)
                }
            }

            project(':util') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':app') {
                dependencies {
                    compile project(':util')
                }
            }
        """

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files 1: [lib1.jar.size, lib2.jar.size, lib3.jar.size]") == 2
        output.count("ids 1: [lib1.jar.size (project :lib), lib2.jar.size (project :lib), lib3.jar.size (lib3.jar)]") == 2
        output.count("components 1: [project :lib, project :lib, lib3.jar]") == 2
        output.count("files 2: [lib1.jar.hash, lib2.jar.hash, lib3.jar.hash]") == 2
        output.count("ids 2: [lib1.jar.hash (project :lib), lib2.jar.hash (project :lib), lib3.jar.hash (lib3.jar)]") == 2
        output.count("components 2: [project :lib, project :lib, lib3.jar]") == 2

        output.count("Transformed") == 6
        isTransformed("lib1.jar", "lib1.jar.size")
        isTransformed("lib2.jar", "lib2.jar.size")
        isTransformed("lib3.jar", "lib3.jar.size")
        isTransformed("lib1.jar", "lib1.jar.hash")
        isTransformed("lib2.jar", "lib2.jar.hash")
        isTransformed("lib3.jar", "lib3.jar.hash")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files 1: [lib1.jar.size, lib2.jar.size, lib3.jar.size]") == 2

        output.count("Transformed") == 0
    }

    @ToBeFixedForInstantExecution
    def "transform is run again and old output is removed after it failed in previous build"() {
        given:
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform() << withJarTasks() << withLibJarDependency("lib3.jar")

        when:
        executer.withArgument("-Dbroken=true")
        fails ":app:resolve"

        then:
        failure.assertHasCause("Could not resolve all files for configuration ':app:compile'.")
        failure.assertHasCause("Failed to transform lib1.jar (project :lib) to match attributes {artifactType=size, usage=api}")
        failure.assertHasCause("Failed to transform lib2.jar (project :lib) to match attributes {artifactType=size, usage=api}")
        def outputDir1 = projectOutputDir("lib1.jar", "lib1.jar.txt")
        def outputDir2 = projectOutputDir("lib2.jar", "lib2.jar.txt")
        def outputDir3 = gradleUserHomeOutputDir("lib3.jar", "lib3.jar.txt")

        when:
        succeeds ":app:resolve"

        then:
        output.count("files: [lib1.jar.txt, lib2.jar.txt, lib3.jar.txt]") == 1

        output.count("Transformed") == 3
        isTransformed("lib1.jar", "lib1.jar.txt")
        isTransformed("lib2.jar", "lib2.jar.txt")
        isTransformed("lib3.jar", "lib3.jar.txt")
        projectOutputDir("lib1.jar", "lib1.jar.txt") == outputDir1
        projectOutputDir("lib2.jar", "lib2.jar.txt") == outputDir2
        gradleUserHomeOutputDir("lib3.jar", "lib3.jar.txt") == outputDir3

        when:
        succeeds ":app:resolve"

        then:
        output.count("files: [lib1.jar.txt, lib2.jar.txt, lib3.jar.txt]") == 1

        output.count("Transformed") == 0
    }

    @ToBeFixedForInstantExecution
    def "transform is re-executed when input file content changes between builds"() {
        given:
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform() << withClassesSizeTransform() << withLibJarDependency()

        file("lib/dir1.classes").file("child").createFile()

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir, lib1.jar.txt]") == 2

        output.count("Transformed") == 2
        isTransformed("dir1.classes", "dir1.classes.dir")
        isTransformed("lib1.jar", "lib1.jar.txt")
        def outputDir1 = projectOutputDir("dir1.classes", "dir1.classes.dir")
        def outputDir2 = gradleUserHomeOutputDir("lib1.jar", "lib1.jar.txt")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir, lib1.jar.txt]") == 2

        output.count("Transformed") == 0

        when:
        file("lib/lib1.jar").text = "abc"
        file("lib/dir1.classes").file("child2").createFile()

        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir, lib1.jar.txt]") == 2

        output.count("Transformed") == 2
        isTransformed("dir1.classes", "dir1.classes.dir")
        isTransformed("lib1.jar", "lib1.jar.txt")
        outputDir("dir1.classes", "dir1.classes.dir") == outputDir1
        gradleUserHomeOutputDir("lib1.jar", "lib1.jar.txt") != outputDir2

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir, lib1.jar.txt]") == 2

        output.count("Transformed") == 0
    }

    @ToBeFixedForInstantExecution(skip = ToBeFixedForInstantExecution.Skip.FLAKY)
    def "transform is executed in different workspace for different file produced in chain"() {
        given:
        buildFile << declareAttributes() << withJarTasks() << duplicatorTransform << """
            project(':util') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':app') {
                dependencies {
                    compile project(':util')
                }
            }

            import java.nio.file.Files

            allprojects {
                dependencies {
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "jar")
                        to.attribute(artifactType, "green")
                        parameters {
                            numberOfOutputFiles = 2
                            differentOutputFileNames = false
                        }
                    }
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "green")
                        to.attribute(artifactType, "blue")
                        parameters {
                            numberOfOutputFiles = 2
                            differentOutputFileNames = false
                        }
                    }
                }
                task resolve(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'blue') }
                    }.artifacts
                }
            }
        """

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [lib1.jar, lib1.jar, lib1.jar, lib1.jar, lib2.jar, lib2.jar, lib2.jar, lib2.jar]") == 2

        output.count("Transforming") == 6
        projectOutputDirs("lib1.jar", "0/lib1.jar").size() == 3
        projectOutputDirs("lib1.jar", "1/lib1.jar").size() == 3
        projectOutputDirs("lib2.jar", "0/lib2.jar").size() == 3
        projectOutputDirs("lib2.jar", "1/lib2.jar").size() == 3

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [lib1.jar, lib1.jar, lib1.jar, lib1.jar, lib2.jar, lib2.jar, lib2.jar, lib2.jar]") == 2
        output.count("Transformed") == 0
    }

    def "long transformation chain works"() {
        given:
        buildFile << declareAttributes() << withJarTasks() << withLibJarDependency("lib3.jar") << duplicatorTransform << """
            project(':util') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':app') {
                dependencies {
                    compile project(':util')
                }
            }

            allprojects {
                dependencies {
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "jar")
                        to.attribute(artifactType, "green")
                        parameters {
                            numberOfOutputFiles = 2
                            differentOutputFileNames = true
                        }
                    }
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "green")
                        to.attribute(artifactType, "blue")
                        parameters {
                            numberOfOutputFiles = 1
                            differentOutputFileNames = false
                        }
                    }
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "blue")
                        to.attribute(artifactType, "yellow")
                        parameters {
                            numberOfOutputFiles = 3
                            differentOutputFileNames = false
                        }
                    }
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "yellow")
                        to.attribute(artifactType, "orange")
                        parameters {
                            numberOfOutputFiles = 1
                            differentOutputFileNames = true
                        }
                    }
                }
                task resolve(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'orange') }
                    }.artifacts
                }
            }
        """

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [${(1..3).collectMany { (["lib${it}.jar00"] * 3) + (["lib${it}.jar10"] * 3) }.join(", ")}]") == 2
    }

    @Unroll
    def "failure in transformation chain propagates (position in chain: #failingTransform)"() {
        given:

        Closure<String> possiblyFailingTransform = { index ->
            index == failingTransform ? "FailingDuplicator" : "Duplicator"
        }
        buildFile << declareAttributes() << withJarTasks() << withLibJarDependency("lib3.jar") << duplicatorTransform << """
            abstract class FailingDuplicator extends Duplicator {

                @Override
                void transform(TransformOutputs outputs) {
                    throw new RuntimeException("broken")
                }
            }

            project(':app') {
                dependencies {
                    compile project(':lib')
                }
            }

            allprojects {
                dependencies {
                    registerTransform(${possiblyFailingTransform(1)}) {
                        from.attribute(artifactType, "jar")
                        to.attribute(artifactType, "green")
                        parameters {
                            numberOfOutputFiles = 2
                            differentOutputFileNames = true
                        }
                    }
                    registerTransform(${possiblyFailingTransform(2)}) {
                        from.attribute(artifactType, "green")
                        to.attribute(artifactType, "blue")
                        parameters {
                            numberOfOutputFiles = 1
                            differentOutputFileNames = false
                        }
                    }
                    registerTransform(${possiblyFailingTransform(3)}) {
                        from.attribute(artifactType, "blue")
                        to.attribute(artifactType, "yellow")
                        parameters {
                            numberOfOutputFiles = 3
                            differentOutputFileNames = false
                        }
                    }
                    registerTransform(${possiblyFailingTransform(4)}) {
                        from.attribute(artifactType, "yellow")
                        to.attribute(artifactType, "orange")
                        parameters {
                            numberOfOutputFiles = 1
                            differentOutputFileNames = true
                        }
                    }
                }
                task resolve(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'orange') }
                    }.artifacts
                }
            }
        """

        when:
        fails ":app:resolve"

        then:
        failure.assertHasDescription("Execution failed for task ':app:resolve'.")
        failure.assertResolutionFailure(":app:compile")

        where:
        failingTransform << (1..4)
    }

    @Unroll
    @ToBeFixedForInstantExecution(iterationMatchers = ".*scheduled: true\\)")
    def "failure in resolution propagates to chain (scheduled: #scheduled)"() {
        given:
        def module = mavenHttpRepo.module("test", "test", "1.3").publish()

        buildFile << declareAttributes() << duplicatorTransform << """
            project(':app') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':lib') {
                dependencies {
                    compile "test:test:1.3"
                }
            }

            allprojects {
                repositories {
                    maven { url "${mavenHttpRepo.uri}" }
                }

                if ($scheduled) {
                    configurations.all {
                        // force scheduled transformation of binary artifact
                        resolutionStrategy.dependencySubstitution.all { }
                    }
                }

                dependencies {
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "jar")
                        to.attribute(artifactType, "green")
                        parameters {
                            numberOfOutputFiles= 2
                            differentOutputFileNames= true
                        }
                    }
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "green")
                        to.attribute(artifactType, "blue")
                        parameters {
                            numberOfOutputFiles= 1
                            differentOutputFileNames= false
                        }
                    }
                    registerTransform(Duplicator) {
                        from.attribute(artifactType, "blue")
                        to.attribute(artifactType, "yellow")
                        parameters {
                            numberOfOutputFiles= 3
                            differentOutputFileNames= false
                        }
                    }
                }
                task resolve(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'yellow') }
                    }.artifacts
                }
            }
        """

        when:
        module.pom.expectGet()
        module.artifact.expectGetBroken()
        fails ":app:resolve"

        then:
        failure.assertHasDescription("Execution failed for task ':app:resolve'.")
        failure.assertResolutionFailure(":app:compile")
        failure.hasErrorOutput("Received status code 500 from server: broken")

        where:
        scheduled << [true, false]
    }

    String duplicatorTransform = """
            import java.nio.file.Files

            abstract class Duplicator implements TransformAction<Parameters> {
                interface Parameters extends TransformParameters {
                    @Input
                    Property<Integer> getNumberOfOutputFiles()
                    @Input
                    Property<Boolean> getDifferentOutputFileNames()
                }

                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                @Override
                void transform(TransformOutputs outputs) {
                    def input = inputArtifact.get().asFile
                    println("Transforming \${input.name}")
                    for (int i = 0; i < parameters.numberOfOutputFiles.get(); i++) {
                        def suffix = parameters.differentOutputFileNames.get() ? i : ""
                        def output = outputs.file("\$i/\${input.name}\$suffix")
                        Files.copy(input.toPath(), output.toPath())
                        println "Transformed \${input.name} to \$i/\${output.name} into \$output.parentFile.parentFile"
                    }
                }
            }
    """

    @Unroll
    @ToBeFixedForInstantExecution
    def "transform is rerun when output is #action between builds"() {
        given:
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform() << withClassesSizeTransform() << withLibJarDependency()

        file("lib/dir1.classes").file("child").createFile()

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir, lib1.jar.txt]") == 2

        output.count("Transformed") == 2
        isTransformed("dir1.classes", "dir1.classes.dir")
        isTransformed("lib1.jar", "lib1.jar.txt")
        def outputDir1 = outputDir("dir1.classes", "dir1.classes.dir")
        def outputDir2 = outputDir("lib1.jar", "lib1.jar.txt")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir, lib1.jar.txt]") == 2

        output.count("Transformed") == 0

        when:
        switch (action) {
            case 'removed':
                outputDir1.deleteDir()
                outputDir2.deleteDir()
                break
            case 'changed':
                outputDir1.file("dir1.classes.dir/child.txt") << "different"
                outputDir2.file("lib1.jar.txt") << "different"
                break
            case 'added':
                outputDir1.file("some-unrelated-file.txt") << "added"
                outputDir2.file("some-unrelated-file.txt") << "added"
                break
            default:
                throw new IllegalStateException("Unknown action: ${action}")
        }

        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir, lib1.jar.txt]") == 2

        output.count("Transformed") == 2
        isTransformed("dir1.classes", "dir1.classes.dir")
        isTransformed("lib1.jar", "lib1.jar.txt")
        outputDir("dir1.classes", "dir1.classes.dir") == outputDir1
        outputDir("lib1.jar", "lib1.jar.txt") == outputDir2

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir, lib1.jar.txt]") == 2

        output.count("Transformed") == 0

        where:
        action << ['changed', 'removed', 'added']
    }

    @ToBeFixedForInstantExecution
    def "transform is supplied with a different output directory when transform implementation changes"() {
        given:
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform(parameterObject: useParameterObject) << withClassesSizeTransform(useParameterObject)

        file("lib/dir1.classes").file("child").createFile()

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir]") == 2

        output.count("Transformed") == 1
        isTransformed("dir1.classes", "dir1.classes.dir")
        def outputDir1 = outputDir("dir1.classes", "dir1.classes.dir")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir]") == 2

        output.count("Transformed") == 0

        when:
        // change the implementation
        buildFile.text = ""
        buildFile << resolveTask << declareAttributes() << multiProjectWithJarSizeTransform(fileValue:  "'new value'", parameterObject: useParameterObject) << withClassesSizeTransform(useParameterObject)
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir]") == 2

        output.count("Transformed") == 1
        isTransformed("dir1.classes", "dir1.classes.dir")
        outputDir("dir1.classes", "dir1.classes.dir") != outputDir1

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir]") == 2

        output.count("Transformed") == 0

        where:
        useParameterObject << [true, false]
    }

    @ToBeFixedForInstantExecution
    def "transform is supplied with a different output directory when parameters change"() {
        given:
        // Use another script to define the value, so that transform implementation does not change when the value is changed
        def otherScript = file("other.gradle")
        otherScript.text = "ext.value = 123"

        buildFile << """
            apply from: 'other.gradle'
        """ << declareAttributes() << multiProjectWithJarSizeTransform(paramValue: "ext.value", parameterObject: useParameterObject) << withClassesSizeTransform(useParameterObject)

        file("lib/dir1.classes").file("child").createFile()

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir]") == 2

        output.count("Transformed") == 1
        isTransformed("dir1.classes", "dir1.classes.dir")
        def outputDir1 = outputDir("dir1.classes", "dir1.classes.dir")

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir]") == 2

        output.count("Transformed") == 0

        when:
        otherScript.replace('123', '123.4')
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir]") == 2

        output.count("Transformed") == 1
        isTransformed("dir1.classes", "dir1.classes.dir")
        outputDir("dir1.classes", "dir1.classes.dir") != outputDir1

        when:
        succeeds ":util:resolve", ":app:resolve"

        then:
        output.count("files: [dir1.classes.dir]") == 2

        output.count("Transformed") == 0

        where:
        useParameterObject << [true, false]
    }

    @ToBeFixedForInstantExecution
    def "transform is supplied with a different output directory when external dependency changes"() {
        def m1 = mavenHttpRepo.module("test", "changing", "1.2").publish()
        def m2 = mavenHttpRepo.module("test", "snapshot", "1.2-SNAPSHOT").publish()

        given:
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform() << """
            allprojects {
                repositories {
                    maven { url '$ivyHttpRepo.uri' }
                }
                configurations.all {
                    resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
                    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
                }
            }

            project(':lib') {
                dependencies {
                    compile("test:changing:1.2") { changing = true }
                    compile("test:snapshot:1.2-SNAPSHOT")
                }
            }
        """

        when:
        m1.pom.expectGet()
        m1.artifact.expectGet()
        m2.metaData.expectGet()
        m2.pom.expectGet()
        m2.artifact.expectGet()

        succeeds ":app:resolve"

        then:
        output.count("files: [changing-1.2.jar.txt, snapshot-1.2-SNAPSHOT.jar.txt]") == 1

        output.count("Transformed") == 2
        isTransformed("changing-1.2.jar", "changing-1.2.jar.txt")
        isTransformed("snapshot-1.2-SNAPSHOT.jar", "snapshot-1.2-SNAPSHOT.jar.txt")
        def outputDir1 = outputDir("changing-1.2.jar", "changing-1.2.jar.txt")
        def outputDir2 = outputDir("snapshot-1.2-SNAPSHOT.jar", "snapshot-1.2-SNAPSHOT.jar.txt")

        when:
        // No changes
        server.resetExpectations()
        m1.pom.expectHead()
        m1.artifact.expectHead()
        m2.metaData.expectHead()
        // TODO - these should not be required for unique versions
        m2.pom.expectHead()
        m2.artifact.expectHead()

        succeeds ":app:resolve"

        then:
        output.count("files: [changing-1.2.jar.txt, snapshot-1.2-SNAPSHOT.jar.txt]") == 1

        output.count("Transformed") == 0

        when:
        // changing module has been changed
        server.resetExpectations()
        m1.publishWithChangedContent()
        m1.pom.expectHead()
        m1.pom.sha1.expectGet()
        m1.pom.expectGet()
        m1.artifact.expectHead()
        m1.artifact.sha1.expectGet()
        m1.artifact.expectGet()
        m2.metaData.expectHead()
        // TODO - these should not be required for unique versions
        m2.pom.expectHead()
        m2.artifact.expectHead()

        succeeds ":app:resolve"

        then:
        output.count("files: [changing-1.2.jar.txt, snapshot-1.2-SNAPSHOT.jar.txt]") == 1

        output.count("Transformed") == 1
        isTransformed("changing-1.2.jar", "changing-1.2.jar.txt")
        outputDir("changing-1.2.jar", "changing-1.2.jar.txt") != outputDir1

        when:
        // No changes
        server.resetExpectations()
        m1.pom.expectHead()
        m1.artifact.expectHead()
        m2.metaData.expectHead()
        // TODO - these should not be required for unique versions
        m2.pom.expectHead()
        m2.artifact.expectHead()

        succeeds ":app:resolve"

        then:
        output.count("files: [changing-1.2.jar.txt, snapshot-1.2-SNAPSHOT.jar.txt]") == 1

        output.count("Transformed") == 0

        when:
        // new snapshot version
        server.resetExpectations()
        m1.pom.expectHead()
        m1.artifact.expectHead()
        m2.publishWithChangedContent()
        m2.metaData.expectHead()
        m2.metaData.expectGet()
        m2.pom.expectHead()
        m2.pom.sha1.expectGet()
        m2.pom.expectGet()
        m2.artifact.expectHead()
        m2.artifact.sha1.expectGet()
        m2.artifact.expectGet()

        succeeds ":app:resolve"

        then:
        output.count("files: [changing-1.2.jar.txt, snapshot-1.2-SNAPSHOT.jar.txt]") == 1

        output.count("Transformed") == 1
        isTransformed("snapshot-1.2-SNAPSHOT.jar", "snapshot-1.2-SNAPSHOT.jar.txt")
        outputDir("snapshot-1.2-SNAPSHOT.jar", "snapshot-1.2-SNAPSHOT.jar.txt") != outputDir2
    }

    @ToBeFixedForInstantExecution
    def "cleans up cache"() {
        given:
        buildFile << declareAttributes() << multiProjectWithJarSizeTransform()
        ["lib1.jar", "lib2.jar"].each { name ->
            buildFile << withLibJarDependency(name)
        }

        when:
        executer.requireIsolatedDaemons() // needs to stop daemon
        requireOwnGradleUserHomeDir() // needs its own journal
        succeeds ":app:resolve"

        then:
        def outputDir1 = outputDir("lib1.jar", "lib1.jar.txt").assertExists()
        def outputDir2 = outputDir("lib2.jar", "lib2.jar.txt").assertExists()
        journal.assertExists()

        when:
        run '--stop' // ensure daemon does not cache file access times in memory
        def beforeCleanup = MILLISECONDS.toSeconds(System.currentTimeMillis())
        writeLastTransformationAccessTimeToJournal(outputDir1, daysAgo(MAX_CACHE_AGE_IN_DAYS + 1))
        gcFile.lastModified = daysAgo(2)

        and:
        // start as new process so journal is not restored from in-memory cache
        executer.withTasks("tasks").start().waitForFinish()

        then:
        outputDir1.assertDoesNotExist()
        outputDir2.assertExists()
        gcFile.lastModified() >= SECONDS.toMillis(beforeCleanup)
    }

    def "cache cleanup does not delete entries that are currently being created"() {
        given:
        requireOwnGradleUserHomeDir() // needs its own journal
        blockingHttpServer.start()

        and:
        buildFile << declareAttributes() << withLibJarDependency() << """
            abstract class BlockingTransform implements TransformAction<TransformParameters.None> {
                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                void transform(TransformOutputs outputs) {
                    def input = inputArtifact.get().asFile
                    def output = outputs.file("\${input.name}.txt")
                    def outputDirectory = output.parentFile
                    output.text = ""
                    println "Transformed \$input.name to \$output.name into \$outputDirectory"
                    ${blockingHttpServer.callFromBuild("transform")}
                }
            }

            project(':app') {
                dependencies {
                    registerTransform(BlockingTransform) {
                        from.attribute(artifactType, "jar")
                        to.attribute(artifactType, "blocking")
                    }
                    compile project(':lib')
                }

                task waitForCleaningBarrier {
                    doLast {
                        ${blockingHttpServer.callFromBuild("cleaning")}
                    }
                }

                task waitForTransformBarrier {
                    doLast {
                        def files = configurations.compile.incoming.artifactView {
                            attributes { it.attribute(artifactType, 'blocking') }
                        }.artifacts.artifactFiles
                        println "files: " + files.collect { it.name }
                    }
                }
            }
"""

        and: 'preconditions for cleanup of untracked files'
        gcFile.createFile().lastModified = daysAgo(2)
        writeJournalInceptionTimestamp(daysAgo(8))

        when: 'cleaning build is started'
        def cleaningBarrier = blockingHttpServer.expectAndBlock("cleaning")
        def cleaningBuild = executer.withTasks("waitForCleaningBarrier").withArgument("--no-daemon").start()

        then: 'cleaning build starts and waits on its barrier'
        cleaningBarrier.waitForAllPendingCalls()

        when: 'transforming build is started'
        def transformBarrier = blockingHttpServer.expectAndBlock("transform")
        def transformingBuild = executer.withTasks("waitForTransformBarrier").start()

        then: 'transforming build starts artifact transform'
        transformBarrier.waitForAllPendingCalls()
        def cachedTransform = null
        poll { // there's a delay in receiving the output
            cachedTransform = gradleUserHomeOutputDir("lib1.jar", "lib1.jar.txt") {
                transformingBuild.standardOutput
            }
        }

        when: 'cleanup is triggered'
        def beforeCleanup = MILLISECONDS.toSeconds(System.currentTimeMillis())
        cleaningBarrier.releaseAll()
        cleaningBuild.waitForFinish()

        then: 'cleanup runs and preserves the cached transform'
        gcFile.lastModified() >= SECONDS.toMillis(beforeCleanup)
        cachedTransform.assertExists()

        when: 'transforming build is allowed to finish'
        transformBarrier.releaseAll()

        then: 'transforming build finishes successfully'
        transformingBuild.waitForFinish()
    }

    String getResolveTask() {
        """
            class Resolve extends DefaultTask {
                @Internal
                final Property<ArtifactCollection> artifacts = project.objects.property(ArtifactCollection)
                @Console
                final Property<String> identifier = project.objects.property(String)

                @Optional
                @InputFiles
                FileCollection getArtifactFiles() {
                    return artifacts.get().artifactFiles
                }

                @TaskAction
                void run() {
                    ArtifactCollection artifacts = this.artifacts.get()
                    String postfix = identifier.map { it -> " " + it }.getOrElse("")
                    println "files\${postfix}: " + artifacts.artifactFiles.collect { it.name }
                    println "ids\${postfix}: " + artifacts.collect { it.id.displayName }
                    println "components\${postfix}: " + artifacts.collect { it.id.componentIdentifier }
                }
            }
        """
    }

    def multiProjectWithJarSizeTransform(Map options = [:]) {
        def paramValue = options.paramValue ?: "1"
        def fileValue = options.fileValue ?: "String.valueOf(input.length())"
        def useParameterObject = options.parameterObject == null ? true : options.parameterObject

        """
            ext.paramValue = $paramValue

${useParameterObject ? registerFileSizerWithParameterObject(fileValue) : registerFileSizerWithConstructorParams(fileValue)}

            project(':util') {
                dependencies {
                    compile project(':lib')
                }
            }

            project(':app') {
                dependencies {
                    compile project(':util')
                }
            }
        """
    }

    String registerFileSizerWithConstructorParams(String fileValue) {
        """
            class FileSizer extends ArtifactTransform {
                @javax.inject.Inject
                FileSizer(Number value) {
                }

                List<File> transform(File input) {
${getFileSizerBody(fileValue, 'new File(outputDirectory, ', 'new File(outputDirectory, ')}
                    return [output]
                }
            }

            allprojects {
                dependencies {
                    registerTransform {
                        from.attribute(artifactType, "jar")
                        to.attribute(artifactType, "size")
                        artifactTransform(FileSizer) { params(paramValue) }
                    }
                }
                task resolve(type: Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'size') }
                    }.artifacts
                }
            }
            """
    }

    String registerFileSizerWithParameterObject(String fileValue) {
        """
            abstract class FileSizer implements TransformAction<Parameters> {
                interface Parameters extends TransformParameters {
                    @Input
                    Number getValue()
                    void setValue(Number value)
                }

                @InputArtifact
                abstract Provider<FileSystemLocation> getInputArtifact()

                private File getInput() {
                    inputArtifact.get().asFile
                }

                void transform(TransformOutputs outputs) {
${getFileSizerBody(fileValue, 'outputs.dir(', 'outputs.file(')}
                }
            }

            allprojects {
                dependencies {
                    registerTransform(FileSizer) {
                        from.attribute(artifactType, "jar")
                        to.attribute(artifactType, "size")
                        parameters {
                            value = paramValue
                        }
                    }
                }
                tasks.register("resolve", Resolve) {
                    artifacts = configurations.compile.incoming.artifactView {
                        attributes { it.attribute(artifactType, 'size') }
                    }.artifacts
                }
            }
            """
    }

    String getFileSizerBody(String fileValue, String obtainOutputDir, String obtainOutputFile) {
        String validateWorkspace = """
            def outputDirectory = output.parentFile
            assert outputDirectory.directory && outputDirectory.list().length == 0
        """
        """
                    assert input.exists()

                    File output
                    if (input.file) {
                        output = ${obtainOutputFile}input.name + ".txt")
                        ${validateWorkspace}
                        output.text = $fileValue
                    } else {
                        output = ${obtainOutputDir}input.name + ".dir")
                        output.delete()
                        ${validateWorkspace}
                        output.mkdirs()
                        new File(output, "child.txt").text = "transformed"
                    }
                    def outputDirectory = output.parentFile
                    println "Transformed \$input.name to \$output.name into \$outputDirectory"

                    if (System.getProperty("broken")) {
                        new File(outputDirectory, "some-garbage").text = "delete-me"
                        throw new RuntimeException("broken")
                    }
        """
    }

    def withJarTasks() {
        """
            project(':lib') {
                task jar1(type: Jar) {
                    archiveFileName = 'lib1.jar'
                }
                task jar2(type: Jar) {
                    archiveFileName = 'lib2.jar'
                }
                tasks.withType(Jar) {
                    destinationDirectory = buildDir
                }
                artifacts {
                    compile jar1
                    compile jar2
                }
            }
        """
    }

    def withClassesSizeTransform(boolean useParameterObject = true) {
        """
            allprojects {
                dependencies {
                    registerTransform${useParameterObject ? "(FileSizer)" : ""} {
                        from.attribute(artifactType, "classes")
                        to.attribute(artifactType, "size")
                        ${useParameterObject ? "parameters { value = paramValue }" : "artifactTransform(FileSizer) { params(paramValue) }"}
                    }
                }
            }
            project(':lib') {
                artifacts {
                    compile file("dir1.classes")
                }
            }
        """
    }

    def withLibJarDependency(name = "lib1.jar") {
        file("lib/${name}").text = name
        """
            project(':lib') {
                dependencies {
                    compile files("${name}")
                }
            }
        """
    }

    def declareAttributes() {
        """
            def usage = Attribute.of('usage', String)
            def artifactType = Attribute.of('artifactType', String)

            allprojects {
                dependencies {
                    attributesSchema {
                        attribute(usage)
                    }
                }
                configurations {
                    compile {
                        attributes.attribute usage, 'api'
                    }
                }
            }
        """
    }

    void isTransformed(String from, String to) {
        def dirs = allOutputDirs(from, to)
        if (dirs.size() == 0) {
            throw new AssertionError("Could not find $from -> $to in output: $output")
        }
        if (dirs.size() > 1) {
            throw new AssertionError("Found $from -> $to more than once in output: $output")
        }
        assert output.count("into " + dirs.first()) == 1
    }

    TestFile outputDir(String from, String to, Closure<Set<TestFile>> determineOutputDirs = this.&allOutputDirs, Closure<String> stream = { output }) {
        def dirs = determineOutputDirs(from, to, stream)
        if (dirs.size() == 1) {
            return dirs.first()
        }
        throw new AssertionError("Could not find exactly one output directory for $from -> $to: $dirs")
    }

    TestFile projectOutputDir(String from, String to, Closure<String> stream = { output }) {
        outputDir(from, to, this.&projectOutputDirs, stream)
    }

    TestFile gradleUserHomeOutputDir(String from, String to, Closure<String> stream = { output }) {
        outputDir(from, to, this.&gradleUserHomeOutputDirs, stream)
    }

    Set<TestFile> allOutputDirs(String from, String to, Closure<String> stream = { output }) {
        return projectOutputDirs(from, to, stream) + gradleUserHomeOutputDirs(from, to, stream)
    }

    Set<TestFile> projectOutputDirs(String from, String to, Closure<String> stream = { output }) {
        def parts = [Pattern.quote(temporaryFolder.getTestDirectory().absolutePath) + ".*", "build", ".transforms", "\\w+"]
        return outputDirs(from, to, parts.join(quotedFileSeparator), stream)
    }

    Set<TestFile> gradleUserHomeOutputDirs(String from, String to, Closure<String> stream = { output }) {
        def parts = [Pattern.quote(cacheDir.file(CacheLayout.TRANSFORMS_STORE.getKey()).absolutePath), "\\w+"]
        outputDirs(from, to, parts.join(quotedFileSeparator), stream)
    }

    private final quotedFileSeparator = Pattern.quote(File.separator)

    Set<TestFile> outputDirs(String from, String to, String outputDirPattern, Closure<String> stream = { output }) {
        Set<TestFile> dirs = []
        def pattern = Pattern.compile("Transformed " + Pattern.quote(from) + " to " + Pattern.quote(to) + " into (${outputDirPattern})")
        for (def line : stream.call().readLines()) {
            def matcher = pattern.matcher(line)
            if (matcher.matches()) {
                dirs.add(new TestFile(matcher.group(1)))
            }
        }
        return dirs
    }

    TestFile getGcFile() {
        return cacheDir.file("gc.properties")
    }

    TestFile getCacheFilesDir() {
        return cacheDir.file(CacheLayout.TRANSFORMS_STORE.getKey())
    }

    TestFile getCacheDir() {
        return getUserHomeCacheDir().file(CacheLayout.TRANSFORMS.getKey())
    }

    void writeLastTransformationAccessTimeToJournal(TestFile outputDir, long millis) {
        def workspace = new DefaultTransformationWorkspace(outputDir)
        writeLastFileAccessTimeToJournal(workspace.getOutputDirectory(), millis)
        writeLastFileAccessTimeToJournal(workspace.getResultsFile(), millis)
    }
}
