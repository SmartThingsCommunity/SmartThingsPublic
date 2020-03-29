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

package org.gradle.api.internal.tasks

import org.gradle.api.internal.file.FileCollectionInternal
import org.gradle.util.UsesNativeServices
import spock.lang.Specification

import java.util.concurrent.Callable

@UsesNativeServices
class DefaultTaskDestroyablesTest extends Specification {

    TaskMutator taskMutator = Stub(TaskMutator) {
        mutate(_, _) >> { String method, Object action ->
            if (action instanceof Runnable) {
                action.run()
            } else if (action instanceof Callable) {
                return action.call()
            }
        }
    }

    TaskDestroyablesInternal taskDestroys = new DefaultTaskDestroyables(taskMutator)

    def "empty destroys by default"() {
        expect:
        taskDestroys.registeredPaths != null
        taskDestroys.registeredPaths.isEmpty()
    }

    def "can declare a file that a task destroys"() {
        when:
        taskDestroys.register("a")

        then:
        taskDestroys.registeredPaths == ["a"]
    }

    def "can declare multiple files that a task destroys"() {
        when:
        taskDestroys.register("a", "b")

        then:
        taskDestroys.registeredPaths == ["a", "b"]
    }

    def "can declare a file collection that a task destroys"() {
        def files = [new File('a'), new File('b')] as Set
        def fileCollection = [getFiles: { files }] as FileCollectionInternal

        when:
        taskDestroys.register(fileCollection)

        then:
        taskDestroys.registeredPaths == [fileCollection]
    }

    def "can declare a file that a task destroys using a closure"() {
        def closure = { 'a' }
        when:
        taskDestroys.register(closure)

        then:
        taskDestroys.registeredPaths == [closure]
    }
}
