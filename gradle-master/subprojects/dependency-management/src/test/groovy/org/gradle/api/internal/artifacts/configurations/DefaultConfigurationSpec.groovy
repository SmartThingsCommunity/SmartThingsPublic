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

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ConfigurablePublishArtifact
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.DomainObjectContext
import org.gradle.api.internal.artifacts.ConfigurationResolver
import org.gradle.api.internal.artifacts.DefaultExcludeRule
import org.gradle.api.internal.artifacts.DefaultResolverResults
import org.gradle.api.internal.artifacts.ResolverResults
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder
import org.gradle.api.internal.artifacts.ivyservice.DefaultLenientConfiguration
import org.gradle.api.internal.artifacts.ivyservice.moduleconverter.RootComponentMetadataBuilder
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ArtifactVisitor
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvableArtifact
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.SelectedArtifactSet
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.VisitedArtifactSet
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.projectresult.ResolvedLocalComponentsResult
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact
import org.gradle.api.internal.file.TestFiles
import org.gradle.api.internal.project.ProjectStateRegistry
import org.gradle.api.internal.tasks.TaskDependencyResolveContext
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskDependency
import org.gradle.configuration.internal.UserCodeApplicationContext
import org.gradle.initialization.ProjectAccessListener
import org.gradle.internal.Factories
import org.gradle.internal.event.AnonymousListenerBroadcast
import org.gradle.internal.event.ListenerManager
import org.gradle.internal.model.ModelContainer
import org.gradle.internal.operations.TestBuildOperationExecutor
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.typeconversion.NotationParser
import org.gradle.internal.typeconversion.NotationParserBuilder
import org.gradle.util.AttributeTestUtil
import org.gradle.util.Path
import org.gradle.util.TestUtil
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.api.artifacts.Configuration.State.RESOLVED
import static org.gradle.api.artifacts.Configuration.State.RESOLVED_WITH_FAILURES
import static org.gradle.api.artifacts.Configuration.State.UNRESOLVED
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

class DefaultConfigurationSpec extends Specification {
    Instantiator instantiator = TestUtil.instantiatorFactory().decorateLenient()

    def configurationsProvider = Mock(ConfigurationsProvider)
    def resolver = Mock(ConfigurationResolver)
    def listenerManager = Mock(ListenerManager)
    def metaDataProvider = Mock(DependencyMetaDataProvider)
    def resolutionStrategy = Mock(ResolutionStrategyInternal)
    def projectAccessListener = Mock(ProjectAccessListener)
    def projectFinder = Mock(ProjectFinder)
    def immutableAttributesFactory = AttributeTestUtil.attributesFactory()
    def rootComponentMetadataBuilder = Mock(RootComponentMetadataBuilder)
    def projectStateRegistry = Mock(ProjectStateRegistry)
    def safeLock = Mock(ProjectStateRegistry.SafeExclusiveLock)
    def domainObjectCollectioncallbackActionDecorator = Mock(CollectionCallbackActionDecorator)
    def userCodeApplicationContext = Mock(UserCodeApplicationContext)

    def setup() {
        _ * listenerManager.createAnonymousBroadcaster(DependencyResolutionListener) >> { new AnonymousListenerBroadcast<DependencyResolutionListener>(DependencyResolutionListener) }
        _ * resolver.getRepositories() >> []
        _ * projectStateRegistry.newExclusiveOperationLock() >> safeLock
        _ * safeLock.withLock(_) >> { args -> args[0].run() }
        _ * domainObjectCollectioncallbackActionDecorator.decorate(_) >> { args -> args[0] }
        _ * userCodeApplicationContext.decorateWithCurrent(_) >> { args -> args[0] }
    }

    void defaultValues() {
        when:
        def configuration = conf("name", ":project")

        then:
        configuration.name == "name"
        configuration.visible
        configuration.extendsFrom.empty
        configuration.transitive
        configuration.description == null
        configuration.state == UNRESOLVED
        configuration.displayName == "configuration ':project:name'"
        configuration.uploadTaskName == "uploadName"
        configuration.attributes.isEmpty()
        configuration.canBeResolved
        configuration.canBeConsumed
    }

    def hasUsefulDisplayName() {
        when:
        def configuration = conf("name", ":project", ":build")

        then:
        configuration.displayName == "configuration ':build:project:name'"
        configuration.toString() == "configuration ':build:project:name'"
        configuration.incoming.toString() == "dependencies ':build:project:name'"
    }

    def "set description, visibility and transitivity"() {
        given:
        def configuration = conf()

        when:
        configuration.setDescription("description")
        configuration.setVisible(false)
        configuration.setTransitive(false)

        then:
        configuration.description == "description"
        !configuration.visible
        !configuration.transitive
    }

    def excludes() {
        def excludeArgs1 = [group: "aGroup"]
        def excludeArgs2 = [module: "aModule"]
        def configuration = conf()
        def rule = new DefaultExcludeRule("groupValue", null)

        when:
        configuration.exclude(excludeArgs1)
        configuration.exclude(excludeArgs2);

        then:
        configuration.excludeRules == [new DefaultExcludeRule("aGroup", null), new DefaultExcludeRule(null, "aModule")] as Set

        when:
        configuration.setExcludeRules([rule] as Set)

        then:
        configuration.excludeRules == [rule] as Set
    }

    def "can extend multiple configurations"() {
        def configuration = conf()
        def configuration1 = conf("otherConf1")
        def configuration2 = conf("otherConf2")

        when:
        configuration.extendsFrom(configuration1)

        then:
        configuration.extendsFrom == [configuration1] as Set

        when:
        configuration.extendsFrom(configuration2);

        then:
        configuration.extendsFrom == [configuration1, configuration2] as Set

        when:
        def configuration3 = conf("replacedConf")
        configuration.setExtendsFrom([configuration3])

        then:
        configuration.extendsFrom == [configuration3] as Set
    }

    def "extended configurations are not duplicated"() {
        def configuration = conf()
        def configuration1 = conf("other")

        when:
        configuration.extendsFrom(configuration1, configuration1)

        then:
        configuration.extendsFrom == [configuration1] as Set
    }

    def "reports direct cycle in configurations"() {
        def configuration = conf()
        def otherConf = conf("other")
        configuration.extendsFrom(otherConf)

        when:
        otherConf.extendsFrom(configuration)

        then:
        thrown InvalidUserDataException
    }

    def "reports indirect cycle in extended configurations"() {
        def configuration = conf()
        def conf1 = conf("other")
        def conf2 = conf("other2")

        when:
        configuration.extendsFrom(conf1)
        conf1.extendsFrom(conf2)
        conf2.extendsFrom(configuration)

        then:
        thrown InvalidUserDataException
    }

    def "reports cycle introduced by setExtends"() {
        def configuration = conf()
        def otherConf = conf("other")
        configuration.extendsFrom(otherConf)

        when:
        otherConf.setExtendsFrom([configuration])

        then:
        thrown InvalidUserDataException
    }

