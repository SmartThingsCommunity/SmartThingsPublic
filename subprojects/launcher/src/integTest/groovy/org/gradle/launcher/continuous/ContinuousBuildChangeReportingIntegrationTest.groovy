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

package org.gradle.launcher.continuous

import groovy.transform.TupleConstructor
import org.gradle.integtests.fixtures.AbstractContinuousIntegrationTest
import org.gradle.internal.os.OperatingSystem
import org.gradle.test.fixtures.file.TestFile

import static org.gradle.internal.filewatch.DefaultFileSystemChangeWaiterFactory.QUIET_PERIOD_SYSPROP
import static org.gradle.internal.filewatch.DefaultFileWatcherEventListener.SHOW_INDIVIDUAL_CHANGES_LIMIT

// Developer is able to easily determine the file(s) that triggered a rebuild
class ContinuousBuildChangeReportingIntegrationTest extends AbstractContinuousIntegrationTest {
    TestFile inputDir
    private static int changesLimit = SHOW_INDIVIDUAL_CHANGES_LIMIT
    // Use an extended quiet period in the test to ensure all file events are reported together.
    def quietPeriod = OperatingSystem.current().isMacOsX() ? 2500L : 1000L

    def setup() {
        buildFile << """
            task theTask {
              inputs.dir "inputDir"
              doLast {}
            }
        """
        inputDir = file("inputDir").createDir()

        executer.withBuildJvmOpts("-D${QUIET_PERIOD_SYSPROP}=${quietPeriod}")
    }

    def "should report the absolute file path of the created file when a single file is created in the input directory"() {
        given:
        def inputFile = inputDir.file("input.txt")
        when:
        succeeds("theTask")
        inputFile.text = 'New input file'

        then:
        succeeds()
        sendEOT()
        assertReportsChanges([new ChangeEntry('new file', inputFile)])
    }

    def "should report the absolute file path of the created files when the number of files added to the input directory is at the limit"() {
        given:
        def inputFiles = (1..changesLimit).collect { inputDir.file("input${it}.txt") }
        when:
        succeeds("theTask")
        inputFiles.each { it.text = 'New input file' }

        then:
        succeeds()
        sendEOT()
        assertReportsChanges(inputFiles.collect { new ChangeEntry('new file', it) })
    }

    def "should report the absolute file path of the created files up to the limit and 'and some more changes' when the number of files added to the input directory is over the limit"() {
        given:
        def inputFiles = (1..9).collect { inputDir.file("input${it}.txt") }
        when:
        succeeds("theTask")
        inputFiles.each { it.text = 'New input file' }

        then:
        succeeds()
        sendEOT()
        assertReportsChanges(inputFiles.collect { new ChangeEntry('new file', it) }, true)
    }

    def "should report the changes when files are removed"(changesCount) {
        given:
        def inputFiles = (1..changesCount).collect { inputDir.file("input${it}.txt") }
        inputFiles.each { it.text = 'New input file' }
        boolean expectMoreChanges = (changesCount > changesLimit)

        when:
        succeeds("theTask")
        inputFiles.each { it.delete() }

        then:
        succeeds()
        sendEOT()
        assertReportsChanges(inputFiles.collect { new ChangeEntry('deleted', it) }, expectMoreChanges)

        where:
        changesCount << [1, changesLimit, 11]
    }

    def "should report the changes when files are modified"(changesCount) {
        given:
        def inputFiles = (1..changesCount).collect { inputDir.file("input${it}.txt") }
        inputFiles.each { it.text = 'New input file' }
        boolean expectMoreChanges = (changesCount > changesLimit)

        when:
        succeeds("theTask")
        inputFiles.each { it.text = 'File modified' }

        then:
        succeeds()
        sendEOT()
        assertReportsChanges(inputFiles.collect { new ChangeEntry('modified', it) }, expectMoreChanges)

        where:
        changesCount << [1, changesLimit, 11]
    }

