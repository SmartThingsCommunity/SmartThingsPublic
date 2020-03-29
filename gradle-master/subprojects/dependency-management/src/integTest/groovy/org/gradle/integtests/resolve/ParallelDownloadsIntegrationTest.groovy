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

import org.gradle.integtests.fixtures.AbstractHttpDependencyResolutionTest
import org.gradle.test.fixtures.server.http.BlockingHttpServer
import org.junit.Rule
import spock.lang.Issue
import spock.lang.Unroll

class ParallelDownloadsIntegrationTest extends AbstractHttpDependencyResolutionTest {
    @Rule
    BlockingHttpServer server = new BlockingHttpServer()

    def setup() {
        server.start()
    }

    String getAuthConfig() { '' }

    @Unroll
    def "downloads artifacts in parallel from a Maven repo - #expression"() {
        def m1 = mavenRepo.module('test', 'test1', '1.0').publish()
        def m2 = mavenRepo.module('test', 'test2', '1.0').publish()
        def m3 = mavenRepo.module('test', 'test3', '1.0').publish()
        def m4 = mavenRepo.module('test', 'test4', '1.0').publish()

        buildFile << """
            repositories {
                maven { 
                    url = uri('$server.uri')
                    $authConfig
                }
            }
            configurations { compile }
            dependencies {
                compile 'test:test1:1.0'
                compile 'test:test2:1.0'
                compile 'test:test3:1.0'
                compile 'test:test4:1.0'
            }
            task resolve {
                inputs.files configurations.compile
                doLast {
                    println ${expression} as List
                }
            }
"""

        given:
        server.expectConcurrent(
            server.get(m1.pom.path).sendFile(m1.pom.file),
            server.get(m2.pom.path).sendFile(m2.pom.file),
            server.get(m3.pom.path).sendFile(m3.pom.file),
            server.get(m4.pom.path).sendFile(m4.pom.file))
        server.expectConcurrent(
            server.get(m1.artifact.path).sendFile(m1.artifact.file),
            server.get(m2.artifact.path).sendFile(m2.artifact.file),
            server.get(m3.artifact.path).sendFile(m3.artifact.file),
            server.get(m4.artifact.path).sendFile(m4.artifact.file))

        expect:
        executer.withArguments('--max-workers', '4')
        succeeds("resolve")

        where:
        expression                                                       | _
        "configurations.compile"                                         | _
        "configurations.compile.fileCollection { true }"                 | _
        "configurations.compile.incoming.files"                          | _
        "configurations.compile.incoming.artifacts.artifactFiles"        | _
        "configurations.compile.resolvedConfiguration.getFiles { true }" | _
    }

    def "downloads artifacts in parallel from an Ivy repo"() {
        def m1 = ivyRepo.module('test', 'test1', '1.0').publish()
        def m2 = ivyRepo.module('test', 'test2', '1.0').publish()
        def m3 = ivyRepo.module('test', 'test3', '1.0').publish()
        def m4 = ivyRepo.module('test', 'test4', '1.0').publish()

        buildFile << """
            repositories {
                ivy { 
                    url = uri('$server.uri')
                    $authConfig
                }
            }
            configurations { compile }
            dependencies {
                compile 'test:test1:1.0'
                compile 'test:test2:1.0'
                compile 'test:test3:1.0'
                compile 'test:test4:1.0'
            }
            task resolve {
                inputs.files configurations.compile
                doLast {
                    println configurations.compile.files
                }
            }
"""

        given:
        server.expectConcurrent(
            server.get(m1.ivy.path).sendFile(m1.ivy.file),
            server.get(m2.ivy.path).sendFile(m2.ivy.file),
            server.get(m3.ivy.path).sendFile(m3.ivy.file),
            server.get(m4.ivy.path).sendFile(m4.ivy.file))
        server.expectConcurrent(
            server.get(m1.jar.path).sendFile(m1.jar.file),
            server.get(m2.jar.path).sendFile(m2.jar.file),
            server.get(m3.jar.path).sendFile(m3.jar.file),
            server.get(m4.jar.path).sendFile(m4.jar.file))

        expect:
        executer.withArguments('--max-workers', '4')
        succeeds("resolve")
    }

    def "parallel download honors max workers"() {
        def m1 = mavenRepo.module('test', 'test1', '1.0').publish()
        def m2 = mavenRepo.module('test', 'test2', '1.0').publish()
        def m3 = mavenRepo.module('test', 'test3', '1.0').publish()
        def m4 = mavenRepo.module('test', 'test4', '1.0').publish()

        buildFile << """
            repositories {
                maven { 
                    url = uri('$server.uri')
                    $authConfig
                }
            }
            configurations { compile }
            dependencies {
                compile 'test:test1:1.0'
                compile 'test:test2:1.0'
                compile 'test:test3:1.0'
                compile 'test:test4:1.0'
            }
            task resolve {
                inputs.files configurations.compile
                doLast {
                    println configurations.compile.files
                }
            }
"""

        given:
        def metadataRequests = server.expectConcurrentAndBlock(2,
            server.get(m1.pom.path).sendFile(m1.pom.file),
            server.get(m2.pom.path).sendFile(m2.pom.file),
            server.get(m3.pom.path).sendFile(m3.pom.file),
            server.get(m4.pom.path).sendFile(m4.pom.file))
        def requests = server.expectConcurrentAndBlock(2,
            server.get(m1.artifact.path).sendFile(m1.artifact.file),
            server.get(m2.artifact.path).sendFile(m2.artifact.file),
            server.get(m3.artifact.path).sendFile(m3.artifact.file),
            server.get(m4.artifact.path).sendFile(m4.artifact.file))

        expect:
        executer.withArguments('--max-workers', '2')
        def build = executer.withTasks("resolve").start()

        metadataRequests.waitForAllPendingCalls()
        metadataRequests.release(2)

        metadataRequests.waitForAllPendingCalls()
        metadataRequests.release(2)

        requests.waitForAllPendingCalls()
        requests.release(2)

        requests.waitForAllPendingCalls()
        requests.release(2)

        build.waitForFinish()
    }

