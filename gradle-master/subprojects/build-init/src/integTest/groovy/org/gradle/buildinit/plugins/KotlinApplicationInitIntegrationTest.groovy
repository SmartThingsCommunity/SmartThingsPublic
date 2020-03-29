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

package org.gradle.buildinit.plugins

import org.gradle.buildinit.plugins.fixtures.ScriptDslFixture
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.test.fixtures.file.LeaksFileHandles
import spock.lang.Unroll

import static org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl.KOTLIN

@LeaksFileHandles
class KotlinApplicationInitIntegrationTest extends AbstractInitIntegrationSpec {

    public static final String SAMPLE_APP_CLASS = "some/thing/App.kt"
    public static final String SAMPLE_APP_TEST_CLASS = "some/thing/AppTest.kt"

    def "defaults to kotlin build scripts"() {
        when:
        run ('init', '--type', 'kotlin-application')

        then:
        dslFixtureFor(KOTLIN).assertGradleFilesGenerated()
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "creates sample source if no source present with #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'kotlin-application', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/kotlin").assertHasDescendants(SAMPLE_APP_CLASS)
        targetDir.file("src/test/kotlin").assertHasDescendants(SAMPLE_APP_TEST_CLASS)

        and:
        commonJvmFilesGenerated(scriptDsl)

        when:
        run("build")

        then:
        assertTestPassed("some.thing.AppTest", "testAppHasAGreeting")

        when:
        run("run")

        then:
        outputContains("Hello world")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "creates sample source with package and #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'kotlin-application', '--package', 'my.app', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/kotlin").assertHasDescendants("my/app/App.kt")
        targetDir.file("src/test/kotlin").assertHasDescendants("my/app/AppTest.kt")

        and:
        commonJvmFilesGenerated(scriptDsl)

        when:
        run("build")

        then:
        assertTestPassed("my.app.AppTest", "testAppHasAGreeting")

        when:
        run("run")

        then:
        outputContains("Hello world")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "setupProjectLayout is skipped when kotlin sources detected with #scriptDsl build scripts"() {
        setup:
        targetDir.file("src/main/kotlin/org/acme/SampleMain.kt") << """
        package org.acme

        class SampleMain {
        }
"""
        targetDir.file("src/test/kotlin/org/acme/SampleMainTest.kt") << """
                package org.acme

                class SampleMainTest {
                }
        """
        when:
        run('init', '--type', 'kotlin-application', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/kotlin").assertHasDescendants("org/acme/SampleMain.kt")
        targetDir.file("src/test/kotlin").assertHasDescendants("org/acme/SampleMainTest.kt")
        dslFixtureFor(scriptDsl).assertGradleFilesGenerated()

        when:
        run("build")

        then:
        executed(":test")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }
}