    def "creates hierarchy"() {
        def root1 = conf("root1")
        def middle1 = conf("middle1").extendsFrom(root1)
        def root2 = conf("root2")
        def middle2 = conf("middle2").extendsFrom(root2)
        def leaf = conf("leaf1").extendsFrom(middle1, middle2)

        when:
        def hierarchy = leaf.hierarchy

        then:
        hierarchy.size() == 5
        hierarchy.iterator().next() == leaf
        assertBothExistsAndOneIsBeforeOther(hierarchy, middle1, root1);
        assertBothExistsAndOneIsBeforeOther(hierarchy, middle2, root2);
    }

    private static void assertBothExistsAndOneIsBeforeOther(Set<Configuration> hierarchy, Configuration beforeConf, Configuration afterConf) {
        assert hierarchy.contains(beforeConf)
        assert hierarchy.contains(afterConf)

        boolean foundBeforeConf = false;
        for (Configuration configuration : hierarchy) {
            if (configuration.equals(beforeConf)) {
                foundBeforeConf = true;
            }
            if (configuration.equals(afterConf)) {
                assertThat(foundBeforeConf, equalTo(true));
            }
        }
    }

    def "get dependencies"() {
        def configuration = conf()
        def dependency = Mock(Dependency)
        def projectDependency = Mock(ProjectDependency.class);

        when:
        configuration.dependencies.add(dependency)
        configuration.dependencies.add(projectDependency)

        then:
        configuration.dependencies as Set == [dependency, projectDependency] as Set
        configuration.dependencies.withType(ProjectDependency) as Set == [projectDependency] as Set
        configuration.dependencies.withType(SelfResolvingDependency) as Set == [projectDependency] as Set
    }

    def "get all dependencies"() {
        def parentConf = conf("parent")
        def configuration = conf().extendsFrom(parentConf)
        def dependency = Mock(Dependency)
        def projectDependency = Mock(ProjectDependency.class);

        when:
        parentConf.dependencies.add(dependency)
        configuration.dependencies.add(projectDependency)

        then:
        configuration.dependencies as Set == [projectDependency] as Set
        configuration.allDependencies as Set == [dependency, projectDependency] as Set
    }

    def "resolves files"() {
        def configuration = conf()
        def fileSet = [new File("somePath")] as Set

        given:
        expectResolved(fileSet);

        when:
        def resolved = configuration.resolve()

        then:
        resolved == fileSet
        configuration.state == RESOLVED
    }

    def "get as path throws failure resolving"() {
        def configuration = conf()
        def failure = new RuntimeException()

        given:
        expectResolved(failure)

        when:
        configuration.getResolvedConfiguration()

        then:
        configuration.getState() == RESOLVED_WITH_FAILURES

        when:
        configuration.resolve()

        then:
        def t = thrown(DefaultLenientConfiguration.ArtifactResolveException)
        t.cause == failure
    }

    def "build dependencies are resolved lazily"() {
        given:
        def configuration = conf()

        when:
        configuration.getBuildDependencies()

        then:
        0 * _._
    }

    def "state indicates failure resolving graph"() {
        given:
        def configuration = conf()
        def failure = new ResolveException("bad", new RuntimeException())

        and:
        _ * resolver.resolveGraph(_, _) >> { ConfigurationInternal config, DefaultResolverResults resolverResults ->
            resolverResults.failed(failure)
        }
        _ * resolutionStrategy.resolveGraphToDetermineTaskDependencies() >> true

        when:
        configuration.getBuildDependencies().getDependencies(null)

        then:
        def t = thrown(GradleException)
        t.cause == failure
        configuration.getState() == RESOLVED_WITH_FAILURES
    }

    def fileCollectionWithDependencies() {
        def dependency1 = dependency("group1", "name", "version");
        def dependency2 = dependency("group2", "name", "version");
        def configuration = conf()

        when:
        def fileCollection = configuration.fileCollection(dependency1)

        then:
        fileCollection.getDependencySpec().isSatisfiedBy(dependency1)
        !fileCollection.getDependencySpec().isSatisfiedBy(dependency2)
    }

    def fileCollectionWithSpec() {
        def configuration = conf()
        Spec<Dependency> spec = Mock(Spec)

        when:
        def fileCollection = configuration.fileCollection(spec)

        then:
        fileCollection.getDependencySpec() == spec
    }

    def fileCollectionWithClosureSpec() {
        def closure = { dep -> dep.group == 'group1' }
        def configuration = conf()

        when:
        def fileCollection = configuration.fileCollection(closure)

        then:
        fileCollection.getDependencySpec().isSatisfiedBy(dependency("group1", "name", "version"))
        !fileCollection.getDependencySpec().isSatisfiedBy(dependency("group2", "name", "version"))
    }

    def filesWithDependencies() {
        def configuration = conf()
        def fileSet = [new File("somePath")] as Set

        when:
        prepareForFilesBySpec(fileSet)

        then:
        configuration.files(Mock(Dependency)) == fileSet
        configuration.state == RESOLVED
    }

    def filesWithSpec() {
        def configuration = conf()
        def fileSet = [new File("somePath")] as Set

        when:
        prepareForFilesBySpec(fileSet)

        then:
        configuration.files(Mock(Spec)) == fileSet
        configuration.state == RESOLVED
    }

    def filesWithClosureSpec() {
        def configuration = conf()
        def closure = { dep -> dep.group == 'group1' }
        def fileSet = [new File("somePath")] as Set

        when:
        prepareForFilesBySpec(fileSet);

        then:
        configuration.files(closure) == fileSet
        configuration.state == RESOLVED
    }

    def "multiple resolves use cached result"() {
        def configuration = conf()

        given:
        expectResolved([] as Set)

        when:
        def r = configuration.getResolvedConfiguration()

        then:
        configuration.getResolvedConfiguration() == r
        configuration.state == RESOLVED
    }

    private prepareForFilesBySpec(Set<File> fileSet) {
        expectResolved(fileSet)
    }

    private void expectResolved(Set<File> files) {
        def resolutionResults = stubResolutionResults()
        def localComponentsResult = Stub(ResolvedLocalComponentsResult)
        def visitedArtifactSet = Stub(VisitedArtifactSet)

        _ * visitedArtifactSet.select(_, _, _, _) >> Stub(SelectedArtifactSet) {
            visitArtifacts(_, _) >> { ArtifactVisitor visitor, boolean l ->
                files.each {
                    def artifact = Stub(ResolvableArtifact)
                    _ * artifact.file >> it
                    visitor.visitArtifact(null, null, artifact)
                }
            }
        }

        _ * localComponentsResult.resolvedProjectConfigurations >> Collections.emptySet()
        _ * resolver.resolveGraph(_, _) >> { ConfigurationInternal config, DefaultResolverResults resolverResults ->
            resolverResults.graphResolved(resolutionResults, localComponentsResult, visitedArtifactSet)
            resolverResults.artifactsResolved(Stub(ResolvedConfiguration), visitedArtifactSet)
        }
        _ * resolver.getRepositories() >> []
    }

