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

package com.acme

import org.gradle.api.JavaVersion
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

// tag::inject_software_component_factory[]
class InstrumentedJarsPlugin @Inject constructor(
    private val softwareComponentFactory: SoftwareComponentFactory) : Plugin<Project> {
// end::inject_software_component_factory[]

    override fun apply(project: Project) = project.run {
        val outgoingConfiguration = createOutgoingConfiguration()
        attachArtifact()
        configurePublication(outgoingConfiguration)
        addVariantToExistingComponent(outgoingConfiguration)
    }

    private fun Project.configurePublication(outgoing: Configuration) {
        // tag::create_adhoc_component[]
        // create an adhoc component
        val adhocComponent = softwareComponentFactory.adhoc("myAdhocComponent")
        // add it to the list of components that this project declares
        components.add(adhocComponent)
        // and register a variant for publication
        adhocComponent.addVariantsFromConfiguration(outgoing) {
            mapToMavenScope("runtime")
        }
        // end::create_adhoc_component[]
    }

    private fun Project.attachArtifact() {
        val instrumentedJar = tasks.register<Jar>("instrumentedJar") {
            archiveClassifier.set("instrumented")
        }

        artifacts {
            add("instrumentedJars", instrumentedJar)
        }

    }

    private fun Project.createOutgoingConfiguration(): Configuration {
        val instrumentedJars by configurations.creating {
            isCanBeConsumed = true
            isCanBeResolved = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, namedAttribute(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, namedAttribute(Usage.JAVA_RUNTIME))
                attribute(Bundling.BUNDLING_ATTRIBUTE, namedAttribute(Bundling.EXTERNAL))
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, JavaVersion.current().majorVersion.toInt())
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, namedAttribute("instrumented-jar"))
            }
        }
        return instrumentedJars
    }

    private fun Project.addVariantToExistingComponent(outgoing: Configuration) {
        // tag::add_variant_to_existing_component[]
        val javaComponent = components.findByName("java") as AdhocComponentWithVariants
        javaComponent.addVariantsFromConfiguration(outgoing) {
            // dependencies for this variant are considered runtime dependencies
            mapToMavenScope("runtime")
            // and also optional dependencies, because we don't want them to leak
            mapToOptional()
        }
        // end::add_variant_to_existing_component[]
    }

}

inline fun <reified T : Named> Project.namedAttribute(value: String) = objects.named(T::class.java, value)
