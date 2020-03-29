/*
 * Copyright 2014 the original author or authors.
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
package org.gradle.api.resource

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.TestResources
import org.gradle.integtests.fixtures.archives.TestReproducibleArchives
import org.gradle.test.fixtures.keystore.TestKeyStore
import org.gradle.test.fixtures.server.http.HttpServer
import org.gradle.util.GUtil
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition
import org.junit.Rule

@TestReproducibleArchives
class TextResourceIntegrationTest extends AbstractIntegrationSpec {
    @Rule
    TestResources resource = new TestResources(temporaryFolder)

    @Rule
    public final HttpServer server = new HttpServer()

    def "string backed text resource"() {
        when:
        run("stringText")

        then:
        result.assertTasksExecuted(":stringText")
        file("output.txt").text == "my config"

        when:
        run("stringText")

        then:
        result.assertTasksSkipped(":stringText")
    }

    def "file backed text resource"() {
        when:
        run("generateConfigFile")
        run("fileText")

        then:
        result.assertTasksExecuted(":fileText")
        file("output.txt").text == "my config"

        when:
        run("fileText")

        then:
        result.assertTasksSkipped(":fileText")
    }

    def "single-element file collection backed text resource generated by eager task"() {
        when:
        run("fileCollectionText")

        then:
        result.assertTasksExecuted(":generateConfigFile", ":fileCollectionText")
        file("output.txt").text == "my config"

        when:
        run("fileCollectionText")

        then:
        result.assertTasksSkipped(":generateConfigFile", ":fileCollectionText")
    }

    def "single-element file collection backed text resource generated by lazy task"() {
        when:
        run("fileCollectionTextUsingTaskProvider")

        then:
        result.assertTasksExecuted(":generateConfigFile", ":fileCollectionTextUsingTaskProvider")
        file("output.txt").text == "my config"

        when:
        run("fileCollectionTextUsingTaskProvider")

        then:
        result.assertTasksSkipped(":generateConfigFile", ":fileCollectionTextUsingTaskProvider")
    }

    def "archive entry backed text resource"() {
        when:
        run("archiveEntryText")

        then:
        result.assertTasksExecuted(":generateConfigFile", ":generateConfigZip", ":archiveEntryText")
        file("output.txt").text == "my config"

        when:
        run("archiveEntryText")

        then:
        result.assertTasksSkipped(":generateConfigFile", ":generateConfigZip", ":archiveEntryText")
    }

    def "uri backed text resource over http"() {
        given:
        def uuid = UUID.randomUUID()
        def resourceFile = file("web-file.txt")
        server.useHostname() // use localhost vs ip
        server.expectGet("/myConfig-${uuid}.txt", resourceFile)
        server.start()

        buildFile << """
            task uriText(type: MyTask) {
                config = resources.text.fromUri("${server.uri}/myConfig-${uuid}.txt")
                output = project.file("output.txt")
            }
"""
        when:
        executer.expectDocumentedDeprecationWarning("Loading a TextResource from an insecure URI has been deprecated. This is scheduled to be removed in Gradle 7.0. " +
            "The provided URI '${server.uri("/myConfig-" + uuid + ".txt")}' uses an insecure protocol (HTTP). " +
            "Switch the URI to '${GUtil.toSecureUrl(server.uri("/myConfig-" + uuid + ".txt"))}' or try 'resources.text.fromInsecureUri(\"${server.uri("/myConfig-" + uuid + ".txt")}\")' to silence the warning. " +
            "See https://docs.gradle.org/current/dsl/org.gradle.api.resources.TextResourceFactory.html#org.gradle.api.resources.TextResourceFactory:fromInsecureUri(java.lang.Object) for more details.")
        run("uriText")

        then:
        result.assertTasksExecuted(":uriText")
        file("output.txt").text == "my config\n"

        when:
        executer.noDeprecationChecks()
        run("uriText")

        then:
        result.assertTasksSkipped(":uriText")
    }

    // Remove when https://bugs.openjdk.java.net/browse/JDK-8219658 is fixed in JDK 12
    @Requires(TestPrecondition.JDK11_OR_EARLIER)
    def "uri backed text resource over https"() {
        given:
        def uuid = UUID.randomUUID()
        def resourceFile = file("web-file.txt")
        def keyStore = TestKeyStore.init(resource.dir)
        keyStore.enableSslWithServerCert(server)
        keyStore.configureServerCert(executer)

        server.expectGet("/myConfig-${uuid}.txt", resourceFile)
        server.start()

        buildFile << """
            task uriText(type: MyTask) {
                config = resources.text.fromUri("${server.uri}/myConfig-${uuid}.txt")
                output = project.file("output.txt")
            }
"""
        when:
        run("uriText")

        then:
        result.assertTasksExecuted(":uriText")
        file("output.txt").text == "my config\n"

        when:
        run("uriText")

        then:
        result.assertTasksSkipped(":uriText")
    }

    def "does not emit warning with insecure option"() {
        given:
        def uuid = UUID.randomUUID()
        def resourceFile = file("web-file.txt")

        server.expectGet("/myConfig-${uuid}.txt", resourceFile)
        server.start()

        buildFile << """
            task uriText(type: MyTask) {
                config = resources.text.fromInsecureUri("${server.uri}/myConfig-${uuid}.txt")
                output = project.file("output.txt")
            }
"""
        when:
        run("uriText")

        then:
        result.assertTasksExecuted(":uriText")
        file("output.txt").text == "my config\n"

        when:
        run("uriText")

        then:
        result.assertTasksSkipped(":uriText")
    }

}
