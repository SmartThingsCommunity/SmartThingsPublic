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

package org.gradle.api.tasks.compile

import org.gradle.jvm.toolchain.JavaToolChain
import org.gradle.test.fixtures.AbstractProjectBuilderSpec
import spock.lang.Issue

class JavaCompileTest extends AbstractProjectBuilderSpec {

    @Issue("https://github.com/gradle/gradle/issues/1645")
    def "can set the Java tool chain"() {
        def javaCompile = project.tasks.create("compileJava", JavaCompile)
        def toolChain = Mock(JavaToolChain)
        when:
        javaCompile.setToolChain(toolChain)
        then:
        javaCompile.toolChain == toolChain
    }
}
