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

// tag::apply-swift-plugin[]
plugins {
    `swift-application` // or `swift-library`
}

version = "1.2.1"
// end::apply-swift-plugin[]

// tag::swift-dependency-mgmt[]
application {
    dependencies {
        implementation(project(":common"))
    }
}
// end::swift-dependency-mgmt[]

// tag::swift-compiler-options-all-variants[]
tasks.withType(SwiftCompile::class.java).configureEach {
    // Define a preprocessor macro for every binary
    macros.add("NDEBUG")

    // Define a compiler options
    compilerArgs.add("-O")
}
// end::swift-compiler-options-all-variants[]

// tag::swift-compiler-options-per-variants[]
application {
    binaries.configureEach(SwiftStaticLibrary::class.java) {
        // Define a preprocessor macro for every binary
        compileTask.get().macros.add("NDEBUG")

        // Define a compiler options
        compileTask.get().compilerArgs.add("-O")
    }
}
// end::swift-compiler-options-per-variants[]

// tag::swift-select-target-machines[]
application {
    targetMachines.set(listOf(machines.linux.x86_64, machines.macOS.x86_64))
}
// end::swift-select-target-machines[]

project(":common") {
    apply(plugin = "swift-library")
    
    // tag::swift-source-set[]
    extensions.configure<SwiftLibrary> {
        source.from(file("Sources/Common"))
    }
    // end::swift-source-set[]
}
