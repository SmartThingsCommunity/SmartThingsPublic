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

package org.gradle.api.file

import org.gradle.api.tasks.TasksWithInputsAndOutputs
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import spock.lang.Issue
import spock.lang.Unroll

class FileCollectionIntegrationTest extends AbstractIntegrationSpec implements TasksWithInputsAndOutputs {
    @Unroll
    def "can use 'as' operator with #type"() {
        buildFile << """
            def fileCollection = files("input.txt")
            def castValue = fileCollection as $type
            println "Cast value: \$castValue (\${castValue.getClass().name})"
            assert castValue instanceof $type
        """

        expect:
        succeeds "help"

        where:
        type << ["Object", "Object[]", "Set", "LinkedHashSet", "List", "LinkedList", "Collection", "FileCollection"]
    }

    @Issue("https://github.com/gradle/gradle/issues/10322")
    def "can construct file collection from the elements of a source directory set"() {
        buildFile << """
            def fileCollection = objects.fileCollection()
            def sourceDirs = objects.sourceDirectorySet('main', 'main files')
            sourceDirs.srcDirs("dir1", "dir2")
            fileCollection.from(sourceDirs.srcDirTrees)
            println("files = \${fileCollection.files.name.sort()}")
        """

        given:
        file("dir1/file1").createFile()
        file("dir1/file2").createFile()
        file("dir2/sub/file3").createFile()

        expect:
        succeeds()
        outputContains("files = [file1, file2, file3]")
    }

    def "can view the elements of file collection as a Provider"() {
        buildFile << """
            def files = objects.fileCollection()
            def elements = files.elements

            def name = 'a'
            files.from { name }

            assert elements.get().asFile == [file('a')]
        """

        expect:
        succeeds()
    }

    @ToBeFixedForInstantExecution
    def "task @InputFiles file collection closure is called once only when task executes"() {
        taskTypeWithInputFileCollection()
        buildFile << """
            task merge(type: InputFilesTask) {
                outFile = file("out.txt")
                inFiles.from {
                    println("calculating value")
                    return 'in.txt'
                }
            }
"""
        file("in.txt").text = "in"

        when:
        run("merge")

        then:
        output.count("calculating value") == 2 // once for task dependency calculation, once for task execution
    }

    @ToBeFixedForInstantExecution
    def "task @InputFiles file collection provider is called once only when task executes"() {
        taskTypeWithInputFileCollection()
        buildFile << """
            task merge(type: InputFilesTask) {
                outFile = file("out.txt")
                inFiles.from providers.provider {
                    println("calculating value")
                    return 'in.txt'
                }
            }
"""
        file("in.txt").text = "in"

        when:
        run("merge")

        then:
        output.count("calculating value") == 2 // once for task dependency calculation, once for task execution
    }

    def "can connect the elements of a file collection to task input ListProperty"() {
        taskTypeWithOutputFileProperty()
        taskTypeWithInputFileListProperty()
        buildFile << """
            task produce1(type: FileProducer) {
                output = file("out1.txt")
                content = "one"
            }
            task produce2(type: FileProducer) {
                output = file("out2.txt")
                content = "two"
            }
            def files = project.files(produce1, produce2)
            task merge(type: InputFilesTask) {
                inFiles.addAll(files.elements)
                outFile = file("merge.txt")
            }
        """

        when:
        run("merge")

        then:
        result.assertTasksExecuted(":produce1", ":produce2", ":merge")
        file("merge.txt").text == "one,two"
    }
}
