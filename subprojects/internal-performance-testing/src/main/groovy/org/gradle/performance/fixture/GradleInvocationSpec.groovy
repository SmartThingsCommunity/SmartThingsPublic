/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.performance.fixture

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.gradle.initialization.ParallelismBuildOptions
import org.gradle.integtests.fixtures.executer.GradleDistribution

@CompileStatic
@EqualsAndHashCode
class GradleInvocationSpec implements InvocationSpec {

    final GradleDistribution gradleDistribution
    final File workingDirectory
    final List<String> tasksToRun
    final List<String> args
    final List<String> jvmOpts
    final List<String> cleanTasks
    final boolean useDaemon
    final boolean useToolingApi
    final boolean expectFailure

    GradleInvocationSpec(GradleDistribution gradleDistribution, File workingDirectory, List<String> tasksToRun, List<String> args, List<String> jvmOpts, List<String> cleanTasks, boolean useDaemon, boolean useToolingApi, boolean expectFailure) {
        this.gradleDistribution = gradleDistribution
        this.workingDirectory = workingDirectory
        this.tasksToRun = tasksToRun
        this.args = args
        this.jvmOpts = jvmOpts
        this.cleanTasks = cleanTasks
        this.useDaemon = useDaemon
        this.useToolingApi = useToolingApi
        this.expectFailure = expectFailure
    }

    boolean getBuildWillRunInDaemon() {
        return useDaemon || useToolingApi
    }

    static InvocationBuilder builder() {
        return new InvocationBuilder()
    }

    InvocationBuilder withBuilder() {
        InvocationBuilder builder = new InvocationBuilder()
        builder.distribution(gradleDistribution)
        builder.workingDirectory(workingDirectory)
        builder.tasksToRun.addAll(this.tasksToRun)
        builder.args.addAll(args)
        builder.gradleOptions.addAll(jvmOpts)
        builder.cleanTasks.addAll(cleanTasks)
        builder.useDaemon = useDaemon
        builder.useToolingApi = useToolingApi
        builder.expectFailure = expectFailure
        builder
    }

    GradleInvocationSpec withAdditionalJvmOpts(List<String> additionalJvmOpts) {
        InvocationBuilder builder = withBuilder()
        builder.gradleOptions.addAll(additionalJvmOpts)
        return builder.build()
    }

    GradleInvocationSpec withAdditionalArgs(List<String> additionalArgs) {
        InvocationBuilder builder = withBuilder()
        builder.args.addAll(additionalArgs)
        return builder.build()
    }

    static class InvocationBuilder implements InvocationSpec.Builder {
        GradleDistribution gradleDistribution
        File workingDirectory
        List<String> tasksToRun = []
        List<String> args = []
        List<String> gradleOptions = []
        List<String> cleanTasks = []
        boolean useDaemon = true
        boolean useToolingApi
        boolean expectFailure


        InvocationBuilder distribution(GradleDistribution gradleDistribution) {
            this.gradleDistribution = gradleDistribution
            this
        }

        InvocationBuilder workingDirectory(File workingDirectory) {
            this.workingDirectory = workingDirectory
            this
        }

        InvocationBuilder tasksToRun(String... taskToRun) {
            this.tasksToRun.addAll(Arrays.asList(taskToRun))
            this
        }

        InvocationBuilder tasksToRun(Iterable<String> taskToRun) {
            this.tasksToRun.addAll(taskToRun)
            this
        }

        InvocationBuilder args(String... args) {
            this.args.addAll(Arrays.asList(args))
            this
        }

        InvocationBuilder gradleOpts(String... gradleOpts) {
            this.gradleOptions.addAll(Arrays.asList(gradleOpts))
            this
        }
        InvocationBuilder gradleOpts(Iterable<String> gradleOpts) {
            this.gradleOptions.addAll(gradleOpts)
            this
        }

        InvocationBuilder cleanTasks(String... cleanTasks) {
            this.cleanTasks(Arrays.asList(cleanTasks))
        }

        InvocationBuilder cleanTasks(Iterable<String> cleanTasks) {
            this.cleanTasks.addAll(cleanTasks)
            this
        }

        InvocationBuilder useDaemon(boolean flag) {
            this.useDaemon = flag
            this
        }

        InvocationBuilder useToolingApi() {
            useToolingApi(true)
            this
        }

        InvocationBuilder useToolingApi(boolean flag) {
            this.useToolingApi = flag
            this
        }

        InvocationBuilder disableParallelWorkers() {
            gradleOpts("-D${ParallelismBuildOptions.MaxWorkersOption.GRADLE_PROPERTY}=1")
        }

        @Override
        InvocationSpec.Builder expectFailure() {
            expectFailure = true
            this
        }

        GradleInvocationSpec build() {
            assert gradleDistribution != null
            assert workingDirectory != null

            return new GradleInvocationSpec(gradleDistribution, workingDirectory, tasksToRun.asImmutable(), args.asImmutable(), gradleOptions.asImmutable(), cleanTasks.asImmutable(), useDaemon, useToolingApi, expectFailure)
        }

    }
}
