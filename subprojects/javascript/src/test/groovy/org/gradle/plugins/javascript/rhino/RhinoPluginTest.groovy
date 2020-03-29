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

package org.gradle.plugins.javascript.rhino

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.DynamicDelegate
import spock.lang.Specification

class RhinoPluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()
    @Delegate
    DynamicDelegate delegate = new DynamicDelegate(project)
    RhinoExtension extension

    def setup() {
        project.pluginManager.apply(RhinoPlugin)
        extension = javaScript.rhino
    }

    def "extension is available"() {
        expect:
        extension != null
    }

    def "default version set"() {
        expect:
        extension.version == RhinoExtension.DEFAULT_RHINO_DEPENDENCY_VERSION
    }

}
