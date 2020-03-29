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

package org.gradle.integtests.resolve

import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.integtests.fixtures.AbstractDependencyResolutionTest
import org.gradle.integtests.fixtures.RichConsoleStyling
import org.gradle.integtests.fixtures.executer.GradleHandle
import org.gradle.integtests.fixtures.executer.LogContent
import org.gradle.test.fixtures.ConcurrentTestUtil
import org.gradle.test.fixtures.server.http.BlockingHttpServer
import org.junit.Rule

class RemoteDependencyResolveConsoleIntegrationTest extends AbstractDependencyResolutionTest implements RichConsoleStyling {
    @Rule
    BlockingHttpServer server = new BlockingHttpServer()

    def setup() {
        server.start()
    }

    def "shows work-in-progress during graph and file resolution"() {
        def m1 = mavenRepo.module("test", "one", "1.2").publish()
        def m2 = mavenRepo.module("test", "two", "1.2").publish()

        buildFile << """
            repositories { 
                maven { url '${server.uri}' }
            }
            configurations { compile }
            dependencies {
                compile "test:one:1.2"
                compile "test:two:1.2"
            }
            task resolve {
                doLast {
                    configurations.compile.each { println it.name }
                }
            }
"""

        given:
        def getM1Pom = server.get(m1.pom.path).sendSomeAndBlock(longXml(m1.pom.file))
        def getM2Pom = server.get(m2.pom.path).sendSomeAndBlock(longXml(m2.pom.file))
        def metaData = server.expectConcurrentAndBlock(getM1Pom, getM2Pom)
        def getM1Jar = server.get(m1.artifact.path).sendSomeAndBlock(longJar(m1.artifact.file))
        def getM2Jar = server.get(m2.artifact.path).sendSomeAndBlock(longJar(m2.artifact.file))
        def jars = server.expectConcurrentAndBlock(getM1Jar, getM2Jar)

        when:
        executer.withTestConsoleAttached()
        executer.withConsole(ConsoleOutput.Rich)
        def build = executer.withTasks("resolve").withArguments("--max-workers=2").start()
        metaData.waitForAllPendingCalls()

        then:
        ConcurrentTestUtil.poll {
            outputContainsProgress(build,
                "> :resolve > Resolve dependencies of :compile",
                "> one-1.2.pom", "> two-1.2.pom"
            )
        }

        when:
        metaData.releaseAll()
        getM1Pom.waitUntilBlocked()
        getM2Pom.waitUntilBlocked()

        then:
        ConcurrentTestUtil.poll {
            outputContainsProgress(build,
                "> :resolve > Resolve dependencies of :compile",
                "> one-1.2.pom > 1 KB/2 KB downloaded", "> two-1.2.pom > 1 KB/2 KB downloaded"
            )
        }

        when:
        getM1Pom.release()
        getM2Pom.release()
        jars.waitForAllPendingCalls()

        then:
        ConcurrentTestUtil.poll {
            outputContainsProgress(build,
                "> :resolve > Resolve files of :compile",
                "> one-1.2.jar", "> two-1.2.jar"
            )
        }

        when:
        jars.releaseAll()
        getM1Jar.waitUntilBlocked()
        getM2Jar.waitUntilBlocked()

        then:
        ConcurrentTestUtil.poll {
            outputContainsProgress(build,
                "> :resolve > Resolve files of :compile",
                "> one-1.2.jar > 1 KB/2 KB downloaded", "> two-1.2.jar > 1 KB/2 KB downloaded"
            )
        }

        getM1Jar.release()
        getM2Jar.release()
        build.waitForFinish()
    }

    void outputContainsProgress(GradleHandle build, String taskProgressLine, String... progressOutputLines) {
        def output = LogContent.of(build.standardOutput).ansiCharsToColorText().withNormalizedEol()
        assert output.contains(workInProgressLine(taskProgressLine)) ||
            progressOutputLines.any { output.contains(workInProgressLine(taskProgressLine + " " + it)) }

        assert progressOutputLines.every {
            output.contains(workInProgressLine(it)) ||
                output.contains(workInProgressLine(taskProgressLine + " " + it))
        }
    }

    byte[] longXml(File file) {
        def instr = new ByteArrayOutputStream()
        instr << file.bytes
        while (instr.size() < 2048) {
            instr << "          ".bytes
        }
        return instr.toByteArray()
    }

    byte[] longJar(File file) {
        return longXml(file)
    }
}
