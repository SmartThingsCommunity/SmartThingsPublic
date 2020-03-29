/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.api.plugins

import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.integtests.fixtures.WellBehavedPluginTest
import spock.lang.Unroll

class HelpTasksPluginIntegrationTest extends WellBehavedPluginTest {

    @Override
    String getMainTask() {
        "tasks"
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "can fetch tasks during configuration - #task"() {
        when:
        buildScript """
            assert tasks.names.contains("$task")
            tasks.findByName "$task"
        """

        then:
        succeeds "tasks"

        where:
        task << ["help", "projects", "tasks", "properties", "dependencyInsight", "dependencies", "components", "model"]
    }
}
