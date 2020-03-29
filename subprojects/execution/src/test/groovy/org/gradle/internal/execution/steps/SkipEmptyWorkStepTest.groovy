/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.internal.execution.steps

import com.google.common.collect.ImmutableSortedMap
import org.gradle.internal.execution.AfterPreviousExecutionContext
import org.gradle.internal.execution.CachingResult
import org.gradle.internal.execution.ExecutionOutcome
import org.gradle.internal.execution.history.AfterPreviousExecutionState
import org.gradle.internal.execution.history.ExecutionHistoryStore
import org.gradle.internal.fingerprint.FileCollectionFingerprint
import spock.lang.Unroll

class SkipEmptyWorkStepTest extends StepSpec<AfterPreviousExecutionContext> {
    def step = new SkipEmptyWorkStep<>(delegate)
    def afterPreviousExecutionState = Mock(AfterPreviousExecutionState)

    def delegateResult = Mock(CachingResult)
    def outputFingerprints = ImmutableSortedMap.<String, FileCollectionFingerprint>of()
    def executionHistoryStore = Mock(ExecutionHistoryStore)

    @Override
    protected AfterPreviousExecutionContext createContext() {
        Stub(AfterPreviousExecutionContext)
    }

    def setup() {
        _ * work.executionHistoryStore >> Optional.of(executionHistoryStore)
    }

    def "delegates when work is not skipped"() {
        when:
        def result = step.execute(context)

        then:
        result == delegateResult

        _ * context.afterPreviousExecutionState >> Optional.of(afterPreviousExecutionState)
        1 * afterPreviousExecutionState.outputFileProperties >> outputFingerprints
        _ * work.skipIfInputsEmpty(outputFingerprints) >> Optional.empty()

        then:
        1 * delegate.execute(context) >> delegateResult
        0 * _
    }

    @Unroll
    def "removes execution history when empty work is skipped (outcome: #outcome)"() {
        when:
        def result = step.execute(context)

        then:
        result.outcome.get() == outcome

        _ * context.afterPreviousExecutionState >> Optional.of(afterPreviousExecutionState)
        1 * afterPreviousExecutionState.outputFileProperties >> outputFingerprints
        _ * work.skipIfInputsEmpty(outputFingerprints) >> Optional.of(outcome)

        then:
        1 * executionHistoryStore.remove(identity)
        0 * _

        where:
        outcome << [ExecutionOutcome.SHORT_CIRCUITED, ExecutionOutcome.EXECUTED_NON_INCREMENTALLY]
    }
}
