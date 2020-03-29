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

package org.gradle.instantexecution.serialization.codecs

import org.gradle.internal.reflect.ClassInspector

import java.lang.reflect.Method


internal
class MethodCache(

    private
    val predicate: Method.() -> Boolean

) {
    private
    val methodCache = hashMapOf<Class<*>, Method?>()

    fun forObject(value: Any) =
        forClass(value.javaClass)

    fun forClass(type: Class<*>) = methodCache.computeIfAbsent(type) {
        it.firstMatchingMethodOrNull(predicate)
    }
}


internal
fun Class<*>.firstMatchingMethodOrNull(predicate: Method.() -> Boolean): Method? =
    ClassInspector.inspect(this)
        .allMethods
        .find(predicate)
        ?.apply { isAccessible = true }
