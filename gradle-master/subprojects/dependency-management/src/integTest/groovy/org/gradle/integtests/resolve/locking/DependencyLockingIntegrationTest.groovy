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

package org.gradle.integtests.resolve.locking

import org.gradle.api.artifacts.dsl.LockMode
import org.gradle.integtests.fixtures.FeaturePreviewsFixture
import org.gradle.integtests.fixtures.ToBeFixedForInstantExecution
import spock.lang.Unroll

class DependencyLockingIntegrationTest extends AbstractValidatingLockingIntegrationTest {

    @Override
    LockMode lockMode() {
        LockMode.DEFAULT
    }

    @ToBeFixedForInstantExecution
    @Unroll
    def 'succeeds without lock file present and does not create one'() {
        mavenRepo.module('org', 'foo', '1.0').publish()

        if (unique) {
            FeaturePreviewsFixture.enableOneLockfilePerProject(settingsFile)
        }
        buildFile << """
dependencyLocking {
    lockAllConfigurations()
}

repositories {
    maven {
        name 'repo'
        url '${mavenRepo.uri}'
    }
}
configurations {
    unlockedConf
}

dependencies {
    unlockedConf 'org:foo:1.+'
}
"""

        when:
        succeeds 'dependencies'

        then:
        lockfileFixture.expectLockStateMissing('unlockedConf', unique)

        where:
        unique << [true, false]
    }

    @Unroll
    def "version selector combinations are resolved equally for locked and unlocked configurations"() {
        ['foo', 'foz', 'bar', 'baz'].each { artifact ->
            mavenRepo.module('org', artifact, '1.0').publish()
            mavenRepo.module('org', artifact, '1.1').publish()
            mavenRepo.module('org', artifact, '1.2').publish()
            mavenRepo.module('org', artifact, '2.0').publish()
        }

        if (unique) {
            FeaturePreviewsFixture.enableOneLockfilePerProject(settingsFile)
        }
        buildFile << """
repositories {
    maven {
        url '${mavenRepo.uri}'
    }
}
configurations {
    conf
    lockEnabledConf {
        extendsFrom conf
        resolutionStrategy.activateDependencyLocking()
    }
}
dependencies {
    conf 'org:foo:[1.0,)'
    conf 'org:foo:1.1'

    conf 'org:foz:latest.integration'
    conf 'org:foz:1.1'

    conf 'org:bar:1.+'
    conf 'org:bar:1.1'

    conf 'org:baz:+'
    conf 'org:baz:1.1'
}
task check {
    doLast {
        assert configurations.conf*.name == configurations.lockEnabledConf*.name
    }
}
"""

        expect:
        succeeds 'check'

        where:
        unique << [true, false]
    }

    @ToBeFixedForInstantExecution
    @Unroll
    def 'writes a new lock file if update done without lockfile present'() {
        mavenRepo.module('org', 'foo', '1.0').publish()
        mavenRepo.module('org', 'foo', '1.1').publish()
        mavenRepo.module('org', 'bar', '1.0').publish()
        mavenRepo.module('org', 'bar', '1.1').publish()

        if (unique) {
            FeaturePreviewsFixture.enableOneLockfilePerProject(settingsFile)
        }
        buildFile << """
dependencyLocking {
    lockAllConfigurations()
}

repositories {
    maven {
        name 'repo'
        url '${mavenRepo.uri}'
    }
}
configurations {
    lockedConf
}

dependencies {
    lockedConf 'org:foo:[1.0,2.0)'
    lockedConf 'org:bar:[1.0,2.0)'
}
"""

        when:
        succeeds 'dependencies', '--update-locks', 'org:foo'

        then:
        lockfileFixture.verifyLockfile('lockedConf', ['org:foo:1.1', 'org:bar:1.1'], unique)

        where:
        unique << [true, false]
    }

    @ToBeFixedForInstantExecution
    @Unroll
    def 'does not write an empty lock file for an empty configuration if not requested'() {
        if (unique) {
            FeaturePreviewsFixture.enableOneLockfilePerProject(settingsFile)
        }
        buildFile << """
dependencyLocking {
    lockAllConfigurations()
}

repositories {
    maven {
        name 'repo'
        url '${mavenRepo.uri}'
    }
}
configurations {
    lockedConf
}
"""
        when:
        succeeds 'dependencies'

        then:
        lockfileFixture.expectLockStateMissing('lockedConf', unique)

        where:
        unique << [true, false]
    }

    def 'attempting to change lock mode after a configuration has been resolved is invalid'() {
        buildFile << """
dependencyLocking {
    lockAllConfigurations()
}

repositories {
    maven {
        name 'repo'
        url '${mavenRepo.uri}'
    }
}
configurations {
    lockedConf
}

task doIt {
    doLast {
        println configurations.lockedConf.files
        dependencyLocking {
            lockMode = LockMode.STRICT
        }
    }
}
"""
        when:
        fails 'doIt'

        then:
        failureHasCause("The value for property 'lockMode' is final and cannot be changed any further.")
    }

    @ToBeFixedForInstantExecution
    def 'can use a custom file location for reading and writing per project lock state'() {
        given:
        mavenRepo.module('org', 'foo', '1.0').publish()
        mavenRepo.module('org', 'foo', '1.1').publish()
        mavenRepo.module('org', 'bar', '1.0').publish()
        mavenRepo.module('org', 'bar', '1.1').publish()

        FeaturePreviewsFixture.enableOneLockfilePerProject(settingsFile)
        buildFile << """
dependencyLocking {
    lockAllConfigurations()
    lockFile = file("\$projectDir/gradle/lock.file")
}

repositories {
    maven {
        name 'repo'
        url '${mavenRepo.uri}'
    }
}
configurations {
    lockedConf
}

dependencies {
    lockedConf 'org:foo:[1.0,2.0)'
    lockedConf 'org:bar:[1.0,2.0)'
}
"""
        def lockFile = testDirectory.file('gradle', 'lock.file')
        LockfileFixture.createCustomLockfile(lockFile,'lockedConf', ['org:foo:1.0', 'org:bar:1.0'])

        when:
        succeeds 'dependencies'

        then:
        outputContains('org:foo:[1.0,2.0) -> 1.0')
        outputContains('org:bar:[1.0,2.0) -> 1.0')

        when:
        succeeds 'dependencies', '--update-locks', 'org:foo', '--refresh-dependencies'

        then:
        LockfileFixture.verifyCustomLockfile(lockFile, 'lockedConf', ['org:foo:1.1', 'org:bar:1.0'])

    }

}
