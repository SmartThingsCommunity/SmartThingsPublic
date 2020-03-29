/*
 * Copyright 2016 the original author or authors.
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
package org.gradle.internal.service;

import javax.annotation.Nullable;
import java.util.List;

public interface ServiceLocator {
    <T> T get(Class<T> serviceType) throws UnknownServiceException;

    <T> List<T> getAll(Class<T> serviceType) throws UnknownServiceException;

    <T> DefaultServiceLocator.ServiceFactory<T> getFactory(Class<T> serviceType) throws UnknownServiceException;

    @Nullable
    <T> DefaultServiceLocator.ServiceFactory<T> findFactory(Class<T> serviceType);
}
