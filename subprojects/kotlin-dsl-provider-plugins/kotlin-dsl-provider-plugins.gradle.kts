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

import build.futureKotlin
import org.gradle.gradlebuild.unittestandcompile.ModuleType

plugins {
    `kotlin-dsl-module`
}

description = "Kotlin DSL Provider Plugins"

gradlebuildJava {
    moduleType = ModuleType.CORE
}


dependencies {
    implementation(project(":kotlinDsl"))

    implementation(project(":baseServices"))
    implementation(project(":logging"))
    implementation(project(":coreApi"))
    implementation(project(":modelCore"))
    implementation(project(":core"))
    implementation(project(":fileCollections"))
    implementation(project(":resources"))
    implementation(project(":plugins"))
    implementation(project(":pluginDevelopment"))
    implementation(project(":toolingApi"))

    implementation(futureKotlin("scripting-compiler-impl-embeddable")) {
        isTransitive = false
    }

    implementation(library("slf4j_api"))
    implementation(library("inject"))

    testImplementation(project(":kotlinDslTestFixtures"))
    testImplementation(testLibrary("mockito_kotlin2"))
}
