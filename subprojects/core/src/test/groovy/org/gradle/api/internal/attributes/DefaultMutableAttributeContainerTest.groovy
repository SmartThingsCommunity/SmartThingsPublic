/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.internal.attributes

import org.gradle.api.attributes.Attribute
import org.gradle.util.AttributeTestUtil
import spock.lang.Specification

class DefaultMutableAttributeContainerTest extends Specification {
    def attributesFactory = AttributeTestUtil.attributesFactory()

    def "can override attributes from parent"() {
        def attr1 = Attribute.of("one", String)
        def attr2 = Attribute.of("two", String)

        given:
        def parent = new DefaultMutableAttributeContainer(attributesFactory)
        parent.attribute(attr1, "parent")
        parent.attribute(attr2, "parent")

        def child = new DefaultMutableAttributeContainer(attributesFactory, parent)
        child.attribute(attr1, "child")

        expect:
        child.getAttribute(attr1) == "child"
        child.getAttribute(attr2) == "parent"

        def immutable1 = child.asImmutable()
        immutable1.getAttribute(attr1) == "child"
        immutable1.getAttribute(attr2) == "parent"

        parent.attribute(attr2, "new parent")

        child.getAttribute(attr1) == "child"
        child.getAttribute(attr2) == "new parent"

        immutable1.getAttribute(attr1) == "child"
        immutable1.getAttribute(attr2) == "parent"

        def immutable2 = child.asImmutable()
        immutable2.getAttribute(attr1) == "child"
        immutable2.getAttribute(attr2) == "new parent"
    }
}
