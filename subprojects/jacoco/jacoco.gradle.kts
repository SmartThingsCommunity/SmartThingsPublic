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
import org.gradle.gradlebuild.unittestandcompile.ModuleType

plugins {
    `java-library`
}

dependencies {
    implementation(project(":baseServices"))
    implementation(project(":logging"))
    implementation(project(":processServices"))
    implementation(project(":coreApi"))
    implementation(project(":modelCore"))
    implementation(project(":core"))
    implementation(project(":platformBase"))
    implementation(project(":testingBase"))
    implementation(project(":testingJvm"))
    implementation(project(":plugins"))
    implementation(project(":reporting"))

    implementation(library("groovy"))
    implementation(library("slf4j_api"))
    implementation(library("guava"))
    implementation(library("commons_lang"))
    implementation(library("inject"))

    testFixturesImplementation(project(":baseServices"))
    testFixturesImplementation(project(":coreApi"))
    testFixturesImplementation(project(":core"))
    testFixturesImplementation(project(":internalIntegTesting"))
    testFixturesImplementation(testLibrary("jsoup"))

    testImplementation(project(":fileCollections"))
    testImplementation(project(":internalTesting"))
    testImplementation(project(":resources"))
    testImplementation(project(":internalIntegTesting"))
    testImplementation(testFixtures(project(":core")))

    testRuntimeOnly(project(":runtimeApiInfo"))
    integTestRuntimeOnly(project(":testingJunitPlatform"))
}

gradlebuildJava {
    moduleType = ModuleType.CORE
}

