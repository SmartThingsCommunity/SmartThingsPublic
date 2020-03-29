/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.plugins.ide.internal.tooling


import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.build.IncludedBuildState
import org.gradle.test.fixtures.file.CleanupTestDirectory
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.TestUtil
import org.gradle.util.UsesNativeServices
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

@UsesNativeServices
@CleanupTestDirectory
class GradleBuildBuilderTest extends Specification {
    @Shared
    @ClassRule
    public TestNameTestDirectoryProvider temporaryFolder = TestNameTestDirectoryProvider.newInstance(getClass())
    def builder = new GradleBuildBuilder()
    @Shared
    def project = TestUtil.builder(temporaryFolder).withName("root").build()
    @Shared
    def child1 = ProjectBuilder.builder().withName("child1").withParent(project).build()
    @Shared
    def child2 = ProjectBuilder.builder().withName("child2").withParent(project).build()

    def "builds model"() {
        expect:
        def model = builder.buildAll("org.gradle.tooling.model.gradle.GradleBuild", startProject)
        model.rootProject.path == ":"
        model.rootProject.name == "root"
        model.rootProject.parent == null
        model.rootProject.projectDirectory == project.projectDir
        model.rootProject.children.size() == 2
        model.rootProject.children.every { it.parent == model.rootProject }
        model.projects*.name == ["root", "child1", "child2"]
        model.projects*.path == [":", ":child1", ":child2"]
        model.includedBuilds.empty
        model.editableBuilds.empty

        where:
        startProject | _
        project      | _
        child2       | _
    }

    def "builds model for included builds"() {
        def rootProject = Mock(ProjectInternal)
        def project1 = Mock(ProjectInternal)
        def project2 = Mock(ProjectInternal)

        def rootDir = temporaryFolder.createDir("root")
        def dir1 = temporaryFolder.createDir("dir1")
        def dir2 = temporaryFolder.createDir("dir2")

        def rootBuild = Mock(GradleInternal)
        def build1 = Mock(GradleInternal)
        def build2 = Mock(GradleInternal)

        def includedBuild1 = Mock(TestIncludedBuild)
        def includedBuild2 = Mock(TestIncludedBuild)

        rootProject.gradle >> rootBuild
        rootProject.rootDir >> rootDir
        rootProject.childProjects >> [:]
        rootProject.allprojects >> [rootProject]

        project1.rootDir >> dir1
        project1.childProjects >> [:]
        project1.allprojects >> [project1]

        project2.rootDir >> dir2
        project2.childProjects >> [:]
        project2.allprojects >> [project2]

        rootBuild.rootProject >> rootProject
        rootBuild.includedBuilds >> [includedBuild1]

        includedBuild1.configuredBuild >> build1

        build1.includedBuilds >> [includedBuild2]
        build1.rootProject >> project1
        build1.parent >> rootBuild

        includedBuild2.configuredBuild >> build2

        build2.includedBuilds >> []
        build2.rootProject >> project2
        build2.parent >> rootBuild

        expect:
        def model = builder.buildAll("org.gradle.tooling.model.gradle.GradleBuild", rootProject)
        model.includedBuilds.size() == 1

        def model1 = model.includedBuilds[0]
        model1.rootDir == dir1
        model1.includedBuilds.size() == 1

        def model2 = model1.includedBuilds[0]
        model2.rootDir == dir2
        model2.includedBuilds.empty

        model.editableBuilds.size() == 2
        model.editableBuilds[0] == model1
        model.editableBuilds[1] == model2

        model1.editableBuilds.empty
        model2.editableBuilds.empty
    }

    interface TestIncludedBuild extends IncludedBuild, IncludedBuildState {
    }
}
