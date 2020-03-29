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

package org.gradle.initialization.buildsrc

import org.gradle.integtests.fixtures.AbstractIntegrationSpec


class BuildSrcIncludedBuildIntegrationTest extends AbstractIntegrationSpec {
    def "buildSrc cannot (yet) define any included builds"() {
        file("buildSrc/settings.gradle") << """
            includeBuild "child"
        """
        file("buildSrc/child/settings.gradle").createFile()

        when:
        fails()

        then:
        failure.assertHasDescription("Cannot include build 'child' in build 'buildSrc'. This is not supported yet.")
    }
}
