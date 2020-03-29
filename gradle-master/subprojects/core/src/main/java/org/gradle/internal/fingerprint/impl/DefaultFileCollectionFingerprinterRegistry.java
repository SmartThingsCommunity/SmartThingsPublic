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

package org.gradle.internal.fingerprint.impl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.gradle.api.tasks.FileNormalizer;
import org.gradle.internal.fingerprint.FileCollectionFingerprinter;
import org.gradle.internal.fingerprint.FileCollectionFingerprinterRegistry;

import java.util.Collection;
import java.util.Map;

public class DefaultFileCollectionFingerprinterRegistry implements FileCollectionFingerprinterRegistry {
    private final Map<Class<? extends FileNormalizer>, FileCollectionFingerprinter> fingerprinters;

    public DefaultFileCollectionFingerprinterRegistry(Collection<FileCollectionFingerprinter> fingerprinters) {
        this.fingerprinters = ImmutableMap.copyOf(Maps.uniqueIndex(fingerprinters, new Function<FileCollectionFingerprinter, Class<? extends FileNormalizer>>() {
            @Override
            public Class<? extends FileNormalizer> apply(FileCollectionFingerprinter fingerprinter) {
                return fingerprinter.getRegisteredType();
            }
        }));
    }

    @Override
    public FileCollectionFingerprinter getFingerprinter(Class<? extends FileNormalizer> type) {
        FileCollectionFingerprinter fingerprinter = fingerprinters.get(type);
        if (fingerprinter == null) {
            throw new IllegalStateException(String.format("No fingerprinter registered with type '%s'", type.getName()));
        }
        return fingerprinter;
    }
}
