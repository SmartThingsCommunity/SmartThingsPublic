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

import org.gradle.api.internal.GeneratedSubclass
import org.gradle.api.internal.GeneratedSubclasses

import org.gradle.instantexecution.serialization.Codec
import org.gradle.instantexecution.serialization.ReadContext
import org.gradle.instantexecution.serialization.WriteContext
import org.gradle.instantexecution.serialization.decodePreservingIdentity
import org.gradle.instantexecution.serialization.encodePreservingIdentityOf
import org.gradle.instantexecution.serialization.withBeanTrace


internal
class BeanCodec : Codec<Any> {

    override suspend fun WriteContext.encode(value: Any) {
        encodePreservingIdentityOf(value) {
            val beanType = GeneratedSubclasses.unpackType(value)
            withBeanTrace(beanType) {
                writeBeanOf(beanType, value)
            }
        }
    }

    override suspend fun ReadContext.decode(): Any? =
        decodePreservingIdentity { id ->
            val beanType = readClass()
            val generated = readBoolean()
            withBeanTrace(beanType) {
                readBeanOf(beanType, generated, id)
            }
        }

    private
    suspend fun WriteContext.writeBeanOf(beanType: Class<*>, value: Any) {
        writeClass(beanType)
        // TODO - should collect the details of the decoration (eg enabled annotations, etc), and also carry this information with the serialized class reference
        //  instead of separately for each bean
        val generated = value is GeneratedSubclass
        writeBoolean(generated)
        beanStateWriterFor(value.javaClass).run {
            writeStateOf(value)
        }
    }

    private
    suspend fun ReadContext.readBeanOf(beanType: Class<*>, generated: Boolean, id: Int): Any {
        // TODO - do a single lookup of reader rather than 2
        val bean = beanStateReaderFor(beanType).run { newBeanWithId(generated, id) }
        beanStateReaderFor(bean.javaClass).run {
            readStateOf(bean)
        }
        return bean
    }
}
