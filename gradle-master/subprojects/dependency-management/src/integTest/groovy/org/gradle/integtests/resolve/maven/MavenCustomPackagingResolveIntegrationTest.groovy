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

package org.gradle.integtests.resolve.maven

import org.gradle.integtests.fixtures.AbstractHttpDependencyResolutionTest
import spock.lang.Issue

class MavenCustomPackagingResolveIntegrationTest extends AbstractHttpDependencyResolutionTest {

    @Issue("https://issues.gradle.org/browse/GRADLE-2984")
    def "can resolve local module with custom packaging"() {
        using m2
        def local = m2.mavenRepo().module("local", "local", "1.0").hasType("aar").hasPackaging("aar").publish()

        when:
        buildScript """
            configurations {
                local
            }
            repositories {
                mavenLocal()
            }
            dependencies {
                local "local:local:1.0"
            }
            task local(type: Sync) {
                into 'local'
                from configurations.local
            }
        """

        then:
        succeeds("local")

        and:
        file("local").assertHasDescendants("local-1.0.aar")
        file('local/local-1.0.aar').assertIsCopyOf(local.artifactFile)
    }

    @Issue(['GRADLE-2188', "https://issues.gradle.org/browse/GRADLE-2984"])
    def "can resolve remote module with custom packaging"() {
        def remote = mavenHttpRepo.module("remote", "remote", "1.0").hasType("aar").hasPackaging("aar").publish()

        given:
        buildScript """
            configurations {
                remote
            }
            repositories {
                maven { url "$mavenHttpRepo.uri" }
            }
            dependencies {
                remote "remote:remote:1.0"
            }
            task remote(type: Sync) {
                into 'remote'
                from configurations.remote
            }
        """

        when:
        remote.pom.expectGet()
        remote.artifact.expectHead()
        remote.artifact.expectGet()

        run("remote")

        then:
        file("remote").assertHasDescendants("remote-1.0.aar")
        file('remote/remote-1.0.aar').assertIsCopyOf(remote.artifactFile)

        when:
        server.resetExpectations()
        run("remote")

        then: // uses cached stuff
        file("remote").assertHasDescendants("remote-1.0.aar")
        file('remote/remote-1.0.aar').assertIsCopyOf(remote.artifactFile)
    }

    @Issue('https://github.com/gradle/gradle/issues/4893')
    def "can resolve remote module with custom packaging but default jar artifact"() {
        def remote = mavenHttpRepo.module("remote", "remote", "1.0").hasType("jar").hasPackaging("hk2-jar").publish()

        given:
        buildScript """
            configurations {
                remote
            }
            repositories {
                maven { url "$mavenHttpRepo.uri" }
            }
            dependencies {
                remote "remote:remote:1.0"
            }
            task remote(type: Sync) {
                into 'remote'
                from configurations.remote
            }
        """

        when:
        remote.pom.expectGet()
        remote.getArtifact("remote-1.0.hk2-jar").expectHeadMissing()
        remote.artifact.expectGet()

        run("remote")

        then:
        file("remote").assertHasDescendants("remote-1.0.jar")
        file('remote/remote-1.0.jar').assertIsCopyOf(remote.artifactFile)

        when:
        server.resetExpectations()
        run("remote")

        then: // uses cached stuff
        file("remote").assertHasDescendants("remote-1.0.jar")
        file('remote/remote-1.0.jar').assertIsCopyOf(remote.artifactFile)
    }

    def "can consume remote module with custom packaging from another module"() {
        def customPackaging = mavenHttpRepo.module("remote", "remote", "1.0").hasType("aar").hasPackaging("aar").publish()
        def consumer = mavenHttpRepo.module("consumer", "consumer", "1.0").dependsOn(customPackaging).publish()

        given:
        buildScript """
            configurations {
                remote
            }
            repositories {
                maven { url "$mavenHttpRepo.uri" }
            }
            dependencies {
                remote "consumer:consumer:1.0"
            }
            task remote(type: Sync) {
                into 'remote'
                from configurations.remote
            }
        """

        when:
        consumer.pom.expectGet()
        consumer.artifact.expectGet()
        customPackaging.pom.expectGet()
        customPackaging.artifact.expectHead()
        customPackaging.artifact.expectGet()

        run("remote")

        then:
        file("remote").assertHasDescendants("consumer-1.0.jar", "remote-1.0.aar")
        file('remote/consumer-1.0.jar').assertIsCopyOf(consumer.artifactFile)
        file('remote/remote-1.0.aar').assertIsCopyOf(customPackaging.artifactFile)

        when:
        server.resetExpectations()
        run("remote")

        then: // uses cached stuff
        file("remote").assertHasDescendants("consumer-1.0.jar", "remote-1.0.aar")
        file('remote/consumer-1.0.jar').assertIsCopyOf(consumer.artifactFile)
        file('remote/remote-1.0.aar').assertIsCopyOf(customPackaging.artifactFile)
    }
}
