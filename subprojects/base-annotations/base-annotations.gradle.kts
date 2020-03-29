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
import org.gradle.gradlebuild.unittestandcompile.ModuleType

plugins {
    `java-library`
    gradlebuild.classycle
    gradlebuild.`publish-public-libraries`
    gradlebuild.`strict-compile`
}

description = "Common shared annotations"

dependencies {
    api(library("jsr305")) { version { require(libraryVersion("jsr305")) } }
}

gradlebuildJava {
    // We need this because org.gradle.internal.nativeintegration.filesystem.Stat is used in workers
    moduleType = ModuleType.WORKER
}
