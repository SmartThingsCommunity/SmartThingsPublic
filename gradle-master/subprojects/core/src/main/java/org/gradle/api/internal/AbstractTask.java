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

package org.gradle.api.internal;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;
import groovy.util.ObservableList;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.gradle.api.Action;
import org.gradle.api.AntBuilder;
import org.gradle.api.Describable;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.TemporaryFileProvider;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.taskfactory.TaskIdentity;
import org.gradle.api.internal.tasks.DefaultTaskDependency;
import org.gradle.api.internal.tasks.DefaultTaskDestroyables;
import org.gradle.api.internal.tasks.DefaultTaskInputs;
import org.gradle.api.internal.tasks.DefaultTaskLocalState;
import org.gradle.api.internal.tasks.DefaultTaskOutputs;
import org.gradle.api.internal.tasks.ImplementationAwareTaskAction;
import org.gradle.api.internal.tasks.InputChangesAwareTaskAction;
import org.gradle.api.internal.tasks.TaskContainerInternal;
import org.gradle.api.internal.tasks.TaskDependencyInternal;
import org.gradle.api.internal.tasks.TaskLocalStateInternal;
import org.gradle.api.internal.tasks.TaskMutator;
import org.gradle.api.internal.tasks.TaskStateInternal;
import org.gradle.api.internal.tasks.properties.PropertyWalker;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.internal.BuildServiceRegistryInternal;
import org.gradle.api.specs.AndSpec;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskDestroyables;
import org.gradle.api.tasks.TaskInstantiationException;
import org.gradle.api.tasks.TaskLocalState;
import org.gradle.initialization.ProjectAccessNotifier;
import org.gradle.internal.Factory;
import org.gradle.internal.execution.history.changes.InputChangesInternal;
import org.gradle.internal.extensibility.ExtensibleDynamicObject;
import org.gradle.internal.hash.ClassLoaderHierarchyHasher;
import org.gradle.internal.instantiation.InstanceGenerator;
import org.gradle.internal.logging.compatbridge.LoggingManagerInternalCompatibilityBridge;
import org.gradle.internal.logging.slf4j.DefaultContextAwareTaskLogger;
import org.gradle.internal.metaobject.DynamicObject;
import org.gradle.internal.resources.ResourceLock;
import org.gradle.internal.resources.SharedResource;
import org.gradle.internal.scripts.ScriptOrigin;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.snapshot.impl.ImplementationSnapshot;
import org.gradle.logging.LoggingManagerInternal;
import org.gradle.logging.StandardOutputCapture;
import org.gradle.util.ConfigureUtil;
import org.gradle.util.GFileUtils;
import org.gradle.util.Path;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.gradle.util.GUtil.uncheckedCall;

public abstract class AbstractTask implements TaskInternal, DynamicObjectAware {
    private static final Logger BUILD_LOGGER = Logging.getLogger(Task.class);
    private static final ThreadLocal<TaskInfo> NEXT_INSTANCE = new ThreadLocal<TaskInfo>();

    private final TaskIdentity<?> identity;

    private final ProjectInternal project;

    private List<InputChangesAwareTaskAction> actions;

    private boolean enabled = true;

    private final DefaultTaskDependency dependencies;

    private final DefaultTaskDependency mustRunAfter;

    private final DefaultTaskDependency finalizedBy;

    private final DefaultTaskDependency shouldRunAfter;

    private ExtensibleDynamicObject extensibleDynamicObject;

    private String description;

    private String group;

    private final Property<Duration> timeout;

    private AndSpec<Task> onlyIfSpec = createNewOnlyIfSpec();

    private final ServiceRegistry services;

    private final TaskStateInternal state;

    private Logger logger = new DefaultContextAwareTaskLogger(BUILD_LOGGER);

    private final TaskMutator taskMutator;
    private ObservableList observableActionList;
    private boolean impliesSubProjects;
    private boolean hasCustomActions;

    private final TaskInputsInternal taskInputs;
    private final TaskOutputsInternal taskOutputs;
    private final TaskDestroyables taskDestroyables;
    private final TaskLocalStateInternal taskLocalState;
    private LoggingManagerInternal loggingManager;

