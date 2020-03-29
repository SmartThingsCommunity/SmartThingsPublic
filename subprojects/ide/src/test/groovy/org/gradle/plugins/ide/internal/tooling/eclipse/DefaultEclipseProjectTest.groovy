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
package org.gradle.plugins.ide.internal.tooling.eclipse

import spock.lang.Specification

class DefaultEclipseProjectTest extends Specification {
    def usesPathForToStringValue() {
        def project = new DefaultEclipseProject("name", ":path", null, null, [])

        expect:
        project.toString() == "project ':path'"
    }

    def emptyDefaultNatureList() {
        def project = new DefaultEclipseProject("name", ":path", null, null, [])

        expect:
        project.projectNatures.isEmpty()
    }

    def emptyDefaultBuilderList() {
        def project = new DefaultEclipseProject("name", ":path", null, null, [])

        expect:
        project.buildCommands.isEmpty()
    }
}
