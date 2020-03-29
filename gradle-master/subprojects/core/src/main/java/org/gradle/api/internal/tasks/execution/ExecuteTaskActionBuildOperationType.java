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

package org.gradle.api.internal.tasks.execution;

import org.gradle.internal.operations.BuildOperationType;
import org.gradle.internal.scan.UsedByScanPlugin;

/**
 * Executing a task action.
 *
 * @since 5.6
 */
@UsedByScanPlugin
public final class ExecuteTaskActionBuildOperationType implements BuildOperationType<ExecuteTaskActionBuildOperationType.Details, ExecuteTaskActionBuildOperationType.Result> {

    // Info about the owning task can be inferred, and we don't provide any further info at this point.
    // This is largely to expose timing information about executed tasks

    public interface Details {
    }

    public interface Result {
    }

    static final Details DETAILS_INSTANCE = new Details() {};
    static final Result RESULT_INSTANCE = new Result() {};
}
