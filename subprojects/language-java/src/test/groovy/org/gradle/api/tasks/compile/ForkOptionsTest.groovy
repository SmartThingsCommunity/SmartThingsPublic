/*
 * Copyright 2007 the original author or authors.
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

import spock.lang.Specification

import static org.junit.Assert.assertEquals

class ForkOptionsTest extends Specification {
    static final List PROPS = ['executable', 'memoryInitialSize', 'memoryMaximumSize', 'tempDir']

    ForkOptions forkOptions = new ForkOptions()

    def 'initial values of forkOptions'() {
        expect:
        forkOptions.executable == null
        forkOptions.javaHome == null
        forkOptions.memoryInitialSize == null
        forkOptions.memoryMaximumSize == null
        forkOptions.tempDir == null
        forkOptions.jvmArgs == []
    }

    def 'options can be read as Map'() {
        when:
        Map optionMap = forkOptions.optionMap()
        then:
        assertEquals(0, optionMap.size())
        when:
        PROPS.each { forkOptions."$it" = "${it}Value" }
        optionMap = forkOptions.optionMap()
        then:
        assertEquals(4, optionMap.size())
        PROPS.each { assert optionMap[it] == "${it}Value" as String }
    }

    def 'options can be defined via a map'() {
        when:
        forkOptions.define(PROPS.collectEntries { [it, "${it}Value" as String ] })
        then:
        PROPS.each { assert forkOptions."${it}" == "${it}Value" as String }
    }
}
