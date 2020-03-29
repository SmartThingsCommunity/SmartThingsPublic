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
package org.gradle.integtests.fixtures.executer;

import org.hamcrest.Matcher;

public interface ExecutionFailure extends ExecutionResult {
    /**
     * {@inheritDoc}
     */
    @Override
    ExecutionFailure getIgnoreBuildSrc();

    ExecutionFailure assertHasLineNumber(int lineNumber);

    ExecutionFailure assertHasFileName(String filename);

    /**
     * Asserts that the given number of failures are present.
     */
    ExecutionFailure assertHasFailures(int count);

    /**
     * Asserts that the reported failure has the given cause (ie the bit after the description).
     *
     * <p>Error messages are normalized to use new-line char as line separator.
     */
    ExecutionFailure assertHasCause(String description);

    /**
     * Asserts that the reported failure has the given cause (ie the bit after the description).
     *
     * <p>Error messages are normalized to use new-line char as line separator.
     */
    ExecutionFailure assertThatCause(Matcher<? super String> matcher);

    /**
     * Asserts that the reported failure has the given description (ie the bit after '* What went wrong').
     *
     * <p>Error messages are normalized to use new-line char as line separator.
     */
    ExecutionFailure assertHasDescription(String context);

    /**
     * Asserts that the reported failure has the given description (ie the bit after '* What went wrong').
     *
     * <p>Error messages are normalized to use new-line char as line separator.
     */
    ExecutionFailure assertThatDescription(Matcher<? super String> matcher);

    /**
     * Asserts that the reported failure has the given resolution (ie the bit after '* Try').
     */
    ExecutionFailure assertHasResolution(String resolution);

    /**
     * Asserts that there is no exception that <em>contains</em> the given description.
     */
    ExecutionFailure assertHasNoCause(String description);

    ExecutionFailure assertHasNoCause();

    ExecutionFailure assertTestsFailed();

    /**
     * @param configurationPath, for example ':compile'
     */
    DependencyResolutionFailure assertResolutionFailure(String configurationPath);
}
