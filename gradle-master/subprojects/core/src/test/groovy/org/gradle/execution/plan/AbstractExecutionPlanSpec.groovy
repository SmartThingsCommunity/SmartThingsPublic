/*
 * Copyright 2019 the original author or authors.
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

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.internal.TaskInputsInternal
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.TaskOutputsInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.project.ProjectState
import org.gradle.api.internal.project.taskfactory.TaskIdentity
import org.gradle.api.internal.tasks.TaskContainerInternal
import org.gradle.api.internal.tasks.TaskDependencyInternal
import org.gradle.api.internal.tasks.TaskDependencyResolveContext
import org.gradle.api.internal.tasks.TaskDestroyablesInternal
import org.gradle.api.internal.tasks.TaskLocalStateInternal
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.TaskDestroyables
import org.gradle.internal.resources.ResourceLock
import org.gradle.internal.resources.ResourceLockState
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.util.Path
import org.gradle.util.TestUtil
import org.junit.Rule
import spock.lang.Specification

abstract class AbstractExecutionPlanSpec extends Specification {
    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = TestNameTestDirectoryProvider.newInstance(getClass())
    private def backing = TestUtil.createRootProject(temporaryFolder.testDirectory)
    private def locks = new ArrayList<MockLock>()
    private def acquired = new HashSet<MockLock>()
    def thisBuild = backing.gradle
    def project = project()
    def resourceLockState = new MockResourceLockState(acquired)

    protected Set<ProjectInternal> getLockedProjects() {
        return locks.findAll { it.locked }.collect { it.project } as Set
    }

    protected void recordLocks(Closure cl) {
        acquired.clear()
        cl()
        acquired.clear()
    }

    protected ProjectInternal project(ProjectInternal parent = null, String name = "root") {
        def projectState = Mock(ProjectState)

        def project = Mock(ProjectInternal, name: name)
        _ * project.identityPath >> (parent == null ? Path.ROOT : Path.ROOT.child(name))
        _ * project.gradle >> thisBuild
        _ * project.mutationState >> projectState
        _ * project.services >> backing.services
        _ * project.tasks >> Stub(TaskContainerInternal)

        def lock = new MockLock(project, acquired)
        locks.add(lock)
        _ * projectState.accessLock >> lock

        return project
    }

    protected TaskInternal createTask(final String name, ProjectInternal project = this.project, Class type = TaskInternal) {
        def path = project.identityPath.child(name)
        TaskInternal task = Mock(type, name: name)
        TaskStateInternal state = Mock()
        task.project >> project
        task.name >> name
        task.path >> path
        task.identityPath >> path
        task.state >> state
        task.toString() >> "task $path"
        task.compareTo(_ as TaskInternal) >> { TaskInternal taskInternal ->
            return path.compareTo(taskInternal.identityPath)
        }
        task.outputs >> emptyTaskOutputs()
        task.destroyables >> emptyTaskDestroys()
        task.localState >> emptyTaskLocalState()
        task.inputs >> emptyTaskInputs()
        task.taskIdentity >> TaskIdentity.create(name, DefaultTask, project)
        return task
    }

    protected void dependsOn(TaskInternal task, List<Task> dependsOnTasks) {
        task.getTaskDependencies() >> taskDependencyResolvingTo(task, dependsOnTasks)
    }

    protected void mustRunAfter(TaskInternal task, List<Task> mustRunAfterTasks) {
        task.getMustRunAfter() >> taskDependencyResolvingTo(task, mustRunAfterTasks)
    }

    protected void finalizedBy(TaskInternal task, List<Task> finalizedByTasks) {
        task.getFinalizedBy() >> taskDependencyResolvingTo(task, finalizedByTasks)
    }

    protected void shouldRunAfter(TaskInternal task, List<Task> shouldRunAfterTasks) {
        task.getShouldRunAfter() >> taskDependencyResolvingTo(task, shouldRunAfterTasks)
    }

    protected TaskDependency taskDependencyResolvingTo(TaskInternal task, List<Task> tasks) {
        Mock(TaskDependencyInternal) {
            visitDependencies(_) >> { TaskDependencyResolveContext context -> tasks.forEach { context.add(it) } }
        }
    }

    private TaskOutputsInternal emptyTaskOutputs() {
        Stub(TaskOutputsInternal)
    }

    private TaskDestroyables emptyTaskDestroys() {
        Stub(TaskDestroyablesInternal)
    }

    private TaskLocalStateInternal emptyTaskLocalState() {
        Stub(TaskLocalStateInternal)
    }

    private TaskInputsInternal emptyTaskInputs() {
        Stub(TaskInputsInternal)
    }

    private static class MockResourceLockState implements ResourceLockState {
        final Collection<MockLock> locks

        MockResourceLockState(Collection<MockLock> locks) {
            this.locks = locks
        }

        @Override
        void registerLocked(ResourceLock resourceLock) {
        }

        @Override
        void registerUnlocked(ResourceLock resourceLock) {
        }

        @Override
        void releaseLocks() {
            locks.forEach { it.unlock() }
            locks.clear()
        }
    }

    private static class MockLock implements ResourceLock {
        final Thread owner = Thread.currentThread()
        final ProjectInternal project
        boolean locked
        final Collection<MockLock> locks

        MockLock(ProjectInternal project, Collection<MockLock> locks) {
            this.locks = locks
            this.project = project
        }

        @Override
        boolean isLockedByCurrentThread() {
            return locked && Thread.currentThread() == owner
        }

        @Override
        boolean tryLock() {
            if (!locks.contains(this) && locked) {
                return false
            }
            locked = true
            locks.add(this)
            return true
        }

        @Override
        void unlock() {
            locked = false
            locks.remove(this)
        }

        @Override
        String getDisplayName() {
            return "some lock"
        }
    }
}
