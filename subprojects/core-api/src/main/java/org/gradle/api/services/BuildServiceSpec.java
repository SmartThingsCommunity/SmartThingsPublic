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

package org.gradle.api.services;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.provider.Property;

/**
 * A set of parameters that defines a service registration.
 *
 * @param <P> The type of parameters to inject into the service implementation.
 * @since 6.1
 */
@Incubating
public interface BuildServiceSpec<P extends BuildServiceParameters> {
    /**
     * Returns the parameters to will be used to create the service instance.
     */
    P getParameters();

    /**
     * Runs the given action to configure the parameters.
     */
    void parameters(Action<? super P> configureAction);

    /**
     * Specifies the maximum number of tasks that can use this service in parallel. Setting this to 1 means that the service will be used by a single task at a time.
     * When this property has no value defined, then any number of tasks may use this service in parallel. This is the default.
     */
    Property<Integer> getMaxParallelUsages();
}
