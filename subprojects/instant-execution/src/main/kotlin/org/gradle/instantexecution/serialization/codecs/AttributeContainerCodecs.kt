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

import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.instantexecution.serialization.Codec
import org.gradle.instantexecution.serialization.ReadContext
import org.gradle.instantexecution.serialization.WriteContext


internal
class AttributeContainerCodec(
    private val attributesFactory: ImmutableAttributesFactory
) : Codec<AttributeContainer> {
    override suspend fun WriteContext.encode(value: AttributeContainer) {
        // TODO - actually write the attributes
    }

    override suspend fun ReadContext.decode(): AttributeContainer? {
        return attributesFactory.mutable()
    }
}


internal
class ImmutableAttributeCodec(
    private val attributesFactory: ImmutableAttributesFactory
) : Codec<ImmutableAttributes> {
    override suspend fun WriteContext.encode(value: ImmutableAttributes) {
        // TODO - actually write the attributes
    }

    override suspend fun ReadContext.decode(): ImmutableAttributes {
        return attributesFactory.mutable().asImmutable()
    }
}
