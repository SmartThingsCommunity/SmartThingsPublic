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
package org.gradle.plugins.ide.internal.tooling.eclipse;

import java.util.List;

public class DefaultEclipseClasspathContainer extends DefaultEclipseClasspathEntry {
    private final String path;
    private final boolean isExported;

    public DefaultEclipseClasspathContainer(String path, boolean isExported, List<DefaultClasspathAttribute> classpathAttributes, List<DefaultAccessRule> accessRules) {
        super(classpathAttributes, accessRules);
        this.path = path;
        this.isExported = isExported;
    }

    @Override
    public String toString() {
        return "classpath container '" + path + "'";
    }

    public String getPath() {
        return path;
    }

    public boolean isExported() {
        return isExported;
    }
}