    private ResolutionResult stubResolutionResults() {
        def resolutionResults
        resolutionResults = Stub(ResolutionResult) {
            hashCode() >> 123
            equals(_) >> {
                it[0].is(resolutionResults)
            }
            getRoot() >> Stub(ResolvedComponentResult)
        }
        resolutionResults
    }

    private void expectResolved(Throwable failure) {
        def resolutionResults = Stub(ResolutionResult)
        def localComponentsResult = Stub(ResolvedLocalComponentsResult)
        def visitedArtifactSet = Stub(VisitedArtifactSet)
        def resolvedConfiguration = Stub(ResolvedConfiguration)

        _ * visitedArtifactSet.select(_, _, _, _) >> Stub(SelectedArtifactSet) {
            visitArtifacts(_, _) >> { ArtifactVisitor v, boolean l -> v.visitFailure(failure) }
        }
        _ * resolvedConfiguration.hasError() >> true

        _ * localComponentsResult.resolvedProjectConfigurations >> Collections.emptySet()
        _ * resolver.resolveGraph(_, _) >> { ConfigurationInternal config, DefaultResolverResults resolverResults ->
            resolverResults.graphResolved(resolutionResults, localComponentsResult, visitedArtifactSet)
            resolverResults.artifactsResolved(resolvedConfiguration, visitedArtifactSet)
        }
    }

    def "artifacts have correct build dependencies"() {
        def configuration = conf()
        def artifactTask2 = Mock(Task)
        def artifactTask1 = Mock(Task)

        given:
        def otherConfiguration = conf("otherConf")

        def artifact1 = artifact("name1")
        artifact1.builtBy(artifactTask1)

        def artifact2 = artifact("name2")
        artifact2.builtBy(artifactTask2)

        when:
        configuration.artifacts.add(artifact1)
        otherConfiguration.artifacts.add(artifact2)
        configuration.extendsFrom(otherConfiguration)

        then:
        configuration.allArtifacts.files.files == [artifact1.file, artifact2.file] as Set
        configuration.allArtifacts.files.buildDependencies.getDependencies(Mock(Task)) == [artifactTask1, artifactTask2] as Set
        configuration.allArtifacts.buildDependencies.getDependencies(Mock(Task)) == [artifactTask1, artifactTask2] as Set
    }

    def "can declare outgoing artifacts for configuration"() {
        def configuration = conf()
        def artifact1 = artifact("name1")

        when:
        configuration.outgoing.artifact(Stub(ConfigurablePublishArtifact))

        then:
        configuration.outgoing.artifacts.size() == 1
        configuration.artifacts.size() == 1

        when:
        configuration.artifacts.add(artifact1)

        then:
        configuration.outgoing.artifacts.size() == 2
        configuration.artifacts.size() == 2
    }

    def "build dependencies are calculated from the artifacts visited during graph resolution"() {
        def configuration = conf()
        def targetTask = Mock(Task)
        def task1 = Mock(Task)
        def task2 = Mock(Task)
        def requiredTasks = [task1, task2] as Set
        def artifactTaskDependencies = Mock(TaskDependency)
        def visitedArtifactSet = Mock(VisitedArtifactSet)
        def selectedArtifactSet = Mock(SelectedArtifactSet)

        given:
        _ * visitedArtifactSet.select(_, _, _, _) >> selectedArtifactSet
        _ * selectedArtifactSet.visitDependencies(_) >> { TaskDependencyResolveContext visitor -> visitor.add(artifactTaskDependencies) }
        _ * artifactTaskDependencies.getDependencies(_) >> requiredTasks

        and:
        _ * resolver.resolveBuildDependencies(_, _) >> { ConfigurationInternal config, ResolverResults resolverResults ->
            resolverResults.graphResolved(Stub(ResolutionResult), Stub(ResolvedLocalComponentsResult), visitedArtifactSet)
        }

        expect:
        configuration.buildDependencies.getDependencies(targetTask) == requiredTasks
        configuration.incoming.dependencies.buildDependencies.getDependencies(targetTask) == requiredTasks
        configuration.incoming.files.buildDependencies.getDependencies(targetTask) == requiredTasks
    }

    def "task dependency from project dependency without common configuration"() {
        // This test exists because a NullPointerException was thrown by getTaskDependencyFromProjectDependency()
        // if the rootProject defined a task as the same name as a subproject task, but did not define the same configuration.

        def configuration = conf()

        def mainTask = Mock(Task)
        def rootProject = Mock(Project)
        def taskProject = Mock(Project)
        mainTask.project >> taskProject
        taskProject.rootProject >> rootProject

        def otherTask = Mock(Task)
        def otherTaskSet = [otherTask] as Set
        def dependentProject = Mock(Project)
        otherTask.project >> dependentProject

        when:
        def td = configuration.getTaskDependencyFromProjectDependency(false, "testit")

        and:
        rootProject.getTasksByName("testit", true) >> otherTaskSet

        and:
        def configurationContainer = Mock(ConfigurationContainer)
        1 * dependentProject.configurations >> configurationContainer
        1 * configurationContainer.findByName(configuration.name) >> null

        then:
        td.getDependencies(mainTask) == [] as Set
    }

    def "all artifacts collection has immediate artifacts"() {
        given:
        def c = conf()

        when:
        c.artifacts << artifact()
        c.artifacts << artifact()

        then:
        c.allArtifacts.size() == 2
    }

    def "all artifacts collection has inherited artifacts"() {
        given:
        def master = conf()

        def masterParent1 = conf()
        def masterParent2 = conf()
        master.extendsFrom masterParent1, masterParent2

        def masterParent1Parent1 = conf()
        def masterParent1Parent2 = conf()
        masterParent1.extendsFrom masterParent1Parent1, masterParent1Parent2

        def masterParent2Parent1 = conf()
        def masterParent2Parent2 = conf()
        masterParent2.extendsFrom masterParent2Parent1, masterParent2Parent2

        def allArtifacts = master.allArtifacts

        def added = []
        allArtifacts.whenObjectAdded { added << it.name }
        def removed = []
        allArtifacts.whenObjectRemoved { removed << it.name }

        expect:
        allArtifacts.empty

        when:
        masterParent1.artifacts << artifact("p1-1")
        masterParent1Parent1.artifacts << artifact("p1p1-1")
        masterParent1Parent2.artifacts << artifact("p1p2-1")
        masterParent2.artifacts << artifact("p2-1")
        masterParent2Parent1.artifacts << artifact("p2p1-1")
        masterParent2Parent2.artifacts << artifact("p2p2-1")

        then:
        allArtifacts.size() == 6
        added == ["p1-1", "p1p1-1", "p1p2-1", "p2-1", "p2p1-1", "p2p2-1"]

        when:
        masterParent2Parent2.artifacts.remove masterParent2Parent2.artifacts.toList().first()

        then:
        allArtifacts.size() == 5
        removed == ["p2p2-1"]

        when:
        removed.clear()
        masterParent1.extendsFrom = []

        then:
        allArtifacts.size() == 3
        removed == ["p1p1-1", "p1p2-1"]
    }

