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

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a property whose type is a {@link List} of elements of type {@link T}.
 *
 * <p>
 * You can create a {@link ListProperty} instance using factory method {@link org.gradle.api.model.ObjectFactory#listProperty(Class)}.
 * </p>
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.
 *
 * @param <T> the type of elements.
 * @since 4.3
 */
public interface ListProperty<T> extends Provider<List<T>>, HasMultipleValues<T> {
    /**
     * {@inheritDoc}
     */
    @Override
    ListProperty<T> empty();

    /**
     * {@inheritDoc}
     */
    @Override
    ListProperty<T> value(@Nullable Iterable<? extends T> elements);

    /**
     * {@inheritDoc}
     */
    @Override
    ListProperty<T> value(Provider<? extends Iterable<? extends T>> provider);

    /**
     * {@inheritDoc}
     */
    @Override
    ListProperty<T> convention(Iterable<? extends T> elements);

    /**
     * {@inheritDoc}
     */
    @Override
    ListProperty<T> convention(Provider<? extends Iterable<? extends T>> provider);
}
