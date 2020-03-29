/*
 * Copyright 2010 the original author or authors.
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

package org.gradle.launcher
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.AvailableJavaHomes
import org.gradle.internal.jvm.JavaInfo
import org.gradle.internal.jvm.Jvm
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition
import org.gradle.util.TextUtil
import spock.lang.IgnoreIf

class GradleConfigurabilityIntegrationSpec extends AbstractIntegrationSpec {
    def buildSucceeds(String script) {
        file('build.gradle') << script
        executer.withArguments("--info").useOnlyRequestedJvmOpts().run()
    }

    def "honours jvm args specified in gradle.properties"() {
        given:
        file("gradle.properties") << "org.gradle.jvmargs=-Dsome-prop=some-value -Xmx32m"

        expect:
        buildSucceeds """
assert System.getProperty('some-prop') == 'some-value'
assert java.lang.management.ManagementFactory.runtimeMXBean.inputArguments.contains('-Xmx32m')
        """
    }

    def "shows decent message when awkward java home used"() {
        def dummyJdk = file("dummyJdk").createDir()
        assert dummyJdk.isDirectory()

        when:
        file("gradle.properties").writeProperties(["org.gradle.java.home": dummyJdk.absolutePath])

        then:
        fails()

        and:
        failure.assertHasDescription("Value '${dummyJdk.absolutePath}' given for org.gradle.java.home Gradle property is invalid (Java home supplied seems to be invalid)")
    }

    @Requires(TestPrecondition.SYMLINKS)
    def "handles java home that is a symlink"() {
        given:
        def javaHome = Jvm.current().javaHome
        def javaLink = file("javaLink")
        javaLink.createLink(javaHome)
        file("tmp").createDir().deleteDir()

        String linkPath = TextUtil.escapeString(javaLink.absolutePath)
        file("gradle.properties") << "org.gradle.java.home=$linkPath"

        when:
        buildSucceeds "println 'java home =' + System.getProperty('java.home')"

        then:
        javaLink != javaHome
        javaLink.canonicalFile == javaHome.canonicalFile

        cleanup:
        javaLink.usingNativeTools().deleteDir()
    }

    def "honours jvm sys property that contain a space in gradle.properties"() {
        given:
        file("gradle.properties") << 'org.gradle.jvmargs=-Dsome-prop="i have space"'

        expect:
        buildSucceeds """
assert System.getProperty('some-prop').toString() == 'i have space'
        """
    }

    def "honours jvm option that contain a space in gradle.properties"() {
        given:
        file("gradle.properties") << 'org.gradle.jvmargs=-XX:HeapDumpPath="/tmp/with space" -Dsome-prop="and some more stress..."'

        expect:
        buildSucceeds """
def inputArgs = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()
assert inputArgs.find { it.contains('-XX:HeapDumpPath=') }
"""
    }

    def String useAlternativeJavaPath(JavaInfo jvm = AvailableJavaHomes.differentJdk) {
        File javaHome = jvm.javaHome
        file("gradle.properties").writeProperties("org.gradle.java.home": javaHome.canonicalPath)
        return javaHome.canonicalPath
    }

    @IgnoreIf({ AvailableJavaHomes.differentJdk == null })
    def "honours java home specified in gradle.properties"() {
        given:
        String javaPath = useAlternativeJavaPath()

        expect:
        buildSucceeds "assert System.getProperty('java.home').startsWith('${TextUtil.escapeString(javaPath)}')"
    }

    @IgnoreIf({ AvailableJavaHomes.differentVersion == null || System.getProperty('java.runtime.version') == null})
    def "does not alter java.runtime.version"() {
        given:

        useAlternativeJavaPath(AvailableJavaHomes.differentVersion)
        String javaRuntimeVersion = System.getProperty('java.runtime.version')

        expect:
        buildSucceeds "assert System.getProperty('java.runtime.version') != '${javaRuntimeVersion}'"
    }
}
