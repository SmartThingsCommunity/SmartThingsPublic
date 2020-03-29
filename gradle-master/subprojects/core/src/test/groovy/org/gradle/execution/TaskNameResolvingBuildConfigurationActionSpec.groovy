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

package org.gradle.execution

import org.gradle.StartParameter
import org.gradle.TaskExecutionRequest
import org.gradle.api.Task
import org.gradle.api.internal.GradleInternal
import org.gradle.execution.commandline.CommandLineTaskParser
import org.gradle.execution.taskgraph.TaskExecutionGraphInternal
import spock.lang.Specification

class TaskNameResolvingBuildConfigurationActionSpec extends Specification {
    GradleInternal gradle
    BuildExecutionContext context
    CommandLineTaskParser parser
    def TaskNameResolvingBuildConfigurationAction action

    def setup() {
        gradle = Mock(GradleInternal)
        context = Mock(BuildExecutionContext)
        parser = Mock(CommandLineTaskParser)
        action = new TaskNameResolvingBuildConfigurationAction(parser)
    }

    def "empty task parameters are no-op action"() {
        given:
        def startParameters = Mock(StartParameter)

        when:
        _ * context.getGradle() >> gradle
        _ * gradle.getStartParameter() >> startParameters
        _ * startParameters.getTaskRequests() >> []

        action.configure(context)

        then:
        1 * context.proceed()
        0 * context._()
        0 * startParameters._()
    }

    def "expand task parameters to tasks"() {
        def startParameters = Mock(StartParameter)
        def taskGraph = Mock(TaskExecutionGraphInternal)
        TaskExecutionRequest request1 = Stub(TaskExecutionRequest)
        TaskExecutionRequest request2 = Stub(TaskExecutionRequest)
        def task1 = Stub(Task)
        def task2 = Stub(Task)
        def task3 = Stub(Task)
        def selection1 = Stub(TaskSelector.TaskSelection)
        def selection2 = Stub(TaskSelector.TaskSelection)

        given:
        _ * gradle.startParameter >> startParameters
        _ * startParameters.taskRequests >> [request1, request2]
        _ * gradle.taskGraph >> taskGraph

        def tasks1 = [task1, task2] as Set
        _ * selection1.tasks >> tasks1

        def tasks2 = [task3] as Set
        _ * selection2.tasks >> tasks2

        when:
        action.configure(context)

        then:
        1 * parser.parseTasks(request1) >> [selection1]
        1 * parser.parseTasks(request2) >> [selection2]
        1 * taskGraph.addEntryTasks(tasks1)
        1 * taskGraph.addEntryTasks(tasks2)
        1 * context.proceed()
        _ * context.gradle >> gradle
        0 * context._()
    }
}
