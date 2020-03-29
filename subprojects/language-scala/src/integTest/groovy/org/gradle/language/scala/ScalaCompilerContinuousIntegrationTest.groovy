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

package org.gradle.language.scala

import org.gradle.api.tasks.compile.AbstractCompilerContinuousIntegrationTest

class ScalaCompilerContinuousIntegrationTest extends AbstractCompilerContinuousIntegrationTest {
    def setup() {
        // Initial scala compilation is very slow: we need to give additional time to wait for build to complete.
        buildTimeout = 90
    }

    @Override
    String getCompileTaskName() {
        return "compileMainJarMainScala"
    }

    @Override
    String getCompileTaskType() {
        return "PlatformScalaCompile"
    }

    @Override
    String getSourceFileName() {
        return "src/main/scala/Foo.scala"
    }

    @Override
    String getInitialSourceContent() {
        return "object Foo {}"
    }

    @Override
    String getChangedSourceContent() {
        return 'object Foo { val bar = "" }'
    }

    @Override
    String getApplyAndConfigure() {
        return """
            plugins {
                id 'jvm-component'
                id 'scala-lang'
            }

            ${mavenCentralRepository()}

            model {
                components {
                    main(JvmLibrarySpec)
                }
            }
        """
    }
}
