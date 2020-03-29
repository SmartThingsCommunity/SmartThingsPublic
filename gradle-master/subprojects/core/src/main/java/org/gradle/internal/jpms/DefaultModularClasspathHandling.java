/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.internal.jpms;

import org.gradle.api.jpms.ModularClasspathHandling;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class DefaultModularClasspathHandling implements ModularClasspathHandling {

    private Property<Boolean> inferModulePath;

    @Inject
    public DefaultModularClasspathHandling(ObjectFactory objects) {
        inferModulePath = objects.property(Boolean.class).convention(false);
    }

    @Override
    public Property<Boolean> getInferModulePath() {
        return inferModulePath;
    }
}
