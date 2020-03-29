/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.performance.fixture;

import org.gradle.performance.measure.MeasuredOperation;

import java.io.File;

public interface BuildExperimentListener {
    void beforeExperiment(BuildExperimentSpec experimentSpec, File projectDir);

    void beforeInvocation(BuildExperimentInvocationInfo invocationInfo);

    void afterInvocation(BuildExperimentInvocationInfo invocationInfo, MeasuredOperation operation, MeasurementCallback measurementCallback);

    interface MeasurementCallback {
        void omitMeasurement();
    }

    static BuildExperimentListener compose(BuildExperimentListener... listeners) {
        return new BuildExperimentListener() {
            @Override
            public void beforeExperiment(BuildExperimentSpec experimentSpec, File projectDir) {
                for (BuildExperimentListener listener : listeners) {
                    listener.beforeExperiment(experimentSpec, projectDir);
                }
            }

            @Override
            public void beforeInvocation(BuildExperimentInvocationInfo invocationInfo) {
                for (BuildExperimentListener listener : listeners) {
                    listener.beforeInvocation(invocationInfo);
                }
            }

            @Override
            public void afterInvocation(BuildExperimentInvocationInfo invocationInfo, MeasuredOperation operation, MeasurementCallback measurementCallback) {
                for (BuildExperimentListener listener : listeners) {
                    listener.afterInvocation(invocationInfo, operation, measurementCallback);
                }
            }
        };
    }

}
