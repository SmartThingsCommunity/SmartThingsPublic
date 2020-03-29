/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.api.tasks

import org.gradle.api.internal.AbstractTask
import org.gradle.internal.extensibility.ConventionAwareHelper
import org.gradle.api.internal.ConventionTask

public abstract class AbstractConventionTaskTest extends AbstractTaskTest {

    public abstract AbstractTask getTask();

    def "is aware of conventions"() {
        given:
        ConventionTask task = (ConventionTask) getTask();

        expect:
        task.getConventionMapping() instanceof ConventionAwareHelper

        when:
        ConventionAwareHelper conventionMapping = (ConventionAwareHelper) task.getConventionMapping();

        then:
        conventionMapping.getConvention().is(task.getConvention())
    }
}


