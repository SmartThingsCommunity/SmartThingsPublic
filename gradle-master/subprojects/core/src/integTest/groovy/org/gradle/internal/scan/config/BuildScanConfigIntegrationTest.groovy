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

package org.gradle.internal.scan.config

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.internal.scan.config.fixtures.GradleEnterprisePluginFixture
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import spock.lang.Unroll

@Unroll
class BuildScanConfigIntegrationTest extends AbstractIntegrationSpec {

    def scanPlugin = new GradleEnterprisePluginFixture(testDirectory, mavenRepo, createExecuter())

    def setup() {
        settingsFile << scanPlugin.pluginManagement()

        scanPlugin.with {
            logConfig = true
            logApplied = true
            publishDummyPlugin(executer)
        }

        buildFile << """
            task t
        """
    }

    @ToBeFixedForInstantExecution
    def "enabled and disabled are false with no flags"() {
        when:
        succeeds "t"

        then:
        scanPlugin.assertEnabled(output, false)
        scanPlugin.assertDisabled(output, false)
    }

    @ToBeFixedForInstantExecution
    def "enabled with --scan"() {
        when:
        succeeds "t", "--scan"

        then:
        scanPlugin.assertEnabled(output, true)
        scanPlugin.assertDisabled(output, false)
    }

    @ToBeFixedForInstantExecution
    def "disabled with --no-scan"() {
        when:
        succeeds "t", "--no-scan"

        then:
        scanPlugin.assertEnabled(output, false)
        scanPlugin.assertDisabled(output, true)
    }

    @ToBeFixedForInstantExecution
    def "not enabled with -Dscan"() {
        // build scan plugin will treat this as enabled
        when:
        succeeds "t", "-Dscan"

        then:
        scanPlugin.assertEnabled(output, false)
        scanPlugin.assertDisabled(output, false)
    }

    @ToBeFixedForInstantExecution
    def "not disabled with -Dscan=false"() {
        when:
        succeeds "t", "-Dscan=false"

        then:
        scanPlugin.assertEnabled(output, false)
        scanPlugin.assertDisabled(output, false)
    }

    @ToBeFixedForInstantExecution
    def "warns if scan requested but no scan plugin applied"() {
        given:
        scanPlugin.collectConfig = false

        when:
        succeeds "t", "--scan"

        then:
        scanPlugin.issuedNoPluginWarning(output)
    }

    @ToBeFixedForInstantExecution
    def "warns if scan requested by sys prop value #value but no scan plugin applied"() {
        given:
        scanPlugin.collectConfig = false

        when:
        succeeds "t", value == null ? "-Dscan" : "-Dscan=$value"

        then:
        scanPlugin.issuedNoPluginWarning(output)

        where:
        value << [null, "", "true", "yes"]
    }

    @ToBeFixedForInstantExecution
    def "does not warn if no scan requested but no scan plugin applied"() {
        given:
        scanPlugin.collectConfig = false

        when:
        succeeds "t", "--no-scan"

        then:
        scanPlugin.didNotIssuedNoPluginWarning(output)
    }

    @ToBeFixedForInstantExecution
    def "fails if plugin is too old"() {
        given:
        scanPlugin.runtimeVersion = "1.7.4"

        when:
        fails "t", "--scan"

        then:
        assertFailedVersionCheck()

        when:
        fails "t", "--no-scan"

        then:
        assertFailedVersionCheck()

        when:
        fails "t"

        then:
        assertFailedVersionCheck()
    }

    @ToBeFixedForInstantExecution
    def "does not warn for each nested build if --scan used"() {
        given:
        scanPlugin.collectConfig = false
        file("buildSrc/build.gradle") << ""
        file("a/buildSrc/build.gradle") << ""
        file("a/build.gradle") << ""
        file("a/settings.gradle") << ""
        file("b/buildSrc/build.gradle") << ""
        file("b/build.gradle") << ""
        file("b/settings.gradle") << ""
        settingsFile << """
            includeBuild "a"
            includeBuild "b"
        """
        buildFile.text = """
            task t
        """

        when:
        succeeds "t", "--scan"

        then:
        scanPlugin.issuedNoPluginWarningCount(output, 1)
    }

    @ToBeFixedForInstantExecution
    def "detects that the build scan plugin has been #description"() {
        given:
        scanPlugin.collectConfig = applied

        when:
        succeeds "t"

        then:
        output.contains("buildScan plugin applied: ${applied}")
        if (applied) {
            with(scanPlugin.attributes(output)) {
                isTaskExecutingBuild()
            }
        }

        where:
        applied << [true, false]
        description = applied ? "applied" : "not applied"
    }

    @ToBeFixedForInstantExecution
    def "conveys that is task executing build"() {
        given:
        scanPlugin.collectConfig = true

        when:
        succeeds "t"

        then:
        with(scanPlugin.attributes(output)) {
            isTaskExecutingBuild()
        }
    }

    @ToBeFixedForInstantExecution
    def "can convey unsupported to plugin that supports it"() {
        given:
        scanPlugin.runtimeVersion = "3.0"
        when:
        succeeds "t", "-D${BuildScanPluginCompatibility.UNSUPPORTED_TOGGLE}=true"

        then:
        scanPlugin.assertUnsupportedMessage(output, BuildScanPluginCompatibility.UNSUPPORTED_TOGGLE_MESSAGE)
        scanPlugin.attributes(output) != null
    }

    void assertFailedVersionCheck() {
        failureCauseContains(BuildScanPluginCompatibility.OLD_SCAN_PLUGIN_VERSION_MESSAGE)
    }


}
