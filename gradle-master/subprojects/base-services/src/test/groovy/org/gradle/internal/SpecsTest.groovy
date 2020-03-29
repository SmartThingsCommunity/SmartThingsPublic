/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.internal

import org.gradle.api.specs.Spec
import spock.lang.Specification

class SpecsTest extends Specification {

    static class A {}
    static class B extends A {}
    static class C extends B {}

    def "isInstance spec is satisfied for all instances extending from the specified type"() {
        when:
        Spec<Object> spec = Specs.isInstance(B)

        then:
        !spec.isSatisfiedBy(new Object())
        !spec.isSatisfiedBy(new A())
        spec.isSatisfiedBy(new B())
        spec.isSatisfiedBy(new C())
    }
}
