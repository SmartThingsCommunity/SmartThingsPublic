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

import org.gradle.api.internal.classpath.Module;
import org.gradle.api.internal.classpath.ModuleRegistry;
import org.gradle.api.internal.classpath.PluginModuleRegistry;
import org.gradle.internal.classpath.ClassPath;

import java.util.Arrays;

import static org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory.ClassPathNotation.GRADLE_API;
import static org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory.ClassPathNotation.GRADLE_KOTLIN_DSL;
import static org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory.ClassPathNotation.GRADLE_TEST_KIT;
import static org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory.ClassPathNotation.LOCAL_GROOVY;

public class DependencyClassPathProvider implements ClassPathProvider {
    private final ModuleRegistry moduleRegistry;
    private final PluginModuleRegistry pluginModuleRegistry;

    private ClassPath gradleApi;

    public DependencyClassPathProvider(ModuleRegistry moduleRegistry, PluginModuleRegistry pluginModuleRegistry) {
        this.moduleRegistry = moduleRegistry;
        this.pluginModuleRegistry = pluginModuleRegistry;
    }

    @Override
    public ClassPath findClassPath(String name) {
        if (name.equals(GRADLE_API.name())) {
            return gradleApi();
        }
        if (name.equals(GRADLE_TEST_KIT.name())) {
            return gradleTestKit();
        }
        if (name.equals(LOCAL_GROOVY.name())) {
            return localGroovy();
        }
        if (name.equals(GRADLE_KOTLIN_DSL.name())) {
            return gradleKotlinDsl();
        }
        return null;
    }

    private ClassPath gradleApi() {
        if (gradleApi == null) {
            gradleApi = initGradleApi();
        }
        return gradleApi;
    }

    private ClassPath initGradleApi() {
        ClassPath classpath = ClassPath.EMPTY;
        for (String moduleName : Arrays.asList("gradle-worker-processes", "gradle-launcher", "gradle-workers", "gradle-dependency-management", "gradle-plugin-use", "gradle-tooling-api")) {
            classpath = classpath.plus(moduleRegistry.getModule(moduleName).getAllRequiredModulesClasspath());
        }
        for (Module pluginModule : pluginModuleRegistry.getApiModules()) {
            classpath = classpath.plus(pluginModule.getClasspath());
        }
        return classpath;
    }

    private ClassPath gradleTestKit() {
        return moduleRegistry.getModule("gradle-test-kit").getClasspath();
    }

    private ClassPath localGroovy() {
        return moduleRegistry.getExternalModule("groovy-all").getClasspath();
    }

    private ClassPath gradleKotlinDsl() {
        return moduleRegistry.getModule("gradle-kotlin-dsl").getAllRequiredModulesClasspath();
    }
}
