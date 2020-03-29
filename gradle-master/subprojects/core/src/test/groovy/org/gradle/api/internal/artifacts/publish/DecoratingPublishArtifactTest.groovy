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

package org.gradle.api.internal.artifacts.publish

import org.gradle.api.Task
import spock.lang.Specification

class DecoratingPublishArtifactTest extends Specification {
    def "can override properties"() {
        def file = new File("file")
        def task = Stub(Task)
        def task2 = Stub(Task)
        def original = new DefaultPublishArtifact("name", "ext", "type", "classifier", new Date(), file, task)
        def decorator = new DecoratingPublishArtifact(original)

        expect:
        decorator.name == "name"
        decorator.extension == "ext"
        decorator.type == "type"
        decorator.classifier == "classifier"
        decorator.file == file
        decorator.buildDependencies.getDependencies(null) == [task] as Set

        when:
        decorator.name = "<name>"
        decorator.extension = "<ext>"
        decorator.type = "<type>"
        decorator.classifier = "<classifier>"
        decorator.builtBy(task2)

        then:
        decorator.name == "<name>"
        decorator.extension == "<ext>"
        decorator.type == "<type>"
        decorator.classifier == "<classifier>"
        decorator.buildDependencies.getDependencies(null) == [task, task2] as Set

        when:
        decorator.classifier = null

        then:
        decorator.classifier == null
    }
}
