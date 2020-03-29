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

package org.gradle.integtests.fixtures

import groovy.transform.SelfType
import org.gradle.test.fixtures.file.TestFile
import org.junit.Before

@SelfType(AbstractIntegrationSpec)
trait DirectoryBuildCacheFixture {
    private TestBuildCache buildCache

    @Before
    void setupCacheDirectory() {
        // Make sure cache dir is empty for every test execution
        buildCache = new TestBuildCache(temporaryFolder.file("cache-dir").deleteDir().createDir())
        settingsFile << buildCache.localCacheConfiguration()
    }

    def localCacheConfiguration() {
        buildCache.localCacheConfiguration()
    }

    TestFile getCacheDir() {
        buildCache.cacheDir
    }

    TestFile gcFile() {
        buildCache.gcFile()
    }

    List<TestFile> listCacheFiles() {
        buildCache.listCacheFiles()
    }
}
