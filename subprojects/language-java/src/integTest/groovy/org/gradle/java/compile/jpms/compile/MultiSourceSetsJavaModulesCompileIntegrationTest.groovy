/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.java.compile.jpms.compile

class MultiSourceSetsJavaModulesCompileIntegrationTest extends AbstractMultipleLocalJavaModulesCompileIntegrationTest {

    def setup() {
        buildFile << """
            sourceSets {
                producer
            }
            java {
                registerFeature('producer') {
                    usingSourceSet(sourceSets.producer)
                }
            }
            dependencies {
                implementation(project(':')) {
                    capabilities {
                        requireCapability("org:consumer-producer")
                    }
                }
            }
        """
    }

    @Override
    protected producingAutomaticModuleNameSetting() {
        buildFile << """
            tasks.producerJar {
                manifest {
                    attributes 'Automatic-Module-Name': 'producer'
                }
            }
        """
    }

    @Override
    protected producingModuleInfo(String... statements) {
        file('src/producer/java/module-info.java').text = "module producer { ${statements.collect { it + ';' }.join(' ') } }"
    }

    @Override
    protected producingModuleClass() {
        file('src/producer/java/producer/ProducerClass.java').text = """
            package producer;

            public class ProducerClass {}
        """
    }
}
