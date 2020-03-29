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

package org.gradle.kotlin.dsl.support.delegates

import groovy.lang.Closure

import org.gradle.api.Action
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.ComponentMetadataHandler
import org.gradle.api.artifacts.dsl.ComponentModuleMetadataHandler
import org.gradle.api.artifacts.dsl.DependencyConstraintHandler
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.query.ArtifactResolutionQuery
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.artifacts.transform.TransformSpec
import org.gradle.api.artifacts.transform.VariantTransform
import org.gradle.api.artifacts.type.ArtifactTypeContainer
import org.gradle.api.attributes.AttributesSchema
import org.gradle.api.plugins.ExtensionContainer


/**
 * Facilitates the implementation of the [DependencyHandler] interface by delegation via subclassing.
 *
 * See [GradleDelegate] for why this is currently necessary.
 */
abstract class DependencyHandlerDelegate : DependencyHandler {

    internal
    abstract val delegate: DependencyHandler

    override fun getExtensions(): ExtensionContainer =
            delegate.extensions

    override fun add(configurationName: String, dependencyNotation: Any): Dependency? =
        delegate.add(configurationName, dependencyNotation)

    override fun add(configurationName: String, dependencyNotation: Any, configureClosure: Closure<Any>): Dependency =
        delegate.add(configurationName, dependencyNotation, configureClosure)

    override fun create(dependencyNotation: Any): Dependency =
        delegate.create(dependencyNotation)

    override fun create(dependencyNotation: Any, configureClosure: Closure<Any>): Dependency =
        delegate.create(dependencyNotation, configureClosure)

    override fun module(notation: Any): Dependency =
        delegate.module(notation)

    override fun module(notation: Any, configureClosure: Closure<Any>): Dependency =
        delegate.module(notation, configureClosure)

    override fun project(notation: Map<String, *>): Dependency =
        delegate.project(notation)

    override fun gradleApi(): Dependency =
        delegate.gradleApi()

    override fun gradleTestKit(): Dependency =
        delegate.gradleTestKit()

    override fun localGroovy(): Dependency =
        delegate.localGroovy()

    override fun getConstraints(): DependencyConstraintHandler =
        delegate.constraints

    override fun constraints(configureAction: Action<in DependencyConstraintHandler>) =
        delegate.constraints(configureAction)

    override fun getComponents(): ComponentMetadataHandler =
        delegate.components

    override fun components(configureAction: Action<in ComponentMetadataHandler>) =
        delegate.components(configureAction)

    override fun getModules(): ComponentModuleMetadataHandler =
        delegate.modules

    override fun modules(configureAction: Action<in ComponentModuleMetadataHandler>) =
        delegate.modules(configureAction)

    override fun createArtifactResolutionQuery(): ArtifactResolutionQuery =
        delegate.createArtifactResolutionQuery()

    override fun attributesSchema(configureAction: Action<in AttributesSchema>): AttributesSchema =
        delegate.attributesSchema(configureAction)

    override fun getAttributesSchema(): AttributesSchema =
        delegate.attributesSchema

    override fun getArtifactTypes(): ArtifactTypeContainer =
        delegate.artifactTypes

    override fun artifactTypes(configureAction: Action<in ArtifactTypeContainer>) =
        delegate.artifactTypes(configureAction)

    override fun registerTransform(registrationAction: Action<in VariantTransform>) =
        delegate.registerTransform(registrationAction)

    override fun <T : TransformParameters?> registerTransform(actionType: Class<out TransformAction<T>>, registrationAction: Action<in TransformSpec<T>>) =
        delegate.registerTransform(actionType, registrationAction)

    override fun platform(notation: Any): Dependency =
        delegate.platform(notation)

    override fun platform(notation: Any, configureAction: Action<in Dependency>): Dependency =
        delegate.platform(notation, configureAction)

    override fun enforcedPlatform(notation: Any): Dependency =
        delegate.enforcedPlatform(notation)

    override fun enforcedPlatform(notation: Any, configureAction: Action<in Dependency>): Dependency =
        delegate.enforcedPlatform(notation, configureAction)

    override fun testFixtures(notation: Any): Dependency =
        delegate.testFixtures(notation)

    override fun testFixtures(notation: Any, configureAction: Action<in Dependency>): Dependency =
        delegate.testFixtures(notation, configureAction)
}
