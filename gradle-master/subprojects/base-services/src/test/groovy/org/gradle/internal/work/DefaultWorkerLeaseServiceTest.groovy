/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.internal.work

import org.gradle.api.Action
import org.gradle.internal.Factory
import org.gradle.internal.concurrent.DefaultParallelismConfiguration
import org.gradle.internal.concurrent.ParallelismConfigurationManager
import org.gradle.internal.concurrent.ParallelismConfigurationManagerFixture
import org.gradle.internal.resources.DefaultResourceLockCoordinationService
import org.gradle.internal.resources.ResourceLockCoordinationService
import org.gradle.internal.resources.TestTrackedResourceLock
import spock.lang.Specification


class DefaultWorkerLeaseServiceTest extends Specification {
    def coordinationService = new DefaultResourceLockCoordinationService()
    def workerLeaseService = new DefaultWorkerLeaseService(coordinationService, new ParallelismConfigurationManagerFixture(true, 1))

    def "can use withLocks to execute a runnable with resources locked"() {
        boolean executed = false
        def lock1 = resourceLock("lock1", false)
        def lock2 = resourceLock("lock1", false)

        when:
        workerLeaseService.withLocks([lock1, lock2], runnable {
            assert lock1.lockedState
            assert lock2.lockedState
            assert lock1.doIsLockedByCurrentThread()
            assert lock2.doIsLockedByCurrentThread()
            executed = true
        })

        then:
        executed

        and:
        !lock1.lockedState
        !lock2.lockedState
    }

    def "can use withLocks to execute a callable with resources locked"() {
        boolean executed = false
        def lock1 = resourceLock("lock1", false)
        def lock2 = resourceLock("lock2", false)

        when:
        executed = workerLeaseService.withLocks([lock1, lock2], factory {
            assert lock1.lockedState
            assert lock2.lockedState
            assert lock1.doIsLockedByCurrentThread()
            assert lock2.doIsLockedByCurrentThread()
            return true
        })

        then:
        executed

        and:
        !lock1.lockedState
        !lock2.lockedState
    }

    def "can use withoutLocks to execute a runnable with locks temporarily released"() {
        boolean executed = false
        def lock1 = resourceLock("lock1", false)
        def lock2 = resourceLock("lock2", false)

        when:
        workerLeaseService.withLocks([lock1, lock2]) {
            assert lock1.lockedState
            assert lock2.lockedState
            workerLeaseService.withoutLocks([lock1, lock2], runnable {
                assert !lock1.lockedState
                assert !lock2.lockedState
                assert !lock1.doIsLockedByCurrentThread()
                assert !lock2.doIsLockedByCurrentThread()
                executed = true
            })
            assert lock1.lockedState
            assert lock2.lockedState
        }

        then:
        executed

        and:
        !lock1.lockedState
        !lock2.lockedState
    }

    def "can use withoutLocks to execute a callable with locks temporarily released"() {
        boolean executed = false
        def lock1 = resourceLock("lock1", false)
        def lock2 = resourceLock("lock2", false)

        when:
        workerLeaseService.withLocks([lock1, lock2]) {
            assert lock1.lockedState
            assert lock2.lockedState
            executed = workerLeaseService.withoutLocks([lock1, lock2], factory {
                assert !lock1.lockedState
                assert !lock2.lockedState
                assert !lock1.doIsLockedByCurrentThread()
                assert !lock2.doIsLockedByCurrentThread()
                return true
            })
            assert lock1.lockedState
            assert lock2.lockedState
        }

        then:
        executed

        and:
        !lock1.lockedState
        !lock2.lockedState
    }

    def "throws an exception from withoutLocks when locks are not currently held"() {
        boolean executed = false
        def lock1 = resourceLock("lock1", false)
        def lock2 = resourceLock("lock2", false)

        when:
        workerLeaseService.withLocks([lock1]) {
            assert lock1.lockedState
            assert !lock2.lockedState
            workerLeaseService.withoutLocks([lock1, lock2], runnable {
                executed = true
            })
            assert lock1.lockedState
            assert !lock2.lockedState
        }

        then:
        thrown(IllegalStateException)
        !executed

        and:
        !lock1.lockedState
        !lock2.lockedState
    }

    def "registers/deregisters a listener for parallelism configuration changes"() {
        ParallelismConfigurationManager parallelExecutionManager = new ParallelismConfigurationManagerFixture(true, 1)

        when:
        workerLeaseService = new DefaultWorkerLeaseService(Mock(ResourceLockCoordinationService), parallelExecutionManager)

        then:
        parallelExecutionManager.listeners.size() == 1

        when:
        workerLeaseService.stop()

        then:
        parallelExecutionManager.listeners.size() == 0
    }

    def "adjusts max worker count on parallelism configuration change"() {
        when:
        workerLeaseService.onParallelismConfigurationChange(new DefaultParallelismConfiguration(true, 2))

        then:
        workerLeaseService.getMaxWorkerCount() == 2

        when:
        workerLeaseService.onParallelismConfigurationChange(new DefaultParallelismConfiguration(false, 4))

        then:
        workerLeaseService.getMaxWorkerCount() == 4
    }

    TestTrackedResourceLock resourceLock(String displayName, boolean locked, boolean hasLock=false) {
        return new TestTrackedResourceLock(displayName, coordinationService, Mock(Action), Mock(Action), locked, hasLock)
    }

    TestTrackedResourceLock resourceLock(String displayName) {
        return resourceLock(displayName, false)
    }

    Runnable runnable(Closure closure) {
        return new Runnable() {
            @Override
            void run() {
                closure.run()
            }
        }
    }

    Factory factory(Closure closure) {
        return new Factory() {
            @Override
            Object create() {
                return closure.call()
            }
        }
    }
}
