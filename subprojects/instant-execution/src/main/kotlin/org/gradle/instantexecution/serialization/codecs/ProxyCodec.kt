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

import org.gradle.instantexecution.serialization.ReadContext
import org.gradle.instantexecution.serialization.WriteContext
import org.gradle.instantexecution.serialization.readClassArray
import org.gradle.instantexecution.serialization.writeClassArray
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy


object ProxyCodec : EncodingProducer, Decoding {
    override fun encodingForType(type: Class<*>): Encoding? =
        ProxyEncoding.takeIf { Proxy.isProxyClass(type) }

    override suspend fun ReadContext.decode(): Any {
        val interfaces = readClassArray()
        val handler = read() as InvocationHandler
        return Proxy.newProxyInstance(interfaces.first().classLoader, interfaces, handler)
    }
}


private
object ProxyEncoding : Encoding {
    override suspend fun WriteContext.encode(value: Any) {
        writeClassArray(value.javaClass.interfaces)
        write(Proxy.getInvocationHandler(value))
    }
}
