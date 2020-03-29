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

package org.gradle.api.internal.tasks.properties.bean

import org.gradle.api.Action
import org.gradle.util.ClosureBackedAction
import org.gradle.util.ConfigureUtil
import spock.lang.Specification
import spock.lang.Unroll

class NestedRuntimeBeanNodeTest extends Specification {
    @Unroll
    def "correct implementation for #type coerced to Action is tracked"() {
        expect:
        NestedRuntimeBeanNode.getImplementationClass(implementation as Action) == implementation.getClass()

        where:
        type      | implementation
        "Closure" | { it }
        "Action"  |  new Action<String>() { @Override void execute(String s) {} }
    }

    def "correct implementation for closure wrapped in Action is tracked"() {
        given:
        def closure = { it }

        expect:
        NestedRuntimeBeanNode.getImplementationClass(ConfigureUtil.configureUsing(closure)) == closure.getClass()

        and:
        NestedRuntimeBeanNode.getImplementationClass(ClosureBackedAction.of(closure)) == closure.getClass()
    }
}
