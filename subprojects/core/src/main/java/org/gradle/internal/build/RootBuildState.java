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

package org.gradle.internal.build;

import org.gradle.StartParameter;
import org.gradle.api.Transformer;
import org.gradle.internal.invocation.BuildController;

/**
 * Represents the root build of a build tree.
 */
public interface RootBuildState extends BuildState {
    /**
     * Returns the start parameter used to define this build.
     */
    StartParameter getStartParameter();

    /**
     * Runs a single invocation of this build, executing the given action and returning the result. Should be called once only for a given build instance.
     */
    <T> T run(Transformer<T, ? super BuildController> buildAction);
}