    def "artifactAdded action is fired"() {
        def configuration = conf()
        def addedAction = Mock(Action)
        def removedAction = Mock(Action)

        def addedArtifact = artifact()

        given:
        configuration.artifacts.whenObjectAdded(addedAction)
        configuration.artifacts.whenObjectRemoved(removedAction)

        when:
        configuration.artifacts.add(addedArtifact)

        then:
        1 * addedAction.execute(addedArtifact)
        0 * removedAction._

        and:
        configuration.artifacts.size() == 1

        when:
        def unknownArtifact = artifact("other")
        configuration.artifacts.remove(unknownArtifact)

        then:
        0 * _._
        configuration.artifacts.size() == 1

        when:
        configuration.artifacts.removeAll(addedArtifact)

        then:
        1 * removedAction.execute(addedArtifact)
        0 * addedAction._

        and:
        configuration.artifacts.empty
    }

    def "can copy"() {
        def configuration = prepareConfigurationForCopyTest()

        def resolutionStrategyCopy = Mock(ResolutionStrategyInternal)

        when:
        1 * resolutionStrategy.copy() >> resolutionStrategyCopy

        and:
        def copiedConfiguration = configuration.copy()

        then:
        checkCopiedConfiguration(configuration, copiedConfiguration, resolutionStrategyCopy)
        assert copiedConfiguration.dependencies == configuration.dependencies
        assert copiedConfiguration.extendsFrom.empty
    }

    def "multiple copies create unique configuration names"() {
        def configuration = prepareConfigurationForCopyTest()
        def resolutionStrategyCopy = Mock(ResolutionStrategyInternal)

        when:
        3 * resolutionStrategy.copy() >> resolutionStrategyCopy

        def copied1Configuration = configuration.copy()
        def copied2Configuration = configuration.copy()
        def copied3Configuration = configuration.copyRecursive()

        then:
        checkCopiedConfiguration(configuration, copied1Configuration, resolutionStrategyCopy)
        checkCopiedConfiguration(configuration, copied2Configuration, resolutionStrategyCopy, 2)
        checkCopiedConfiguration(configuration, copied3Configuration, resolutionStrategyCopy, 3)
    }

    @Unroll
    void "copies configuration role"() {
        def configuration = prepareConfigurationForCopyTest()
        def resolutionStrategyCopy = Mock(ResolutionStrategyInternal)
        1 * resolutionStrategy.copy() >> resolutionStrategyCopy

        when:
        configuration.canBeResolved = resolveAllowed
        configuration.canBeConsumed = consumeAllowed
        def copy = configuration.copy()


        then:
        copy.canBeResolved == configuration.canBeResolved
        copy.canBeConsumed == configuration.canBeConsumed

        where:
        resolveAllowed | consumeAllowed
        false          | false
        true           | false
        false          | true
        true           | true
    }

    def "can copy with spec"() {
        def configuration = prepareConfigurationForCopyTest()
        def resolutionStrategyCopy = Mock(ResolutionStrategyInternal)
        configuration.getDependencies().add(dependency("group3", "name3", "version3"));

        when:
        1 * resolutionStrategy.copy() >> resolutionStrategyCopy

        and:
        def copiedConfiguration = configuration.copy(new Spec<Dependency>() {
            boolean isSatisfiedBy(Dependency element) {
                return !element.getGroup().equals("group3");
            }
        })

        then:
        checkCopiedConfiguration(configuration, copiedConfiguration, resolutionStrategyCopy)
        assert copiedConfiguration.dependencies.collect({ it.group }) == ["group1", "group2"]
    }

    def "can copy recursive"() {
        def resolutionStrategyCopy = Mock(ResolutionStrategyInternal)
        def configuration = prepareConfigurationForCopyTest()


        when:
        1 * resolutionStrategy.copy() >> resolutionStrategyCopy

        and:
        def copiedConfiguration = configuration.copyRecursive()

        then:
        checkCopiedConfiguration(configuration, copiedConfiguration, resolutionStrategyCopy)
        assert copiedConfiguration.dependencies == configuration.allDependencies
        assert copiedConfiguration.extendsFrom.empty
    }

    @Issue("gradle/gradle#1567")
    def "Calls resolve actions and closures from original configuration when copied configuration is resolved"() {
        Action<ResolvableDependencies> beforeResolveAction = Mock()
        Action<ResolvableDependencies> afterResolveAction = Mock()
        def config = conf("conf")
        def beforeResolveCalled = false
        def afterResolveCalled = false

        given:
        config.incoming.beforeResolve(beforeResolveAction)
        config.incoming.afterResolve(afterResolveAction)
        config.incoming.beforeResolve {
            beforeResolveCalled = true
        }
        config.incoming.afterResolve {
            afterResolveCalled = true
        }
        def copy = config.copy()

        when:
        copy.resolvedConfiguration

        then:
        interaction { resolveConfig(copy) }
        1 * beforeResolveAction.execute(copy.incoming)
        1 * afterResolveAction.execute(copy.incoming)
        beforeResolveCalled
        afterResolveCalled
    }

    @Issue("gradle/gradle#1567")
    def "A copy of a configuration that has no resolution listeners also has no resolution listeners"() {
        given:
        def config = conf("conf")

        expect:
        config.dependencyResolutionListeners.isEmpty()

        when:
        def copy = config.copy()

        then:
        copy.dependencyResolutionListeners.isEmpty()
    }

    private prepareConfigurationForCopyTest() {
        def configuration = conf()
        configuration.visible = false
        configuration.transitive = false
        configuration.description = "descript"
        configuration.exclude([group: "value"])
        configuration.exclude([group: "value2"]);
        configuration.artifacts.add(artifact("name1", "ext1", "type1", "classifier1"))
        configuration.artifacts.add(artifact("name2", "ext2", "type2", "classifier2"))
        configuration.dependencies.add(dependency("group1", "name1", "version1"))
        configuration.dependencies.add(dependency("group2", "name2", "version2"))
        configuration.getAttributes().attribute(Attribute.of('key', String.class), 'value')
        configuration.resolutionStrategy

        def otherConf = conf("other")
        otherConf.dependencies.add(dependency("otherGroup", "name3", "version3"))
        configuration.extendsFrom(otherConf)
        return configuration
    }

    private void checkCopiedConfiguration(Configuration original, Configuration copy, def resolutionStrategyInCopy, int copyCount = 1) {
        assert copy.name == original.name + "Copy${copyCount > 1 ? copyCount : ''}"
        assert copy.visible == original.visible
        assert copy.transitive == original.transitive
        assert copy.description == original.description
        assert copy.allArtifacts as Set == original.allArtifacts as Set
        assert copy.excludeRules == original.excludeRules
        assert copy.resolutionStrategy == resolutionStrategyInCopy
        assert copy.attributes.empty && original.attributes.empty || !copy.attributes.is(original.attributes)
        original.attributes.keySet().each {
            assert copy.attributes.getAttribute(it) == original.attributes.getAttribute(it)
        }
        assert copy.canBeResolved == original.canBeResolved
        assert copy.canBeConsumed == original.canBeConsumed
        true
    }