    def "should report the changes when directories are created"(changesCount) {
        given:
        def inputDirectories = (1..changesCount).collect { inputDir.file("input${it}Directory") }
        boolean expectMoreChanges = (changesCount > changesLimit)

        when:
        succeeds("theTask")
        inputDirectories.each { it.mkdir() }

        then:
        succeeds()
        sendEOT()
        assertReportsChanges(inputDirectories.collect { new ChangeEntry('new directory', it) }, expectMoreChanges)

        where:
        changesCount << [1, changesLimit, 11]
    }


    def "should report the changes when directories are deleted"(changesCount) {
        given:
        def inputDirectories = (1..changesCount).collect { inputDir.file("input${it}Directory").createDir() }
        boolean expectMoreChanges = (changesCount > changesLimit)

        when:
        succeeds("theTask")
        inputDirectories.each { it.delete() }

        then:
        succeeds()
        sendEOT()
        assertReportsChanges(inputDirectories.collect { new ChangeEntry('deleted', it) }, expectMoreChanges)

        where:
        changesCount << [1, changesLimit, 11]
    }

    def "should report the changes when multiple changes are made at once"() {
        given:
        def inputFiles = (1..11).collect { inputDir.file("input${it}.txt") }
        inputFiles.each { it.text = 'Input file' }
        def newfile1 = inputDir.file("input12.txt")
        def newfile2 = inputDir.file("input13.txt")

        when:
        succeeds("theTask")
        newfile1.text = 'New Input file'
        inputFiles[2].text = 'Modified file'
        inputFiles[7].delete()
        newfile2.text = 'New Input file'

        then:
        succeeds()
        sendEOT()
        assertReportsChanges([new ChangeEntry("new file", newfile1), new ChangeEntry("modified", inputFiles[2]), new ChangeEntry("deleted", inputFiles[7]), new ChangeEntry("new file", newfile2)], true)
    }

    def "should report changes that happen when the build is executing"() {
        given:
        buildFile << """
            gradle.taskGraph.afterTask { Task task ->
                if(task.path == ':theTask' && !file('changetrigged').exists()) {
                   sleep(500) // attempt to workaround JDK-8145981
                   file('inputDir/input.txt').text = 'New input file'
                   file('changetrigged').text = 'done'
                }
            }
        """
        waitAtEndOfBuildForQuietPeriod(quietPeriod)

        when:
        succeeds("theTask")

        then:
        sendEOT()
        results.size() == 2
        results.each {
            assert it.assertTasksExecuted(':theTask')
        }
        assertReportsChanges([new ChangeEntry("new file", file("inputDir/input.txt"))])
    }

    def "should report changes when quiet logging used"() {
        given:
        def inputFile = inputDir.file("input.txt")

        when:
        executer.withArgument("-q")
        succeeds("theTask")
        inputFile.text = 'New input file'

        then:
        succeeds()
        sendEOT()
        assertReportsChanges([new ChangeEntry('new file', inputFile)])
    }

    @TupleConstructor
    static class ChangeEntry {
        String type
        File file
    }

    private void assertReportsChanges(List<ChangeEntry> entries) {
        assertReportsChanges(entries, false)
    }

    private void assertReportsChanges(List<ChangeEntry> entries, boolean expectMoreChanges) {
        String changeReportOutput
        result.output.with {
            int pos = it.indexOf('Change detected, executing build...')
            if (pos > -1) {
                changeReportOutput = it.substring(0, pos)
            }
        }
        assert changeReportOutput != null: 'No change report output.'

        List<String> actualLines = changeReportOutput.readLines()
        boolean actualMoreChanges = false
        if (actualLines.last() == 'and some more changes') {
            actualLines.remove(actualLines.size() - 1)
            actualMoreChanges = true
        }

        if (entries != null) {
            Set<String> expectedLines = entries.collect { "${it.type}: ${it.file.absolutePath}".toString() }
            actualLines.each {
                assert expectedLines.contains(it): "Expected lines didn't contain '$it'"
            }
            int expectedLinesCount = Math.min(expectedLines.size(), changesLimit)
            assert actualLines.size() == expectedLinesCount
        }

        if (actualMoreChanges || expectMoreChanges) {
            assert actualMoreChanges: "Expecting 'more changes' line, but it wasn't found"
            assert expectMoreChanges: "Not expecting a 'more changes' line"
        }
    }
}
