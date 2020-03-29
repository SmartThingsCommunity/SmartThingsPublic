/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.tasks;

import org.gradle.api.Incubating;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.gradle.process.ProcessForkOptions;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * {@code AbstractExecTask} is the base class for all exec tasks.
 *
 * @param <T> The concrete type of the class.
 */
public abstract class AbstractExecTask<T extends AbstractExecTask> extends ConventionTask implements ExecSpec {
    private final Class<T> taskType;
    private final Property<ExecResult> execResult;
    private ExecAction execAction;

    public AbstractExecTask(Class<T> taskType) {
        execAction = getExecActionFactory().newExecAction();
        execResult = getObjectFactory().property(ExecResult.class);
        this.taskType = taskType;
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    protected void exec() {
        execResult.set(execAction.execute());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T commandLine(Object... arguments) {
        execAction.commandLine(arguments);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T commandLine(Iterable<?> args) {
        execAction.commandLine(args);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T args(Object... args) {
        execAction.args(args);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T args(Iterable<?> args) {
        execAction.args(args);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T setArgs(List<String> arguments) {
        execAction.setArgs(arguments);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T setArgs(@Nullable Iterable<?> arguments) {
        execAction.setArgs(arguments);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Optional
    @Input
    @Override
    public List<String> getArgs() {
        return execAction.getArgs();
    }

    /**
     * {@inheritDoc}
     */
    @Nested
    @Override
    public List<CommandLineArgumentProvider> getArgumentProviders() {
        return execAction.getArgumentProviders();
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    @Override
    public List<String> getCommandLine() {
        return execAction.getCommandLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCommandLine(List<String> args) {
        execAction.setCommandLine(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCommandLine(Iterable<?> args) {
        execAction.setCommandLine(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCommandLine(Object... args) {
        execAction.setCommandLine(args);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Optional
    @Input
    @Override
    public String getExecutable() {
        return execAction.getExecutable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutable(@Nullable String executable) {
        execAction.setExecutable(executable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutable(Object executable) {
        execAction.setExecutable(executable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T executable(Object executable) {
        execAction.executable(executable);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Internal
    // TODO:LPTR Should be a content-less @InputDirectory
    public File getWorkingDir() {
        return execAction.getWorkingDir();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkingDir(File dir) {
        execAction.setWorkingDir(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkingDir(Object dir) {
        execAction.setWorkingDir(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T workingDir(Object dir) {
        execAction.workingDir(dir);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    @Override
    public Map<String, Object> getEnvironment() {
        return execAction.getEnvironment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnvironment(Map<String, ?> environmentVariables) {
        execAction.setEnvironment(environmentVariables);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T environment(String name, Object value) {
        execAction.environment(name, value);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T environment(Map<String, ?> environmentVariables) {
        execAction.environment(environmentVariables);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T copyTo(ProcessForkOptions target) {
        execAction.copyTo(target);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T setStandardInput(InputStream inputStream) {
        execAction.setStandardInput(inputStream);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    @Override
    public InputStream getStandardInput() {
        return execAction.getStandardInput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T setStandardOutput(OutputStream outputStream) {
        execAction.setStandardOutput(outputStream);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    @Override
    public OutputStream getStandardOutput() {
        return execAction.getStandardOutput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T setErrorOutput(OutputStream outputStream) {
        execAction.setErrorOutput(outputStream);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    @Override
    public OutputStream getErrorOutput() {
        return execAction.getErrorOutput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T setIgnoreExitValue(boolean ignoreExitValue) {
        execAction.setIgnoreExitValue(ignoreExitValue);
        return taskType.cast(this);
    }

    /**
     * {@inheritDoc}
     */
    @Input
    @Override
    public boolean isIgnoreExitValue() {
        return execAction.isIgnoreExitValue();
    }

    void setExecAction(ExecAction execAction) {
        this.execAction = execAction;
    }

    /**
     * Returns the result for the command run by this task. Returns {@code null} if this task has not been executed yet.
     *
     * @return The result. Returns {@code null} if this task has not been executed yet.
     *
     * @see #getExecutionResult() for the preferred way of accessing this property.
     */
    @Internal
    @Nullable
    public ExecResult getExecResult() {
        // TODO: Once getExecutionResult is stable, make this deprecated
        // DeprecationLogger.deprecateMethod(AbstractExecTask.class, "getExecResult()").replaceWith("AbstractExecTask.getExecutionResult()").undocumented().nagUser();
        return execResult.getOrNull();
    }

    /**
     * Returns the result for the command run by this task. The provider has no value if this task has not been executed yet.
     *
     * @return A provider of the result.
     * @since 6.1
     */
    @Internal
    @Incubating
    public Provider<ExecResult> getExecutionResult() {
        return execResult;
    }
}
