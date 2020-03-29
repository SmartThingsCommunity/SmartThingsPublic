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

package org.gradle.api.internal.changedetection.changes;

import org.gradle.api.Action;
import org.gradle.api.tasks.incremental.InputFileDetails;

public class RebuildIncrementalTaskInputs extends StatefulIncrementalTaskInputs {
    private final Iterable<InputFileDetails> inputChanges;

    public RebuildIncrementalTaskInputs(Iterable<InputFileDetails> inputChanges) {
        this.inputChanges = inputChanges;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void doOutOfDate(final Action<? super InputFileDetails> outOfDateAction) {
        for (InputFileDetails inputFileChange : inputChanges) {
            outOfDateAction.execute(inputFileChange);
        }
    }

    @Override
    public void doRemoved(Action<? super InputFileDetails> removedAction) {
    }
}
