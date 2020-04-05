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

package Gradle_Check.configurations

import common.applyDefaultSettings
import configurations.BaseGradleBuildType
import configurations.publishBuildStatusToGithub
import configurations.snapshotDependencies
import jetbrains.buildServer.configs.kotlin.v2019_2.AbsoluteId
import model.CIBuildModel
import projects.FunctionalTestProject

class FunctionalTestsPass(model: CIBuildModel, functionalTestProject: FunctionalTestProject) : BaseGradleBuildType(model, init = {
    uuid = functionalTestProject.uuid + "_Trigger"
    id = AbsoluteId(uuid)
    name = functionalTestProject.name + " (Trigger)"

    applyDefaultSettings()

    features {
        publishBuildStatusToGithub(model)
    }

    dependencies {
        snapshotDependencies(functionalTestProject.functionalTests)
    }
})
