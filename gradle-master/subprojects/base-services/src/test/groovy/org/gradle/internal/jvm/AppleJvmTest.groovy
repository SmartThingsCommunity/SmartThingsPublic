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
package org.gradle.internal.jvm

import org.gradle.internal.jvm.Jvm.AppleJvm
import org.gradle.internal.os.OperatingSystem
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.util.SetSystemProperties
import org.junit.Rule
import spock.lang.Specification

class AppleJvmTest extends Specification {
    @Rule TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider(getClass())
    @Rule SetSystemProperties sysProp = new SetSystemProperties()
    OperatingSystem os = Mock(OperatingSystem)

    def "has no tools jar"() {
        def jvm = new AppleJvm(os)

        expect:
        jvm.toolsJar == null
    }

    def "filters environment variables"() {
        def env = ['APP_NAME_1234': 'App', 'JAVA_MAIN_CLASS_1234': 'MainClass', 'OTHER': 'value']
        def jvm = new AppleJvm(os)

        expect:
        jvm.getInheritableEnvironmentVariables(env) == ['OTHER': 'value']
    }
}
