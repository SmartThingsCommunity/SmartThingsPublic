/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.groovy.scripts.internal;

/**
 * Data extracted from a build script at compile time.
 */
public class BuildScriptData {
    private final boolean hasImperativeStatements;

    public BuildScriptData(boolean hasImperativeStatements) {
        this.hasImperativeStatements = hasImperativeStatements;
    }

    /**
     * Returns true when the build script contains legacy imperative statements. When false, the script contains only model rule statements and its execution
     * can be deferred until rule execution is required.
     */
    public boolean getHasImperativeStatements() {
        return hasImperativeStatements;
    }
}
