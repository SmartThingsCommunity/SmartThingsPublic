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

package org.gradle.buildinit.plugins.internal;

public class PackageNameBuilder {
    public static String toPackageName(String name) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        while (pos < name.length()) {
            while (pos < name.length() && !Character.isJavaIdentifierStart(name.charAt(pos))) {
                pos++;
            }
            if (pos == name.length()) {
                break;
            }
            if (result.length() != 0) {
                result.append('.');
            }
            result.append(name.charAt(pos));
            pos++;
            while (pos < name.length() && Character.isJavaIdentifierPart(name.charAt(pos))) {
                result.append(name.charAt(pos));
                pos++;
            }
        }
        return result.toString();
    }
}
