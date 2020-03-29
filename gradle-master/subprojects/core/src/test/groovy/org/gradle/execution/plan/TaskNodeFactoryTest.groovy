/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.execution.plan

import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.composite.internal.IncludedBuildTaskGraph
import org.gradle.internal.build.BuildState
import org.gradle.internal.service.ServiceRegistry
import spock.lang.Specification

class TaskNodeFactoryTest extends Specification {
    def gradle = Stub(GradleInternal)
    def project = Stub(ProjectInternal)
    def graph
    def a = task('a')
    def b = task('b')
    def c = task('c')
    def d = task('d')
    def e = task('e')

    def setup() {
        def services = Stub(ServiceRegistry)
        gradle.services >> services
        services.get(BuildState) >> Stub(BuildState)
        project.gradle >> gradle

        graph = new TaskNodeFactory(gradle, Stub(IncludedBuildTaskGraph))
    }

    private TaskInternal task(String name) {
        Mock(TaskInternal) {
            getName() >> name
            compareTo(_) >> { args -> name.compareTo(args[0].name)}
            getProject() >> project
        }
    }

    void 'can create a node for a task'() {
        when:
        def node = graph.getOrCreateNode(a)

        then:
        !node.inKnownState
        node.dependencyPredecessors.empty
        node.mustSuccessors.empty
        node.dependencySuccessors.empty
        node.shouldSuccessors.empty
        node.finalizingSuccessors.empty
        node.finalizers.empty
    }

    void 'caches node for a given task'() {
        when:
        def node = graph.getOrCreateNode(a)

        then:
        graph.getOrCreateNode(a).is(node)
    }

    void 'can add multiple nodes'() {
        when:
        graph.getOrCreateNode(a)
        graph.getOrCreateNode(b)

        then:
        graph.tasks == [a, b] as Set
    }

    void 'clear'() {
        when:
        graph.getOrCreateNode(a)
        graph.getOrCreateNode(b)
        graph.getOrCreateNode(c)
        graph.clear()

        then:
        !graph.tasks
    }
}
