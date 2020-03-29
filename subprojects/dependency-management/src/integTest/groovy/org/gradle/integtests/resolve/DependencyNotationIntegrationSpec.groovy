/*
 * Copyright 2012 the original author or authors.
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

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.hamcrest.CoreMatchers
import spock.lang.Issue

class DependencyNotationIntegrationSpec extends AbstractIntegrationSpec {

    @ToBeFixedForInstantExecution(because = "unsupported type Dependency")
    def "understands dependency notations"() {
        when:
        buildFile <<  """
import org.gradle.api.internal.artifacts.dependencies.*
configurations {
    conf
    gradleStuff
    allowsCollections
}

def someDependency = new DefaultSelfResolvingDependency(files('foo.txt'))
dependencies {
    conf someDependency
    conf "org.mockito:mockito-core:1.8"
    conf group: 'org.spockframework', name: 'spock-core', version: '1.0'
    conf('org.test:configured') {
        version {
           prefer '1.1'
        }
        transitive = false
        force = true
    }

    conf module('org.foo:moduleOne:1.0'), module('org.foo:moduleTwo:1.0')

    gradleStuff gradleApi()

    allowsCollections "org.mockito:mockito-core:1.8", someDependency
}

task checkDeps {
    doLast {
        def deps = configurations.conf.incoming.dependencies
        assert deps.contains(someDependency)
        assert deps.find { it instanceof ExternalDependency && it.group == 'org.mockito' && it.name == 'mockito-core' && it.version == '1.8'  }
        assert deps.find { it instanceof ExternalDependency && it.group == 'org.spockframework' && it.name == 'spock-core' && it.version == '1.0'  }
        def configuredDep = deps.find { it instanceof ExternalDependency && it.group == 'org.test' && it.name == 'configured' }
        assert configuredDep.version == '1.1'
        assert configuredDep.transitive == false
        assert configuredDep.force == true
        
        assert deps.find { it instanceof ClientModule && it.name == 'moduleOne' && it.group == 'org.foo' }
        assert deps.find { it instanceof ClientModule && it.name == 'moduleTwo' && it.version == '1.0' }

        deps = configurations.gradleStuff.dependencies
        assert deps.findAll { it instanceof SelfResolvingDependency }.size() > 0 : "should include gradle api jars"

        deps = configurations.allowsCollections.dependencies
        assert deps.size() == 2
        assert deps.find { it instanceof ExternalDependency && it.group == 'org.mockito' }
        assert deps.contains(someDependency)
    }
}
"""
        then:
        executer.expectDeprecationWarning()
        succeeds 'checkDeps'
    }

    def "understands project notations"() {
        when:
        settingsFile << "include 'otherProject'"

        buildFile <<  """
configurations {
    conf
    confTwo
}

project(':otherProject') {
    configurations {
        otherConf
    }
}

dependencies {
    conf project(':otherProject')
    confTwo project(path: ':otherProject', configuration: 'otherConf')
}

task checkDeps {
    doLast {
        def deps = configurations.conf.incoming.dependencies
        assert deps.size() == 1
        assert deps.find { it.dependencyProject.path == ':otherProject' && it.targetConfiguration == null }

        deps = configurations.confTwo.incoming.dependencies
        assert deps.size() == 1
        assert deps.find { it.dependencyProject.path == ':otherProject' && it.targetConfiguration == 'otherConf' }
    }
}
"""
        then:
        succeeds 'checkDeps'
    }

    def "understands client module notation with dependencies"() {
        when:
        buildFile <<  """
configurations {
    conf
}

dependencies {
    conf module('org.foo:moduleOne:1.0') {
        dependency 'org.foo:bar:1.0'
        dependencies ('org.foo:one:1', 'org.foo:two:1')
        dependency ('high:five:5') { transitive = false }
        dependency('org.test:lateversion') { 
               version {
                  prefer '1.0' 
                  strictly '1.1' // intentionally overriding "prefer" 
               } 
           }
    }
}

task checkDeps {
    doLast {
        def deps = configurations.conf.incoming.dependencies
        assert deps.size() == 1
        def dep = deps.find { it instanceof ClientModule && it.name == 'moduleOne' }
        assert dep
        assert dep.dependencies.size() == 5
        assert dep.dependencies.find { it.group == 'org.foo' && it.name == 'bar' && it.version == '1.0' && it.transitive == true }
        assert dep.dependencies.find { it.group == 'org.foo' && it.name == 'one' && it.version == '1' }
        assert dep.dependencies.find { it.group == 'org.foo' && it.name == 'two' && it.version == '1' }
        assert dep.dependencies.find { it.group == 'high' && it.name == 'five' && it.version == '5' && it.transitive == false }
        assert dep.dependencies.find { it.group == 'org.test' && it.name == 'lateversion' && it.version == '1.1' }
    }
}
"""
        then:
        succeeds 'checkDeps'
    }

    def "fails gracefully for invalid notations"() {
        when:
        buildFile <<  """
configurations {
    conf
}

dependencies {
    conf 100
}

task checkDeps
"""
        then:
        fails 'checkDeps'
        failure.assertThatCause(CoreMatchers.startsWith("Cannot convert the provided notation to an object of type Dependency: 100."))
    }

    def "fails gracefully for single null notation"() {
        when:
        buildFile <<  """
configurations {
    conf
}

dependencies {
    conf null
}

task checkDeps
"""
        then:
        fails 'checkDeps'
        failure.assertThatCause(CoreMatchers.startsWith("Cannot convert a null value to an object of type Dependency"))
    }

    def "fails gracefully for null notation in list"() {
        when:
        buildFile <<  """
configurations {
    conf
}

dependencies {
    conf "a:b:c", null, "d:e:f"
}

task checkDeps
"""
        then:
        fails 'checkDeps'
        failure.assertThatCause(CoreMatchers.startsWith("Cannot convert a null value to an object of type Dependency"))
    }

    @Issue("https://issues.gradle.org/browse/GRADLE-3271")
    def "gradleApi dependency implements contentEquals"() {
        when:
        buildFile << """
            configurations {
              conf
            }

            dependencies {
              conf gradleApi()
            }

            task check {
                doLast {
                    assert dependencies.gradleApi().contentEquals(dependencies.gradleApi())
                    assert dependencies.gradleApi().is(dependencies.gradleApi())
                    assert dependencies.gradleApi() == dependencies.gradleApi()
                    assert configurations.conf.dependencies.contains(dependencies.gradleApi())
                }
            }
        """

        then:
        succeeds "check"
    }
}
