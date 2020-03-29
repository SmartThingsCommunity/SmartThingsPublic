/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.internal.tasks

import org.gradle.api.JavaVersion
import org.gradle.internal.jvm.inspection.JvmVersionDetector
import org.gradle.test.fixtures.file.CleanupTestDirectory
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule

@CleanupTestDirectory
class JavaHomeBasedJavaToolChainTest extends AbstractJavaToolChainTest {
    @Rule
    public final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())

    JvmVersionDetector jvmVersionDetector = Stub(JvmVersionDetector) {
        getJavaVersion(_) >> {
            toolChainJavaVersion
        }
    }
    JavaVersion toolChainJavaVersion = JavaVersion.VERSION_1_8
    def javaHome = temporaryFolder.file('javaHome').create {
        bin {
            file('java').text = 'java'
            file('java.exe').text = 'java.exe'
        }
    }

    JavaHomeBasedJavaToolChain toolChain = new JavaHomeBasedJavaToolChain(javaHome, javaCompilerFactory, execActionFactory, jvmVersionDetector)
}
