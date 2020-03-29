/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.classpath;

import java.util.Set;

/**
 * A registry of dynamically loaded plugin modules.
 */
public interface PluginModuleRegistry {
    /**
     * Plugin modules exposed in the Gradle API.
     */
    Set<Module> getApiModules();

    /**
     * Plugin modules exposed to the Gradle runtime only.
     */
    Set<Module> getImplementationModules();
}
