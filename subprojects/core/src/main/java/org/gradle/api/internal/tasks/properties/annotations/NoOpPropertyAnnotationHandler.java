/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.internal.tasks.properties.annotations;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.internal.tasks.properties.BeanPropertyContext;
import org.gradle.api.internal.tasks.properties.PropertyValue;
import org.gradle.api.internal.tasks.properties.PropertyVisitor;
import org.gradle.internal.reflect.AnnotationCategory;
import org.gradle.internal.reflect.PropertyMetadata;

import java.lang.annotation.Annotation;

public class NoOpPropertyAnnotationHandler implements PropertyAnnotationHandler {
    private final Class<? extends Annotation> annotationType;

    public NoOpPropertyAnnotationHandler(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public ImmutableSet<? extends AnnotationCategory> getAllowedModifiers() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isPropertyRelevant() {
        return false;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    @Override
    public boolean shouldVisit(PropertyVisitor visitor) {
        return false;
    }

    @Override
    public void visitPropertyValue(String propertyName, PropertyValue value, PropertyMetadata propertyMetadata, PropertyVisitor visitor, BeanPropertyContext context) {
    }
}
