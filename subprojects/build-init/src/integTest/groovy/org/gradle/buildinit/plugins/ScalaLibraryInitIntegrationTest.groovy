/*
 * Copyright 2013 the original author or authors.
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
import spock.lang.Unroll

class ScalaLibraryInitIntegrationTest extends AbstractInitIntegrationSpec {

    public static final String SAMPLE_LIBRARY_CLASS = "some/thing/Library.scala"
    public static final String SAMPLE_LIBRARY_TEST_CLASS = "some/thing/LibrarySuite.scala"

    @Unroll
    def "creates sample source if no source present with #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'scala-library', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/scala").assertHasDescendants(SAMPLE_LIBRARY_CLASS)
        targetDir.file("src/test/scala").assertHasDescendants(SAMPLE_LIBRARY_TEST_CLASS)

        and:
        commonJvmFilesGenerated(scriptDsl)

        when:
        run("build")

        then:
        assertTestPassed("some.thing.LibrarySuite", "someLibraryMethod is always true")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    @Unroll
    def "creates sample source with package and #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'scala-library', '--package', 'my.lib', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/scala").assertHasDescendants("my/lib/Library.scala")
        targetDir.file("src/test/scala").assertHasDescendants("my/lib/LibrarySuite.scala")

        and:
        commonJvmFilesGenerated(scriptDsl)

        when:
        run("build")

        then:
        assertTestPassed("my.lib.LibrarySuite", "someLibraryMethod is always true")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    @Unroll
    def "source generation is skipped when scala sources detected with #scriptDsl build scripts"() {
        setup:
        targetDir.file("src/main/scala/org/acme/SampleMain.scala") << """
            package org.acme;

            class SampleMain{
            }
    """
        targetDir.file("src/test/scala/org/acme/SampleMainTest.scala") << """
                    package org.acme;

                    class SampleMainTest{
                    }
            """

        when:
        run('init', '--type', 'scala-library', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/scala").assertHasDescendants("org/acme/SampleMain.scala")
        targetDir.file("src/test/scala").assertHasDescendants("org/acme/SampleMainTest.scala")
        dslFixtureFor(scriptDsl).assertGradleFilesGenerated()

        when:
        run("build")

        then:
        executed(":test")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }
}
