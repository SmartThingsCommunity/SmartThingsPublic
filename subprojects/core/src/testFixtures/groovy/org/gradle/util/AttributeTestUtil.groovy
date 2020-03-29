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

package org.gradle.util

import org.gradle.api.attributes.Attribute
import org.gradle.api.internal.attributes.DefaultImmutableAttributesFactory
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.api.internal.attributes.ImmutableAttributesFactory

class AttributeTestUtil {
    static ImmutableAttributesFactory attributesFactory() {
        return new DefaultImmutableAttributesFactory(SnapshotTestUtil.valueSnapshotter(), TestUtil.objectInstantiator())
    }

    static ImmutableAttributes attributes(Map<String, ?> values) {
        def attrs = ImmutableAttributes.EMPTY
        if (values) {
            values.each { String key, Object value ->
                attrs = attributesFactory().concat(attrs, Attribute.of(key, value.class), value)
            }
        }
        return attrs
    }
}
