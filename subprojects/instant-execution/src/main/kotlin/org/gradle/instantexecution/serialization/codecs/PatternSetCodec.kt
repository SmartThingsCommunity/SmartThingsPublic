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

import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.util.PatternSet
import org.gradle.api.tasks.util.internal.IntersectionPatternSet
import org.gradle.instantexecution.serialization.Codec
import org.gradle.instantexecution.serialization.ReadContext
import org.gradle.instantexecution.serialization.WriteContext
import org.gradle.instantexecution.serialization.readCollection
import org.gradle.instantexecution.serialization.readStrings
import org.gradle.instantexecution.serialization.writeCollection
import org.gradle.instantexecution.serialization.writeStrings


object PatternSetCodec : Codec<PatternSet> {

    override suspend fun WriteContext.encode(value: PatternSet) {
        writeStrings(value.includes)
        writeStrings(value.excludes)
        writeCollection(value.includeSpecs)
        writeCollection(value.excludeSpecs)
    }

    override suspend fun ReadContext.decode() =
        PatternSet().apply {
            setIncludes(readStrings())
            setExcludes(readStrings())
            readCollection {
                include(read() as Spec<FileTreeElement>)
            }
            readCollection {
                exclude(read() as Spec<FileTreeElement>)
            }
        }
}


object IntersectPatternSetCodec : Codec<IntersectionPatternSet> {
    override suspend fun WriteContext.encode(value: IntersectionPatternSet) {
        write(value.other)
        writeStrings(value.includes)
        writeStrings(value.excludes)
    }

    override suspend fun ReadContext.decode(): IntersectionPatternSet? {
        val other = read() as PatternSet
        return IntersectionPatternSet(other).apply {
            setIncludes(readStrings())
            setExcludes(readStrings())
        }
    }
}
