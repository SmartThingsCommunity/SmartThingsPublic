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

package Gradle_Check.model

import com.alibaba.fastjson.JSON
import model.GradleSubproject
import model.Stage
import model.TestCoverage
import java.io.File

val slowSubprojects = listOf("platformPlay")

val ignoredSubprojects = listOf(
    "soak", // soak test
    "distributions", // build distributions
    "docs", // sanity check
    "architectureTest" // sanity check
)

interface GradleSubprojectProvider {
    val subprojects: List<GradleSubproject>
    fun getSubprojectsFor(testConfig: TestCoverage, stage: Stage): List<GradleSubproject>
    fun getSubprojectByName(name: String): GradleSubproject?
    fun getSlowSubprojects(): List<GradleSubproject>
}

data class JsonBasedGradleSubprojectProvider(private val jsonFile: File) : GradleSubprojectProvider {
    override val subprojects = JSON.parseArray(jsonFile.readText()).map { toSubproject(it as Map<String, Object>) }
    private val nameToSubproject = subprojects.map { it.name to it }.toMap()

    override fun getSubprojectsFor(testConfig: TestCoverage, stage: Stage) =
        subprojects.filterNot { it.containsSlowTests && stage.omitsSlowProjects }
            .filter { it.hasTestsOf(testConfig.testType) }
            .filterNot { testConfig.os.ignoredSubprojects.contains(it.name) }

    override fun getSubprojectByName(name: String) = nameToSubproject[name]
    override fun getSlowSubprojects() = subprojects.filter { it.containsSlowTests }

    private
    fun toSubproject(subproject: Map<String, Object>): GradleSubproject {
        val name = subproject["name"] as String
        val unitTests = !ignoredSubprojects.contains(name) && subproject["unitTests"] as Boolean
        val functionalTests = !ignoredSubprojects.contains(name) && subproject["functionalTests"] as Boolean
        val crossVersionTests = !ignoredSubprojects.contains(name) && subproject["crossVersionTests"] as Boolean
        return GradleSubproject(name, unitTests, functionalTests, crossVersionTests, name in slowSubprojects)
    }
}
