/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.profile;

import org.gradle.BuildResult;
import org.gradle.api.Describable;
import org.gradle.api.Project;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.ProjectState;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.artifacts.transform.ArtifactTransformListener;
import org.gradle.api.internal.project.taskfactory.TaskIdentity;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.TaskState;
import org.gradle.execution.taskgraph.TaskListenerInternal;
import org.gradle.initialization.BuildCompletionListener;
import org.gradle.internal.InternalBuildListener;
import org.gradle.internal.buildevents.BuildStartedTime;
import org.gradle.internal.time.Clock;

/**
 * Adapts various events to build a {@link BuildProfile} model, and then notifies a {@link ReportGeneratingProfileListener} when the model is ready.
 */
public class ProfileEventAdapter implements InternalBuildListener, ProjectEvaluationListener, TaskListenerInternal, DependencyResolutionListener, BuildCompletionListener, ArtifactTransformListener {
    private final BuildStartedTime buildStartedTime;
    private final Clock clock;
    private final ProfileListener listener;
    private final ThreadLocal<ContinuousOperation> currentTransformation = new ThreadLocal<ContinuousOperation>();
    private BuildProfile buildProfile;

    public ProfileEventAdapter(BuildStartedTime buildStartedTime, Clock clock, ProfileListener listener) {
        this.buildStartedTime = buildStartedTime;
        this.clock = clock;
        this.listener = listener;
    }

    // BuildListener
    @Override
    public void buildStarted(Gradle gradle) {
        long now = clock.getCurrentTime();
        buildProfile = new BuildProfile(gradle.getStartParameter());
        buildProfile.setBuildStarted(now);
        buildProfile.setProfilingStarted(buildStartedTime.getStartTime());
    }

    @Override
    public void settingsEvaluated(Settings settings) {
        buildProfile.setSettingsEvaluated(clock.getCurrentTime());
    }

    @Override
    public void projectsLoaded(Gradle gradle) {
        buildProfile.setProjectsLoaded(clock.getCurrentTime());
    }

    @Override
    public void projectsEvaluated(Gradle gradle) {
        buildProfile.setProjectsEvaluated(clock.getCurrentTime());
    }

    @Override
    public void buildFinished(BuildResult result) {
        buildProfile.setSuccessful(result.getFailure() == null);
    }

    @Override
    public void completed() {
        if (buildProfile != null) {
            buildProfile.setBuildFinished(clock.getCurrentTime());
            try {
                listener.buildFinished(buildProfile);
            } finally {
                buildProfile = null;
            }
        }
    }

    // ProjectEvaluationListener
    @Override
    public void beforeEvaluate(Project project) {
        long now = clock.getCurrentTime();
        buildProfile.getProjectProfile(project.getPath()).getConfigurationOperation().setStart(now);
    }

    @Override
    public void afterEvaluate(Project project, ProjectState state) {
        long now = clock.getCurrentTime();
        ProjectProfile projectProfile = buildProfile.getProjectProfile(project.getPath());
        projectProfile.getConfigurationOperation().setFinish(now);
    }

    // TaskListenerInternal
    @Override
    public void beforeExecute(TaskIdentity taskIdentity) {
        long now = clock.getCurrentTime();
        ProjectProfile projectProfile = buildProfile.getProjectProfile(taskIdentity.getProjectPath());
        projectProfile.getTaskProfile(taskIdentity.getTaskPath()).setStart(now);
    }

    @Override
    public void afterExecute(TaskIdentity taskIdentity, TaskState state) {
        long now = clock.getCurrentTime();
        ProjectProfile projectProfile = buildProfile.getProjectProfile(taskIdentity.getProjectPath());
        TaskExecution taskExecution = projectProfile.getTaskProfile(taskIdentity.getTaskPath());
        taskExecution.setFinish(now);
        taskExecution.completed(state);
    }

    // DependencyResolutionListener
    @Override
    public void beforeResolve(ResolvableDependencies dependencies) {
        long now = clock.getCurrentTime();
        buildProfile.getDependencySetProfile(dependencies.getPath()).setStart(now);
    }

    @Override
    public void afterResolve(ResolvableDependencies dependencies) {
        long now = clock.getCurrentTime();
        buildProfile.getDependencySetProfile(dependencies.getPath()).setFinish(now);
    }

    @Override
    public void beforeTransformerInvocation(Describable transformer, Describable subject) {
        long now = clock.getCurrentTime();
        String transformationDescription = subject.getDisplayName() + " with " + transformer.getDisplayName();
        FragmentedOperation transformationProfile = buildProfile.getTransformationProfile(transformationDescription);
        currentTransformation.set(transformationProfile.start(now));
    }

    @Override
    public void afterTransformerInvocation(Describable transformer, Describable subject) {
        long now = clock.getCurrentTime();
        currentTransformation.get().setFinish(now);
        currentTransformation.remove();
    }
}
