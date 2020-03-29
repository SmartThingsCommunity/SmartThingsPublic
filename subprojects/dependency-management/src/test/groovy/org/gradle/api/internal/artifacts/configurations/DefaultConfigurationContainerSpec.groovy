/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.artifacts.configurations

import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.DomainObjectContext
import org.gradle.api.internal.artifacts.ComponentSelectorConverter
import org.gradle.api.internal.artifacts.ConfigurationResolver
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory
import org.gradle.api.internal.artifacts.component.ComponentIdentifierFactory
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyLockingProvider
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder
import org.gradle.api.internal.artifacts.ivyservice.dependencysubstitution.DependencySubstitutionRules
import org.gradle.api.internal.artifacts.ivyservice.moduleconverter.LocalComponentMetadataBuilder
import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.internal.project.ProjectStateRegistry
import org.gradle.api.internal.tasks.TaskResolver
import org.gradle.configuration.internal.UserCodeApplicationContext
import org.gradle.initialization.ProjectAccessListener
import org.gradle.internal.event.ListenerManager
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.AttributeTestUtil
import org.gradle.util.Path
import org.gradle.util.TestUtil
import org.gradle.vcs.internal.VcsMappingsStore
import spock.lang.Specification

class DefaultConfigurationContainerSpec extends Specification {

    private ConfigurationResolver resolver = Mock()
    private Instantiator instantiator = TestUtil.instantiatorFactory().decorateLenient()
    private DomainObjectContext domainObjectContext = Mock()
    private ListenerManager listenerManager = Mock()
    private DependencyMetaDataProvider metaDataProvider = Mock()
    private ProjectAccessListener projectAccessListener = Mock()
    private ProjectFinder projectFinder = Mock()
    private LocalComponentMetadataBuilder metaDataBuilder = Mock()
    private FileCollectionFactory fileCollectionFactory = Mock()
    private ComponentIdentifierFactory componentIdentifierFactory = Mock()
    private DependencySubstitutionRules globalSubstitutionRules = Mock()
    private VcsMappingsStore vcsMappingsInternal = Mock()
    private BuildOperationExecutor buildOperationExecutor = Mock()
    private TaskResolver taskResolver = Mock()
    private ImmutableModuleIdentifierFactory moduleIdentifierFactory = Mock() {
        module(_, _) >> { args ->
            DefaultModuleIdentifier.newId(*args)
        }
    }
    private ComponentSelectorConverter componentSelectorConverter = Mock()
    private DependencyLockingProvider dependencyLockingProvider = Mock()
    private ProjectStateRegistry projectStateRegistry = Mock()
    private DocumentationRegistry documentationRegistry = Mock()
    private UserCodeApplicationContext userCodeApplicationContext = Mock()

    private CollectionCallbackActionDecorator domainObjectCollectionCallbackActionDecorator = Mock()
    def immutableAttributesFactory = AttributeTestUtil.attributesFactory()

    private DefaultConfigurationContainer configurationContainer = new DefaultConfigurationContainer(resolver, instantiator, domainObjectContext, listenerManager, metaDataProvider,
            projectAccessListener, projectFinder, metaDataBuilder, fileCollectionFactory, globalSubstitutionRules, vcsMappingsInternal, componentIdentifierFactory, buildOperationExecutor, taskResolver,
            immutableAttributesFactory, moduleIdentifierFactory, componentSelectorConverter, dependencyLockingProvider, projectStateRegistry, documentationRegistry,
            domainObjectCollectionCallbackActionDecorator, userCodeApplicationContext, TestUtil.domainObjectCollectionFactory())

    def "adds and gets"() {
        1 * domainObjectContext.identityPath("compile") >> Path.path(":build:compile")
        1 * domainObjectContext.projectPath("compile") >> Path.path(":compile")

        when:
        def compile = configurationContainer.create("compile")

        then:
        compile.name == "compile"
        compile.path == ":compile"
        compile instanceof DefaultConfiguration

        and:
        configurationContainer.getByName("compile") == compile

        //finds configurations
        configurationContainer.findByName("compile") == compile
        configurationContainer.findByName("foo") == null
        configurationContainer.findAll { it.name == "compile" } as Set == [compile] as Set
        configurationContainer.findAll { it.name == "foo" } as Set == [] as Set

        configurationContainer as List == [compile] as List

        when:
        configurationContainer.getByName("fooo")

        then:
        thrown(UnknownConfigurationException)
    }

    def "configures and finds"() {
        1 * domainObjectContext.identityPath("compile") >> Path.path(":build:compile")
        1 * domainObjectContext.projectPath("compile") >> Path.path(":compile")

        when:
        def compile = configurationContainer.create("compile") {
            description = "I compile!"
        }

        then:
        configurationContainer.getByName("compile") == compile
        compile.description == "I compile!"
        compile.path == ":compile"
    }

    def "creates detached"() {
        given:
        1 * domainObjectContext.projectPath("detachedConfiguration1") >> Path.path(":detachedConfiguration1")
        def dependency1 = new DefaultExternalModuleDependency("group", "name", "version")
        def dependency2 = new DefaultExternalModuleDependency("group", "name2", "version")

        when:
        def detached = configurationContainer.detachedConfiguration(dependency1, dependency2);

        then:
        detached.name == "detachedConfiguration1"
        detached.getAll() == [detached] as Set
        detached.getHierarchy() == [detached] as Set
        [dependency1, dependency2].each { detached.getDependencies().contains(it) }
        detached.getDependencies().size() == 2
        detached.path == ":detachedConfiguration1"
    }
}
