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

package org.gradle.tooling.internal.provider;

import org.gradle.tooling.internal.protocol.PhasedActionResult;
import org.gradle.tooling.internal.provider.serialization.SerializedPayload;

import java.io.Serializable;

/**
 * Result of one of the actions of a phased action. Must be serializable since will be dispatched to client.
 */
public class PhasedBuildActionResult implements Serializable {
    public final SerializedPayload result;
    public final PhasedActionResult.Phase phase;

    public PhasedBuildActionResult(SerializedPayload result, PhasedActionResult.Phase phase) {
        this.result = result;
        this.phase = phase;
    }
}