    def "incoming dependencies set has same name and path as owner configuration"() {
        def config = conf("conf", ":project")

        expect:
        config.incoming.name == "conf"
        config.incoming.path == ":project:conf"
    }

    def "incoming dependencies set contains immediate dependencies"() {
        def config = conf("conf")
        Dependency dep1 = Mock()

        given:
        config.dependencies.add(dep1)

        expect:
        config.incoming.dependencies as List == [dep1]
    }

    def "incoming dependencies set contains inherited dependencies"() {
        def parent = conf("conf")
        def config = conf("conf")
        Dependency dep1 = Mock()

        given:
        config.extendsFrom parent
        parent.dependencies.add(dep1)

        expect:
        config.incoming.dependencies as List == [dep1]
    }

    def "incoming dependencies set files are resolved lazily"() {
        setup:
        def config = conf("conf")

        when:
        def files = config.incoming.files

        then:
        0 * _._

        when:
        files.files

        then:
        interaction { resolveConfig(config) }
        1 * resolver.getRepositories() >> []
        0 * resolver._
    }

    def "notifies beforeResolve action on incoming dependencies set when dependencies are resolved"() {
        Action<ResolvableDependencies> action = Mock()
        def config = conf("conf")

        given:
        config.incoming.beforeResolve(action)

        when:
        config.resolvedConfiguration

        then:
        interaction { resolveConfig(config) }
        1 * action.execute(config.incoming)
    }

    def "calls beforeResolve closure on incoming dependencies set when dependencies are resolved"() {
        def config = conf("conf")
        resolveConfig(config)
        def called = false

        expect:
        config.incoming.afterResolve {
            assert it == config.incoming
            called = true
        }

        when:
        config.resolvedConfiguration

        then:
        called
    }

    def "notifies afterResolve action on incoming dependencies set when dependencies are resolved"() {
        Action<ResolvableDependencies> action = Mock()
        def config = conf("conf")

        given:
        config.incoming.afterResolve(action)

        when:
        config.resolvedConfiguration

        then:
        interaction { resolveConfig(config) }
        1 * action.execute(config.incoming)

    }

    def "calls afterResolve closure on incoming dependencies set when dependencies are resolved"() {
        def config = conf("conf")
        resolveConfig(config)
        def called = false

        expect:
        config.incoming.afterResolve {
            assert it == config.incoming
            called = true
        }

        when:
        config.resolvedConfiguration

        then:
        called
    }

    def "a recursive copy of a configuration includes inherited exclude rules"() {
        given:
        def (p1, p2, child) = [conf("p1"), conf("p2"), conf("child")]
        child.extendsFrom p1, p2

        and:
        def (p1Exclude, p2Exclude) = [[group: 'p1', module: 'p1'], [group: 'p2', module: 'p2']]
        p1.exclude p1Exclude
        p2.exclude p2Exclude

        when:
        def copied = child.copyRecursive()

        then:
        copied.excludeRules.size() == 2
        copied.excludeRules.collect { [group: it.group, module: it.module] }.sort { it.group } == [p1Exclude, p2Exclude]
    }

    def "copied configuration has own instance of resolution strategy"() {
        def strategy = Mock(ResolutionStrategyInternal)
        def conf = conf()
        conf.resolutionStrategy

        when:
        def copy = conf.copy()

        then:
        1 * resolutionStrategy.copy() >> strategy
        conf.resolutionStrategy != copy.resolutionStrategy
        copy.resolutionStrategy == strategy
    }

    def "provides resolution result"() {
        def config = conf("conf")
        def result = stubResolutionResults()

        resolves(config, result, Mock(ResolvedConfiguration))

        when:
        def out = config.incoming.resolutionResult

        then:
        out.root == result.root
    }

    def resolves(ConfigurationInternal config, ResolutionResult resolutionResult, ResolvedConfiguration resolvedConfiguration) {
        def localComponentsResult = Mock(ResolvedLocalComponentsResult)
        localComponentsResult.resolvedProjectConfigurations >> []
        def visitedArtifactSet = Mock(VisitedArtifactSet)

        _ * visitedArtifactSet.select(_, _, _, _) >> Stub(SelectedArtifactSet) {
            collectFiles(_) >> { return it[0] }
        }

        resolver.resolveGraph(config, _) >> { ConfigurationInternal conf, DefaultResolverResults res ->
            res.graphResolved(resolutionResult, localComponentsResult, visitedArtifactSet)
        }
        resolver.resolveArtifacts(config, _) >> { ConfigurationInternal conf, DefaultResolverResults res ->
            res.artifactsResolved(resolvedConfiguration, visitedArtifactSet)
        }
    }

    def "resolving configuration marks parent configuration as observed"() {
        def parent = conf("parent", ":parent")
        def config = conf("conf")
        config.extendsFrom parent
        def result = Mock(ResolutionResult)
        resolves(config, result, Mock(ResolvedConfiguration))

        when:
        config.resolve()

        then:
        parent.observedState == ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED
    }

    def "resolving configuration puts it into the right state and broadcasts events"() {
        def listenerBroadcaster = Mock(AnonymousListenerBroadcast)
        def listener = Mock(DependencyResolutionListener)
        def config

        when:
        config = conf("conf")
        def result = Mock(ResolutionResult)
        resolves(config, result, Mock(ResolvedConfiguration))
        config.incoming.getResolutionResult().root

        then:
        1 * listenerManager.createAnonymousBroadcaster(_) >> listenerBroadcaster
        _ * listenerBroadcaster.getSource() >> listener
        1 * listener.beforeResolve(_) >> { ResolvableDependencies dependencies -> assert dependencies == config.incoming }
        1 * listener.afterResolve(_) >> { ResolvableDependencies dependencies -> assert dependencies == config.incoming }
        config.resolvedState == ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED
        config.state == RESOLVED
    }

    def "can determine task dependencies when graph resolution is required"() {
        def config = conf("conf")

        given:
        _ * resolutionStrategy.resolveGraphToDetermineTaskDependencies() >> true

        when:
        config.getBuildDependencies().getDependencies(null)

        then:
        config.resolvedState == ConfigurationInternal.InternalState.GRAPH_RESOLVED
        config.state == RESOLVED

        and:
        1 * resolver.resolveGraph(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.graphResolved(Stub(ResolutionResult), Stub(ResolvedLocalComponentsResult), visitedArtifacts())
        }
        1 * resolver.getRepositories() >> []
        0 * resolver._
    }

    def "can determine task dependencies when graph resolution is not"() {
        def config = conf("conf")

        given:
        _ * resolutionStrategy.resolveGraphToDetermineTaskDependencies() >> false

        when:
        config.getBuildDependencies().getDependencies(null)

        then:
        config.resolvedState == ConfigurationInternal.InternalState.BUILD_DEPENDENCIES_RESOLVED
        config.state == UNRESOLVED

        and:
        1 * resolver.resolveBuildDependencies(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.graphResolved(Stub(ResolutionResult), Stub(ResolvedLocalComponentsResult), visitedArtifacts())
        }
        0 * resolver._
    }

