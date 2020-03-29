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
import org.gradle.gradlebuild.unittestandcompile.ModuleType

plugins {
    `java-library`
    gradlebuild.`publish-public-libraries`
    gradlebuild.classycle
}

description = "Package build cache results"

dependencies {
    api(project(":buildCacheBase"))
    api(project(":snapshots"))
    api(project(":hashing"))
    api(project(":files"))

    implementation(project(":baseAnnotations"))

    implementation(library("guava")) { version { require(libraryVersion("guava")) } }
    implementation(library("commons_compress")) { version { require(libraryVersion("commons_compress")) } }
    implementation(library("commons_io")) { version { require(libraryVersion("commons_io")) } }

    testImplementation(project(":processServices"))
    testImplementation(project(":fileCollections"))
    testImplementation(project(":resources"))

    testImplementation(testFixtures(project(":baseServices")))
    testImplementation(testFixtures(project(":core")))
    testImplementation(testFixtures(project(":snapshots")))
    testImplementation(testFixtures(project(":coreApi")))
}

gradlebuildJava {
    moduleType = ModuleType.CORE
}
