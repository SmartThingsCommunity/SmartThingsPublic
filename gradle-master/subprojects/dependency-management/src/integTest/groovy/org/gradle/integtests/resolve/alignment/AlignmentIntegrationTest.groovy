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

package org.gradle.integtests.resolve.alignment

import org.gradle.integtests.fixtures.GradleMetadataResolveRunner
import org.gradle.integtests.fixtures.RequiredFeature
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import spock.lang.Issue

class AlignmentIntegrationTest extends AbstractAlignmentSpec {

    def "should align leaves to the same version"() {
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.1'
            path 'json:1.1 -> core:1.1'
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org:json:1.1'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            module('core') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('xml') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('json') alignsTo('1.1') byVirtualPlatform()
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                    module('org:core:1.1')
                }
                module("org:json:1.1") {
                    module('org:core:1.1')
                }
            }
        }
    }

    def "should not align modules outside of platform"() {
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.1'
            path 'json:1.1 -> core:1.1'
            path 'outside:module:1.0 -> core:1.0'
            path 'outside:module:1.1 -> core:1.0'
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org:json:1.1'
                conf 'outside:module:1.0'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            module('core') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('xml') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('json') alignsTo('1.1') byVirtualPlatform()

            // the following will NOT upgrade to 1.1, despite core being 1.1, because this module
            // does not belong to the same platform
            module('module') group('outside') alignsTo('1.0') byVirtualPlatform('outside', 'platform')
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                    module('org:core:1.1')
                }
                module("org:json:1.1") {
                    module('org:core:1.1')
                }
                module("outside:module:1.0") {
                    edge('org:core:1.0', 'org:core:1.1').byConflictResolution("between versions 1.1 and 1.0")
                }
            }
        }
    }

    def "should align leaf with core"() {
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.1'
            path 'json:1.1 -> core:1.1'
        }

        given:
        buildFile << """
            dependencies {
                conf('org:xml:1.0') {
                    transitive = false
                }
                conf 'org:json:1.0'
                conf 'org:core:1.1'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            module('xml') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('json') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('core') alignsTo('1.1') byVirtualPlatform()
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                }
                edge("org:json:1.0", "org:json:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                    module('org:core:1.1')
                }
                module('org:core:1.1')
            }
        }
    }

    def "shouldn't fail if target alignment version doesn't exist"() {
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'json:1.1 -> core:1.1'
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org:json:1.1'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            module('xml') misses('1.1') alignsTo('1.0') byVirtualPlatform()
            module('core') tries('1.0') alignsTo('1.1')
            module('json') alignsTo('1.1') byVirtualPlatform()
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                module("org:xml:1.0") {
                    edge('org:core:1.0', 'org:core:1.1')
                        .byConflictResolution("between versions 1.1 and 1.0")
                        .byConstraint("belongs to platform org:platform:1.1")
                }
                module("org:json:1.1") {
                    module('org:core:1.1')
                }
            }
        }
    }

    def "should align leaves to the same version when core has lower version"() {
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.0' // patch releases
            path 'json:1.1 -> core:1.0' // patch releases
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org:json:1.1'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            module('xml') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('core') misses('1.1') alignsTo('1.0')
            module('json') alignsTo('1.1') byVirtualPlatform()
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.1") {
                    module('org:core:1.0')
                    byConstraint("belongs to platform org:platform:1.1")
                }
                module("org:json:1.1") {
                    module('org:core:1.0')
                }
            }
        }
    }

    /**
     * This test demonstrates a real world example, where some published versions are heterogeneous
     * or even inconsistent. For example, databind 2.9.4 depends on annotations 2.9.0, but we still
     * want to upgrade annotations to the highest version of the platform seen in the graph, which
     * is 2.9.4.1, a **patch** release in this case. This patch release doesn't publish all versions
     */
    def "can align heterogeneous versions"() {
        repository {
            path 'databind:2.7.9 -> core:2.7.9'
            path 'databind:2.7.9 -> annotations:2.7.9'
            path 'databind:2.9.4 -> core:2.9.4'
            path 'databind:2.9.4 -> annotations:2.9.0' // intentional!
            path 'kt:2.9.4.1 -> databind:2.9.4'
            'org:annotations:2.9.0'()
            'org:annotations:2.9.4'()
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:core:2.9.4'
                conf 'org:databind:2.7.9'
                conf 'org:kt:2.9.4.1'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            module('core') misses('2.9.4.1') alignsTo('2.9.4') byVirtualPlatform()
            module('databind') tries('2.7.9') misses('2.9.4.1') alignsTo('2.9.4') byVirtualPlatform()
            module('annotations') tries('2.7.9', '2.9.0') misses('2.9.4.1') alignsTo('2.9.4') byVirtualPlatform()
            module('kt') alignsTo('2.9.4.1') byVirtualPlatform()
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                module('org:core:2.9.4')
                edge('org:databind:2.7.9', 'org:databind:2.9.4').byConstraint("belongs to platform org:platform:2.9.4.1")
                module('org:kt:2.9.4.1') {
                    module('org:databind:2.9.4') {
                        module('org:core:2.9.4').byConstraint("belongs to platform org:platform:2.9.4.1")
                        edge('org:annotations:2.9.0', 'org:annotations:2.9.4').byConstraint("belongs to platform org:platform:2.9.4.1")
                    }
                }
            }
        }

    }

    /**
     * This test is a variant of the previous one where there's an additional catch: one
     * of the modules (annotations) is supposedly nonexistent in 2.7.9 (say, it appeared in 2.9.x)
     */
    def "can align heterogeneous versions with new modules appearing in later releases"() {
        repository {
            path 'databind:2.7.9 -> core:2.7.9'
            path 'databind:2.9.4 -> core:2.9.4'
            path 'databind:2.9.4 -> annotations:2.9.0' // intentional!
            path 'kt:2.9.4.1 -> databind:2.9.4'
            'org:annotations:2.9.0'()
            'org:annotations:2.9.4'()
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:core:2.9.4'
                conf 'org:databind:2.7.9'
                conf 'org:kt:2.9.4.1'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            module('core') misses('2.9.4.1') alignsTo('2.9.4') byVirtualPlatform()
            module('databind') tries('2.7.9') misses('2.9.4.1') alignsTo('2.9.4') byVirtualPlatform()
            module('annotations') tries('2.9.0') misses('2.9.4.1') alignsTo('2.9.4') byVirtualPlatform()
            module('kt') alignsTo('2.9.4.1') byVirtualPlatform()
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                module('org:core:2.9.4').byConstraint("belongs to platform org:platform:2.9.4.1")
                edge('org:databind:2.7.9', 'org:databind:2.9.4').byConstraint("belongs to platform org:platform:2.9.4.1")
                module('org:kt:2.9.4.1') {
                    module('org:databind:2.9.4') {
                        module('org:core:2.9.4').byConstraint("belongs to platform org:platform:2.9.4.1")
                        edge('org:annotations:2.9.0', 'org:annotations:2.9.4').byConstraint("belongs to platform org:platform:2.9.4.1")
                    }
                }
            }
        }

    }


    // Platforms cannot be published with plain Ivy
    @RequiredFeature(feature = GradleMetadataResolveRunner.REPOSITORY_TYPE, value = "maven")
    def "can align thanks to a published platform"() {
        repository {
            path 'databind:2.7.9 -> core:2.7.9'
            path 'databind:2.7.9 -> annotations:2.7.9'
            path 'databind:2.9.4 -> core:2.9.4'
            path 'databind:2.9.4 -> annotations:2.9.0' // intentional!
            path 'kt:2.9.4.1 -> databind:2.9.4'
            'org:annotations:2.9.0'()
            'org:annotations:2.9.4'()

            // define "real" platforms, as published modules.
            // The platforms are supposed to declare _in extenso_ what modules
            // they include, by constraints
            'org:platform' {
                '2.7.9' {
                    asPlatform()
                    constraint("org:databind:2.7.9")
                    constraint("org:core:2.7.9")
                    constraint("org:annotations:2.7.9")
                }
                '2.9.0' {
                    asPlatform()
                    constraint("org:databind:2.9.0")
                    constraint("org:core:2.9.0")
                    constraint("org:annotations:2.9.0")
                }
                '2.9.4' {
                    asPlatform()
                    constraint("org:databind:2.9.4")
                    constraint("org:core:2.9.4")
                    constraint("org:annotations:2.9.0")
                }
                '2.9.4.1' {
                    asPlatform()
                    // versions here are intentionally lower
                    constraint("org:databind:2.9.4")
                    constraint("org:core:2.9.4")
                    constraint("org:annotations:2.9.4")
                }
            }
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:core:2.9.4'
                conf 'org:databind:2.7.9'
                conf 'org:kt:2.9.4.1'

                components.all(DeclarePlatform)
            }

            class DeclarePlatform implements ComponentMetadataRule {
                void execute(ComponentMetadataContext ctx) {
                    ctx.details.with {
                        belongsTo("org:platform:\${id.version}", false)
                    }
                }
            }
        """


        when:
        expectAlignment {
            module('core') alignsTo('2.9.4') byPublishedPlatform()
            module('databind') tries('2.7.9') alignsTo('2.9.4') byPublishedPlatform()
            module('annotations') tries('2.7.9') alignsTo('2.9.4') byPublishedPlatform()
            module('kt') alignsTo('2.9.4.1') byPublishedPlatform()

            doesNotGetPlatform("org", "platform", "2.7.9") // because of conflict resolution
            doesNotGetPlatform("org", "platform", "2.9.0") // because of conflict resolution
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                module('org:core:2.9.4') {
                    edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                }
                edge('org:databind:2.7.9', 'org:databind:2.9.4') {
                    edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                }
                module('org:kt:2.9.4.1') {
                    module("org:platform:2.9.4.1") {
                        noArtifacts()
                        constraint("org:core:2.9.4")
                        constraint("org:databind:2.9.4")
                        constraint("org:annotations:2.9.4")
                        module("org:platform:2.9.4.1")
                    }
                    module('org:databind:2.9.4') {
                        module('org:core:2.9.4')
                        edge('org:annotations:2.9.0', 'org:annotations:2.9.4') {
                            edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                        }
                    }
                }
            }
        }

    }

    def "can align 2 different platforms"() {
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.1'
            path 'json:1.1 -> core:1.1'

            path 'org2:xml:1.0 -> org2:core:1.0'
            path 'org2:json:1.0 -> org2:core:1.0'
            path 'org2:xml:1.1 -> org2:core:1.1'
            path 'org2:json:1.1 -> org2:core:1.1'
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org:json:1.1'

                conf 'org2:xml:1.0'
                conf 'org2:json:1.1'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            ['org', 'org2'].each { group ->
                module('core') group(group) tries('1.0') alignsTo('1.1') byVirtualPlatform(group)
                module('xml') group(group) tries('1.0') alignsTo('1.1') byVirtualPlatform(group)
                module('json') group(group) alignsTo('1.1') byVirtualPlatform(group)
            }
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                    module('org:core:1.1')
                }
                module("org:json:1.1") {
                    module('org:core:1.1')
                }

                edge("org2:xml:1.0", "org2:xml:1.1") {
                    byConstraint("belongs to platform org2:platform:1.1")
                    module('org2:core:1.1')
                }
                module("org2:json:1.1") {
                    module('org2:core:1.1')
                }
            }
        }
    }

    def "doesn't align on evicted edge"() {
        given:
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.1'
            path 'json:1.1 -> core:1.1'

            path 'org2:foo:1.0 -> org4:a:1.0 -> org:json:1.1'
            path 'org3:bar:1.0 -> org4:b:1.1 -> org4:a:1.1'
        }

        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org2:foo:1.0'
                conf 'org3:bar:1.0'
            }
        """

        and:
        "align the 'org' group only"()

        when:
        expectAlignment {
            module('xml') alignsTo('1.0') byVirtualPlatform()
            module('core') alignsTo('1.0')
            module('json') tries('1.1')
            module('foo') group('org2') alignsTo('1.0')
            module('bar') group('org3') alignsTo('1.0')
            module('a') group('org4') tries('1.0') alignsTo('1.1')
            module('b') group('org4') alignsTo('1.1')
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                module('org:xml:1.0') {
                    module('org:core:1.0')
                }
                module('org2:foo:1.0') {
                    edge('org4:a:1.0', 'org4:a:1.1') {
                        byConflictResolution("between versions 1.1 and 1.0")
                    }
                }
                module('org3:bar:1.0') {
                    module('org4:b:1.1') {
                        module('org4:a:1.1')
                    }
                }
            }
        }
    }

    def "should not align on rejected version"() {
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.1'
            path 'json:1.1 -> core:1.1'
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org:json:1.1'
            }

            configurations.conf.resolutionStrategy {
                componentSelection {
                    withModule('org:xml') {
                        if (it.candidate.version == '1.1') {
                            reject("version 1.1 is buggy")
                        }
                    }
                }
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        repositoryInteractions {
            'org:core:1.0' {
                expectGetMetadata()
            }
            'org:xml:1.0' {
                expectResolve()
            }
            'org:xml:1.1' {
                expectGetMetadata()
            }
            'org:core:1.1' {
                expectResolve()
            }
            'org:json:1.1' {
                expectResolve()
            }
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.0") {
                    byConstraint("belongs to platform org:platform:1.1")
                    // byReason("version 1.1 is buggy") // TODO CC: uncomment when we collect rejection from component selection rule
                    edge('org:core:1.0', 'org:core:1.1') {
                        byConflictResolution("between versions 1.1 and 1.0")
                    }
                }
                module("org:json:1.1") {
                    module('org:core:1.1')
                }
            }
        }
    }

    // This documents the current behavior. It doesn't really make sense to "belong to"
    // 2 different virtual platforms, as they would resolve exactly the same
    def "can belong to multiple virtual platforms"() {
        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.1'
            path 'json:1.1 -> core:1.1'
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org:json:1.1'
            }
        """
        and:
        "align the 'org' group to 2 different virtual platforms"()

        when:
        expectAlignment {
            module('core') tries('1.0') alignsTo('1.1') byVirtualPlatform('org', 'platform') byVirtualPlatform('org', 'platform2')
            module('xml') tries('1.0') alignsTo('1.1') byVirtualPlatform('org', 'platform') byVirtualPlatform('org', 'platform2')
            module('json') alignsTo('1.1') byVirtualPlatform('org', 'platform') byVirtualPlatform('org', 'platform2')
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                    byConstraint("belongs to platform org:platform2:1.1")
                    module('org:core:1.1')
                }
                module("org:json:1.1") {
                    module('org:core:1.1')
                }
            }
        }
    }


    // Platforms cannot be published with plain Ivy
    @RequiredFeature(feature = GradleMetadataResolveRunner.REPOSITORY_TYPE, value = "maven")
    def "can belong to 2 different published platforms"() {
        given:
        repository {
            ['2.4', '2.5'].each { groovyVersion ->
                // first, we define a "Groovy" library
                path "org.apache.groovy:json:$groovyVersion -> org.apache.groovy:core:$groovyVersion"
                path "org.apache.groovy:xml:$groovyVersion -> org.apache.groovy:core:$groovyVersion"

                // Groovy "belongs to" the Groovy platform
                "org.apache.groovy:platform:$groovyVersion" {
                    asPlatform()
                    constraint("org.apache.groovy:core:$groovyVersion")
                    constraint("org.apache.groovy:json:$groovyVersion")
                    constraint("org.apache.groovy:xml:$groovyVersion")
                }
            }

            // a library belonging to Spring platform. This test intentionally doesn't say that this
            // library also belongs to the Spring platform, because we want to check that _because_ groovy
            // belongs to it too, we will automatically upgrade spring core to 1.1
            group('org.springframework') {
                module('core') {
                    version('1.0')
                    version('1.1')
                }
            }

            // Groovy also belongs to the "Spring" platform, which uses a different versioning scheme
            'org.springframework:spring-platform:1.0' {
                asPlatform()
                constraint('org.apache.groovy:core:2.4')
                constraint('org.springframework:core:1.1')
            }
        }
        buildFile << """
            dependencies {
                conf 'org.apache.groovy:xml:2.4'
                conf 'org.apache.groovy:json:2.5'
                conf 'org.springframework:core:1.0'
            }
        """

        and:
        'a rule which declares that Groovy belongs to the Groovy and the Spring platforms'()

        when:
        expectAlignment {
            module('core') {
                group('org.apache.groovy') tries('2.4') alignsTo('2.5')
                byPublishedPlatform('org.apache.groovy', 'platform')
                byPublishedPlatform('org.springframework', 'spring-platform', '1.0')
            }
            module('xml') {
                group('org.apache.groovy') tries('2.4') alignsTo('2.5')
                byPublishedPlatform('org.apache.groovy', 'platform')
                byPublishedPlatform('org.springframework', 'spring-platform', '1.0')
            }
            module('json') {
                group('org.apache.groovy') alignsTo('2.5')
                byPublishedPlatform('org.apache.groovy', 'platform')
                byPublishedPlatform('org.springframework', 'spring-platform', '1.0')
            }

            // Spring core intentionally doesn't belong to the Spring platform.
            module('core') group('org.springframework') tries('1.0') alignsTo('1.1')
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org.apache.groovy:xml:2.4", "org.apache.groovy:xml:2.5") {
                    module("org.apache.groovy:core:2.5") {
                        module("org.apache.groovy:platform:2.5") {
                            noArtifacts()
                            module("org.apache.groovy:platform:2.5")          // The way the rule is defined, it is applied to the platform itself.
                            module("org.springframework:spring-platform:1.0") // This is not good practice, but we keep this to describe the current behavior.
                            constraint("org.apache.groovy:core:2.5")
                            constraint("org.apache.groovy:json:2.5")
                            constraint("org.apache.groovy:xml:2.5")
                        }
                        module("org.springframework:spring-platform:1.0") {
                            noArtifacts()
                            constraint("org.apache.groovy:core:2.4", "org.apache.groovy:core:2.5")
                            constraint("org.springframework:core:1.1")
                        }
                    }
                    module("org.apache.groovy:platform:2.5")
                    module("org.springframework:spring-platform:1.0")
                }
                module("org.apache.groovy:json:2.5") {
                    module("org.apache.groovy:core:2.5")
                    module("org.apache.groovy:platform:2.5")
                    module("org.springframework:spring-platform:1.0")
                }
                edge("org.springframework:core:1.0", "org.springframework:core:1.1") {
                    byConflictResolution("between versions 1.1 and 1.0")
                }
            }
        }
    }

    // Platforms cannot be published with plain Ivy
    @RequiredFeature(feature = GradleMetadataResolveRunner.REPOSITORY_TYPE, value = "maven")
    def "belonging to both a virtual and a published platforms resolves with alignment"() {
        given:
        repository {
            // In this setup, the "Groovy" platform is going to be virtual
            ['2.4', '2.5'].each { groovyVersion ->
                // first, we define a "Groovy" library
                path "org.apache.groovy:json:$groovyVersion -> org.apache.groovy:core:$groovyVersion"
                path "org.apache.groovy:xml:$groovyVersion -> org.apache.groovy:core:$groovyVersion"
            }

            // a library belonging to Spring platform. This test intentionally doesn't say that this
            // library also belongs to the Spring platform, because we want to check that _because_ groovy
            // belongs to it too, we will automatically upgrade spring core to 1.1
            group('org.springframework') {
                module('core') {
                    version('1.0')
                    version('1.1')
                }
            }

            // Groovy also belongs to the "Spring" platform, which uses a different versioning scheme
            'org.springframework:spring-platform:1.0' {
                asPlatform()
                constraint('org.apache.groovy:core:2.4')
                constraint('org.springframework:core:1.1')
            }
        }
        buildFile << """
            dependencies {
                conf 'org.apache.groovy:xml:2.4'
                conf 'org.apache.groovy:json:2.5'
                conf 'org.springframework:core:1.0'
            }
        """

        and:
        'a rule which declares that Groovy belongs to the Groovy and the Spring platforms'(true)

        when:
        expectAlignment {
            module('core') {
                group('org.apache.groovy') tries('2.4') alignsTo('2.5')
                byVirtualPlatform('org.apache.groovy', 'platform')
                byPublishedPlatform('org.springframework', 'spring-platform', '1.0')
            }
            module('xml') {
                group('org.apache.groovy') tries('2.4') alignsTo('2.5')
                byVirtualPlatform('org.apache.groovy', 'platform')
                byPublishedPlatform('org.springframework', 'spring-platform', '1.0')
            }
            module('json') {
                group('org.apache.groovy') alignsTo('2.5')
                byVirtualPlatform('org.apache.groovy', 'platform')
                byPublishedPlatform('org.springframework', 'spring-platform', '1.0')
            }

            // Spring core intentionally doesn't belong to the Spring platform.
            module('core') group('org.springframework') tries('1.0') alignsTo('1.1')
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org.apache.groovy:xml:2.4", "org.apache.groovy:xml:2.5") {
                    module("org.apache.groovy:core:2.5") {
                        module("org.springframework:spring-platform:1.0") {
                            noArtifacts()
                            constraint("org.apache.groovy:core:2.4", "org.apache.groovy:core:2.5")
                            constraint("org.springframework:core:1.1")
                        }
                    }
                    module("org.springframework:spring-platform:1.0")
                }
                module("org.apache.groovy:json:2.5") {
                    module("org.apache.groovy:core:2.5")
                    module("org.springframework:spring-platform:1.0")
                }
                edge("org.springframework:core:1.0", "org.springframework:core:1.1") {
                    byConflictResolution("between versions 1.1 and 1.0")
                }
            }
        }
    }

    // We only need to test one flavor
    @RequiredFeature(feature = GradleMetadataResolveRunner.GRADLE_METADATA, value = "true")
    @RequiredFeature(feature = GradleMetadataResolveRunner.REPOSITORY_TYPE, value = "maven")
    @ToBeFixedForInstantExecution
    def "virtual platform missing modules are cached across builds"() {
        // Disable daemon, so that the second run executes with the file cache
        // and therefore make sure that we read the "missing" status from disk
        executer.withArgument('--no-daemon')

        repository {
            path 'xml -> core'
            path 'json -> core'
            path 'xml:1.1 -> core:1.1'
            path 'json:1.1 -> core:1.1'
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:xml:1.0'
                conf 'org:json:1.1'
            }
        """
        and:
        "a rule which infers module set from group and version"()

        when:
        expectAlignment {
            module('core') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('xml') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('json') alignsTo('1.1') byVirtualPlatform()
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                    module('org:core:1.1')
                }
                module("org:json:1.1") {
                    module('org:core:1.1')
                }
            }
        }

        when:
        resetExpectations()
        run ':checkDeps'

        then: "no network requests are issued"
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:xml:1.0", "org:xml:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                    module('org:core:1.1')
                }
                module("org:json:1.1") {
                    module('org:core:1.1')
                }
            }
        }
    }

    // Platforms cannot be published with plain Ivy
    @RequiredFeature(feature = GradleMetadataResolveRunner.REPOSITORY_TYPE, value = "maven")
    @ToBeFixedForInstantExecution
    def "published platform can be found in a different repository"() {
        // Disable daemon, so that the second run executes with the file cache
        // and therefore make sure that we read the "missing" status from disk
        executer.withArgument('--no-daemon')

        // An empty repository, that will only return misses
        def emptyRepo = mavenHttpRepo("nothing")

        repository {
            path 'databind:2.7.9 -> core:2.7.9'
            path 'databind:2.7.9 -> annotations:2.7.9'
            path 'databind:2.9.4 -> core:2.9.4'
            path 'databind:2.9.4 -> annotations:2.9.0' // intentional!
            path 'kt:2.9.4.1 -> databind:2.9.4'
            'org:annotations:2.9.0'()
            'org:annotations:2.9.4'()

            // define "real" platforms, as published modules.
            // The platforms are supposed to declare _in extenso_ what modules
            // they include, by constraints
            'org:platform' {
                '2.7.9' {
                    asPlatform()
                    constraint("org:databind:2.7.9")
                    constraint("org:core:2.7.9")
                    constraint("org:annotations:2.7.9")
                }
                '2.9.0' {
                    asPlatform()
                    constraint("org:databind:2.9.0")
                    constraint("org:core:2.9.0")
                    constraint("org:annotations:2.9.0")
                }
                '2.9.4' {
                    asPlatform()
                    constraint("org:databind:2.9.4")
                    constraint("org:core:2.9.4")
                    constraint("org:annotations:2.9.0")
                }
                '2.9.4.1' {
                    asPlatform()
                    // versions here are intentionally lower
                    constraint("org:databind:2.9.4")
                    constraint("org:core:2.9.4")
                    constraint("org:annotations:2.9.4")
                }
            }
        }

        given:
        buildFile << """
            repositories {
                def repo = maven { url "$emptyRepo.uri" }
                remove(repo)
                addFirst(repo)
            }

            dependencies {
                conf 'org:core:2.9.4'
                conf 'org:databind:2.7.9'
                conf 'org:kt:2.9.4.1'

                components.all(DeclarePlatform)
            }

            class DeclarePlatform implements ComponentMetadataRule {
                void execute(ComponentMetadataContext ctx) {
                    ctx.details.with {
                        belongsTo("org:platform:\${id.version}", false)
                    }
                }
            }
        """


        when:
        [['core', 'databind', 'kt', 'annotations', 'platform'], ['2.7.9', '2.9.0', '2.9.4', '2.9.4.1']].combinations { module, version ->
            emptyRepo.module('org', module, version).missing()
        }

        expectAlignment {
            module('core') alignsTo('2.9.4') byPublishedPlatform()
            module('databind') tries('2.7.9') alignsTo('2.9.4') byPublishedPlatform()
            module('annotations') tries('2.7.9') alignsTo('2.9.4') byPublishedPlatform()
            module('kt') alignsTo('2.9.4.1') byPublishedPlatform()

            doesNotGetPlatform("org", "platform", "2.7.9") // because of conflict resolution
            doesNotGetPlatform("org", "platform", "2.9.0") // because of conflict resolution
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                module('org:core:2.9.4') {
                    edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                }
                edge('org:databind:2.7.9', 'org:databind:2.9.4') {
                    edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                }
                module('org:kt:2.9.4.1') {
                    module("org:platform:2.9.4.1") {
                        noArtifacts()
                        constraint("org:core:2.9.4")
                        constraint("org:databind:2.9.4")
                        constraint("org:annotations:2.9.4")
                        module("org:platform:2.9.4.1")
                    }
                    module('org:databind:2.9.4') {
                        module('org:core:2.9.4')
                        edge('org:annotations:2.9.0', 'org:annotations:2.9.4') {
                            edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                        }
                    }
                }
            }
        }

        when:
        resetExpectations()
        emptyRepo.server.resetExpectations()
        run ':checkDeps'

        then: "no more network requests should be issued"
        resolve.expectGraph {
            root(":", ":test:") {
                module('org:core:2.9.4') {
                    edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                }
                edge('org:databind:2.7.9', 'org:databind:2.9.4') {
                    edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                }
                module('org:kt:2.9.4.1') {
                    module("org:platform:2.9.4.1") {
                        noArtifacts()
                        constraint("org:core:2.9.4")
                        constraint("org:databind:2.9.4")
                        constraint("org:annotations:2.9.4")
                        module("org:platform:2.9.4.1")
                    }
                    module('org:databind:2.9.4') {
                        module('org:core:2.9.4')
                        edge('org:annotations:2.9.0', 'org:annotations:2.9.4') {
                            edge("org:platform:2.9.4", "org:platform:2.9.4.1")
                        }
                    }
                }
            }
        }

    }

    @RequiredFeature(feature = GradleMetadataResolveRunner.REPOSITORY_TYPE, value = "maven")
    def "virtual platform constraints shouldn't be transitive"() {
        repository {
            "org:member1:1.1" {
                dependsOn(group: "other", artifact:"transitive", version:"1.0")
            }
            "org:member2:1.1" {
                dependsOn(group:'org', artifact:'member1', version:'1.1', exclusions: [[module: 'transitive']])
            }
            "other:transitive:1.0"()
        }

        given:
        buildFile << """
            dependencies {
                conf 'org:member2:1.1'
            }
        """
        and:
        "align the 'org' group only"()

        when:
        expectAlignment {
            module('member1') alignsTo('1.1') byVirtualPlatform()
            module('member2') alignsTo('1.1') byVirtualPlatform()
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                module("org:member2:1.1") {
                    module("org:member1:1.1")
                }
            }
        }

    }

    @Issue("gradle/gradle#7916")
    def "shouldn't fail when a referenced component is a virtual platform"() {
        repository {
            'org:foo:1.0'()
            'org:foo:1.1'()
        }

        given:
        buildFile << '''
            dependencies {
              constraints {
                  conf "org:platform:1.1"
              }

              conf 'org:foo:1.0'
            }
        '''

        and:
        "align the 'org' group only"()

        when:
        expectAlignment {
            module('foo') tries('1.0') alignsTo('1.1') byVirtualPlatform()
        }
        run ':checkDeps', 'dependencyInsight', '--configuration', 'conf', '--dependency', 'foo'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge("org:foo:1.0", "org:foo:1.1") {
                    byConstraint("belongs to platform org:platform:1.1")
                }
            }
            virtualConfiguration("org:platform:1.1")
        }
    }

    // We only need to test one flavor
    @RequiredFeature(feature = GradleMetadataResolveRunner.GRADLE_METADATA, value = "true")
    @RequiredFeature(feature = GradleMetadataResolveRunner.REPOSITORY_TYPE, value = "maven")
    def "should manage to realign through two conflicts"() {
        repository {
            path 'start:start:1.0 -> foo:1.0'

            path 'foo:1.0 -> bar:1.0'
            path 'foo:1.1 -> bar:1.1'

            'org:bar:1.0'()
            'org:bar:1.1'()
        }

        given:
        buildFile << '''
            dependencies {
              constraints {
                  conf "org:platform:1.1"
              }

              conf 'start:start:1.0'
            }
        '''

        and:
        "align the 'org' group only"()

        when:
        expectAlignment {
            module('start') group('start') alignsTo('1.0')
            module('foo') tries('1.0') alignsTo('1.1') byVirtualPlatform()
            module('bar') tries('1.0') alignsTo('1.1') byVirtualPlatform()
        }
        run ':checkDeps', 'dependencyInsight', '--configuration', 'conf', '--dependency', 'bar'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                module("start:start:1.0") {
                    edge("org:foo:1.0", "org:foo:1.1") {
                        byConstraint("belongs to platform org:platform:1.1")
                        module("org:bar:1.1") {
                            byConstraint("belongs to platform org:platform:1.1")
                        }
                    }
                }
            }
            virtualConfiguration("org:platform:1.1")
        }
    }
}