    def "resolving graph for task dependencies, and then resolving it for results does not re-resolve graph"() {
        def config = conf("conf")

        given:
        _ * resolutionStrategy.resolveGraphToDetermineTaskDependencies() >> true

        when:
        config.getBuildDependencies().getDependencies(null)

        then:
        config.resolvedState == ConfigurationInternal.InternalState.GRAPH_RESOLVED
        config.state == RESOLVED

        and:
        1 * resolver.resolveGraph(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.graphResolved(Stub(ResolutionResult), Stub(ResolvedLocalComponentsResult), visitedArtifacts())
        }
        1 * resolver.getRepositories() >> []
        0 * resolver._

        when:
        config.incoming.getResolutionResult().root

        then:
        config.resolvedState == ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED
        config.state == RESOLVED

        and:
        1 * resolver.resolveArtifacts(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.artifactsResolved(Stub(ResolvedConfiguration), visitedArtifacts())
        }
        0 * resolver._
    }

    def "resolves graph when result requested after resolving task dependencies"() {
        def config = conf("conf")

        given:
        _ * resolutionStrategy.resolveGraphToDetermineTaskDependencies() >> false

        when:
        config.getBuildDependencies().getDependencies(null)

        then:
        config.resolvedState == ConfigurationInternal.InternalState.BUILD_DEPENDENCIES_RESOLVED
        config.state == UNRESOLVED

        and:
        1 * resolver.resolveBuildDependencies(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.graphResolved(Stub(ResolutionResult), Stub(ResolvedLocalComponentsResult), visitedArtifacts())
        }
        0 * resolver._

        when:
        config.incoming.getResolutionResult().root

        then:
        config.resolvedState == ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED
        config.state == RESOLVED

        and:
        1 * resolver.resolveGraph(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.graphResolved(Stub(ResolutionResult), Stub(ResolvedLocalComponentsResult), visitedArtifacts())
        }
        1 * resolver.resolveArtifacts(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.artifactsResolved(Stub(ResolvedConfiguration), visitedArtifacts())
        }
        1 * resolver.getRepositories() >> []
        0 * resolver._
    }

    def "resolving configuration for results, and then resolving task dependencies required does not re-resolve graph"() {
        def config = conf("conf")

        given:
        _ * resolutionStrategy.resolveGraphToDetermineTaskDependencies() >> graphResolveRequired

        when:
        config.incoming.getResolutionResult().root

        then:
        config.resolvedState == ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED
        config.state == RESOLVED

        and:
        1 * resolver.resolveGraph(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.graphResolved(Stub(ResolutionResult), Stub(ResolvedLocalComponentsResult), visitedArtifacts())
        }
        1 * resolver.resolveArtifacts(config, _) >> { ConfigurationInternal c, ResolverResults r ->
            r.artifactsResolved(Stub(ResolvedConfiguration), visitedArtifacts())
        }
        1 * resolver.getRepositories() >> []
        0 * resolver._

        when:
        config.getBuildDependencies()

        then:
        config.resolvedState == ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED
        config.state == RESOLVED

        and:
        0 * resolver._

        where:
        graphResolveRequired << [true, false]
    }

    def "resolving configuration twice returns the same result objects"() {
        def config = conf("conf")
        when:
        expectResolved([new File("result")] as Set)

        def previousFiles = config.files
        def previousResolutionResult = config.incoming.resolutionResult
        def previousResolvedConfiguration = config.resolvedConfiguration

        then:
        config.resolvedState == ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED
        config.state == RESOLVED

        when:
        def nextFiles = config.files
        def nextResolutionResult = config.incoming.resolutionResult
        def nextResolvedConfiguration = config.resolvedConfiguration

        then:
        0 * resolver._
        nextResolutionResult.root // forces lazy resolution
        config.resolvedState == ConfigurationInternal.InternalState.ARTIFACTS_RESOLVED
        config.state == RESOLVED

        // We get back the same resolution results
        previousResolutionResult == nextResolutionResult

        // We get back the same resolved configuration
        previousResolvedConfiguration == nextResolvedConfiguration

        // And the same files
        previousFiles == nextFiles
    }

    def "copied configuration is not resolved"() {
        def config = conf("conf")
        def result = Mock(ResolutionResult)

        given:
        resolves(config, result, Mock(ResolvedConfiguration))

        config.resolutionStrategy
        config.incoming.resolutionResult

        when:
        def copy = config.copy()

        then:
        1 * resolutionStrategy.copy() >> Mock(ResolutionStrategyInternal)
        copy.resolvedState == ConfigurationInternal.InternalState.UNRESOLVED
        copy.state == UNRESOLVED
    }

    def "provides task dependency from project dependency using 'needed'"() {
        def conf = conf("conf")
        when:
        def dep = conf.getTaskDependencyFromProjectDependency(true, "foo") as TasksFromProjectDependencies
        then:
        dep.taskName == "foo"
    }

    def "provides task dependency from project dependency using 'dependents'"() {
        def conf = conf("conf")
        when:
        def dep = conf.getTaskDependencyFromProjectDependency(false, "bar") as TasksFromDependentProjects
        then:
        dep.taskName == "bar"
        dep.configurationName == "conf"
    }

    def "mutations are prohibited after resolution"() {
        def conf = conf("conf")
        def result = Mock(ResolutionResult)

        given:
        resolves(conf, result, Mock(ResolvedConfiguration))
        conf.incoming.getResolutionResult().root

        when:
        conf.dependencies.add(Mock(Dependency))
        then:
        def exDependency = thrown(InvalidUserDataException);
        exDependency.message == "Cannot change dependencies of dependency configuration ':conf' after it has been resolved."

        when:
        conf.artifacts.add(Mock(PublishArtifact))
        then:
        def exArtifact = thrown(InvalidUserDataException);
        exArtifact.message == "Cannot change artifacts of dependency configuration ':conf' after it has been resolved."
    }

    def "defaultDependencies action does not trigger when config has dependencies"() {
        def conf = conf("conf")
        def defaultDependencyAction = Mock(Action)
        conf.defaultDependencies defaultDependencyAction
        conf.dependencies.add(Mock(Dependency))

        when:
        conf.runDependencyActions()

        then:
        0 * _
    }

    def "second defaultDependencies action does not trigger if first one already added dependencies"() {
        def conf = conf("conf")
        def defaultDependencyAction1 = Mock(Action)
        def defaultDependencyAction2 = Mock(Action)
        conf.defaultDependencies defaultDependencyAction1
        conf.defaultDependencies defaultDependencyAction2

        when:
        conf.runDependencyActions()

        then:
        1 * defaultDependencyAction1.execute(conf.dependencies) >> {
            conf.dependencies.add(Mock(Dependency))
        }
        0 * _
    }

