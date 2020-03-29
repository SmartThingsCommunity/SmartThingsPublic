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

package org.gradle.api.plugins.quality.codenarc

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution

class CodeNarcCompilationClasspathIntegrationTest extends AbstractIntegrationSpec {

    private final static String CONFIG_FILE_PATH = 'config/codenarc/rulesets.groovy'
    private final static String SUPPORTED_COMPILATION_CLASSPATH_VERSION = '0.27.0'
    private final static String UNSUPPORTED_COMPILATION_CLASSPATH_VERSION = '0.26.0'

    @ToBeFixedForInstantExecution
    def "compilation classpath can be specified for a CodeNarc task"() {
        given:
        buildFileWithCodeNarcAndCompilationClasspath(SUPPORTED_COMPILATION_CLASSPATH_VERSION)
        cloneWithoutCloneableRuleEnabled()
        codeViolatingCloneWithoutCloneableRule()

        when:
        fails("codenarcMain")

        then:
        failure.assertHasCause('CodeNarc rule violations were found')
    }

    @ToBeFixedForInstantExecution
    def "an informative error is shown when a compilation classpath is specified on a CodeNarc task when using an incompatible CodeNarc version"() {
        given:
        buildFileWithCodeNarcAndCompilationClasspath(UNSUPPORTED_COMPILATION_CLASSPATH_VERSION)
        cloneWithoutCloneableRuleEnabled()
        codeViolatingCloneWithoutCloneableRule()

        when:
        fails("codenarcMain")

        then:
        failure.assertHasCause("The compilationClasspath property of CodeNarc task can only be non-empty when using CodeNarc $SUPPORTED_COMPILATION_CLASSPATH_VERSION or newer.")
    }

    private void buildFileWithCodeNarcAndCompilationClasspath(String codeNarcVersion) {
        buildFile << """
            apply plugin: "codenarc"
            apply plugin: "groovy"

            ${mavenCentralRepository()}

            codenarc {
                toolVersion = '$codeNarcVersion'
                codenarc.configFile = file('$CONFIG_FILE_PATH') 
            }
            
            dependencies {
                implementation localGroovy()
            }
            
            codenarcMain {
                compilationClasspath = configurations.compileClasspath
            }
        """
    }

    private void cloneWithoutCloneableRuleEnabled() {
        file(CONFIG_FILE_PATH) << '''
            ruleset {
                CloneWithoutCloneable
            }
        '''
    }

    private void codeViolatingCloneWithoutCloneableRule() {
        file('src/main/groovy/ViolatingClass.groovy') << '''
            class ViolatingClass extends Tuple {
                ViolatingClass clone() {}
            }
        '''
    }
}
