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

package org.gradle.integtests.fixtures

import org.gradle.test.fixtures.file.TestFile

abstract class AbstractProjectRelocationIntegrationTest extends AbstractIntegrationSpec implements DirectoryBuildCacheFixture {

    @ToBeFixedForInstantExecution(bottomSpecs = [
        "JavaGradlePluginRelocationTest",
        "Antlr2RelocationIntegrationTest",
        "Antlr3RelocationIntegrationTest",
        "Antlr4RelocationIntegrationTest",
        "CheckstyleRelocationIntegrationTest",
        "PmdRelocationIntegrationTest",
        "CodeNarcRelocationIntegrationTest",
        "JacocoTestRelocationIntegrationTest",
        "ScalaCompileRelocationIntegrationTest"
    ])
    def "project is relocatable"() {
        def originalDir = file("original-dir")
        originalDir.file("settings.gradle") << localCacheConfiguration()
        setupProjectIn(originalDir)

        def relocatedDir = file("relocated-dir")
        relocatedDir.file("settings.gradle") << localCacheConfiguration()
        setupProjectIn(relocatedDir)

        when: "task is built in the original location"
        inDirectory(originalDir)
        withBuildCache().run taskName
        def originalResults = extractResultsFrom(originalDir)
        then: "it is executed and cached"
        executedAndNotSkipped taskName

        when: "task is re-executed without the cache"
        inDirectory(originalDir)
        run taskName
        then: "it is UP-TO-DATE"
        result.assertTaskSkipped taskName

        when: "it is executed in the new location"
        prepareForRelocation(relocatedDir)
        inDirectory(relocatedDir)
        withBuildCache().run taskName
        then: "it is loaded from cache"
        result.assertTaskSkipped taskName

        when: "it is re-executed with a clean project in the new location"
        removeResults(relocatedDir)
        inDirectory(relocatedDir)
        run taskName
        then: "it is executed again"
        executedAndNotSkipped taskName

        when: "comparing the (normalized) results from the original execution and the relocated one"
        def relocatedResults = extractResultsFrom(relocatedDir)
        then: "the two results should be the same"
        assertResultsEqual originalResults, relocatedResults
    }

    abstract protected String getTaskName()

    abstract protected void setupProjectIn(TestFile projectDir)

    abstract protected def extractResultsFrom(TestFile projectDir)

    @SuppressWarnings("GrMethodMayBeStatic")
    protected void prepareForRelocation(TestFile projectDir) {
        // Do nothing
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    protected void removeResults(TestFile projectDir) {
        projectDir.file("build").deleteDir()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    protected void assertResultsEqual(def originalResult, def relocatedResult) {
        assert relocatedResult == originalResult
    }
}
