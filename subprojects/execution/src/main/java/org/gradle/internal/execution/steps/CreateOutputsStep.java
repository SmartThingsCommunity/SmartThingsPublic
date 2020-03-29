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

package org.gradle.internal.execution.steps;

import org.gradle.internal.execution.Context;
import org.gradle.internal.execution.Result;
import org.gradle.internal.execution.Step;
import org.gradle.internal.file.TreeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.gradle.util.GFileUtils.mkdirs;

public class CreateOutputsStep<C extends Context, R extends Result> implements Step<C, R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOutputsStep.class);

    private final Step<? super C, ? extends R> delegate;

    public CreateOutputsStep(Step<? super C, ? extends R> delegate) {
        this.delegate = delegate;
    }

    @Override
    public R execute(C context) {
        context.getWork().visitOutputProperties((name, type, root) -> {
            ensureOutput(name, root, type);
        });
        return delegate.execute(context);
    }

    private static void ensureOutput(String name, File outputRoot, TreeType type) {
        switch (type) {
            case DIRECTORY:
                LOGGER.debug("Ensuring directory exists for property {} at {}", name, outputRoot);
                mkdirs(outputRoot);
                break;
            case FILE:
                LOGGER.debug("Ensuring parent directory exists for property {} at {}", name, outputRoot);
                mkdirs(outputRoot.getParentFile());
                break;
            default:
                throw new AssertionError();
        }
    }
}
