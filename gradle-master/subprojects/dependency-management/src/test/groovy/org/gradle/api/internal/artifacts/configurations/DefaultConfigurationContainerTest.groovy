/*
 * Copyright 2010 the original author or authors.
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

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.internal.artifacts.ComponentSelectorConverter
import org.gradle.api.internal.artifacts.ConfigurationResolver
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory
import org.gradle.api.internal.artifacts.component.ComponentIdentifierFactory
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyLockingProvider
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder
import org.gradle.api.internal.artifacts.ivyservice.dependencysubstitution.DependencySubstitutionRules
import org.gradle.api.internal.artifacts.ivyservice.moduleconverter.LocalComponentMetadataBuilder
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.internal.file.TestFiles
import org.gradle.api.internal.initialization.RootScriptDomainObjectContext
import org.gradle.api.internal.project.ProjectStateRegistry
import org.gradle.api.internal.tasks.TaskResolver
import org.gradle.configuration.internal.UserCodeApplicationContext
import org.gradle.initialization.ProjectAccessListener
import org.gradle.internal.event.ListenerManager
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.AttributeTestUtil
import org.gradle.util.TestUtil
import org.gradle.vcs.internal.VcsMappingsStore
import spock.lang.Specification

class DefaultConfigurationContainerTest extends Specification {

    private ConfigurationResolver resolver = Mock(ConfigurationResolver)
    private ListenerManager listenerManager = Stub(ListenerManager.class)
    private DependencyMetaDataProvider metaDataProvider = Mock(DependencyMetaDataProvider.class)
    private ProjectAccessListener projectAccessListener = Mock(ProjectAccessListener.class)
    private ProjectFinder projectFinder = Mock(ProjectFinder)
    private LocalComponentMetadataBuilder metaDataBuilder = Mock(LocalComponentMetadataBuilder)
    private ComponentIdentifierFactory componentIdentifierFactory = Mock(ComponentIdentifierFactory)
    private DependencySubstitutionRules globalSubstitutionRules = Mock(DependencySubstitutionRules)
    private VcsMappingsStore vcsMappingsInternal = Mock(VcsMappingsStore)
    private BuildOperationExecutor buildOperationExecutor = Mock(BuildOperationExecutor)
    private TaskResolver taskResolver = Mock(TaskResolver)
    private DependencyLockingProvider lockingProvider = Mock(DependencyLockingProvider)
    private ProjectStateRegistry projectStateRegistry = Mock(ProjectStateRegistry)
    private DocumentationRegistry documentationRegistry = Mock(DocumentationRegistry)
    private CollectionCallbackActionDecorator callbackActionDecorator = Mock()
    private UserCodeApplicationContext userCodeApplicationContext = Mock()

    private Instantiator instantiator = TestUtil.instantiatorFactory().decorateLenient()
    private ImmutableAttributesFactory immutableAttributesFactory = AttributeTestUtil.attributesFactory()
    private ImmutableModuleIdentifierFactory moduleIdentifierFactory = Mock() {
        module(_, _) >> { args ->
            DefaultModuleIdentifier.newId(*args)
        }
    }
    private ComponentSelectorConverter componentSelectorConverter = Mock()
    private DefaultConfigurationContainer configurationContainer = instantiator.newInstance(DefaultConfigurationContainer.class,
            resolver, instantiator, new RootScriptDomainObjectContext(),
            listenerManager, metaDataProvider, projectAccessListener, projectFinder, metaDataBuilder, TestFiles.fileCollectionFactory(),
            globalSubstitutionRules, vcsMappingsInternal, componentIdentifierFactory, buildOperationExecutor, taskResolver,
            immutableAttributesFactory, moduleIdentifierFactory, componentSelectorConverter, lockingProvider, projectStateRegistry,
            documentationRegistry, callbackActionDecorator, userCodeApplicationContext, TestUtil.domainObjectCollectionFactory())

    def addsNewConfigurationWhenConfiguringSelf() {
        when:
        configurationContainer.configure {
            newConf
        }

        then:
        configurationContainer.findByName('newConf') != null
        configurationContainer.newConf != null
    }

    def doesNotAddNewConfigurationWhenNotConfiguringSelf() {
        when:
        configurationContainer.getByName('unknown')

        then:
        thrown(UnknownConfigurationException)
    }

    def makesExistingConfigurationAvailableAsProperty() {
        when:
        Configuration configuration = configurationContainer.create('newConf')

        then:
        configuration != null
        configurationContainer.getByName("newConf").is(configuration)
        configurationContainer.newConf.is(configuration)
    }

    def addsNewConfigurationWithClosureWhenConfiguringSelf() {
        when:
        String someDesc = 'desc1'
        configurationContainer.configure {
            newConf {
                description = someDesc
            }
        }

        then:
        configurationContainer.newConf.getDescription() == someDesc
    }

    def makesExistingConfigurationAvailableAsConfigureMethod() {
        when:
        String someDesc = 'desc1'
        configurationContainer.create('newConf')
        Configuration configuration = configurationContainer.newConf {
            description = someDesc
        }

        then:
        configuration.getDescription() == someDesc
    }

    def makesExistingConfigurationAvailableAsConfigureMethodWhenConfiguringSelf() {
        when:
        String someDesc = 'desc1'
        Configuration configuration = configurationContainer.create('newConf')
        configurationContainer.configure {
            newConf {
                description = someDesc
            }
        }

        then:
        configuration.getDescription() == someDesc
    }

    def newConfigurationWithNonClosureParametersShouldThrowMissingMethodEx() {
        when:
        configurationContainer.newConf('a', 'b')

        then:
        thrown MissingMethodException
    }
}
