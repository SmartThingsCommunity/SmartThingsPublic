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
package org.gradle.profile

import org.gradle.initialization.StartParameterBuildOptions
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ProfilingIntegrationTest extends AbstractIntegrationSpec {

    def "can generate profiling report"() {
        given:
        file('settings.gradle') << 'include "a", "b", "c"'
        buildFile << '''
allprojects {
    apply plugin: 'java'
    task fooTask
    task barTask
}
'''
        when:
        executer.withArgument("--profile")
        succeeds("build", "fooTask", "-x", "barTask")

        then:
        def reportFile = file('build/reports/profile').listFiles().find { it.name ==~ /profile-.+.html/ }
        Document document = Jsoup.parse(reportFile, null)
        !document.select("TD:contains(:jar)").isEmpty()
        !document.select("TD:contains(:a:jar)").isEmpty()
        !document.select("TD:contains(:b:jar)").isEmpty()
        !document.select("TD:contains(:c:jar)").isEmpty()
        document.text().contains("build fooTask")
        document.text().contains("-x barTask")
        output.contains("See the profiling report at:")
        output.contains("A fine-grained performance profile is available: use the --${StartParameterBuildOptions.BuildScanOption.LONG_OPTION} option.")
    }
}