    private Set<Provider<? extends BuildService<?>>> requiredServices;

    protected AbstractTask() {
        this(taskInfo());
    }

    private static TaskInfo taskInfo() {
        return NEXT_INSTANCE.get();
    }

    private AbstractTask(TaskInfo taskInfo) {
        if (taskInfo == null) {
            throw new TaskInstantiationException(String.format("Task of type '%s' has been instantiated directly which is not supported. Tasks can only be created using the Gradle API or DSL.", getClass().getName()));
        }

        this.identity = taskInfo.identity;
        this.project = taskInfo.project;
        assert project != null;
        assert identity.name != null;
        this.state = new TaskStateInternal();
        TaskContainerInternal tasks = project.getTasks();
        this.mustRunAfter = new DefaultTaskDependency(tasks);
        this.finalizedBy = new DefaultTaskDependency(tasks);
        this.shouldRunAfter = new DefaultTaskDependency(tasks);
        this.services = project.getServices();

        PropertyWalker propertyWalker = services.get(PropertyWalker.class);
        FileCollectionFactory fileCollectionFactory = services.get(FileCollectionFactory.class);
        taskMutator = new TaskMutator(this);
        taskInputs = new DefaultTaskInputs(this, taskMutator, propertyWalker, fileCollectionFactory);
        taskOutputs = new DefaultTaskOutputs(this, taskMutator, propertyWalker, fileCollectionFactory);
        taskDestroyables = new DefaultTaskDestroyables(taskMutator);
        taskLocalState = new DefaultTaskLocalState(taskMutator);

        this.dependencies = new DefaultTaskDependency(tasks, ImmutableSet.of(taskInputs));

        this.timeout = project.getObjects().property(Duration.class);
    }

    private void assertDynamicObject() {
        if (extensibleDynamicObject == null) {
            extensibleDynamicObject = new ExtensibleDynamicObject(this, identity.type, services.get(InstanceGenerator.class));
        }
    }

    public static <T extends Task> T injectIntoNewInstance(ProjectInternal project, TaskIdentity<T> identity, Callable<T> factory) {
        NEXT_INSTANCE.set(new TaskInfo(identity, project));
        try {
            return uncheckedCall(factory);
        } finally {
            NEXT_INSTANCE.set(null);
        }
    }

    @Override
    public TaskStateInternal getState() {
        return state;
    }

    @Override
    public AntBuilder getAnt() {
        return project.getAnt();
    }

    @Override
    public Project getProject() {
        if (state.getExecuting()) {
            notifyProjectAccess();
        }
        return project;
    }

    private void notifyProjectAccess() {
        services.get(ProjectAccessNotifier.class).getListener()
            .onProjectAccess("Task.project", this);
    }

    @Override
    public String getName() {
        return identity.name;
    }

    @Override
    public TaskIdentity<?> getTaskIdentity() {
        return identity;
    }

