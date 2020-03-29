/*
 * Copyright 2012 the original author or authors.
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

package org.gradle

import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.test.fixtures.file.TestFile
import spock.lang.Shared

class AllDistributionIntegrationSpec extends DistributionIntegrationSpec {

    @Shared String version = buildContext.distZipVersion.version

    @Override
    String getDistributionLabel() {
        "all"
    }

    @ToBeFixedForInstantExecution
    def allZipContents() {
        given:
        TestFile contentsDir = unpackDistribution()

        expect:
        checkMinimalContents(contentsDir)

        // Source
        contentsDir.file('src').eachFile { TestFile file -> file.assertIsDir() }
        contentsDir.file('src/core-api/org/gradle/api/Project.java').assertIsFile()
        contentsDir.file('src/wrapper/org/gradle/wrapper/WrapperExecutor.java').assertIsFile()

        // Samples
        contentsDir.file('samples').assertDoesNotExist()

        assertDocsExist(contentsDir, version)
    }
}
