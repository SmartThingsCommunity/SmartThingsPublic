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

package org.gradle.util

import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class JarUtils {
    static def jarWithContents(Map<String, String> contents) {
        def out = new ByteArrayOutputStream()
        def jarOut = new JarOutputStream(out)
        try {
            contents.each { file, fileContents ->
                def zipEntry = new ZipEntry(file)
                zipEntry.setTime(0)
                jarOut.putNextEntry(zipEntry)
                jarOut << fileContents
            }
        } finally {
            jarOut.close()
        }
        return out.toByteArray()
    }
}
