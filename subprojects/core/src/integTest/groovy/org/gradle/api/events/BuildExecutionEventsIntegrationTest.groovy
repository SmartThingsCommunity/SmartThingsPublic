/*
 * Copyright 2011 the original author or authors.
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

package org.gradle.api.events

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class BuildExecutionEventsIntegrationTest extends AbstractIntegrationSpec {

    def "events passed to any task execution listener are synchronised"() {
        settingsFile << "include 'a', 'b', 'c'"
        buildFile << """
            def listener = new MyListener()
            gradle.addListener(listener)

            allprojects {
                task foo
            }

            class MyListener implements TaskExecutionListener {
                def called = []
                void beforeExecute(Task task) {
                    check(task)
                }
                void afterExecute(Task task, TaskState state) {
                    check(task)
                }
                void check(task) {
                    called << task
                    Thread.sleep(100)
                    //the last task added to the list should be exactly what we have added before sleep
                    //this way we assert that events passed to the listener are synchronised and any listener implementation is thread safe
                    assert called[-1] == task
                }
            }
        """

        when:
        run("foo")

        then:
        noExceptionThrown()
    }
}
