package org.gradle.kotlin.dsl

import com.nhaarman.mockito_kotlin.KStubbing
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ClientModule
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyConstraint
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyConstraintHandler
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class DependencyHandlerExtensionsTest {

    @Test
    fun `given group, name, version, configuration, classifier and ext, 'create' extension will build corresponding map`() {

        val expectedModuleMap = mapOf(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "configuration" to "cfg",
            "classifier" to "cls",
            "ext" to "x")

        val dependencies = newDependencyHandlerMock()
        val dependency: ExternalModuleDependency = mock()
        whenever(dependencies.create(expectedModuleMap)).thenReturn(dependency)

        assertThat(
            dependencies.create(
                group = "g",
                name = "n",
                version = "v",
                configuration = "cfg",
                classifier = "cls",
                ext = "x"),
            sameInstance(dependency))
    }

    @Test
    fun `given group and module, 'exclude' extension will build corresponding map`() {

        val dependencyHandlerMock = newDependencyHandlerMock()
        val dependencies = DependencyHandlerScope.of(dependencyHandlerMock)
        val dependency: ExternalModuleDependency = mock()
        val events = mutableListOf<String>()
        whenever(dependencyHandlerMock.create("dependency")).then {
            events.add("created")
            dependency
        }
        whenever(dependency.exclude(mapOf("group" to "g", "module" to "m"))).then {
            events.add("configured")
            dependency
        }
        whenever(dependencyHandlerMock.add("configuration", dependency)).then {
            events.add("added")
            dependency
        }

        dependencies {

            "configuration"("dependency") {
                val configuredDependency =
                    exclude(group = "g", module = "m")
                assertThat(
                    configuredDependency,
                    sameInstance(dependency))
            }
        }

        assertThat(
            events,
            equalTo(listOf("created", "configured", "added")))
    }

    @Test
    fun `given path and configuration, 'project' extension will build corresponding map`() {

        val dependencyHandlerMock = newDependencyHandlerMock()
        val dependencies = DependencyHandlerScope.of(dependencyHandlerMock)
        val dependency: ProjectDependency = mock()
        val events = mutableListOf<String>()
        val expectedProjectMap = mapOf("path" to ":project", "configuration" to "default")
        whenever(dependencyHandlerMock.project(expectedProjectMap)).then {
            events.add("created")
            dependency
        }
        whenever(dependencyHandlerMock.add("configuration", dependency)).then {
            events.add("added")
            dependency
        }
        val project: Project = mock()
        whenever(dependency.dependencyProject).thenReturn(project)

        dependencies {

            "configuration"(project(path = ":project", configuration = "default")) {
                events.add("configured")
                assertThat(
                    dependencyProject,
                    sameInstance(project))
            }
        }

        assertThat(
            events,
            equalTo(listOf("created", "configured", "added")))
    }

    @Test
    fun `given configuration name and dependency notation, it will add the dependency`() {

        val dependencyHandler = newDependencyHandlerMock {
            on { add(any(), any()) } doReturn mock<Dependency>()
        }

        val dependencies = DependencyHandlerScope.of(dependencyHandler)
        dependencies {
            "configuration"("notation")
        }

        verify(dependencyHandler).add("configuration", "notation")
    }

    @Test
    fun `given configuration and dependency notation, it will add the dependency to the named configuration`() {

        val dependencyHandler = newDependencyHandlerMock {
            on { add(any(), any()) } doReturn mock<Dependency>()
        }
        val configuration = mock<Configuration> {
            on { name } doReturn "c"
        }

        val dependencies = DependencyHandlerScope.of(dependencyHandler)
        dependencies {
            configuration("notation")
        }

        verify(dependencyHandler).add("c", "notation")
    }

    @Test
    fun `client module configuration`() {

        val clientModule = mock<ClientModule> {
            on { setTransitive(any()) }.thenAnswer { it.mock }
        }

        val commonsCliDependency = mock<ExternalModuleDependency>(name = "commonsCliDependency")

        val antModule = mock<ClientModule>(name = "antModule")
        val antLauncherDependency = mock<ExternalModuleDependency>(name = "antLauncherDependency")
        val antJUnitDependency = mock<ExternalModuleDependency>(name = "antJUnitDependency")

        val dependencies = newDependencyHandlerMock {
            on { module("org.codehaus.groovy:groovy:2.4.7") } doReturn clientModule

            on { create("commons-cli:commons-cli:1.0") } doReturn commonsCliDependency

            val antModuleNotation = mapOf("group" to "org.apache.ant", "name" to "ant", "version" to "1.9.6")
            on { module(antModuleNotation) } doReturn antModule
            on { create("org.apache.ant:ant-launcher:1.9.6@jar") } doReturn antLauncherDependency
            on { create("org.apache.ant:ant-junit:1.9.6") } doReturn antJUnitDependency

            on { add("runtime", clientModule) } doReturn clientModule
        }

        dependencies.apply {
            val groovy = module("org.codehaus.groovy:groovy:2.4.7") {

                // Configures the module itself
                isTransitive = false

                dependency("commons-cli:commons-cli:1.0") {
                    // Configures the external module dependency
                    isTransitive = false
                }

                module(group = "org.apache.ant", name = "ant", version = "1.9.6") {
                    // Configures the inner module dependencies
                    dependencies(
                        "org.apache.ant:ant-launcher:1.9.6@jar",
                        "org.apache.ant:ant-junit:1.9.6")
                }
            }
            add("runtime", groovy)
        }

        verify(clientModule).isTransitive = false
        verify(clientModule).addDependency(commonsCliDependency)
        verify(clientModule).addDependency(antModule)

        verify(commonsCliDependency).isTransitive = false

        verify(antModule).addDependency(antLauncherDependency)
        verify(antModule).addDependency(antJUnitDependency)
    }

    @Test
    fun `dependency on configuration using string notation doesn't cause IllegalStateException`() {

        val dependencyHandler = newDependencyHandlerMock {
            on { add(any(), any()) }.thenReturn(null)
        }

        val baseConfig = mock<Configuration>()

        val dependencies = DependencyHandlerScope.of(dependencyHandler)
        dependencies {
            "configuration"(baseConfig)
        }

        verify(dependencyHandler).add("configuration", baseConfig)
    }

    @Test
    fun `dependency on configuration doesn't cause IllegalStateException`() {

        val dependencyHandler = newDependencyHandlerMock {
            on { add(any(), any()) }.thenReturn(null)
        }

        val configuration = mock<Configuration> {
            on { name } doReturn "configuration"
        }

        val baseConfig = mock<Configuration>()

        val dependencies = DependencyHandlerScope.of(dependencyHandler)
        dependencies {
            configuration(baseConfig)
        }

        verify(dependencyHandler).add("configuration", baseConfig)
    }

    @Test
    fun `can declare dependency constraints`() {

        val constraint = mock<DependencyConstraint>()
        val constraintHandler = mock<DependencyConstraintHandler> {
            on { add(any(), any()) } doReturn constraint
            on { add(any(), any(), any()) } doReturn constraint
        }
        val dependenciesHandler = newDependencyHandlerMock {
            on { constraints } doReturn constraintHandler
            on { constraints(any()) } doAnswer {
                (it.getArgument(0) as Action<DependencyConstraintHandler>).execute(constraintHandler)
            }
        }
        val dependencies = DependencyHandlerScope.of(dependenciesHandler)

        // using the api
        dependencies {
            constraints {
                add("api", "some:thing:1.0")
                add("api", "other:thing") {
                    it.version { it.strictly("1.0") }
                }
            }
        }

        // using generated accessors
        fun DependencyConstraintHandler.api(dependencyConstraintNotation: Any): DependencyConstraint? =
            add("api", dependencyConstraintNotation)

        fun DependencyConstraintHandler.api(dependencyConstraintNotation: Any, configuration: DependencyConstraint.() -> Unit): DependencyConstraint? =
            add("api", dependencyConstraintNotation, configuration)

        dependencies {
            constraints {
                api("some:thing:1.0")
                api("other:thing") {
                    version { it.strictly("1.0") }
                }
            }
        }

        // using the string invoke syntax (requires extra parentheses around the `constraints` property)
        dependencies {
            (constraints) {
                "api"("some:thing:1.0")
                "api"("other:thing") {
                    version { it.strictly("1.0") }
                }
            }
        }

        inOrder(constraintHandler) {
            repeat(3) {
                verify(constraintHandler).add(eq("api"), eq("some:thing:1.0"))
                verify(constraintHandler).add(eq("api"), eq("other:thing"), any())
            }
            verifyNoMoreInteractions()
        }
    }
}


fun newDependencyHandlerMock(stubbing: KStubbing<DependencyHandler>.(DependencyHandler) -> Unit = {}) =
    mock<DependencyHandler>(extraInterfaces = arrayOf(ExtensionAware::class), stubbing = stubbing)
