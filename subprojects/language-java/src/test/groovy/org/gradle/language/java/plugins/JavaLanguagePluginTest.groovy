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

package org.gradle.language.java.plugins

import org.gradle.api.Plugin
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.java.JavaSourceSet
import org.gradle.test.fixtures.plugin.AbstractLanguagePluginSpec

class JavaLanguagePluginTest extends AbstractLanguagePluginSpec {

    @Override
    Class<Plugin> getPluginClass() {
        return JavaLanguagePlugin
    }

    @Override
    Class<? extends LanguageSourceSet> getLanguageSourceSet() {
        return JavaSourceSet
    }

    @Override
    String getLanguageId() {
        return "java"
    }
}
