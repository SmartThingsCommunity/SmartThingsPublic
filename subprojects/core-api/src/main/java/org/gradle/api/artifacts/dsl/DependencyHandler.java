/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.artifacts.dsl;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.artifacts.transform.VariantTransform;
import org.gradle.api.artifacts.type.ArtifactTypeContainer;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.plugins.ExtensionAware;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * <p>A {@code DependencyHandler} is used to declare dependencies. Dependencies are grouped into
 * configurations (see {@link org.gradle.api.artifacts.Configuration}).</p>
 *
 * <p>To declare a specific dependency for a configuration you can use the following syntax:</p>
 *
 * <pre>
 * dependencies {
 *     <i>configurationName</i> <i>dependencyNotation</i>
 * }
 * </pre>
 *
 * <p>Example shows a basic way of declaring dependencies.
 * <pre class='autoTested'>
 * apply plugin: 'java'
 * //so that we can use 'implementation', 'testImplementation' for dependencies
 *
 * dependencies {
 *   //for dependencies found in artifact repositories you can use
 *   //the group:name:version notation
 *   implementation 'commons-lang:commons-lang:2.6'
 *   testImplementation 'org.mockito:mockito:1.9.0-rc1'
 *
 *   //map-style notation:
 *   implementation group: 'com.google.code.guice', name: 'guice', version: '1.0'
 *
 *   //declaring arbitrary files as dependencies
 *   implementation files('hibernate.jar', 'libs/spring.jar')
 *
 *   //putting all jars from 'libs' onto compile classpath
 *   implementation fileTree('libs')
 * }
 * </pre>
 *
 * <h2>Advanced dependency configuration</h2>
 * <p>To do some advanced configuration on a dependency when it is declared, you can additionally pass a configuration closure:</p>
 *
 * <pre>
 * dependencies {
 *     <i>configurationName</i>(<i>dependencyNotation</i>){
 *         <i>configStatement1</i>
 *         <i>configStatement2</i>
 *     }
 * }
 * </pre>
 *
 * Examples of advanced dependency declaration including:
 * <ul>
 * <li>Forcing certain dependency version in case of the conflict.</li>
 * <li>Excluding certain dependencies by name, group or both.
 *      More details about per-dependency exclusions can be found in
 *      docs for {@link org.gradle.api.artifacts.ModuleDependency#exclude(java.util.Map)}.</li>
 * <li>Avoiding transitive dependencies for certain dependency.</li>
 * </ul>
 *
 * <pre class='autoTestedWithDeprecations'>
 * apply plugin: 'java' //so that I can declare 'implementation' dependencies
 *
 * dependencies {
 *   implementation('org.hibernate:hibernate:3.1') {
 *     //in case of versions conflict '3.1' version of hibernate wins:
 *     force = true
 *
 *     //excluding a particular transitive dependency:
 *     exclude module: 'cglib' //by artifact name
 *     exclude group: 'org.jmock' //by group
 *     exclude group: 'org.unwanted', module: 'iAmBuggy' //by both name and group
 *
 *     //disabling all transitive dependencies of this dependency
 *     transitive = false
 *   }
 * }
 * </pre>
 *
 * More examples of advanced configuration, useful when dependency module has multiple artifacts:
 * <ul>
 *   <li>Declaring dependency to a specific configuration of the module.</li>
 *   <li>Explicit specification of the artifact. See also {@link org.gradle.api.artifacts.ModuleDependency#artifact(groovy.lang.Closure)}.</li>
 * </ul>
 *
 * <pre class='autoTested'>
 * apply plugin: 'java' //so that I can declare 'implementation' dependencies
 *
 * dependencies {
 *   //configuring dependency to specific configuration of the module
 *   implementation configuration: 'someConf', group: 'org.someOrg', name: 'someModule', version: '1.0'
 *
 *   //configuring dependency on 'someLib' module
 *   implementation(group: 'org.myorg', name: 'someLib', version:'1.0') {
 *     //explicitly adding the dependency artifact:
 *     artifact {
 *       //useful when some artifact properties unconventional
 *       name = 'someArtifact' //artifact name different than module name
 *       extension = 'someExt'
 *       type = 'someType'
 *       classifier = 'someClassifier'
 *     }
 *   }
 * }
 * </pre>
 *
 * <h2>Dependency notations</h2>
 *
 * <p>There are several supported dependency notations. These are described below. For each dependency declared this
 * way, a {@link Dependency} object is created. You can use this object to query or further configure the
 * dependency.</p>
 *
 * <p>You can also always add instances of
 * {@link org.gradle.api.artifacts.Dependency} directly:</p>
 *
 * <code><i>configurationName</i> &lt;instance&gt;</code>
 *
 * <h3>External dependencies</h3>
 *
 * <p>There are two notations supported for declaring a dependency on an external module.
 * One is a string notation formatted this way:</p>
 *
 * <code><i>configurationName</i> "<i>group</i>:<i>name</i>:<i>version</i>:<i>classifier</i>@<i>extension</i>"</code>
 *
 * <p>The other is a map notation:</p>
 *
 * <code><i>configurationName</i> group: <i>group</i>, name: <i>name</i>, version: <i>version</i>, classifier:
 * <i>classifier</i>, ext: <i>extension</i></code>
 *
 * <p>In both notations, all properties, except name, are optional.</p>
 *
 * <p>External dependencies are represented by a {@link
 * org.gradle.api.artifacts.ExternalModuleDependency}.</p>
 *
 * <pre class='autoTested'>
 * apply plugin: 'java'
 * //so that we can use 'implementation', 'testImplementation' for dependencies
 *
 * dependencies {
 *   //for dependencies found in artifact repositories you can use
 *   //the string notation, e.g. group:name:version
 *   implementation 'commons-lang:commons-lang:2.6'
 *   testImplementation 'org.mockito:mockito:1.9.0-rc1'
 *
 *   //map notation:
 *   implementation group: 'com.google.code.guice', name: 'guice', version: '1.0'
 * }
 * </pre>
 *
 * <h3>Project dependencies</h3>
 *
 * <p>To add a project dependency, you use the following notation:
 * <p><code><i>configurationName</i> project(':someProject')</code>
 *
 * <p>The notation <code>project(':projectA')</code> is similar to the syntax you use
 * when configuring a projectA in a multi-module gradle project.
 *
 * <p>By default, when you declare dependency to projectA, you actually declare dependency to the 'default' configuration of the projectA.
 * If you need to depend on a specific configuration of projectA, use map notation for projects:
 * <p><code><i>configurationName</i> project(path: ':projectA', configuration: 'someOtherConfiguration')</code>
 *
 * <p>Project dependencies are represented using a {@link org.gradle.api.artifacts.ProjectDependency}.
 *
 * <h3>File dependencies</h3>
 *
 * <p>You can also add a dependency using a {@link org.gradle.api.file.FileCollection}:</p>
 * <code><i>configurationName</i> files('a file')</code>
 *
 * <pre class='autoTested'>
 * apply plugin: 'java'
 * //so that we can use 'implementation', 'testImplementation' for dependencies
 *
 * dependencies {
 *   //declaring arbitrary files as dependencies
 *   implementation files('hibernate.jar', 'libs/spring.jar')
 *
 *   //putting all jars from 'libs' onto compile classpath
 *   implementation fileTree('libs')
 * }
 * </pre>
 *
 * <p>File dependencies are represented using a {@link org.gradle.api.artifacts.SelfResolvingDependency}.</p>
 *
 * <h3>Dependencies to other configurations</h3>
 *
 * <p>You can add a dependency using a {@link org.gradle.api.artifacts.Configuration}.</p>
 *
 * <p>When the configuration is from the same project as the target configuration, the target configuration is changed
 * to extend from the provided configuration.</p>
 *
 * <p>When the configuration is from a different project, a project dependency is added.</p>
 *
 * <h3>Gradle distribution specific dependencies</h3>
 *
 * <p>It is possible to depend on certain Gradle APIs or libraries that Gradle ships with.
 * It is particularly useful for Gradle plugin development. Example:</p>
 *
 * <pre class='autoTested'>
 * //Our Gradle plugin is written in groovy
 * apply plugin: 'groovy'
 * //now we can use the 'implementation' configuration for declaring dependencies
 *
 * dependencies {
 *   //we will use the Groovy version that ships with Gradle:
 *   implementation localGroovy()
 *
 *   //our plugin requires Gradle API interfaces and classes to compile:
 *   implementation gradleApi()
 *
 *   //we will use the Gradle test-kit to test build logic:
 *   testImplementation gradleTestKit()
 * }
 * </pre>
 *
 * <h3>Client module dependencies</h3>
 *
 * <p>To add a client module to a configuration you can use the notation:</p>
 *
 * <pre>
 * <i>configurationName</i> module(<i>moduleNotation</i>) {
 *     <i>module dependencies</i>
 * }
 * </pre>
 *
 * The module notation is the same as the dependency notations described above, except that the classifier property is
 * not available. Client modules are represented using a {@link org.gradle.api.artifacts.ClientModule}.
 */
public interface DependencyHandler extends ExtensionAware {
    /**
     * Adds a dependency to the given configuration.
     *
     * @param configurationName The name of the configuration.
     * @param dependencyNotation
     *
     * The dependency notation, in one of the notations described above.
     * @return The dependency.
     */
    @Nullable
    Dependency add(String configurationName, Object dependencyNotation);

    /**
     * Adds a dependency to the given configuration, and configures the dependency using the given closure.
     *
     * @param configurationName The name of the configuration.
     * @param dependencyNotation The dependency notation, in one of the notations described above.
     * @param configureClosure The closure to use to configure the dependency.
     * @return The dependency.
     */
    Dependency add(String configurationName, Object dependencyNotation, Closure configureClosure);

    /**
     * Creates a dependency without adding it to a configuration.
     *
     * @param dependencyNotation The dependency notation, in one of the notations described above.
     * @return The dependency.
     */
    Dependency create(Object dependencyNotation);

    /**
     * Creates a dependency without adding it to a configuration, and configures the dependency using
     * the given closure.
     *
     * @param dependencyNotation The dependency notation, in one of the notations described above.
     * @param configureClosure The closure to use to configure the dependency.
     * @return The dependency.
     */
    Dependency create(Object dependencyNotation, Closure configureClosure);

    /**
     * Creates a dependency on a client module.
     *
     * @param notation The module notation, in one of the notations described above.
     * @return The dependency.
     */
    Dependency module(Object notation);

    /**
     * Creates a dependency on a client module. The dependency is configured using the given closure before it is
     * returned.
     *
     * @param notation The module notation, in one of the notations described above.
     * @param configureClosure The closure to use to configure the dependency.
     * @return The dependency.
     */
    Dependency module(Object notation, Closure configureClosure);

    /**
     * Creates a dependency on a project.
     *
     * @param notation The project notation, in one of the notations described above.
     * @return The dependency.
     */
    Dependency project(Map<String, ?> notation);

    /**
     * Creates a dependency on the API of the current version of Gradle.
     *
     * @return The dependency.
     */
    Dependency gradleApi();

    /**
     * Creates a dependency on the <a href="https://docs.gradle.org/current/userguide/test_kit.html" target="_top">Gradle test-kit</a> API.
     *
     * @return The dependency.
     * @since 2.6
     */
    Dependency gradleTestKit();

    /**
     * Creates a dependency on the Groovy that is distributed with the current version of Gradle.
     *
     * @return The dependency.
     */
    Dependency localGroovy();

    /**
     * Returns the dependency constraint handler for this project.
     *
     * @return the dependency constraint handler for this project
     * @since 4.5
     */
    DependencyConstraintHandler getConstraints();

    /**
     * Configures dependency constraint for this project.
     *
     * <p>This method executes the given action against the {@link org.gradle.api.artifacts.dsl.DependencyConstraintHandler} for this project.</p>
     *
     * @param configureAction the action to use to configure module metadata
     * @since 4.5
     */
    void constraints(Action<? super DependencyConstraintHandler> configureAction);

    /**
     * Returns the component metadata handler for this project. The returned handler can be used for adding rules
     * that modify the metadata of depended-on software components.
     *
     * @return the component metadata handler for this project
     * @since 1.8
     */
    ComponentMetadataHandler getComponents();

    /**
     * Configures component metadata for this project.
     *
     * <p>This method executes the given action against the {@link org.gradle.api.artifacts.dsl.ComponentMetadataHandler} for this project.</p>
     *
     * @param configureAction the action to use to configure module metadata
     * @since 1.8
     */
    void components(Action<? super ComponentMetadataHandler> configureAction);

    /**
     * Returns the component module metadata handler for this project. The returned handler can be used for adding rules
     * that modify the metadata of depended-on software components.
     *
     * @return the component module metadata handler for this project
     * @since 2.2
     */
    ComponentModuleMetadataHandler getModules();

    /**
     * Configures module metadata for this project.
     *
     * <p>This method executes the given action against the {@link org.gradle.api.artifacts.dsl.ComponentModuleMetadataHandler} for this project.
     *
     * @param configureAction the action to use to configure module metadata
     * @since 2.2
     */
    void modules(Action<? super ComponentModuleMetadataHandler> configureAction);

    /**
     * Creates an artifact resolution query.
     *
     * @since 2.0
     */
    ArtifactResolutionQuery createArtifactResolutionQuery();

    /**
     * Configures the attributes schema. The action is passed a {@link AttributesSchema} instance.
     * @param configureAction the configure action
     * @return the configured schema
     *
     * @since 3.4
     */
    AttributesSchema attributesSchema(Action<? super AttributesSchema> configureAction);

    /**
     * Returns the attributes schema for this handler.
     * @return the attributes schema
     *
     * @since 3.4
     */
    AttributesSchema getAttributesSchema();

    /**
     * Returns the artifact type definitions for this handler.
     * @since 4.0
     */
    ArtifactTypeContainer getArtifactTypes();

    /**
     * Configures the artifact type definitions for this handler.
     * @since 4.0
     */
    void artifactTypes(Action<? super ArtifactTypeContainer> configureAction);

    /**
     * Registers an artifact transform.
     *
     * @deprecated use {@link #registerTransform(Class, Action)} instead.
     * @see org.gradle.api.artifacts.transform.ArtifactTransform
     * @since 3.5
     */
    @Deprecated
    void registerTransform(Action<? super VariantTransform> registrationAction);

    /**
     * Registers an <a href="https://docs.gradle.org/current/userguide/artifact_transforms.html">artifact transform</a>.
     *
     * <p>
     *     The registration action needs to specify the {@code from} and {@code to} attributes.
     *     It may also provide parameters for the transform action by using {@link TransformSpec#parameters(Action)}.
     * </p>
     *
     * <p>For example:</p>
     *
     * <pre class='autoTested'>
     * // You have a transform action like this:
     * abstract class MyTransform implements TransformAction&lt;Parameters&gt; {
     *     interface Parameters extends TransformParameters {
     *         {@literal @}Input
     *         Property&lt;String&gt; getStringParameter();
     *         {@literal @}InputFiles
     *         ConfigurableFileCollection getInputFiles();
     *     }
     *
     *     void transform(TransformOutputs outputs) {
     *         // ...
     *     }
     * }
     *
     * // Then you can register the action like this:
     *
     * def artifactType = Attribute.of('artifactType', String)
     *
     * dependencies.registerTransform(MyTransform) {
     *     from.attribute(artifactType, "jar")
     *     to.attribute(artifactType, "java-classes-directory")
     *
     *     parameters {
     *         stringParameter.set("Some string")
     *         inputFiles.from("my-input-file")
     *     }
     * }
     * </pre>
     *
     * @see TransformAction
     * @since 5.3
     */
    <T extends TransformParameters> void registerTransform(Class<? extends TransformAction<T>> actionType, Action<? super TransformSpec<T>> registrationAction);

    /**
     * Declares a dependency on a platform. If the target coordinates represent multiple
     * potential components, the platform component will be selected, instead of the library.
     *
     * @param notation the coordinates of the platform
     *
     * @since 5.0
     */
    Dependency platform(Object notation);

    /**
     * Declares a dependency on a platform. If the target coordinates represent multiple
     * potential components, the platform component will be selected, instead of the library.
     *
     * @param notation the coordinates of the platform
     * @param configureAction the dependency configuration block
     *
     * @since 5.0
     */
    Dependency platform(Object notation, Action<? super Dependency> configureAction);

    /**
     * Declares a dependency on an enforced platform. If the target coordinates represent multiple
     * potential components, the platform component will be selected, instead of the library.
     * An enforced platform is a platform for which the direct dependencies are forced, meaning
     * that they would override any other version found in the graph.
     *
     * @param notation the coordinates of the platform
     *
     * @since 5.0
     */
    @Incubating
    Dependency enforcedPlatform(Object notation);

    /**
     * Declares a dependency on an enforced platform. If the target coordinates represent multiple
     * potential components, the platform component will be selected, instead of the library.
     * An enforced platform is a platform for which the direct dependencies are forced, meaning
     * that they would override any other version found in the graph.
     *
     * @param notation the coordinates of the platform
     * @param configureAction the dependency configuration block
     *
     * @since 5.0
     */
    @Incubating
    Dependency enforcedPlatform(Object notation, Action<? super Dependency> configureAction);

    /**
     * Declares a dependency on the test fixtures of a component.
     * @param notation the coordinates of the component to use test fixtures for
     *
     * @since 5.6
     */
    @Incubating
    Dependency testFixtures(Object notation);

    /**
     * Declares a dependency on the test fixtures of a component and allows configuring
     * the resulting dependency.
     * @param notation the coordinates of the component to use test fixtures for
     *
     * @since 5.6
     */
    @Incubating
    Dependency testFixtures(Object notation, Action<? super Dependency> configureAction);
}
