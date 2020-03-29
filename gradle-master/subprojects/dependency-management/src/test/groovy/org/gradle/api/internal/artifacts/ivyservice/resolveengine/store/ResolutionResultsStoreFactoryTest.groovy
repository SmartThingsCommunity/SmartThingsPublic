/*
 * Copyright 2011 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.store

import org.gradle.api.internal.file.TmpDirTemporaryFileProvider
import org.gradle.cache.internal.BinaryStore
import org.gradle.internal.concurrent.CompositeStoppable
import spock.lang.Specification

class ResolutionResultsStoreFactoryTest extends Specification {

    def f = new ResolutionResultsStoreFactory(new TmpDirTemporaryFileProvider())

    def "provides binary stores"() {
        def stores = f.createStoreSet()
        def store1 = stores.nextBinaryStore()
        def store2 = stores.nextBinaryStore()

        expect:
        store1 != store2
        store1 == f.createStoreSet().nextBinaryStore()
    }

    def "rolls the file"() {
        f = new ResolutionResultsStoreFactory(new TmpDirTemporaryFileProvider(), 2)

        when:
        def store = f.createStoreSet().nextBinaryStore()
        store.write({it.writeByte((byte) 1); it.writeByte((byte) 2) } as BinaryStore.WriteAction)
        store.done()
        def store2 = f.createStoreSet().nextBinaryStore()

        then:
        store.file == store2.file

        when:
        store2.write({it.writeByte((byte) 4)} as BinaryStore.WriteAction)
        store2.done()

        then:
        f.createStoreSet().nextBinaryStore().file != store2.file
    }

    def "cleans up binary files"() {
        f = new ResolutionResultsStoreFactory(new TmpDirTemporaryFileProvider(), 1);
        def stores1 = f.createStoreSet()

        when:
        def store = stores1.nextBinaryStore()
        store.write({it.writeByte((byte) 1); it.writeByte((byte) 2) } as BinaryStore.WriteAction)
        store.done()
        def store2 = stores1.nextBinaryStore() // rolled
        def store3 = f.createStoreSet().nextBinaryStore()

        then:
        store.file != store2.file //rolled
        [store.file, store2.file, store3.file].each { it.exists() }

        when:
        new CompositeStoppable().add(store, store2, store3)

        then:
        [store.file, store2.file, store3.file].each { !it.exists() }
    }

    def "provides stores"() {
        def set1 = f.createStoreSet()
        def set2 = f.createStoreSet()

        expect:
        set1.oldModelCache().load({"1"} as org.gradle.internal.Factory) == "1"
        set1.oldModelCache().load({"2"} as org.gradle.internal.Factory) == "1"
        set2.oldModelCache().load({"3"} as org.gradle.internal.Factory) == "3"

        set1.newModelCache().load({"1"} as org.gradle.internal.Factory) == "1"
        set1.newModelCache().load({"2"} as org.gradle.internal.Factory) == "1"
        set2.newModelCache().load({"3"} as org.gradle.internal.Factory) == "3"
    }
}
