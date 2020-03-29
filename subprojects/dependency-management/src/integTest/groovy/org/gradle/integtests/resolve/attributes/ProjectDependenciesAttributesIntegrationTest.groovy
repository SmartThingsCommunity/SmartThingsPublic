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

package org.gradle.integtests.resolve.attributes

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.resolve.ResolveTestFixture
import spock.lang.Unroll

class ProjectDependenciesAttributesIntegrationTest extends AbstractIntegrationSpec {

    ResolveTestFixture resolve = new ResolveTestFixture(buildFile, 'conf')

    def setup() {
        buildFile << """
            configurations {
               conf
            }
        """
        settingsFile << """
            rootProject.name = 'test'
        """
        resolve.prepare()
    }

    @Unroll
    def "uses dependency attributes to select the right configuration on the target project (color=#color)"() {
        given:
        settingsFile << "include 'dep'"
        buildFile << """
            dependencies {
                conf(project(':dep')) {
                    attributes {
                        attribute(Attribute.of('color', String), '$color')                        
                    }
                }
            }
        """
        file("dep/build.gradle") << blueAndRedVariants()

        when:
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                project(':dep', "test:dep:unspecified") {
                    variant "${color}Variant", [color: color]
                    noArtifacts()
                }
            }
        }

        where:
        color << ['blue', 'red']
    }

    def "Fails with reasonable error message when no target variant can be found"() {
        given:
        settingsFile << "include 'dep'"
        buildFile << """
            dependencies {
                conf(project(':dep')) {
                    attributes {
                        attribute(Attribute.of('color', String), 'green')                        
                    }
                }
            }
        """
        file("dep/build.gradle") << blueAndRedVariants()

        when:
        fails ':checkDeps'

        then:
        failure.assertHasCause("""Unable to find a matching variant of project :dep:
  - Variant 'blueVariant' capability test:dep:unspecified:
      - Incompatible attribute:
          - Required color 'green' and found incompatible value 'blue'.
  - Variant 'redVariant' capability test:dep:unspecified:
      - Incompatible attribute:
          - Required color 'green' and found incompatible value 'red'.""")
    }

    def "dependency attributes override configuration attributes"() {
        given:
        settingsFile << "include 'dep'"
        buildFile << """
            configurations {
                conf {
                    attributes {
                        attribute(Attribute.of('color', String), 'blue')                        
                    }
                }
            }
            dependencies {
                conf(project(':dep')) {
                    attributes {
                        attribute(Attribute.of('color', String), 'red')                        
                    }
                }
            }
        """
        file("dep/build.gradle") << blueAndRedVariants()

        when:
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                project(':dep', "test:dep:unspecified") {
                    variant "redVariant", [color: 'red']
                    noArtifacts()
                }
            }
        }

    }

    private static String blueAndRedVariants() {
        """
            configurations {
                blueVariant {
                    canBeResolved = false
                    canBeConsumed = true
                    attributes {
                        attribute(Attribute.of('color', String), 'blue')
                    }
                }
                redVariant {
                    canBeResolved = false
                    canBeConsumed = true
                    attributes {
                        attribute(Attribute.of('color', String), 'red')
                    }
                }
            }
        """
    }
}
