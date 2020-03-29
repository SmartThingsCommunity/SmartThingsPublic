/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.language.objectivecpp
import org.gradle.nativeplatform.fixtures.AbstractInstalledToolChainIntegrationSpec
import org.gradle.nativeplatform.fixtures.RequiresInstalledToolChain
import org.gradle.nativeplatform.fixtures.ToolChainRequirement
import org.gradle.nativeplatform.fixtures.app.ObjectiveCppHelloWorldApp

import static org.hamcrest.CoreMatchers.containsString

@RequiresInstalledToolChain(ToolChainRequirement.VISUALCPP)
class ObjectiveCppUnsupportedIntegrationTest extends AbstractInstalledToolChainIntegrationSpec{

    def helloWorldApp = new ObjectiveCppHelloWorldApp()

    def "setup"() {
        buildFile << helloWorldApp.pluginScript
        buildFile << helloWorldApp.extraConfiguration
    }

    def "fails with decent error message with visual studio toolchain"() {
        given:
        buildFile << """
model {
    components {
        main(NativeExecutableSpec)
    }
}
        """

        and:
        helloWorldApp.writeSources(file("src/main"))

        when:
        fails "compileMainExecutableMainObjcpp"

        then:
        failure.assertThatCause(containsString("Objective-C++ is not available on the Visual C++ toolchain"))
    }
}
