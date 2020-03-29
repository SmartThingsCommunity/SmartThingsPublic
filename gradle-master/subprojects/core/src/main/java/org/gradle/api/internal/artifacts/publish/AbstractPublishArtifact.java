/*
 * Copyright 2007-2009 the original author or authors.
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
package org.gradle.api.internal.artifacts.publish;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.internal.artifacts.PublishArtifactInternal;
import org.gradle.api.internal.tasks.DefaultTaskDependency;
import org.gradle.api.internal.tasks.TaskResolver;
import org.gradle.api.tasks.TaskDependency;

import javax.annotation.Nullable;

public abstract class AbstractPublishArtifact implements PublishArtifactInternal {
    private final DefaultTaskDependency taskDependency;

    public AbstractPublishArtifact(@Nullable TaskResolver resolver, Object... tasks) {
        taskDependency = new DefaultTaskDependency(resolver, ImmutableSet.copyOf(tasks));
    }

    public AbstractPublishArtifact(Object... tasks) {
        this(null, tasks);
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return taskDependency;
    }

    public AbstractPublishArtifact builtBy(Object... tasks) {
        taskDependency.add(tasks);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + getName() + ":" + getType() + ":" +getExtension()  + ":" + getClassifier();
    }
}
