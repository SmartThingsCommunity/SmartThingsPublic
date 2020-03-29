/*
 * Copyright 2018 the original author or authors.
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

import org.gradle.util.GradleVersion;

/**
 * Locates documentation for various features.
 */
public class DocumentationRegistry {
    private final GradleVersion gradleVersion;

    public DocumentationRegistry() {
        this.gradleVersion = GradleVersion.current();
    }

    /**
     * Returns the location of the documentation for the given feature, referenced by id. The location may be local or remote.
     */
    public String getDocumentationFor(String id) {
        return String.format("https://docs.gradle.org/%s/userguide/%s.html", gradleVersion.getVersion(), id);
    }

    public String getDocumentationFor(String id, String section) {
        return String.format("https://docs.gradle.org/%s/userguide/%s.html#%s", gradleVersion.getVersion(), id, section);
    }

    public String getDslRefForProperty(Class<?> clazz, String property) {
        String className = clazz.getName();
        return String.format("https://docs.gradle.org/%s/dsl/%s.html#%s:%s", gradleVersion.getVersion(), className, className, property);
    }

    /**
     * Returns the location of the guide for the given name (e.g., "creating-new-gradle-builds").
     */
    public String getGuideFor(String guide) {
        return String.format("https://guides.gradle.org/%s", guide);
    }

    /**
     * Returns the location of the search results for guides for the given topic (e.g., "Native").
     */
    public String getTopicGuidesFor(String topic) {
        return String.format("https://guides.gradle.org?q=%s", topic);
    }

}
