/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.launcher.cli;

import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;

public interface CommandLineAction {
    /**
     * Configures the given parser with the options used by this action.
     */
    void configureCommandLineParser(CommandLineParser parser);

    /**
     * Creates an executable action from the given command-line args. Returns null if this action was not selected by the given
     * command-line args.
     */
    Runnable createAction(CommandLineParser parser, ParsedCommandLine commandLine);
}
