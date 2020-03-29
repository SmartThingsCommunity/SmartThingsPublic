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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.invocation.Gradle
import org.gradle.initialization.BuildCancellationToken
import org.gradle.internal.concurrent.DefaultParallelismConfiguration
import org.gradle.internal.concurrent.ExecutorFactory
import org.gradle.internal.concurrent.ManagedExecutor
import org.gradle.internal.resources.ResourceLockCoordinationService
import org.gradle.internal.resources.ResourceLockState
import org.gradle.internal.work.WorkerLeaseService
import spock.lang.Specification

class DefaultPlanExecutorTest extends Specification {
    def executionPlan = Mock(ExecutionPlan)
    def worker = Mock(Action)
    def executorFactory = Mock(ExecutorFactory)
    def cancellationHandler = Mock(BuildCancellationToken)
    def coordinationService = Stub(ResourceLockCoordinationService) {
        withStateLock(_) >> { transformer ->
            transformer[0].transform(Stub(ResourceLockState))
        }
    }
    def executor = new DefaultPlanExecutor(new DefaultParallelismConfiguration(false, 1), executorFactory, Stub(WorkerLeaseService), cancellationHandler, coordinationService)

    def "executes tasks until no further tasks remain"() {
        def gradle = Mock(Gradle)
        def project = Mock(Project)
        def node = Mock(LocalTaskNode)
        def task = Mock(TaskInternal)
        def state = Mock(TaskStateInternal)
        project.gradle >> gradle
        task.project >> project
        task.state >> state

        when:
        executor.process(executionPlan, [], worker)

        then:
        1 * executorFactory.create(_) >> Mock(ManagedExecutor)
        1 * cancellationHandler.isCancellationRequested() >> false
        1 * executionPlan.hasNodesRemaining() >> true
        1 * executionPlan.selectNext(_, _) >> node
        1 * worker.execute(node)

        then:
        1 * cancellationHandler.isCancellationRequested() >> false
        1 * executionPlan.hasNodesRemaining() >> false
        1 * executionPlan.allNodesComplete() >> true
        1 * executionPlan.collectFailures([])
    }

    def "execution is canceled when cancellation requested"() {
        def gradle = Mock(Gradle)
        def project = Mock(Project)
        def node = Mock(LocalTaskNode)
        def task = Mock(TaskInternal)
        def state = Mock(TaskStateInternal)
        project.gradle >> gradle
        task.project >> project
        task.state >> state

        when:
        executor.process(executionPlan, [], worker)

        then:
        1 * executionPlan.getDisplayName() >> "task plan"
        1 * executorFactory.create(_) >> Mock(ManagedExecutor)
        1 * cancellationHandler.isCancellationRequested() >> false
        1 * executionPlan.hasNodesRemaining() >> true
        1 * executionPlan.selectNext(_, _) >> node
        1 * worker.execute(node)
        1 * executionPlan.finishedExecuting(node)

        then:
        1 * cancellationHandler.isCancellationRequested() >> true
        1 * executionPlan.cancelExecution()
        1 * executionPlan.hasNodesRemaining() >> false
        1 * executionPlan.allNodesComplete() >> true
        1 * executionPlan.collectFailures([])
        0 * executionPlan._
    }
}
