/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.instantexecution.serialization.codecs

import org.gradle.api.Project
import org.gradle.api.Script
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyConstraintSet
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.LenientConfiguration
import org.gradle.api.artifacts.ResolutionStrategy
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.dsl.ComponentMetadataHandler
import org.gradle.api.artifacts.dsl.ComponentModuleMetadataHandler
import org.gradle.api.artifacts.dsl.DependencyConstraintHandler
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.DependencyLockingHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.query.ArtifactResolutionQuery
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.type.ArtifactTypeContainer
import org.gradle.api.attributes.AttributeMatchingStrategy
import org.gradle.api.attributes.AttributesSchema
import org.gradle.api.attributes.CompatibilityRuleChain
import org.gradle.api.attributes.DisambiguationRuleChain
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.instantexecution.serialization.unsupported
import org.gradle.kotlin.dsl.*
import java.io.FileDescriptor
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.ThreadFactory


internal
fun BindingsBuilder.unsupportedTypes() {

    // Live JVM state
    bind(unsupported<ClassLoader>())
    bind(unsupported<Thread>())
    bind(unsupported<ThreadFactory>())
    bind(unsupported<Executor>())
    bind(unsupported<InputStream>())
    bind(unsupported<OutputStream>())
    bind(unsupported<FileDescriptor>())
    bind(unsupported<RandomAccessFile>())
    bind(unsupported<Socket>())
    bind(unsupported<ServerSocket>())

    // Gradle Scripts
    bind(unsupported<Script>())
    bind(unsupported<KotlinScript>())

    // Gradle Build Model
    bind(unsupported<Gradle>())
    bind(unsupported<Settings>())
    bind(unsupported<Project>())
    bind(unsupported<TaskContainer>())
    bind(unsupported<SourceSetContainer>())
    bind(unsupported<SourceSet>())

    // Dependency Resolution Services
    bind(unsupported<ConfigurationContainer>())
    bind(unsupported<ResolutionStrategy>())
    bind(unsupported<ResolvedConfiguration>())
    bind(unsupported<LenientConfiguration>())
    bind(unsupported<DependencyConstraintSet>())
    bind(unsupported<RepositoryHandler>())
    bind(unsupported<ArtifactRepository>())
    bind(unsupported<DependencyHandler>())
    bind(unsupported<DependencyConstraintHandler>())
    bind(unsupported<ComponentMetadataHandler>())
    bind(unsupported<ComponentModuleMetadataHandler>())
    bind(unsupported<ArtifactTypeContainer>())
    bind(unsupported<AttributesSchema>())
    bind(unsupported<AttributeMatchingStrategy<*>>())
    bind(unsupported<CompatibilityRuleChain<*>>())
    bind(unsupported<DisambiguationRuleChain<*>>())
    bind(unsupported<ArtifactResolutionQuery>())
    bind(unsupported<DependencySet>())
    bind(unsupported<Dependency>())
    bind(unsupported<DependencyLockingHandler>())
}
