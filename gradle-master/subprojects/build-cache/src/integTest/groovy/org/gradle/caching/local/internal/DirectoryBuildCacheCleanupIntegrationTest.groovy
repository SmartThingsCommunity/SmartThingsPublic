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

package org.gradle.caching.local.internal

import org.gradle.cache.internal.DefaultPersistentDirectoryStore
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.BuildOperationsFixture
import org.gradle.integtests.fixtures.DirectoryBuildCacheFixture
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.integtests.fixtures.cache.FileAccessTimeJournalFixture
import org.gradle.integtests.fixtures.executer.ExecutionResult
import org.gradle.internal.hash.Hashing
import spock.lang.Ignore
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

class DirectoryBuildCacheCleanupIntegrationTest extends AbstractIntegrationSpec implements DirectoryBuildCacheFixture, FileAccessTimeJournalFixture {
    private final static int MAX_CACHE_AGE_IN_DAYS = 7

    def operations = new BuildOperationsFixture(executer, testDirectoryProvider)

    def setup() {
        settingsFile << configureCacheEviction()
        def bytes = new byte[1024 * 1024]
        new Random().nextBytes(bytes)
        file("output.txt").bytes = bytes

        buildFile << """
            @CacheableTask
            class CustomTask extends DefaultTask {
                @OutputFile File outputFile = new File(temporaryDir, "output.txt")
                @Input String run = project.findProperty("run") ?: ""
                @TaskAction 
                void generate() {
                    logger.warn("Run " + run)
                    project.copy {
                        from("output.txt")
                        into temporaryDir
                    }
                }
            }
            
            task cacheable(type: CustomTask) {
                description = "Generates a 1MB file"
            }
        """
    }

    static def configureCacheEviction() {
        """
            buildCache {
                local {
                    removeUnusedEntriesAfterDays = ${MAX_CACHE_AGE_IN_DAYS}
                }
            }
        """
    }

    @ToBeFixedForInstantExecution
    def "cleans up entries"() {
        executer.requireIsolatedDaemons() // needs to stop daemon
        requireOwnGradleUserHomeDir() // needs its own journal
        run() // Make sure cache directory is initialized
        run '--stop' // ensure daemon does not cache file access times in memory
        def lastCleanupCheck = gcFile().makeOlder().lastModified()

        def hashStringLength = Hashing.defaultFunction().hexDigits

        when:
        def newTrashFile = cacheDir.file("0" * hashStringLength).createFile()
        def oldTrashFile = cacheDir.file("1" * hashStringLength).createFile()
        writeLastFileAccessTimeToJournal(newTrashFile, System.currentTimeMillis())
        writeLastFileAccessTimeToJournal(oldTrashFile, daysAgo(MAX_CACHE_AGE_IN_DAYS + 1))
        run()
        then:
        newTrashFile.assertIsFile()
        oldTrashFile.assertIsFile()
        assertCacheWasNotCleanedUpSince(lastCleanupCheck)

        when:
        lastCleanupCheck = markCacheForCleanup()
        run()
        then:
        newTrashFile.assertIsFile()
        oldTrashFile.assertDoesNotExist()
        assertCacheWasCleanedUpSince(lastCleanupCheck)
    }

    @Unroll
    def "produces reasonable message when cache retention is too short (#days days)"() {
        settingsFile << """
            buildCache {
                local {
                    removeUnusedEntriesAfterDays = ${days}
                }
            }
        """
        expect:
        fails("help")
        failure.assertHasCause("Directory build cache needs to retain entries for at least a day.")

        where:
        days << [-1, 0]
    }

    @ToBeFixedForInstantExecution
    def "build cache cleanup is triggered after max number of hours expires"() {
        run()
        def originalCheckTime = gcFile().lastModified()

        // One hour isn't enough to trigger
        when:
        // Set the time back 1 hour
        gcFile().setLastModified(originalCheckTime - TimeUnit.HOURS.toMillis(1))
        def lastCleanupCheck = gcFile().lastModified()
        run()
        then:
        assertCacheWasNotCleanedUpSince(lastCleanupCheck)

        // checkInterval-1 hours is not enough to trigger
        when:
        gcFile().setLastModified(originalCheckTime - TimeUnit.HOURS.toMillis(DefaultPersistentDirectoryStore.CLEANUP_INTERVAL_IN_HOURS - 1))
        lastCleanupCheck = gcFile().lastModified()
        run()
        then:
        assertCacheWasNotCleanedUpSince(lastCleanupCheck)

        // checkInterval hours is enough to trigger
        when:
        gcFile().setLastModified(originalCheckTime - TimeUnit.HOURS.toMillis(DefaultPersistentDirectoryStore.CLEANUP_INTERVAL_IN_HOURS))
        run()
        then:
        assertCacheWasCleanedUpSince(lastCleanupCheck)

        // More than checkInterval hours is enough to trigger
        when:
        gcFile().setLastModified(originalCheckTime - TimeUnit.HOURS.toMillis(DefaultPersistentDirectoryStore.CLEANUP_INTERVAL_IN_HOURS * 10))
        run()
        then:
        assertCacheWasCleanedUpSince(lastCleanupCheck)
    }

    @ToBeFixedForInstantExecution
    def "buildSrc does not try to clean build cache"() {
        // Copy cache configuration
        file("buildSrc/settings.gradle").text = settingsFile.text

        run()
        def lastCleanupCheck = gcFile().makeOlder().lastModified()

        when:
        run()
        then:
        assertCacheWasNotCleanedUpSince(lastCleanupCheck)

        when:
        lastCleanupCheck = markCacheForCleanup()
        run()
        then:
        assertCacheWasCleanedUpSince(lastCleanupCheck)
    }

    @Ignore("Looks like GradleBuild doesn't work with build operations, see https://github.com/gradle/gradle/issues/3983")
    def "GradleBuild tasks do not try to clean build cache"() {
        file("included/build.gradle") << """
            apply plugin: 'java'
            group = "com.example"
            version = "2.0"
        """
        // Copy cache configuration
        file("included/settings.gradle").text = settingsFile.text
        buildFile << """
            task gradleBuild(type: GradleBuild) {
                dir = file("included/")
                tasks = [ "build" ]
            }
            
            cacheable {
                dependsOn gradleBuild
            }
        """

        run()
        def lastCleanupCheck = gcFile().makeOlder().lastModified()

        expect:
        run()
        assertCacheWasNotCleanedUpSince(lastCleanupCheck)
    }

    long markCacheForCleanup() {
        gcFile().touch()
        gcFile().lastModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(MAX_CACHE_AGE_IN_DAYS) * 2
        return gcFile().lastModified()
    }

    private ExecutionResult run() {
        withBuildCache().succeeds("cacheable")
    }

    private void assertCacheWasCleanedUpSince(long lastCleanupCheck) {
        operations.only("Clean up Build cache ($cacheDir)")
        gcFile().lastModified() > lastCleanupCheck
    }

    private void assertCacheWasNotCleanedUpSince(long lastCleanupCheck) {
        operations.none("Clean up Build cache ($cacheDir)")
        gcFile().lastModified() == lastCleanupCheck
    }
}
