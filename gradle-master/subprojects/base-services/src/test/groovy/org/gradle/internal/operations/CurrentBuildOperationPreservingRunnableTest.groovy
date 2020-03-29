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

package org.gradle.internal.operations

import spock.lang.Specification

class CurrentBuildOperationPreservingRunnableTest extends Specification {

    private static final EXPECTED_BUILD_OPERATION = new DefaultBuildOperationRef(new OperationIdentifier(42L), new OperationIdentifier(1))

    def delegate = Mock(Runnable)
    def currentBuildOperationRef = new CurrentBuildOperationRef()

    def runner() {
        new CurrentBuildOperationPreservingRunnable(delegate, currentBuildOperationRef)
    }

    def "forward execution to delegate runnable"() {
        when:
        runner().run()

        then:
        1 * delegate.run()
        0 * _
    }

    def "preserve build operation identifier during execution of the delegate runnable"() {
        given:
        currentBuildOperationRef.set(EXPECTED_BUILD_OPERATION)

        when:
        runner().run()

        then:
        1 * delegate.run() >> {
            assert currentBuildOperationRef.get() == EXPECTED_BUILD_OPERATION
        }
        currentBuildOperationRef.get() != EXPECTED_BUILD_OPERATION
    }
}