    def "defaultDependencies action is called even if parent config has dependencies"() {
        def parent = conf("parent", ":parent")
        parent.dependencies.add(Mock(Dependency))

        def conf = conf("conf")
        def defaultDependencyAction = Mock(Action)
        conf.extendsFrom parent
        conf.defaultDependencies defaultDependencyAction

        when:
        conf.runDependencyActions()

        then:
        1 * defaultDependencyAction.execute(conf.dependencies)
        0 * _
    }

    def "dependency actions are called on self first, then on parent"() {
        def parentWhenEmptyAction = Mock(Action)
        def parentMutation = Mock(Action)
        def parent = conf("parent", ":parent")
        parent.defaultDependencies parentWhenEmptyAction
        parent.withDependencies parentMutation

        def conf = conf("conf")
        def defaultDependencyAction = Mock(Action)
        def mutation = Mock(Action)
        conf.extendsFrom parent
        conf.defaultDependencies defaultDependencyAction
        conf.withDependencies mutation

        when:
        conf.runDependencyActions()

        then:
        1 * defaultDependencyAction.execute(conf.dependencies)
        1 * mutation.execute(conf.dependencies)

        then:
        1 * parentWhenEmptyAction.execute(parent.dependencies)
        1 * parentMutation.execute(conf.dependencies)
        0 * _
    }

    def propertyChangeWithNonUnresolvedStateShouldThrowEx() {
        def configuration = conf()
        prepareForFilesBySpec([] as Set)

        given:
        configuration.resolve();

        when:
        configuration.setTransitive(true)
        then:
        thrown(InvalidUserDataException)

        when:
        configuration.setVisible(false)
        then:
        thrown(InvalidUserDataException)

        when:
        configuration.exclude([:])
        then:
        thrown(InvalidUserDataException)

        when:
        configuration.setExcludeRules([] as Set)
        then:
        thrown(InvalidUserDataException)

        when:
        configuration.extendsFrom(conf("other"))
        then:
        thrown(InvalidUserDataException)

        when:
        configuration.dependencies.add(Mock(Dependency))
        then:
        thrown(InvalidUserDataException)

        when:
        configuration.dependencies.remove(Mock(Dependency))
        then:
        thrown(InvalidUserDataException)

        when:
        configuration.artifacts.add(artifact())
        then:
        thrown(InvalidUserDataException)

        when:
        configuration.artifacts.remove(artifact())
        then:
        thrown(InvalidUserDataException)
    }

    def "can define typed attributes"() {
        def conf = conf()
        def flavor = Attribute.of('flavor', Flavor) // give it a name and a type
        def buildType = Attribute.of(BuildType) // infer the name from the type

        when:
        conf.getAttributes().attribute(flavor, new FlavorImpl(name: 'free'))
        conf.getAttributes().attribute(buildType, new BuildTypeImpl(name: 'release'))

        then:
        !conf.attributes.isEmpty()
        conf.attributes.getAttribute(flavor).name == 'free'
        conf.attributes.getAttribute(buildType).name == 'release'
    }

    def "cannot define two attributes with the same name but different types"() {
        def conf = conf()
        def flavor = Attribute.of('flavor', Flavor)

        when:
        conf.getAttributes().attribute(flavor, new FlavorImpl(name: 'free'))
        conf.getAttributes().attribute(Attribute.of('flavor', String.class), 'paid')

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Cannot have two attributes with the same name but different types. This container already has an attribute named \'flavor\' of type \'org.gradle.api.internal.artifacts.configurations.DefaultConfigurationSpec$Flavor\' and you are trying to store another one of type \'java.lang.String\''
    }

    def "can overwrite a configuration attribute"() {
        def conf = conf()
        def flavor = Attribute.of(Flavor)
        conf.getAttributes().attribute(flavor, new FlavorImpl(name: 'free'))

        when:
        conf.getAttributes().attribute(flavor, new FlavorImpl(name: 'paid'))

        then:
        conf.attributes.getAttribute(flavor).name == 'paid'
    }

    def "can have two attributes with the same type but different names"() {
        def conf = conf()
        def targetPlatform = Attribute.of('targetPlatform', Platform)
        def runtimePlatform = Attribute.of('runtimePlatform', Platform)

        when:
        conf.getAttributes().attribute(targetPlatform, Platform.JAVA6)
        conf.getAttributes().attribute(runtimePlatform, Platform.JAVA7)

        then:
        conf.attributes.getAttribute(targetPlatform) == Platform.JAVA6
        conf.attributes.getAttribute(runtimePlatform) == Platform.JAVA7
    }

    def "wraps attribute container to throw a custom exception"() {
        given:
        def conf = conf()
        def a1 = Attribute.of('a1', String)

        when:
        conf.preventFromFurtherMutation()
        conf.getAttributes().attribute(a1, "a1")

        then:
        IllegalArgumentException t = thrown()
        t.message == "Cannot change attributes of dependency configuration ':conf' after it has been resolved"
    }

    def "wrapper attribute container behaves similar to the delegatee"() {
        given:
        def conf = conf()
        def a1 = Attribute.of('a1', String)
        def a2 = Attribute.of('a2', String)
        def containerMutable = conf.getAttributes()
        containerMutable.attribute(a1, 'a1')
        def containerImmutable = conf.getAttributes().asImmutable()

        when:
        conf.preventFromFurtherMutation()
        def containerWrapped = conf.getAttributes()

        then:
        containerWrapped != containerImmutable
        containerWrapped.asImmutable() == containerImmutable
        containerWrapped.getAttribute(a1) == containerImmutable.getAttribute(a1)
        containerWrapped.keySet() == containerImmutable.keySet()
        containerWrapped.contains(a1) == containerImmutable.contains(a1)
        containerWrapped.contains(a2) == containerImmutable.contains(a2)
        containerWrapped.empty == containerImmutable.empty
    }

    def "the component filter of an artifact view can only be set once"() {
        given:
        def conf = conf()

        when:
        conf.incoming.artifactView {
            it.componentFilter { true }
            it.componentFilter { true }
        }

        then:
        IllegalStateException t = thrown()
        t.message == "The component filter can only be set once before the view was computed"
    }

    def "attributes of an artifact view can be modified several times"() {
        def conf = conf()
        def a1 = Attribute.of('a1', Integer)
        def a2 = Attribute.of('a2', String)

        when:
        def artifactView = conf.incoming.artifactView {
            it.attributes.attribute(a1, 1)
            it.attributes { it.attribute(a2, "A") }
            it.attributes.attribute(a1, 10)
        }

        then:
        artifactView.attributes.keySet() == [a1, a2] as Set
        artifactView.attributes.getAttribute(a1) == 10
        artifactView.attributes.getAttribute(a2) == "A"
    }

    def "attributes of view are immutable"() {
        given:
        def conf = conf()
        def a1 = Attribute.of('a1', String)
        def artifactView = conf.incoming.artifactView {}

        when:
        artifactView.attributes.attribute(a1, "A")

        then:
        UnsupportedOperationException t = thrown()
        t.message == "Mutation of attributes is not allowed"
    }

