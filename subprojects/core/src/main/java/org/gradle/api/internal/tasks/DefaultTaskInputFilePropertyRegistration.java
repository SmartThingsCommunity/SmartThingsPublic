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

package org.gradle.api.internal.tasks;

import org.gradle.api.NonNullApi;
import org.gradle.api.internal.tasks.properties.FileParameterUtils;
import org.gradle.api.internal.tasks.properties.InputFilePropertyType;
import org.gradle.api.tasks.FileNormalizer;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.internal.fingerprint.AbsolutePathInputNormalizer;

@NonNullApi
public class DefaultTaskInputFilePropertyRegistration extends AbstractTaskFilePropertyRegistration implements TaskInputFilePropertyRegistration {

    private final InputFilePropertyType filePropertyType;
    private boolean skipWhenEmpty;
    private Class<? extends FileNormalizer> normalizer = AbsolutePathInputNormalizer.class;

    public DefaultTaskInputFilePropertyRegistration(StaticValue value, InputFilePropertyType filePropertyType) {
        super(value);
        this.filePropertyType = filePropertyType;
    }

    @Override
    public InputFilePropertyType getFilePropertyType() {
        return filePropertyType;
    }

    @Override
    public TaskInputFilePropertyBuilderInternal withPropertyName(String propertyName) {
        setPropertyName(propertyName);
        return this;
    }

    @Override
    public boolean isSkipWhenEmpty() {
        return skipWhenEmpty;
    }

    @Override
    public TaskInputFilePropertyBuilderInternal skipWhenEmpty(boolean skipWhenEmpty) {
        this.skipWhenEmpty = skipWhenEmpty;
        return this;
    }

    @Override
    public TaskInputFilePropertyBuilderInternal skipWhenEmpty() {
        return skipWhenEmpty(true);
    }

    @Override
    public TaskInputFilePropertyBuilderInternal optional(boolean optional) {
        setOptional(optional);
        return this;
    }

    @Override
    public TaskInputFilePropertyBuilderInternal optional() {
        return optional(true);
    }

    @Override
    public TaskInputFilePropertyBuilderInternal withPathSensitivity(PathSensitivity sensitivity) {
        return withNormalizer(FileParameterUtils.determineNormalizerForPathSensitivity(sensitivity));
    }

    @Override
    public TaskInputFilePropertyBuilderInternal withNormalizer(Class<? extends FileNormalizer> normalizer) {
        this.normalizer = normalizer;
        return this;
    }

    @Override
    public Class<? extends FileNormalizer> getNormalizer() {
        return normalizer;
    }

    @Override
    public String toString() {
        return getPropertyName() + " (" + getNormalizer().getSimpleName().replace("Normalizer", "") + ")";
    }
}
