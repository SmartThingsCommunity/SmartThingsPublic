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
package org.gradle.launcher

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.executer.GradleContextualExecuter
import spock.lang.IgnoreIf

/**
 * Verifies that Gradle doesn't pollute the system class loader.
 *
 * This is important for plugins that need to use isolated class loaders to avoid conflicts.
 *
 * When running without the daemon, success is dependant on the start scripts doing the right thing.
 * When running with the daemon, success is dependent on DaemonConnector forking the daemon process with the right classpath.
 *
 * This test is not meaningful when running the embedded integration test mode, so we ignore it in that case.
 */
class SystemClassLoaderTest extends AbstractIntegrationSpec {

    static heading = "systemClassLoader info"
    static noInfoHeading = "no systemClassLoader info"

    /*
        Note: The IBM ClassLoader.systemClassLoader does not throw ClassNotFoundException like it should when you ask
        for a class it doesn't have, it simply returns null. I've not been able to find any official documentation
        explaining why this is.
    */
    @IgnoreIf({ GradleContextualExecuter.embedded })
    def "daemon bootstrap classpath is bare bones"() {
        given:
        buildFile << """
            task loadClasses {
                doLast {
                    def systemLoader = ClassLoader.systemClassLoader

                    systemLoader.loadClass(org.gradle.launcher.GradleMain.name) // this should be on the classpath, it's from the launcher package

                    def nonLauncherOrCoreClass = "org.apache.commons.lang.WordUtils"

                    // Check that this is a dependency (somewhat redundant, but for good measure)
                    assert Project.classLoader.loadClass(nonLauncherOrCoreClass) != null

                    try {
                        def clazz = systemLoader.loadClass(nonLauncherOrCoreClass)
                        assert clazz == null : "ClassNotFoundException should have been thrown trying to load a “\${nonLauncherOrCoreClass}” class from the system classloader as its not a launcher or core class (loaded class: \$clazz)"
                    } catch (ClassNotFoundException e) {
                        //
                    }

                    if (systemLoader instanceof java.net.URLClassLoader) {
                        def systemLoaderUrls = systemLoader.URLs
                        println "$heading"
                        println systemLoaderUrls.size()
                        println systemLoaderUrls[0]
                    } else {
                        println "$noInfoHeading"
                    }
                }
            }
        """

        when:
        succeeds "loadClasses"

        then:
        def lines = output.readLines()
        if (lines.find { it == noInfoHeading }) { return }

        lines.find { it == heading } // here for nicer output if the output isn't what we expect
        def headingIndex = lines.indexOf(heading)
        lines[headingIndex + 1] == "1"
        lines[headingIndex + 2].contains("gradle-launcher")
    }
}