    @Override
    public List<Action<? super Task>> getActions() {
        if (observableActionList == null) {
            observableActionList = new ObservableActionWrapperList(getTaskActions());
            observableActionList.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    taskMutator.assertMutable("Task.getActions()", evt);
                }
            });
        }
        return observableActionList;
    }

    @Override
    public List<InputChangesAwareTaskAction> getTaskActions() {
        if (actions == null) {
            actions = new ArrayList<InputChangesAwareTaskAction>(3);
        }
        return actions;
    }

    @Override
    public boolean hasTaskActions() {
        return actions != null && !actions.isEmpty();
    }

    @Override
    public void setActions(final List<Action<? super Task>> replacements) {
        taskMutator.mutate("Task.setActions(List<Action>)", new Runnable() {
            @Override
            public void run() {
                getTaskActions().clear();
                for (Action<? super Task> action : replacements) {
                    doLast(action);
                }
            }
        });
    }

    @Override
    public TaskDependencyInternal getTaskDependencies() {
        return dependencies;
    }

    @Override
    public Set<Object> getDependsOn() {
        return dependencies.getMutableValues();
    }

    @Override
    public void setDependsOn(final Iterable<?> dependsOn) {
        taskMutator.mutate("Task.setDependsOn(Iterable)", new Runnable() {
            @Override
            public void run() {
                dependencies.setValues(dependsOn);
            }
        });
    }

    @Override
    public void onlyIf(final Closure onlyIfClosure) {
        taskMutator.mutate("Task.onlyIf(Closure)", new Runnable() {
            @Override
            public void run() {
                onlyIfSpec = onlyIfSpec.and(onlyIfClosure);
            }
        });
    }

    @Override
    public void onlyIf(final Spec<? super Task> spec) {
        taskMutator.mutate("Task.onlyIf(Spec)", new Runnable() {
            @Override
            public void run() {
                onlyIfSpec = onlyIfSpec.and(spec);
            }
        });
    }

    @Override
    public void setOnlyIf(final Spec<? super Task> spec) {
        taskMutator.mutate("Task.setOnlyIf(Spec)", new Runnable() {
            @Override
            public void run() {
                onlyIfSpec = createNewOnlyIfSpec().and(spec);
            }
        });
    }

    @Override
    public void setOnlyIf(final Closure onlyIfClosure) {
        taskMutator.mutate("Task.setOnlyIf(Closure)", new Runnable() {
            @Override
            public void run() {
                onlyIfSpec = createNewOnlyIfSpec().and(onlyIfClosure);
            }
        });
    }

    private AndSpec<Task> createNewOnlyIfSpec() {
        return new AndSpec<Task>(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task element) {
                return element == AbstractTask.this && enabled;
            }
        });
    }

    @Override
    public Spec<? super TaskInternal> getOnlyIf() {
        return onlyIfSpec;
    }

    @Override
    public boolean getDidWork() {
        return state.getDidWork();
    }

    @Override
    public void setDidWork(boolean didWork) {
        state.setDidWork(didWork);
    }

    @Internal
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        taskMutator.mutate("Task.setEnabled(boolean)", new Runnable() {
            @Override
            public void run() {
                AbstractTask.this.enabled = enabled;
            }
        });
    }

    @Override
    public boolean getImpliesSubProjects() {
        return impliesSubProjects;
    }

    @Override
    public void setImpliesSubProjects(boolean impliesSubProjects) {
        this.impliesSubProjects = impliesSubProjects;
    }

    @Override
    public String getPath() {
        return identity.projectPath.toString();
    }

    @Override
    public Path getIdentityPath() {
        return identity.identityPath;
    }

    @Override
    public Task dependsOn(final Object... paths) {
        taskMutator.mutate("Task.dependsOn(Object...)", new Runnable() {
            @Override
            public void run() {
                dependencies.add(paths);
            }
        });
        return this;
    }

    @Override
    public Task doFirst(final Action<? super Task> action) {
        return doFirst("doFirst {} action", action);
    }

    @Override
    public Task doFirst(final String actionName, final Action<? super Task> action) {
        hasCustomActions = true;
        if (action == null) {
            throw new InvalidUserDataException("Action must not be null!");
        }
        taskMutator.mutate("Task.doFirst(Action)", new Runnable() {
            @Override
            public void run() {
                getTaskActions().add(0, wrap(action, actionName));
            }
        });
        return this;
    }

    @Override
    public Task doLast(final Action<? super Task> action) {
        return doLast("doLast {} action", action);
    }

    @Override
    public Task doLast(final String actionName, final Action<? super Task> action) {
        hasCustomActions = true;
        if (action == null) {
            throw new InvalidUserDataException("Action must not be null!");
        }
        taskMutator.mutate("Task.doLast(Action)", new Runnable() {
            @Override
            public void run() {
                getTaskActions().add(wrap(action, actionName));
            }
        });
        return this;
    }

    @Override
    public int compareTo(Task otherTask) {
        int depthCompare = project.compareTo(otherTask.getProject());
        if (depthCompare == 0) {
            return getPath().compareTo(otherTask.getPath());
        } else {
            return depthCompare;
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public LoggingManagerInternal getLogging() {
        if (loggingManager == null) {
            loggingManager = new LoggingManagerInternalCompatibilityBridge(services.getFactory(org.gradle.internal.logging.LoggingManagerInternal.class).create());
        }
        return loggingManager;
    }

    @Override
    public void replaceLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public StandardOutputCapture getStandardOutputCapture() {
        return getLogging();
    }

    @Override
    public Object property(String propertyName) throws MissingPropertyException {
        assertDynamicObject();
        return extensibleDynamicObject.getProperty(propertyName);
    }

    @Override
    public boolean hasProperty(String propertyName) {
        assertDynamicObject();
        return extensibleDynamicObject.hasProperty(propertyName);
    }

    @Override
    public void setProperty(String name, Object value) {
        assertDynamicObject();
        extensibleDynamicObject.setProperty(name, value);
    }

    @Override
    public Convention getConvention() {
        assertDynamicObject();
        return extensibleDynamicObject.getConvention();
    }

    @Internal
    @Override
    public ExtensionContainer getExtensions() {
        return getConvention();
    }

    @Internal
    @Override
    public DynamicObject getAsDynamicObject() {
        assertDynamicObject();
        return extensibleDynamicObject;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public TaskInputsInternal getInputs() {
        return taskInputs;
    }

    @Override
    public TaskOutputsInternal getOutputs() {
        return taskOutputs;
    }

    @Override
    public TaskDestroyables getDestroyables() {
        return taskDestroyables;
    }

    @Override
    public TaskLocalState getLocalState() {
        return taskLocalState;
    }

    @Internal
    protected ServiceRegistry getServices() {
        return services;
    }

    @Override
    public Task doFirst(final Closure action) {
        hasCustomActions = true;
        if (action == null) {
            throw new InvalidUserDataException("Action must not be null!");
        }
        taskMutator.mutate("Task.doFirst(Closure)", new Runnable() {
            @Override
            public void run() {
                getTaskActions().add(0, convertClosureToAction(action, "doFirst {} action"));
            }
        });
        return this;
    }

    @Override
    public Task doLast(final Closure action) {
        hasCustomActions = true;
        if (action == null) {
            throw new InvalidUserDataException("Action must not be null!");
        }
        taskMutator.mutate("Task.doLast(Closure)", new Runnable() {
            @Override
            public void run() {
                getTaskActions().add(convertClosureToAction(action, "doLast {} action"));
            }
        });
        return this;
    }

    @Override
    public Task configure(Closure closure) {
        return ConfigureUtil.configureSelf(closure, this);
    }

    @Override
    public File getTemporaryDir() {
        File dir = getServices().get(TemporaryFileProvider.class).newTemporaryFile(getName());
        GFileUtils.mkdirs(dir);
        return dir;
    }

    // note: this method is on TaskInternal
    @Override
    public Factory<File> getTemporaryDirFactory() {
        return new Factory<File>() {
            @Override
            public File create() {
                return getTemporaryDir();
            }
        };
    }

    private InputChangesAwareTaskAction convertClosureToAction(Closure actionClosure, String actionName) {
        return new ClosureTaskAction(actionClosure, actionName);
    }

    private InputChangesAwareTaskAction wrap(final Action<? super Task> action) {
        return wrap(action, "unnamed action");
    }

    private InputChangesAwareTaskAction wrap(final Action<? super Task> action, String actionName) {
        if (action instanceof InputChangesAwareTaskAction) {
            return (InputChangesAwareTaskAction) action;
        }
        return new TaskActionWrapper(action, actionName);
    }

    private static class TaskInfo {
        private final TaskIdentity<?> identity;
        private final ProjectInternal project;

        private TaskInfo(TaskIdentity<?> identity, ProjectInternal project) {
            this.identity = identity;
            this.project = project;
        }
    }

    private static class ClosureTaskAction implements InputChangesAwareTaskAction {
        private final Closure closure;
        private final String actionName;

        private ClosureTaskAction(Closure closure, String actionName) {
            this.closure = closure;
            this.actionName = actionName;
        }

        @Override
        public void setInputChanges(InputChangesInternal inputChanges) {
        }

        @Override
        public void clearInputChanges() {
        }

        @Override
        public void execute(Task task) {
            closure.setDelegate(task);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(closure.getClass().getClassLoader());
            try {
                if (closure.getMaximumNumberOfParameters() == 0) {
                    closure.call();
                } else {
                    closure.call(task);
                }
            } catch (InvokerInvocationException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw e;
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        }

        @Override
        public ImplementationSnapshot getActionImplementation(ClassLoaderHierarchyHasher hasher) {
            return ImplementationSnapshot.of(AbstractTask.getActionClassName(closure), hasher.getClassLoaderHash(closure.getClass().getClassLoader()));
        }

        @Override
        public String getDisplayName() {
            return "Execute " + actionName;
        }
    }

    private static class TaskActionWrapper implements InputChangesAwareTaskAction {
        private final Action<? super Task> action;
        private final String maybeActionName;

        /**
         * The <i>action name</i> is used to construct a human readable name for
         * the actions to be used in progress logging. It is only used if
         * the wrapped action does not already implement {@link Describable}.
         */
        public TaskActionWrapper(Action<? super Task> action, String maybeActionName) {
            this.action = action;
            this.maybeActionName = maybeActionName;
        }

        @Override
        public void setInputChanges(InputChangesInternal inputChanges) {
            if (action instanceof InputChangesAwareTaskAction) {
                ((InputChangesAwareTaskAction) action).setInputChanges(inputChanges);
            }
        }

        @Override
        public void clearInputChanges() {
            if (action instanceof InputChangesAwareTaskAction) {
                ((InputChangesAwareTaskAction) action).clearInputChanges();
            }
        }

        @Override
        public void execute(Task task) {
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(action.getClass().getClassLoader());
            try {
                action.execute(task);
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        }

        @Override
        public ImplementationSnapshot getActionImplementation(ClassLoaderHierarchyHasher hasher) {
            if (action instanceof ImplementationAwareTaskAction) {
                return ((ImplementationAwareTaskAction) action).getActionImplementation(hasher);
            }
            return ImplementationSnapshot.of(AbstractTask.getActionClassName(action), hasher.getClassLoaderHash(action.getClass().getClassLoader()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TaskActionWrapper)) {
                return false;
            }

            TaskActionWrapper that = (TaskActionWrapper) o;

            if (action != null ? !action.equals(that.action) : that.action != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return action != null ? action.hashCode() : 0;
        }

        @Override
        public String getDisplayName() {
            if (action instanceof Describable) {
                return ((Describable) action).getDisplayName();
            }
            return "Execute " + maybeActionName;
        }
    }

    private static String getActionClassName(Object action) {
        if (action instanceof ScriptOrigin) {
            ScriptOrigin origin = (ScriptOrigin) action;
            return origin.getOriginalClassName() + "_" + origin.getContentHash();
        } else {
            return action.getClass().getName();
        }
    }

    @Override
    public void setMustRunAfter(final Iterable<?> mustRunAfterTasks) {
        taskMutator.mutate("Task.setMustRunAfter(Iterable)", new Runnable() {
            @Override
            public void run() {
                mustRunAfter.setValues(mustRunAfterTasks);
            }
        });
    }

    @Override
    public Task mustRunAfter(final Object... paths) {
        taskMutator.mutate("Task.mustRunAfter(Object...)", new Runnable() {
            @Override
            public void run() {
                mustRunAfter.add(paths);
            }
        });
        return this;
    }

    @Override
    public TaskDependency getMustRunAfter() {
        return mustRunAfter;
    }

    @Override
    public void setFinalizedBy(final Iterable<?> finalizedByTasks) {
        taskMutator.mutate("Task.setFinalizedBy(Iterable)", new Runnable() {
            @Override
            public void run() {
                finalizedBy.setValues(finalizedByTasks);
            }
        });
    }

    @Override
    public Task finalizedBy(final Object... paths) {
        taskMutator.mutate("Task.finalizedBy(Object...)", new Runnable() {
            @Override
            public void run() {
                finalizedBy.add(paths);
            }
        });
        return this;
    }

    @Override
    public TaskDependency getFinalizedBy() {
        return finalizedBy;
    }

    @Override
    public TaskDependency shouldRunAfter(final Object... paths) {
        taskMutator.mutate("Task.shouldRunAfter(Object...)", new Runnable() {
            @Override
            public void run() {
                shouldRunAfter.add(paths);
            }
        });
        return shouldRunAfter;
    }

    @Override
    public void setShouldRunAfter(final Iterable<?> shouldRunAfterTasks) {
        taskMutator.mutate("Task.setShouldRunAfter(Iterable)", new Runnable() {
            @Override
            public void run() {
                shouldRunAfter.setValues(shouldRunAfterTasks);
            }
        });
    }

    @Override
    public TaskDependency getShouldRunAfter() {
        return shouldRunAfter;
    }

    private class ObservableActionWrapperList extends ObservableList {
        public ObservableActionWrapperList(List delegate) {
            super(delegate);
        }

        @Override
        public boolean add(Object action) {
            if (action == null) {
                throw new InvalidUserDataException("Action must not be null!");
            }
            return super.add(wrap((Action<? super Task>) action));
        }

        @Override
        public void add(int index, Object action) {
            if (action == null) {
                throw new InvalidUserDataException("Action must not be null!");
            }
            super.add(index, wrap((Action<? super Task>) action));
        }

        @Override
        public boolean addAll(Collection actions) {
            if (actions == null) {
                throw new InvalidUserDataException("Actions must not be null!");
            }
            return super.addAll(transformToContextAwareTaskActions(actions));
        }

        @Override
        public boolean addAll(int index, Collection actions) {
            if (actions == null) {
                throw new InvalidUserDataException("Actions must not be null!");
            }
            return super.addAll(index, transformToContextAwareTaskActions(actions));
        }

        @Override
        public Object set(int index, Object action) {
            if (action == null) {
                throw new InvalidUserDataException("Action must not be null!");
            }
            return super.set(index, wrap((Action<? super Task>) action));
        }

        @Override
        public boolean removeAll(Collection actions) {
            return super.removeAll(transformToContextAwareTaskActions(actions));
        }

        @Override
        public boolean remove(Object action) {
            return super.remove(wrap((Action<? super Task>) action));
        }

        private Collection<InputChangesAwareTaskAction> transformToContextAwareTaskActions(Collection<Object> c) {
            return Collections2.transform(c, new Function<Object, InputChangesAwareTaskAction>() {
                @Override
                public InputChangesAwareTaskAction apply(@Nullable Object input) {
                    return wrap((Action<? super Task>) input);
                }
            });
        }
    }

    @Override
    public void prependParallelSafeAction(final Action<? super Task> action) {
        if (action == null) {
            throw new InvalidUserDataException("Action must not be null!");
        }
        getTaskActions().add(0, wrap(action));
    }

    @Override
    public void appendParallelSafeAction(final Action<? super Task> action) {
        if (action == null) {
            throw new InvalidUserDataException("Action must not be null!");
        }
        getTaskActions().add(wrap(action));
    }

    @Override
    public boolean isHasCustomActions() {
        return hasCustomActions;
    }

    @Override
    public Property<Duration> getTimeout() {
        return timeout;
    }

    @Override
    public void usesService(Provider<? extends BuildService<?>> service) {
        taskMutator.mutate("Task.usesService(Provider)", () -> {
            if (requiredServices == null) {
                requiredServices = new HashSet<>();
            }
            requiredServices.add(service);
        });
    }

    public Set<Provider<? extends BuildService<?>>> getRequiredServices() {
        if (requiredServices == null) {
            return Collections.emptySet();
        }
        return requiredServices;
    }

    @Override
    public List<ResourceLock> getSharedResources() {
        if (requiredServices == null) {
            return Collections.emptyList();
        }
        ImmutableList.Builder<ResourceLock> locks = ImmutableList.builder();
        BuildServiceRegistryInternal serviceRegistry = getServices().get(BuildServiceRegistryInternal.class);
        for (Provider<? extends BuildService<?>> service : requiredServices) {
            SharedResource resource = serviceRegistry.forService(service);
            if (resource.getMaxUsages() > 0) {
                locks.add(resource.getResourceLock(1));
            }
        }
        return locks.build();
    }
}
