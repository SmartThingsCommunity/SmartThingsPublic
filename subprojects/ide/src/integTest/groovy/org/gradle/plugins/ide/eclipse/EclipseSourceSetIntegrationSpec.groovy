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

package org.gradle.plugins.ide.eclipse

import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.integtests.fixtures.TestResources
import org.junit.Rule

class EclipseSourceSetIntegrationSpec extends AbstractEclipseIntegrationSpec {

    @Rule
    public final TestResources testResources = new TestResources(testDirectoryProvider)

    @ToBeFixedForInstantExecution
    def "Source set defined on dependencies"() {
        setup:
        buildFile << """
            apply plugin: 'java'
            apply plugin: 'eclipse'

            ${jcenterRepository()}

            dependencies {
                implementation 'com.google.guava:guava:18.0'
                compileOnly 'commons-logging:commons-logging:1.2'
                testImplementation 'junit:junit:4.12'
            }
        """

        when:
        run 'eclipse'

        then:
        EclipseClasspathFixture classpath = classpath('.')
        classpath.lib('guava-18.0.jar').assertHasAttribute('gradle_used_by_scope', 'main,test')
        classpath.lib('commons-logging-1.2.jar').assertHasAttribute('gradle_used_by_scope', '')
        classpath.lib('junit-4.12.jar').assertHasAttribute('gradle_used_by_scope', 'test')
    }

    @ToBeFixedForInstantExecution
    def "Source sets defined on source folders"() {
        setup:
        buildFile << """
            apply plugin: 'java'
            apply plugin: 'eclipse'
        """
        file('src/main/java').mkdirs()
        file('src/test/java').mkdirs()

        when:
        run 'eclipse'

        then:
        EclipseClasspathFixture classpath = classpath('.')
        classpath.sourceDir('src/main/java').assertHasAttribute('gradle_used_by_scope', 'main,test')
        classpath.sourceDir('src/test/java').assertHasAttribute('gradle_used_by_scope', 'test')
    }

    @ToBeFixedForInstantExecution
    def "Source set information is customizable in whenMerged block"() {
        setup:
        buildFile << """
            apply plugin: 'java'
            apply plugin: 'eclipse'

            ${jcenterRepository()}

            dependencies {
                implementation 'com.google.guava:guava:18.0'
                testImplementation 'junit:junit:4.12'
            }

            eclipse.classpath.file.whenMerged {
                def testDir = entries.find { entry -> entry.path == 'src/test/java' }
                def guavaDep = entries.find { entry -> entry.path.contains 'guava-18.0.jar' }
                testDir.entryAttributes['gradle_used_by_scope'] = 'test,integTest'
                guavaDep.entryAttributes['gradle_used_by_scope'] = 'main,test,integTest'
            }
        """
        file('src/test/java').mkdirs()

        when:
        run 'eclipse'

        then:
        EclipseClasspathFixture classpath = classpath('.')
        classpath.sourceDir('src/test/java').assertHasAttribute('gradle_used_by_scope', 'test,integTest')
        classpath.lib('junit-4.12.jar').assertHasAttribute('gradle_used_by_scope', 'test')
        classpath.lib('guava-18.0.jar').assertHasAttribute('gradle_used_by_scope', 'main,test,integTest')
    }

    @ToBeFixedForInstantExecution
    def "Source dirs have default output locations"() {
        setup:
        buildFile << """
            apply plugin: 'java'
            apply plugin: 'eclipse'
            
            sourceSets {
                integTest {
                    java {
                        srcDirs 'src/int_test/java'
                    }
                }
            }
        """
        file('src/main/java').mkdirs()
        file('src/main/resources').mkdirs()
        file('src/test/java').mkdirs()
        file('src/test/resources').mkdirs()
        file('src/int_test/java').mkdirs()

        when:
        run 'eclipse'

        then:
        EclipseClasspathFixture classpath = classpath('.')
        classpath.sourceDir('src/main/java').assertOutputLocation('bin/main')
        classpath.sourceDir('src/main/resources').assertOutputLocation('bin/main')
        classpath.sourceDir('src/test/java').assertOutputLocation('bin/test')
        classpath.sourceDir('src/test/resources').assertOutputLocation('bin/test')
        classpath.sourceDir('src/int_test/java').assertOutputLocation('bin/integTest')
    }

    @ToBeFixedForInstantExecution
    def "Source folder output location can be customized in whenMerged block"() {
        setup:
        buildFile << """
            apply plugin: 'java'
            apply plugin: 'eclipse'

            eclipse.classpath.file.whenMerged {
                entries.find { entry -> entry.path == 'src/main/java' }.output = null
                entries.find { entry -> entry.path == 'src/main/resources' }.output = 'out/res'
            }
        """
        file('src/main/java').mkdirs()
        file('src/main/resources').mkdirs()

        when:
        run 'eclipse'

        then:
        EclipseClasspathFixture classpath = classpath('.')
        classpath.sourceDir('src/main/java').assertOutputLocation(null)
        classpath.sourceDir('src/main/resources').assertOutputLocation('out/res')
    }

    @ToBeFixedForInstantExecution
    def "Overlapping default and source folder output paths are deduplicated"() {
        setup:
        buildFile << """
            apply plugin: 'java'
            apply plugin: 'eclipse'
            
            sourceSets {
                "default" {
                    java {
                        srcDirs 'src/default/java'
                    }
                }
                
                default_ {
                    java {
                        srcDirs 'src/default_/java'
                    }
                }
            }
        """
        file('src/default/java').mkdirs()
        file('src/default_/java').mkdirs()


        when:
        run 'eclipse'

        then:
        EclipseClasspathFixture classpath = classpath('.')
        classpath.output == 'bin/default'
        classpath.sourceDir('src/default/java').assertOutputLocation('bin/default_')
        classpath.sourceDir('src/default_/java').assertOutputLocation('bin/default__')
    }
}
