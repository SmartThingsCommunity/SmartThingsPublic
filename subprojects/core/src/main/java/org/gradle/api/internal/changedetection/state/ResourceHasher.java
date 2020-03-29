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

package org.gradle.api.internal.changedetection.state;

import org.gradle.internal.hash.HashCode;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Hashes resources (e.g., a class file in a jar or a class file in a directory)
 */
public interface ResourceHasher extends ConfigurableNormalizer, RegularFileHasher {
    /**
     * Returns {@code null} if the zip entry should be ignored.
     */
    @Nullable
    HashCode hash(ZipEntry zipEntry) throws IOException;
}
