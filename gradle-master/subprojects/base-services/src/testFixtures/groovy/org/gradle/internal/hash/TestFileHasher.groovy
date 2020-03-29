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

package org.gradle.internal.hash

import com.google.common.io.Files

class TestFileHasher implements FileHasher {
    @Override
    HashCode hash(File file) {
        HashingOutputStream hashingStream = Hashing.primitiveStreamHasher();
        try {
            Files.copy(file, hashingStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return hashingStream.hash();
    }

    @Override
    HashCode hash(File file, long length, long lastModified) {
        return hash(file)
    }
}
