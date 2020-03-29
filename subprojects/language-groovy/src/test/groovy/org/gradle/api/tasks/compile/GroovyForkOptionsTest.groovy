/*
 * Copyright 2007-2008 the original author or authors.
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

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

public class GroovyForkOptionsTest {
    static final Map PROPS = [memoryInitialSize: 'memoryInitialSize', memoryMaximumSize: 'memoryMaximumSize']

    GroovyForkOptions forkOptions

    @Before public void setUp()  {
        forkOptions = new GroovyForkOptions()
    }

    @Test public void testCompileOptions() {
        assertNull(forkOptions.memoryInitialSize)
        assertNull(forkOptions.memoryMaximumSize)
        assertTrue(forkOptions.jvmArgs.empty)
    }

    @Test public void testOptionMap() {
        Map optionMap = forkOptions.optionMap()
        assertEquals(0, optionMap.size())
        PROPS.keySet().each { forkOptions."$it" = "${it}Value" }
        optionMap = forkOptions.optionMap()
        assertEquals(2, optionMap.size())
        PROPS.keySet().each {assertEquals("${it}Value" as String, optionMap[PROPS[it]])}
    }

    @Test public void testDefine() {
        forkOptions.define(PROPS.keySet().inject([:]) { Map map, String prop ->
            map[prop] = "${prop}Value" as String
            map
        })
        PROPS.keySet().each {assertEquals("${it}Value" as String, forkOptions."${it}")}
    }
}
