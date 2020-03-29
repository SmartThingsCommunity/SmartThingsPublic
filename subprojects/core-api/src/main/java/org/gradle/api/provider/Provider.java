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

package org.gradle.api.provider;

import org.gradle.api.Incubating;
import org.gradle.api.NonExtensible;
import org.gradle.api.Transformer;
import org.gradle.internal.HasInternalProtocol;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * A container object that provides a value of a specific type. The value can be retrieved using one of the query methods such as {@link #get()} or {@link #getOrNull()}.
 *
 * <p>
 * A provider may not always have a value available, for example when the value may not yet be known but will be known at some point in the future.
 * When a value is not available, {@link #isPresent()} returns {@code false} and retrieving the value will fail with an exception.
 * </p>
 *
 * <p>
 * A provider may not always provide the same value. Although there are no methods on this interface to change the value,
 * the provider implementation may be mutable or use values from some changing source. A provider may also provide a value that is mutable and that changes over time.
 * </p>
 *
 * <p>
 * A provider may be used to represent a task output. Such a provider carries information about which task produces its value. When attached to a task input, this allows Gradle to automatically add dependencies between tasks based on the values they use as inputs and produce as outputs.
 * </p>
 *
 * <p>
 * A typical use of a provider is to pass values from one Gradle model element to another, e.g. from a project extension to a task, or between tasks.
 * Providers also allow expensive computations to be deferred until their value is actually needed, usually at task execution time.
 * </p>
 *
 * <p>There are a number of ways to create a {@link Provider} instance. Some common methods:</p>
 *
 * <ul>
 *     <li>A number of Gradle types, such as {@link Property}, extend {@link Provider} and can be used directly as a provider.</li>
 *     <li>Calling {@link #map(Transformer)} to create a new provider from an existing provider.</li>
 *     <li>Using the return value of {@link org.gradle.api.tasks.TaskContainer#register(String)}, which is a provider that represents the task instance.</li>
 *     <li>Using the methods on {@link org.gradle.api.file.Directory} and {@link org.gradle.api.file.DirectoryProperty} to produce file providers.</li>
 *     <li>By calling {@link ProviderFactory#provider(Callable)} or {@link org.gradle.api.Project#provider(Callable)} to create a new provider from a {@link Callable}.</li>
 * </ul>
 *
 * <p>
 *  For a provider whose value can be mutated, see {@link Property} and the methods on {@link org.gradle.api.model.ObjectFactory}.
 * </p>
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.</p>
 *
 * @param <T> Type of value represented by provider
 * @since 4.0
 */
@HasInternalProtocol
@NonExtensible
public interface Provider<T> {

    /**
     * Returns the value of this provider if it has a value present, otherwise throws {@code java.lang.IllegalStateException}.
     *
     * @return the current value of this provider.
     * @throws IllegalStateException if there is no value present
     */
    T get();

    /**
     * Returns the value of this provider if it has a value present. Returns {@code null} a value is not available.
     *
     * @return the value or {@code null}
     */
    @Nullable
    T getOrNull();

    /**
     * Returns the value of this provider if it has a value present. Returns the given default value if a value is not available.
     *
     * @return the value or the default value.
     * @since 4.3
     */
    T getOrElse(T defaultValue);

    /**
     * Returns a new {@link Provider} whose value is the value of this provider transformed using the given function.
     *
     * <p>The new provider will be live, so that each time it is queried, it queries this provider and applies the transformation to the result.
     * Whenever this provider has no value, the new provider will also have no value and the transformation will not be called.
     *
     * <p>When this provider represents a task or the output of a task, the new provider will be considered an output of the task and will carry dependency information that Gradle can use to automatically attach task dependencies to tasks that use the new provider for input values.</p>
     *
     * @param transformer The transformer to apply to values. May return {@code null}, in which case the provider will have no value.
     * @since 4.3
     */
    <S> Provider<S> map(Transformer<? extends S, ? super T> transformer);

    /**
     * Returns a new {@link Provider} from the value of this provider transformed using the given function.
     *
     * <p>The new provider will be live, so that each time it is queried, it queries this provider and applies the transformation to the result.
     * Whenever this provider has no value, the new provider will also have no value and the transformation will not be called.
     *
     * <p>Any task details associated with this provider are ignored. The new provider will use whatever task details are associated with the return value of the function.</p>
     *
     * @param transformer The transformer to apply to values. May return {@code null}, in which case the provider will have no value.
     * @since 5.0
     */
    @Incubating
    <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer);

    /**
     * Returns {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    boolean isPresent();

    /**
     * Returns a {@link Provider} whose value is the value of this provider, if present, otherwise the given default value.
     *
     * @param value The default value to use when this provider has no value.
     * @since 5.6
     */
    @Incubating
    Provider<T> orElse(T value);

    /**
     * Returns a {@link Provider} whose value is the value of this provider, if present, otherwise uses the value from the given provider, if present.
     *
     * @param provider The provider whose value should be used when this provider has no value.
     * @since 5.6
     */
    @Incubating
    Provider<T> orElse(Provider<? extends T> provider);
}
