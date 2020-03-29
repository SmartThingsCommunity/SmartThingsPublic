import org.gradle.gradlebuild.unittestandcompile.ModuleType

/*
 * Copyright 2014 the original author or authors.
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
plugins {
    `java-library`
    gradlebuild.classycle
}

dependencies {
    implementation(project(":baseServices"))
    implementation(project(":logging"))
    implementation(project(":coreApi"))
    implementation(project(":modelCore"))
    implementation(project(":core"))
    implementation(project(":fileCollections"))
    implementation(project(":dependencyManagement"))
    implementation(project(":ide"))
    implementation(project(":platformBase"))
    implementation(project(":platformNative"))
    implementation(project(":languageNative"))
    implementation(project(":testingBase"))
    implementation(project(":testingNative"))

    implementation(library("groovy"))
    implementation(library("slf4j_api"))
    implementation(library("guava"))
    implementation(library("commons_lang"))
    implementation(library("inject"))
    implementation(library("plist"))

    testImplementation(testFixtures(project(":core")))
    testImplementation(testFixtures(project(":platformNative")))
    testImplementation(testFixtures(project(":languageNative")))
    testImplementation(testFixtures(project(":versionControl")))

    testRuntimeOnly(project(":runtimeApiInfo"))

    integTestImplementation(project(":native"))
    integTestImplementation(library("commons_io"))
    integTestImplementation(library("jgit"))

    testFixturesApi(testFixtures(project(":ide")))
    testFixturesImplementation(library("plist"))
    testFixturesImplementation(library("guava"))
    testFixturesImplementation(testFixtures(project(":ide")))
}

gradlebuildJava {
    moduleType = ModuleType.CORE
}

