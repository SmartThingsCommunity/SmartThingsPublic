/*
 * Copyright 2014 the original author or authors.
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
package org.gradle.test.fixtures

import org.gradle.test.fixtures.server.http.HttpDirectoryResource

interface HttpRepository extends Repository {
    enum MetadataType {
        DEFAULT,
        ONLY_ORIGINAL,
        ONLY_GRADLE
    }

    @Override
    HttpModule module(String group, String module)

    @Override
    HttpModule module(String group, String module, String version)

    HttpDirectoryResource directoryList(String organisation, String module)

    MetadataType getProvidesMetadata()
}
