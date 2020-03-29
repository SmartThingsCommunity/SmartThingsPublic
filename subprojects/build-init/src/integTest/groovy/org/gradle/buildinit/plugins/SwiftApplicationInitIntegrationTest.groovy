/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.buildinit.plugins


import org.gradle.buildinit.plugins.fixtures.ScriptDslFixture
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.nativeplatform.fixtures.AvailableToolChains
import org.gradle.nativeplatform.fixtures.ExecutableFixture
import org.gradle.nativeplatform.fixtures.RequiresInstalledToolChain
import org.gradle.nativeplatform.fixtures.ToolChainRequirement
import spock.lang.Unroll

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationInitIntegrationTest extends AbstractInitIntegrationSpec {

    public static final String SAMPLE_APPLICATION_CLASS = "main.swift"
    public static final String SAMPLE_APPLICATION_TEST_CLASS = "GreeterTests.swift"
    public static final String LINUX_MAIN_DOT_SWIFT = "LinuxMain.swift"

    private final AvailableToolChains.InstalledToolChain swiftcToolChain = AvailableToolChains.getToolChain(ToolChainRequirement.SWIFTC)

    def setup() {
        swiftcToolChain.initialiseEnvironment()
    }

    def cleanup() {
        swiftcToolChain.resetEnvironment()
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "creates sample source if no source present with #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'swift-application', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/swift").assertHasDescendants(SAMPLE_APPLICATION_CLASS)
        targetDir.file("src/test/swift").assertHasDescendants(SAMPLE_APPLICATION_TEST_CLASS, LINUX_MAIN_DOT_SWIFT)

        and:
        targetDir.file("src/test/swift/${SAMPLE_APPLICATION_TEST_CLASS}").text.contains("@testable import SomeThing")

        and:
        commonFilesGenerated(scriptDsl)

        and:
        succeeds("build")

        and:
        executable("build/exe/main/debug/SomeThing").exec().out ==  "Hello, World!\n"

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "creates sample source if project name is specified with #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'swift-application', '--project-name', 'app', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/swift").assertHasDescendants(SAMPLE_APPLICATION_CLASS)
        targetDir.file("src/test/swift").assertHasDescendants(SAMPLE_APPLICATION_TEST_CLASS, LINUX_MAIN_DOT_SWIFT)

        and:
        targetDir.file("src/test/swift/${SAMPLE_APPLICATION_TEST_CLASS}").text.contains("@testable import App")

        and:
        commonFilesGenerated(scriptDsl)

        and:
        succeeds("build")

        and:
        executable("build/exe/main/debug/App").exec().out ==  "Hello, World!\n"

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    @Unroll
    @ToBeFixedForInstantExecution
    def "source generation is skipped when swift sources detected with #scriptDsl build scripts"() {
        setup:
        targetDir.file("src/main/swift/main.swift") << """
            public func hola() -> String {
                return "Hola, Mundo!"
            }
            
            print(hola())
        """
        targetDir.file("src/test/swift/HolaTests.swift") << """
            import XCTest
            @testable import App
            
            class HolaTests: XCTestCase {
                public static var allTests = [
                    ("testGreeting", testGreeting),
                ]
            
                func testGreeting() {
                    XCTAssertEqual("Hola, Mundo!", hola())
                }
            }
        """
        targetDir.file("src/test/swift/${LINUX_MAIN_DOT_SWIFT}") << """
            import XCTest

            XCTMain([testCase(HolaTests.allTests)])
        """

        when:
        run('init', '--type', 'swift-application', '--project-name', 'app', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/swift").assertHasDescendants("main.swift")
        targetDir.file("src/test/swift").assertHasDescendants("HolaTests.swift", LINUX_MAIN_DOT_SWIFT)
        dslFixtureFor(scriptDsl).assertGradleFilesGenerated()

        and:
        targetDir.file("src/main/swift/${SAMPLE_APPLICATION_CLASS}").text.contains("hola()")
        targetDir.file("src/test/swift/${SAMPLE_APPLICATION_TEST_CLASS}").assertDoesNotExist()
        targetDir.file("src/test/swift/${LINUX_MAIN_DOT_SWIFT}").text.contains("HolaTests.allTests")

        when:
        run("build")

        then:
        executed(":test")

        and:
        executable("build/exe/main/debug/App").exec().out ==  "Hola, Mundo!\n"

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    ExecutableFixture executable(String path) {
        AvailableToolChains.getToolChain(ToolChainRequirement.SWIFTC).executable(targetDir.file(path))
    }
}
