/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.api.plugins.quality.pmd

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution

class PmdPluginDependenciesIntegrationTest extends AbstractIntegrationSpec {

    def setup() {
        buildFile << """
            apply plugin: "java"
            apply plugin: "pmd"

            ${mavenCentralRepository()}

            tasks.withType(Pmd) {
                // clear the classpath to avoid file locking issues on PMD version < 5.5.1
                classpath = files()
            }
        """
        badCode()
    }

    @ToBeFixedForInstantExecution
    def "allows configuring tool dependencies explicitly"() {
        def testDependency = 'net.sourceforge.pmd:pmd:5.1.1'
        expect: //defaults exist and can be inspected
        succeeds("dependencies", "--configuration", "pmd")
        output.contains "pmd:pmd-java:"

        when:
        buildFile << """
            dependencies {
                //downgrade version:
                pmd "$testDependency"
            }
        """.stripIndent()

        then:
        fails("check")
        failure.assertHasDescription("Execution failed for task ':pmdTest'.")
        and:
        succeeds("dependencies", "--configuration", "pmd")
        output.contains "$testDependency"
    }

    private badCode() {
        // No Warnings
        file("src/main/java/org/gradle/Class1.java") <<
            "package org.gradle; class Class1 { public boolean isFoo(Object arg) { return true; } }"
        // PMD Lvl 2 Warning BooleanInstantiation
        // PMD Lvl 3 Warning OverrideBothEqualsAndHashcode
        file("src/test/java/org/gradle/Class1Test.java") <<
            "package org.gradle; class Class1Test<T> { public boolean equals(Object arg) { return java.lang.Boolean.valueOf(true); } }"
    }
}
