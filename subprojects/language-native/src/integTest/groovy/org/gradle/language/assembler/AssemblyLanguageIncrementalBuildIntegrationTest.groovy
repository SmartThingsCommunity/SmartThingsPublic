/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.language.assembler

import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.nativeplatform.fixtures.AbstractInstalledToolChainIntegrationSpec
import org.gradle.nativeplatform.fixtures.RequiresInstalledToolChain
import org.gradle.nativeplatform.fixtures.ToolChainRequirement
import org.gradle.nativeplatform.fixtures.app.HelloWorldApp
import org.gradle.nativeplatform.fixtures.app.MixedLanguageHelloWorldApp
import org.gradle.test.fixtures.file.TestFile
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition

@RequiresInstalledToolChain(ToolChainRequirement.SUPPORTS_32_AND_64)
class AssemblyLanguageIncrementalBuildIntegrationTest extends AbstractInstalledToolChainIntegrationSpec {

    HelloWorldApp app = new MixedLanguageHelloWorldApp(AbstractInstalledToolChainIntegrationSpec.toolChain)
    TestFile asmSourceFile
    def install

    def "setup"() {
        buildFile << """
            plugins {
                id 'assembler'
                id 'c'
                id 'cpp'
            }

            $app.extraConfiguration

            model {
                components {
                    hello(NativeLibrarySpec)
                    main(NativeExecutableSpec) {
                        binaries.all {
                            lib library: 'hello'
                        }
                    }
                }
            }
        """
        settingsFile << "rootProject.name = 'test'"

        app.executable.writeSources(file("src/main"))
        app.library.writeSources(file("src/hello"))
        asmSourceFile = file("src/hello/asm/sum.s")

        run "installMainExecutable"

        install = installation("build/install/main")
    }

    @ToBeFixedForInstantExecution
    def "does not re-execute build with no change"() {
        when:
        run "mainExecutable"

        then:
        allSkipped()
    }

    @Requires(TestPrecondition.CAN_INSTALL_EXECUTABLE)
    @ToBeFixedForInstantExecution
    def "reassembles binary with assembler option change"() {
        when:
        buildFile << """
            model {
                components {
                    hello {
                        binaries.all {
                            if (toolChain in VisualCpp) {
                                assembler.args '/Zf'
                            } else {
                                assembler.args '-W'
                            }
                        }
                    }
                }
            }
"""

        run "installMainExecutable"

        then:
        executedAndNotSkipped ":assembleHelloSharedLibraryHelloAsm"

        and:
        install.exec().out == app.englishOutput
    }

    @Requires([TestPrecondition.NOT_WINDOWS, TestPrecondition.CAN_INSTALL_EXECUTABLE])
    @ToBeFixedForInstantExecution
    def "reassembles binary with target platform change"() {
        when:
        buildFile.text = buildFile.text.replace("i386", "x86-64")

        run "installMainExecutable"

        then:
        executedAndNotSkipped ":assembleHelloSharedLibraryHelloAsm"

        // Need to have valid x86-64 sources, so that we can verify the output: currently we're producing a binary that won't work on x86-64
    }

    @ToBeFixedForInstantExecution
    def "cleans up stale object files when source file renamed"() {
        def oldObjFile = objectFileFor(asmSourceFile, "build/objs/hello/shared/helloAsm")
        def newObjFile = objectFileFor(file('src/hello/asm/changed_sum.s'), "build/objs/hello/shared/helloAsm")
        assert oldObjFile.file
        assert !newObjFile.file

        when:
        asmSourceFile.renameTo(file("src/hello/asm/changed_sum.s"))
        run "mainExecutable"

        then:
        executedAndNotSkipped ":assembleHelloSharedLibraryHelloAsm"

        and:
        !oldObjFile.file
        newObjFile.file
    }

    @ToBeFixedForInstantExecution
    def "reassembles binary with source comment change"() {
        when:
        asmSourceFile << "# A comment at the end of the file\n"
        run "mainExecutable"

        then:
        executedAndNotSkipped ":assembleHelloSharedLibraryHelloAsm"
    }
}

