/*
 * Copyright 2016 the original author or authors.
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
package org.gradle.build

import com.google.common.base.Charsets
import org.gradle.internal.util.PropertiesUtils

class ReproduciblePropertiesWriter {

    /**
     * Writes {@link Map} of data as {@link Properties} to a file, but without including the timestamp comment.
     *
     * See {@link PropertiesUtils#store(java.util.Properties, java.io.File)}.
     */
    static void store(Map<String, ?> data, File file, String comment = null) {
        store(propertiesFrom(data), file, comment)
    }

    /**
     * Writes {@link Properties} to a file, but without including the timestamp comment.
     *
     * See {@link PropertiesUtils#store(java.util.Properties, java.io.File)}.
     */
    static void store(Properties properties, File file, String comment = null) {
        PropertiesUtils.store(properties, file, comment, Charsets.ISO_8859_1, "\n")
    }

    private static Properties propertiesFrom(Map<String, ?> data) {
        new Properties().with {
            data.forEach { key, value ->
                put(key, value ?: value.toString())
            }
            it
        }
    }
}
