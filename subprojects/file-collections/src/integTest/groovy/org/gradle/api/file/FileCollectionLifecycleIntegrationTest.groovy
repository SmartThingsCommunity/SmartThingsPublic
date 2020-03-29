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

class FileCollectionLifecycleIntegrationTest extends AbstractIntegrationSpec implements TasksWithInputsAndOutputs {
    def "finalized file collection resolves locations and ignores later changes to source paths"() {
        buildFile << """
            def files = objects.fileCollection()
            Integer counter = 0
            files.from { "a\${++counter}" }

            def names = ['b', 'c']
            files.from(names)

            assert files.files as List == [file('a1'), file('b'), file('c')]

            files.finalizeValue()

            assert counter == 2

            assert files.files as List == [file('a2'), file('b'), file('c')]

            counter = 45
            names.clear()

            assert files.files as List == [file('a2'), file('b'), file('c')]
        """

        expect:
        succeeds()
    }

    def "finalize on read file collection resolves locations and ignores later changes to source paths"() {
        buildFile << """
            def files = objects.fileCollection()
            Integer counter = 0
            files.from { "a\${++counter}" }

            def names = ['b', 'c']
            files.from(names)

            assert files.files as List == [file('a1'), file('b'), file('c')]

            files.finalizeValueOnRead()

            assert counter == 1

            assert files.files as List == [file('a2'), file('b'), file('c')]

            counter = 45
            names.clear()

            assert files.files as List == [file('a2'), file('b'), file('c')]
        """

        expect:
        succeeds()
    }

    def "finalized file collection ignores later changes to nested file collections"() {
        buildFile << """
            def nested = objects.fileCollection()
            def name = 'a'
            nested.from { name }

            def names = ['b', 'c']
            nested.from(names)

            def files = objects.fileCollection()
            files.from(nested)
            files.finalizeValue()
            name = 'ignore-me'
            names.clear()
            nested.from('ignore-me')

            assert files.files as List == [file('a'), file('b'), file('c')]
        """

        expect:
        succeeds()
    }

    def "finalized file collection still reflects changes to file system but not changes to locations"() {
        buildFile << """
            def files = objects.fileCollection()
            def name = 'a'
            files.from {
                fileTree({ name }) {
                    include '**/*.txt'
                }
            }

            files.finalizeValue()
            name = 'b'

            assert files.files.empty

            file('a').mkdirs()
            def f1 = file('a/thing.txt')
            f1.text = 'thing'

            assert files.files as List == [f1]
        """

        expect:
        succeeds()
    }

    def "cannot mutate finalized file collection"() {
        buildFile << """
            def files = objects.fileCollection()
            files.finalizeValue()

            task broken {
                doLast {
                    files.from('bad')
                }
            }
        """

        when:
        fails('broken')

        then:
        failure.assertHasCause("The value for this file collection is final and cannot be changed.")
    }

    def "can disallow changes to file collection without finalizing value"() {
        buildFile << """
            def files = objects.fileCollection()
            def name = 'other'
            files.from { name }

            def names = ['b', 'c']
            files.from(names)

            files.disallowChanges()
            name = 'a'
            names.clear()

            assert files.files as List == [file('a')]

            files.from('broken')
        """

        when:
        fails('broken')

        then:
        failure.assertHasCause("The value for this file collection cannot be changed.")
    }

    def "can write but cannot read strict project file collection instance before afterEvaluate starts"() {
        given:
        settingsFile << 'rootProject.name = "broken"'
        buildFile << """
            interface ProjectModel {
                ConfigurableFileCollection getProp()
            }

            project.extensions.create('thing', ProjectModel.class)
            thing.prop.disallowUnsafeRead()
            thing.prop.from(layout.buildDirectory)

            try {
                thing.prop.files
            } catch(IllegalStateException e) {
                println("get files failed with: \${e.message}")
            }

            try {
                thing.prop.elements.get()
            } catch(IllegalStateException e) {
                println("get failed with: \${e.message}")
            }

            thing.prop.from = "some-file"

            afterEvaluate {
                println("value = \${thing.prop.files}")
                try {
                    thing.prop.from("ignore me")
                } catch(IllegalStateException e) {
                    println("set after read failed with: \${e.message}")
                }
            }

            task show {
                doLast {
                    println("value = \${thing.prop.files}")
                }
            }
        """


        when:
        run("show")

        then:
        outputContains("get files failed with: Cannot query the value for this file collection because configuration of root project 'broken' has not finished yet.")
        outputContains("get failed with: Cannot query the value for this file collection because configuration of root project 'broken' has not finished yet.")
        outputContains("set after read failed with: The value for this file collection is final and cannot be changed.")
        output.count("value = [${file('some-file')}]") == 2
    }

    def "can change value of strict file collection after afterEvaluate starts and before the value has been read"() {
        given:
        settingsFile << 'rootProject.name = "broken"'
        buildFile << """
            interface ProjectModel {
                ConfigurableFileCollection getProp()
            }

            project.extensions.create('thing', ProjectModel.class)
            thing.prop.disallowUnsafeRead()

            afterEvaluate {
                thing.prop.from("some-file")
                println("value = \${thing.prop.files}")
                try {
                    thing.prop.from("ignore me")
                } catch(IllegalStateException e) {
                    println("set failed with: \${e.message}")
                }
            }

            task show {
                doLast {
                    println("value = \${thing.prop.files}")
                }
            }
        """

        when:
        run("show")

        then:
        outputContains("set failed with: The value for this file collection is final and cannot be changed.")
        output.count("value = [${file('some-file')}]") == 2
    }

    def "task @InputFiles file collection property is implicitly finalized and changes ignored when task starts execution"() {
        taskTypeWithInputFileCollection()
        buildFile << """
            task merge(type: InputFilesTask) {
                outFile = file("out.txt")
                inFiles.from = "in.txt"
                doFirst {
                    inFiles.from("other.txt")
                }
            }
"""
        file("in.txt").text = "in"

        when:
        executer.expectDocumentedDeprecationWarning("Changing the value for a FileCollection with a final value has been deprecated. " +
            "This will fail with an error in Gradle 7.0. " +
            "See https://docs.gradle.org/current/userguide/lazy_configuration.html#unmodifiable_property for more details.")
        run("merge")

        then:
        file("out.txt").text == "in"
    }

    def "task ad hoc input file collection property is implicitly finalized and changes ignored when task starts execution"() {
        buildFile << """
            def files = project.files()
            def outFile = file("out.txt")
            task show {
                inputs.files files
                outputs.file outFile
                files.from("in.txt")
                doFirst {
                    files.from("other.txt")
                    outFile.text = files.files*.name.join(',')
                }
            }
"""
        file("in.txt").text = "in"

        when:
        executer.expectDocumentedDeprecationWarning("Changing the value for a FileCollection with a final value has been deprecated. " +
            "This will fail with an error in Gradle 7.0. " +
            "See https://docs.gradle.org/current/userguide/lazy_configuration.html#unmodifiable_property for more details.")
        run("show")

        then:
        file("out.txt").text == "in.txt"
    }
}
