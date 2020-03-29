/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.internal.execution.history.changes

import com.google.common.collect.ImmutableList
import org.gradle.api.DefaultTask
import org.gradle.api.Describable
import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.tasks.InputChangesAwareTaskAction
import org.gradle.internal.Cast
import org.gradle.internal.hash.ClassLoaderHierarchyHasher
import org.gradle.internal.hash.HashCode
import org.gradle.internal.snapshot.impl.ImplementationSnapshot
import spock.lang.Specification

class ImplementationChangesTest extends Specification {
    def taskLoaderHash = HashCode.fromInt(123)
    def executable = Stub(Describable) {
        getDisplayName() >> "task ':test'"
    }

    def "up-to-date when task is the same"() {
        expect:
        changesBetween(
            impl(SimpleTask), [impl(TestAction)],
            impl(SimpleTask), [impl(TestAction)]
        ).empty
    }

    def "not up-to-date when task name changed"() {
        expect:
        changesBetween(
            impl(PreviousTask), [impl(TestAction)],
            impl(SimpleTask), [impl(TestAction)]
        ) == ["The type of task ':test' has changed from '$PreviousTask.name' to '$SimpleTask.name'." as String]
    }

    def "not up-to-date when class-loader has changed"() {
        def previousHash = HashCode.fromInt(987)
        expect:
        changesBetween(
            impl(SimpleTask, previousHash), [impl(TestAction)],
            impl(SimpleTask), [impl(TestAction)]
        ) == ["Class path of task ':test' has changed from ${previousHash} to ${taskLoaderHash}." as String]
    }

    def "not up-to-date when action class-loader has changed"() {
        def previousHash = HashCode.fromInt(987)
        expect:
        changesBetween(
            impl(SimpleTask), [impl(TestAction, previousHash)],
            impl(SimpleTask), [impl(TestAction)]
        ) == ["One or more additional actions for task ':test' have changed."]
    }

    def "not up-to-date when action is added"() {
        expect:
        changesBetween(
            impl(SimpleTask), [],
            impl(SimpleTask), [impl(TestAction)]
        ) == ["One or more additional actions for task ':test' have changed."]
    }

    def "not up-to-date when action is removed"() {
        expect:
        changesBetween(
            impl(SimpleTask), [impl(TestAction)],
            impl(SimpleTask), []
        ) == ["One or more additional actions for task ':test' have changed."]
    }

    def "not up-to-date when action with same class-loader is added"() {
        expect:
        changesBetween(
            impl(SimpleTask), [impl(TestAction)],
            impl(SimpleTask), [impl(TestAction), impl(TestAction)]
        ) == ["One or more additional actions for task ':test' have changed."]
    }

    def "not up-to-date when task is loaded with an unknown classloader"() {
        def taskClassLoader = new GroovyClassLoader(getClass().getClassLoader())
        Class<? extends TaskInternal> simpleTaskClass = Cast.uncheckedCast(taskClassLoader.parseClass("""
            import org.gradle.api.*

            class SimpleTask extends DefaultTask {}
        """))

        expect:
        changesBetween(
            impl(simpleTaskClass, null), [impl(TestAction)],
            impl(simpleTaskClass, null), [impl(TestAction)]
        ) == ["The type of task ':test' was loaded with an unknown classloader (class 'SimpleTask')."]
    }

    def "not up-to-date when task action is loaded with an unknown classloader"() {
        expect:
        changesBetween(
            impl(SimpleTask), [impl(TestAction)],
            impl(SimpleTask), [impl(TestAction, null)]
        ) == ["Additional action for task ':test': was loaded with an unknown classloader (class '$TestAction.name')." as String]
    }

    def "not up-to-date when task was previously loaded with an unknown classloader"() {
        expect:
        changesBetween(
            impl(SimpleTask, null), [impl(TestAction)],
            impl(SimpleTask), [impl(TestAction)]
        ) == ["During the previous execution of task ':test', it was loaded with an unknown classloader (class '$SimpleTask.name')." as String]
    }

    def "not up-to-date when task action was previously loaded with an unknown classloader"() {
        expect:
        changesBetween(
            impl(SimpleTask), [impl(TestAction, null)],
            impl(SimpleTask), [impl(TestAction)]
        ) == ["During the previous execution of task ':test', it had an additional action that was loaded with an unknown classloader (class '$TestAction.name')." as String]
    }

    List<String> changesBetween(
            ImplementationSnapshot previousImpl, List<ImplementationSnapshot> previousAdditionalImpls,
            ImplementationSnapshot currentImpl, List<ImplementationSnapshot> currentAdditionalImpls
    ) {
        def visitor = new CollectingChangeVisitor()
        new ImplementationChanges(
                previousImpl, ImmutableList.copyOf(previousAdditionalImpls),
                currentImpl, ImmutableList.copyOf(currentAdditionalImpls),
                executable
        ).accept(visitor)
        return visitor.changes*.message
    }

    private ImplementationSnapshot impl(Class<?> type, HashCode classLoaderHash = taskLoaderHash) {
        ImplementationSnapshot.of(type.getName(), classLoaderHash)
    }

    private class SimpleTask extends DefaultTask {}
    private class PreviousTask extends DefaultTask {}

    private static class TestAction implements InputChangesAwareTaskAction {
        @Override
        void setInputChanges(InputChangesInternal inputChanges) {
        }

        @Override
        void clearInputChanges() {
        }

        @Override
        void execute(Task task) {
        }

        @Override
        String getDisplayName() {
            return "Execute test action"
        }

        @Override
        ImplementationSnapshot getActionImplementation(ClassLoaderHierarchyHasher hasher) {
            return ImplementationSnapshot.of(getClass(), hasher)
        }
    }
}
