/*
 * Copyright 2013 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

public class ServiceRegistryBuilder {
    private final List<ServiceRegistry> parents = new ArrayList<ServiceRegistry>();
    private final List<Object> providers = new ArrayList<Object>();
    private String displayName;

    private ServiceRegistryBuilder() {
    }

    public static ServiceRegistryBuilder builder() {
        return new ServiceRegistryBuilder();
    }

    public ServiceRegistryBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ServiceRegistryBuilder parent(ServiceRegistry parent) {
        this.parents.add(parent);
        return this;
    }

    public ServiceRegistryBuilder provider(Object provider) {
        this.providers.add(provider);
        return this;
    }

    public ServiceRegistry build() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry(displayName, parents.toArray(new ServiceRegistry[0]));
        for (Object provider : providers) {
            registry.addProvider(provider);
        }
        return registry;
    }
}
