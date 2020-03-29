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

package org.gradle.api.plugins.quality.codenarc

import org.gradle.api.JavaVersion
import org.gradle.integtests.fixtures.AbstractProjectRelocationIntegrationTest
import org.gradle.test.fixtures.file.TestFile

import static org.gradle.util.TextUtil.normaliseLineSeparators

class CodeNarcRelocationIntegrationTest extends AbstractProjectRelocationIntegrationTest {

    @Override
    protected String getTaskName() {
        return ":codenarc"
    }

    @Override
    protected void setupProjectIn(TestFile projectDir) {
        projectDir.file("config/codenarc/codenarc.xml") << """
            <ruleset xmlns="http://codenarc.org/ruleset/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://codenarc.org/ruleset/1.0 http://codenarc.org/ruleset-schema.xsd"
                     xsi:noNamespaceSchemaLocation="http://codenarc.org/ruleset-schema.xsd">
                <ruleset-ref path='rulesets/naming.xml'/>
            </ruleset>
        """
        projectDir.file("src/main/groovy/org/gradle/Class1.groovy") << """
            package org.gradle

            class Class1 {
                public static final int constant = 1
                public boolean is() { return true; }
            }
        """

        projectDir.file("build.gradle") << """
            apply plugin: "codenarc"

            ${mavenCentralRepository()}

            task codenarc(type: CodeNarc) {
                source "src/main/groovy"
                ignoreFailures = true
                reports.html.enabled = false
                reports.text.enabled = true
            }

            ${JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_14) ?
            """
            configurations.codenarc {
                resolutionStrategy.force 'org.codehaus.groovy:groovy:${GroovySystem.version}'
            }
            """ : ""}
        """
    }

    @Override
    protected extractResultsFrom(TestFile projectDir) {
        return normaliseLineSeparators(projectDir.file("build/reports/codenarc/codenarc.txt").text)
            .split("\n")
            .findAll { !it.startsWith("CodeNarc Report -") }
            .join("\n")
    }
}
