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

package org.gradle.instantexecution.serialization.beans

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.ConventionTask
import org.gradle.api.internal.TaskInternal

import java.lang.reflect.Field
import java.lang.reflect.Modifier


internal
fun relevantStateOf(taskType: Class<*>): List<Field> =
    relevantTypeHierarchyOf(taskType)
        .toList()
        .flatMap(Class<*>::relevantFields)
        .onEach(Field::makeAccessible)


private
fun relevantTypeHierarchyOf(taskType: Class<*>) = sequence<Class<*>> {
    var current: Class<*>? = taskType
    while (current != null) {
        if (isRelevantDeclaringClass(current)) {
            yield(current)
        }
        current = current.superclass
    }
}


private
fun isRelevantDeclaringClass(declaringClass: Class<*>): Boolean =
    declaringClass !in irrelevantDeclaringClasses


private
val irrelevantDeclaringClasses = setOf(
    Object::class.java,
    Task::class.java,
    TaskInternal::class.java,
    DefaultTask::class.java,
    ConventionTask::class.java
)


private
val Class<*>.relevantFields: List<Field>
    get() = declaredFields.toList()
        .filterNot { field ->
            Modifier.isStatic(field.modifiers)
                || Modifier.isTransient(field.modifiers)
        }
        .filter { field ->
            field.declaringClass != AbstractTask::class.java || field.name == "actions"
        }
        .sortedBy { it.name }


internal
fun Field.makeAccessible() {
    @Suppress("deprecation")
    if (!isAccessible) isAccessible = true
}
