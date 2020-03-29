/*
 * Copyright 2016 the original author or authors.
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
import accessors.java
import org.gradle.gradlebuild.unittestandcompile.ModuleType

plugins {
    `java-library`
    gradlebuild.classycle
}

val reports by configurations.creating
val flamegraph by configurations.creating
configurations.compileOnly { extendsFrom(flamegraph) }

repositories {
    googleApisJs()
}

dependencies {
    reports("jquery:jquery.min:1.11.0@js")
    reports("flot:flot:0.8.1:min@js")

    api(library("gradleProfiler")) {
        because("Consumers need to instantiate BuildMutators")
    }

    implementation(project(":baseServices"))
    implementation(project(":native"))
    implementation(project(":cli"))
    implementation(project(":logging"))
    implementation(project(":processServices"))
    implementation(project(":coreApi"))
    implementation(project(":buildOption"))
    implementation(project(":fileCollections"))
    implementation(project(":snapshots"))
    implementation(project(":resources"))
    implementation(project(":persistentCache"))
    implementation(project(":jvmServices"))
    implementation(project(":wrapper"))
    implementation(project(":internalIntegTesting"))

    implementation(library("junit"))
    implementation(testLibrary("spock"))
    implementation(library("groovy"))
    implementation(library("slf4j_api"))
    implementation(library("joda"))
    implementation(library("jatl"))
    implementation(library("jgit"))
    implementation(library("commons_httpclient"))
    implementation(library("jsch"))
    implementation(library("commons_math"))
    implementation(library("jcl_to_slf4j"))
    implementation("org.gradle.org.openjdk.jmc:flightrecorder:7.0.0-alpha01")
    implementation("org.gradle.ci.health:tagging:0.63")
    implementation(testLibrary("mina"))
    implementation(testLibrary("jetty"))
    implementation(testFixtures(project(":core")))
    implementation(testFixtures(project(":toolingApi")))

    runtimeOnly("com.h2database:h2:1.4.192")
}

gradlebuildJava {
    moduleType = ModuleType.INTERNAL
}

val generatedResourcesDir = gradlebuildJava.generatedResourcesDir

val reportResources = tasks.register<Copy>("reportResources") {
    from(reports)
    into("$generatedResourcesDir/org/gradle/reporting")
}

java.sourceSets.main { output.dir(mapOf("builtBy" to reportResources), generatedResourcesDir) }

tasks.jar {
    inputs.files(flamegraph)
        .withPropertyName("flamegraph")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    from(files(deferred{ flamegraph.map { zipTree(it) } }))
}
