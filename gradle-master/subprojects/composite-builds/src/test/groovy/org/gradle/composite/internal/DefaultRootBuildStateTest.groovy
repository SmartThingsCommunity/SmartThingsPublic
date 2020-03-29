/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.composite.internal

import org.gradle.api.Transformer
import org.gradle.api.internal.BuildDefinition
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.artifacts.DefaultBuildIdentifier
import org.gradle.api.internal.project.ProjectStateRegistry
import org.gradle.initialization.GradleLauncher
import org.gradle.initialization.GradleLauncherFactory
import org.gradle.initialization.RootBuildLifecycleListener
import org.gradle.internal.event.ListenerManager
import org.gradle.internal.invocation.BuildController
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.service.scopes.BuildTreeScopeServices
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.test.fixtures.work.TestWorkerLeaseService
import spock.lang.Specification

class DefaultRootBuildStateTest extends Specification {
    def factory = Mock(GradleLauncherFactory)
    def launcher = Mock(GradleLauncher)
    def gradle = Mock(GradleInternal)
    def listenerManager = Mock(ListenerManager)
    def lifecycleListener = Mock(RootBuildLifecycleListener)
    def action = Mock(Transformer)
    def sessionServices = Mock(BuildTreeScopeServices)
    def buildDefinition = Mock(BuildDefinition)
    def projectStateRegistry = Mock(ProjectStateRegistry)
    DefaultRootBuildState build

    def setup() {
        _ * factory.newInstance(buildDefinition, _, sessionServices) >> launcher
        _ * listenerManager.getBroadcaster(RootBuildLifecycleListener) >> lifecycleListener
        _ * sessionServices.get(ProjectStateRegistry) >> projectStateRegistry
        _ * sessionServices.get(BuildOperationExecutor) >> Stub(BuildOperationExecutor)
        _ * sessionServices.get(WorkerLeaseService) >> new TestWorkerLeaseService()
        _ * launcher.gradle >> gradle
        _ * gradle.services >> sessionServices
        _ * projectStateRegistry.withLenientState(_) >> { args -> return args[0].create() }

        build = new DefaultRootBuildState(buildDefinition, factory, listenerManager, sessionServices)
    }

    def "has identifier"() {
        expect:
        build.buildIdentifier == DefaultBuildIdentifier.ROOT
    }

    def "stops launcher on stop"() {
        when:
        build.stop()

        then:
        1 * launcher.stop()
    }

    def "runs action after notifying listeners"() {
        when:
        def result = build.run(action)

        then:
        result == '<result>'

        then:
        1 * lifecycleListener.afterStart(_ as GradleInternal)

        then:
        1 * action.transform(!null) >> { BuildController controller ->
            '<result>'
        }

        then:
        1 * lifecycleListener.beforeComplete(_ as GradleInternal)
    }

    def "can have null result"() {
        when:
        def result = build.run(action)

        then:
        result == null

        and:
        1 * action.transform(!null) >> { BuildController controller ->
            return null
        }
    }

    def "runs build when requested by action"() {
        when:
        def result = build.run(action)

        then:
        result == '<result>'

        and:
        1 * launcher.executeTasks() >> gradle
        1 * action.transform(!null) >> { BuildController controller ->
            assert controller.run() == gradle
            return '<result>'
        }
    }

    def "configures build when requested by action"() {
        when:
        def result = build.run(action)

        then:
        result == '<result>'

        and:
        1 * launcher.getConfiguredBuild() >> gradle
        1 * action.transform(!null) >> { BuildController controller ->
            assert controller.configure() == gradle
            return '<result>'
        }
    }

    def "cannot request configuration after build has been run"() {
        given:
        action.transform(!null) >> { BuildController controller ->
            controller.run()
            controller.configure()
        }

        when:
        build.run(action)

        then:
        IllegalStateException e = thrown()
        e.message == 'Cannot use launcher after build has completed.'

        and:
        1 * launcher.executeTasks() >> gradle
    }

    def "forwards action failure and cleans up"() {
        def failure = new RuntimeException()

        when:
        build.run(action)

        then:
        RuntimeException e = thrown()
        e == failure

        and:
        1 * action.transform(!null) >> { BuildController controller -> throw failure }
        1 * lifecycleListener.beforeComplete(_ as GradleInternal)
    }

    def "forwards build failure and cleans up"() {
        def failure = new RuntimeException()

        when:
        build.run(action)

        then:
        RuntimeException e = thrown()
        e == failure

        and:
        1 * launcher.executeTasks() >> { throw failure }
        1 * action.transform(!null) >> { BuildController controller ->
            controller.run()
        }
        1 * lifecycleListener.beforeComplete(_ as GradleInternal)
    }

    def "forwards configure failure and cleans up"() {
        def failure = new RuntimeException()

        when:
        build.run(action)

        then:
        RuntimeException e = thrown()
        e == failure

        and:
        1 * launcher.getConfiguredBuild() >> { throw failure }
        1 * action.transform(!null) >> { BuildController controller ->
            controller.configure()
        }
        1 * lifecycleListener.beforeComplete(_ as GradleInternal)
    }

    def "cannot run after configuration failure"() {
        when:
        build.run(action)

        then:
        IllegalStateException e = thrown()
        e.message == 'Cannot use launcher after build has completed.'

        and:
        1 * launcher.configuredBuild >> { throw new RuntimeException() }
        1 * action.transform(!null) >> { BuildController controller ->
            try {
                controller.configure()
            } catch (RuntimeException) {
                // Ignore
            }
            controller.run()
        }
        1 * lifecycleListener.beforeComplete(_ as GradleInternal)
    }
}