    def "component metadata rules are executed synchronously"() {
        def m1 = ivyRepo.module('test', 'test1', '1.0').publish()
        def m2 = ivyRepo.module('test', 'test2', '1.0').publish()
        def m3 = ivyRepo.module('test', 'test3', '1.0').publish()
        def m4 = ivyRepo.module('test', 'test4', '1.0').publish()

        buildFile << """
            repositories {
                ivy { 
                    url = uri('$server.uri')
                    $authConfig
                }
            }
            
            configurations { compile }
            def lock = new java.util.concurrent.locks.ReentrantLock()
            
            dependencies {
                compile 'test:test1:1.0'
                compile 'test:test2:1.0'
                compile 'test:test3:1.0'
                compile 'test:test4:1.0'
                
                components {
                    all { ComponentMetadataDetails details ->
                        if (!lock.tryLock()) {
                            throw new AssertionError("Rule called concurrently")
                        }
                        lock.unlock()
                    }
                    withModule("test:test1") { ComponentMetadataDetails details ->
                        // need to make sure that rules are not executed concurrently
                        // because they can share state (typically... this lock!)
                        if (!lock.tryLock()) {
                            throw new AssertionError("Rule called concurrently")
                        }
                        lock.unlock()
                    }
                }
            }
            task resolve {
                inputs.files configurations.compile
                doLast {
                    println configurations.compile.files
                }
            }
"""

        given:
        server.expectConcurrent(
            server.get(m1.ivy.path).sendFile(m1.ivy.file),
            server.get(m2.ivy.path).sendFile(m2.ivy.file),
            server.get(m3.ivy.path).sendFile(m3.ivy.file),
            server.get(m4.ivy.path).sendFile(m4.ivy.file))
        server.expectConcurrent(
            server.get(m1.jar.path).sendFile(m1.jar.file),
            server.get(m2.jar.path).sendFile(m2.jar.file),
            server.get(m3.jar.path).sendFile(m3.jar.file),
            server.get(m4.jar.path).sendFile(m4.jar.file))

        expect:
        executer.withArguments('--max-workers', '4')
        succeeds("resolve")
    }

    @Issue("gradle/gradle#2415")
    def "should not deadlock when downloading parent pom concurrently with a top-level dependency"() {
        given:
        def child1 = mavenRepo.module("org", "child1", "1.0")
        child1.parent("org", "parent1", "1.0")
        child1.publish()

        def child2 = mavenRepo.module("org", "child2", "1.0")
        child2.parent("org", "parent2", "1.0")
        child2.publish()

        def parent1 = mavenRepo.module("org", "parent1", "1.0")
        parent1.hasPackaging('pom')
        parent1.publish()

        def parent2 = mavenRepo.module("org", "parent2", "1.0")
        parent2.hasPackaging('pom')
        parent2.publish()

        buildFile << """
            repositories {
                maven { 
                    url = uri('$server.uri')
                    $authConfig
                }
            }

            configurations { compile }
            dependencies { 
                compile 'org:child1:1.0' 
                compile 'org:child2:1.0'    
            }
            
            task resolve {
               inputs.files configurations.compile
                  doLast {
                      println configurations.compile.files
                  }
            }
        """

        when:
        def getChildrenConcurrently = server.expectConcurrentAndBlock(
            server.get(child1.pom.path).sendFile(child1.pom.file),
            server.get(child2.pom.path).sendFile(child2.pom.file),
        )

        def getParentsConcurrently = server.expectConcurrentAndBlock(
            server.get(parent1.pom.path).sendFile(parent1.pom.file),
            server.get(parent2.pom.path).sendFile(parent2.pom.file),
        )

        def jars = server.expectConcurrentAndBlock(
            server.get(child1.artifact.path).sendFile(child1.artifact.file),
            server.get(child2.artifact.path).sendFile(child2.artifact.file),
        )

        then:
        executer.withArguments('--max-workers', '4')
        def build = executer.withTasks("resolve").start()

        getChildrenConcurrently.waitForAllPendingCalls()
        getChildrenConcurrently.releaseAll()

        getParentsConcurrently.waitForAllPendingCalls()
        getParentsConcurrently.releaseAll()

        jars.waitForAllPendingCalls()
        jars.release(2)

        build.waitForFinish()
    }

}
