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

plugins {
    `java-library`
    `maven-publish`
    `instrumented-jars`
}

publishing {
    repositories {
        maven {
            setUrl("${buildDir}/repo")
        }
    }
    publications {
        create<MavenPublication>("myPublication") {
            from(components["myAdhocComponent"])
        }
    }
}

if (project.hasProperty("disableGradleMetadata")) {
    // tag::disable_gradle_metadata_publication[]
    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }
    // end::disable_gradle_metadata_publication[]
}

if (project.hasProperty("customRepository")) {
    // tag::gradle_metadata_source[]
    repositories {
        maven {
            setUrl("http://repo.mycompany.com/repo")
            metadataSources {
                gradleMetadata()
            }
        }
    }
    // end::gradle_metadata_source[]
}