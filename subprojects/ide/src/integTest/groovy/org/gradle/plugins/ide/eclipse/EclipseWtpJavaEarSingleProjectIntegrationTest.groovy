/*
 * Copyright 2014 the original author or authors.
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
package org.gradle.plugins.ide.eclipse

import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import org.gradle.test.fixtures.maven.MavenFileModule
import org.gradle.test.fixtures.maven.MavenFileRepository

class EclipseWtpJavaEarSingleProjectIntegrationTest extends AbstractEclipseIntegrationSpec {

    String localMaven

    def setup() {
        MavenFileRepository mavenRepo = new MavenFileRepository(file('maven-repo'))
        MavenFileModule lib1Api = mavenRepo.module('org.example', 'lib1-api', '1.0').publish()
        mavenRepo.module('org.example', 'lib1-impl', '1.0').dependsOn(lib1Api).publish()
        MavenFileModule lib2Api = mavenRepo.module('org.example', 'lib2-api', '2.0').publish()
        mavenRepo.module('org.example', 'lib2-impl', '2.0').dependsOn(lib2Api).publish()
        localMaven = "maven { url '${mavenRepo.uri}' }"
    }

    @ToBeFixedForInstantExecution
    def "generates configuration files for an ear project"() {
        file('src/main/java').mkdirs()

        settingsFile << "rootProject.name = 'ear'"

        buildFile <<
        """apply plugin: 'eclipse-wtp'
           apply plugin: 'ear'
           apply plugin: 'java'

           repositories { $localMaven }

           dependencies {
               earlib 'org.example:lib1-impl:1.0'
               deploy 'org.example:lib2-impl:2.0'
           }
        """

        when:
        run 'eclipse'

        then:
        // Builders and natures
        def project = project
        project.assertHasNatures('org.eclipse.wst.common.project.facet.core.nature',
                'org.eclipse.wst.common.modulecore.ModuleCoreNature',
                'org.eclipse.jem.workbench.JavaEMFNature',
                'org.eclipse.jdt.core.javanature')
        project.assertHasBuilders('org.eclipse.wst.common.project.facet.core.builder',
                'org.eclipse.wst.validation.validationbuilder')

        // Classpath
        def classpath = classpath
        classpath.assertHasLibs('lib2-impl-2.0.jar', 'lib1-impl-1.0.jar', 'lib1-api-1.0.jar')
        classpath.lib('lib1-api-1.0.jar').assertIsDeployedTo('/lib')
        classpath.lib('lib1-impl-1.0.jar').assertIsDeployedTo('/lib')
        classpath.lib('lib2-impl-2.0.jar').assertIsDeployedTo('/')

        // Facets
        def facets = wtpFacets
        facets.assertHasFixedFacets('jst.ear')
        facets.assertHasInstalledFacets('jst.ear')
        facets.assertFacetVersion('jst.ear', '5.0')

        // Component
        def component = wtpComponent
        component.deployName == 'ear'
        component.resources.size() == 1
        component.sourceDirectory('src/main/java').assertDeployedAt('/')
        component.modules.size() == 0
    }

    @ToBeFixedForInstantExecution
    def "ear deployment location can be configured via libDirName"() {
        settingsFile << "rootProject.name = 'ear'"

        buildFile <<
        """apply plugin: 'eclipse-wtp'
           apply plugin: 'ear'
           apply plugin: 'java'

           repositories { $localMaven }

           dependencies {
               earlib 'org.example:lib1-impl:1.0'
               deploy 'org.example:lib2-impl:2.0'
           }
            ear {
               libDirName = 'APP-INF/lib'
           }
        """

        when:
        run 'eclipse'

        then:
        def classpath = classpath
        classpath.assertHasLibs('lib2-impl-2.0.jar', 'lib1-impl-1.0.jar', 'lib1-api-1.0.jar')
        classpath.lib('lib1-api-1.0.jar').assertIsDeployedTo('/APP-INF/lib')
        classpath.lib('lib1-impl-1.0.jar').assertIsDeployedTo('/APP-INF/lib')
        classpath.lib('lib2-impl-2.0.jar').assertIsDeployedTo('/')
    }
}