    def dumpString() {
        when:
        def configurationDependency = dependency("dumpgroup1", "dumpname1", "dumpversion1");
        def otherConfSimilarDependency = dependency("dumpgroup1", "dumpname1", "dumpversion1");
        def otherConfDependency = dependency("dumpgroup2", "dumpname2", "dumpversion2");
        def otherConf = conf("dumpConf");
        otherConf.getDependencies().add(otherConfDependency);
        otherConf.getDependencies().add(otherConfSimilarDependency);

        def configuration = conf().extendsFrom(otherConf)
        configuration.getDependencies().add(configurationDependency);

        then:
        configuration.dump() == """
Configuration:  class='class org.gradle.api.internal.artifacts.configurations.DefaultConfiguration'  name='conf'  hashcode='${configuration.hashCode()}'
Local Dependencies:
   DefaultExternalModuleDependency{group='dumpgroup1', name='dumpname1', version='dumpversion1', configuration='default'}
Local Artifacts:
   none
All Dependencies:
   DefaultExternalModuleDependency{group='dumpgroup1', name='dumpname1', version='dumpversion1', configuration='default'}
   DefaultExternalModuleDependency{group='dumpgroup2', name='dumpname2', version='dumpversion2', configuration='default'}
All Artifacts:
   none"""
    }

    def "copied configuration has independent listeners"() {
        def original = conf()
        def seenOriginal = [] as Set<ResolvableDependencies>
        original.incoming.beforeResolve { seenOriginal.add(it) }

        def copied = original.copy()
        def seenCopied = [] as Set<ResolvableDependencies>
        copied.incoming.beforeResolve { seenCopied.add(it) }

        expectResolved([] as Set)

        when:
        original.getResolvedConfiguration()

        then:
        seenOriginal == [original.incoming] as Set
        seenCopied.empty

        when:
        copied.getResolvedConfiguration()

        then:
        seenOriginal == [original.incoming, copied.incoming] as Set
        seenCopied == [copied.incoming] as Set
    }

    def "copied configuration inherits withDependencies actions"() {
        def original = conf()
        def seenOriginal = [] as Set<DependencySet>
        original.withDependencies { seenOriginal.add(it) }

        def copied = original.copy()
        def seenCopied = [] as Set<DependencySet>
        copied.withDependencies { seenCopied.add(it) }

        expectResolved([] as Set)

        when:
        original.getResolvedConfiguration()

        then:
        seenOriginal == [original.dependencies] as Set
        seenCopied.empty

        when:
        copied.getResolvedConfiguration()

        then:
        seenOriginal == [original.dependencies, copied.dependencies] as Set
        seenCopied == [copied.dependencies] as Set
    }

    def "collects exclude rules from hierarchy"() {
        given:
        def firstRule = new DefaultExcludeRule("foo", "bar")
        def secondRule = new DefaultExcludeRule("bar", "baz")
        def thirdRule = new DefaultExcludeRule("baz", "qux")
        def rootConfig = configurationWithExcludeRules(thirdRule)
        def parentConfig = configurationWithExcludeRules(secondRule).extendsFrom(rootConfig)
        def config = configurationWithExcludeRules(firstRule).extendsFrom(parentConfig)

        expect:
        config.getAllExcludeRules() == [firstRule, secondRule, thirdRule] as Set
        parentConfig.getAllExcludeRules() == [secondRule, thirdRule] as Set
        rootConfig.getAllExcludeRules() == [thirdRule] as Set
    }

    private DefaultConfiguration configurationWithExcludeRules(ExcludeRule... rules) {
        def config = conf()
        config.setExcludeRules(rules as LinkedHashSet)
        config
    }

    // You need to wrap this in an interaction {} block when calling it
    private ResolvedConfiguration resolveConfig(ConfigurationInternal config) {
        def resolvedConfiguration = Mock(ResolvedConfiguration)
        def resolutionResult = Mock(ResolutionResult)

        resolves(config, resolutionResult, resolvedConfiguration)
        resolvedConfiguration
    }

    private visitedArtifacts() {
        def visitedArtifactSet = Stub(VisitedArtifactSet)
        def selectedArtifactSet = Stub(SelectedArtifactSet)
        _ * visitedArtifactSet.select(_, _, _, _) >> selectedArtifactSet
        _ * selectedArtifactSet.visitDependencies(_) >> { Collection<Object> deps -> deps }
        visitedArtifactSet
    }

    private dependency(String group, String name, String version) {
        new DefaultExternalModuleDependency(group, name, version);
    }

    private DefaultConfiguration conf(String confName = "conf", String projectPath = ":", String buildPath = ":") {
        def domainObjectContext = Stub(DomainObjectContext)
        def modelContainer = Stub(ModelContainer)
        def build = Path.path(buildPath)
        _ * domainObjectContext.identityPath(_) >> { String p -> build.append(Path.path(projectPath)).child(p) }
        _ * domainObjectContext.projectPath(_) >> { String p -> Path.path(projectPath).child(p) }
        _ * domainObjectContext.buildPath >> Path.path(buildPath)
        _ * domainObjectContext.model >> modelContainer
        _ * modelContainer.hasMutableState() >> true
        _ * modelContainer.withMutableState(_) >> { throw new RuntimeException() }
        _ * modelContainer.withLenientState(_) >> { throw new RuntimeException() }

        def publishArtifactNotationParser = NotationParserBuilder.toType(ConfigurablePublishArtifact).toComposite()
        new DefaultConfiguration(domainObjectContext, confName, configurationsProvider, resolver, listenerManager, metaDataProvider,
            Factories.constant(resolutionStrategy), projectAccessListener, projectFinder, TestFiles.fileCollectionFactory(),
            new TestBuildOperationExecutor(), instantiator, publishArtifactNotationParser, Stub(NotationParser), immutableAttributesFactory, rootComponentMetadataBuilder, Stub(DocumentationRegistry), userCodeApplicationContext, domainObjectContext, projectStateRegistry, TestUtil.domainObjectCollectionFactory())
    }

    private DefaultPublishArtifact artifact(String name) {
        artifact(name, "ext", "type", "classy")
    }

    private DefaultPublishArtifact artifact(String name, String extension, String type, String classifier) {
        return new DefaultPublishArtifact(name, extension, type, classifier, new Date(), new File(name));
    }

    private DefaultPublishArtifact artifact(Map props = [:]) {
        new DefaultPublishArtifact(
            props.name ?: "artifact",
            props.extension ?: "artifact",
            props.type,
            props.classifier,
            props.date,
            props.file,
            props.tasks ?: []
        )
    }

    interface Flavor extends Named {}

    static class FlavorImpl implements Flavor, Serializable {
        String name
    }

    interface BuildType extends Named {}

    static class BuildTypeImpl implements BuildType, Serializable {
        String name
    }

    enum Platform {
        JAVA6,
        JAVA7
    }
}
