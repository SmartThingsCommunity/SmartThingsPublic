/*
 * Copyright 2016 the original author or authors.
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
import spock.lang.Unroll

class LazyDownloadsIntegrationTest extends AbstractHttpDependencyResolutionTest {
    def module = mavenHttpRepo.module("test", "test", "1.0").publish()
    def module2 = mavenHttpRepo.module("test", "test2", "1.0").publish()

    def setup() {
        settingsFile << "include 'child'"
        buildFile << """
            allprojects {
                repositories {
                    maven { url '$mavenHttpRepo.uri' }
                }
                configurations {
                    compile
                    create('default').extendsFrom compile
                }
            }
            
            dependencies {
                compile project(':child')
            }
            project(':child') {
                dependencies {
                    compile 'test:test:1.0'
                    compile 'test:test2:1.0'
                }                
            }
"""
    }

    def "downloads only the metadata when dependency graph is queried"() {
        given:
        buildFile << """
            task graph {
                doLast {
                    println configurations.compile.incoming.resolutionResult.allComponents
                }
            }
"""

        when:
        module.pom.expectGet()
        module2.pom.expectGet()

        then:
        succeeds("graph")
    }

    def "downloads only the metadata when resolved artifacts are queried"() {
        given:
        buildFile << """
            task artifacts {
                doLast {
                    println configurations.compile.resolvedConfiguration.resolvedArtifacts
                }
            }
"""

        when:
        module.pom.expectGet()
        module2.pom.expectGet()

        then:
        succeeds("artifacts")
    }

    @Unroll
    def "downloads only the metadata on failure to resolve the graph - #expression"() {
        given:
        buildFile << """
            task artifacts {
                doLast {
                    configurations.compile.${expression}.each { it }
                }
            }
"""

        when:
        module.pom.expectGetUnauthorized()
        module2.pom.expectGet()

        then:
        fails("artifacts")
        failure.assertResolutionFailure(":compile")
        failure.assertHasCause("Could not resolve test:test:1.0.")

        where:
        expression                                | _
        "files"                                   | _
        "fileCollection { true }"                 | _
        "resolvedConfiguration.resolvedArtifacts" | _
        "incoming.artifacts"                      | _
    }
}
