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

package org.gradle.initialization.buildsrc;

import org.gradle.internal.operations.BuildOperationType;
import org.gradle.internal.scan.UsedByScanPlugin;

/**
 * Building the buildSrc project.
 *
 * @since 4.3
 */
public final class BuildBuildSrcBuildOperationType implements BuildOperationType<BuildBuildSrcBuildOperationType.Details, BuildBuildSrcBuildOperationType.Result> {

    @UsedByScanPlugin
    public interface Details {
        /**
         * Returns the path of the _containing_ build.
         * @since 4.6
         */
        String getBuildPath();
    }

    @UsedByScanPlugin
    public interface Result {
    }

    private BuildBuildSrcBuildOperationType(){
    }
}
