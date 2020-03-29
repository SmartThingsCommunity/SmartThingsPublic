/*
 * Copyright 2013 the original author or authors.
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
package org.gradle.api.internal.artifacts;

import org.gradle.api.internal.artifacts.ivyservice.dependencysubstitution.DependencySubstitutionRules;

public interface GlobalDependencyResolutionRules {

    ComponentMetadataProcessorFactory NO_OP_FACTORY = resolutionContext -> ComponentMetadataProcessor.NO_OP;

    GlobalDependencyResolutionRules NO_OP = new GlobalDependencyResolutionRules() {
        @Override
        public ComponentMetadataProcessorFactory getComponentMetadataProcessorFactory() {
            return NO_OP_FACTORY;
        }

        @Override
        public ComponentModuleMetadataProcessor getModuleMetadataProcessor() {
            return ComponentModuleMetadataProcessor.NO_OP;
        }

        @Override
        public DependencySubstitutionRules getDependencySubstitutionRules() {
            return DependencySubstitutionRules.NO_OP;
        }
    };

    ComponentMetadataProcessorFactory getComponentMetadataProcessorFactory();
    ComponentModuleMetadataProcessor getModuleMetadataProcessor();
    DependencySubstitutionRules getDependencySubstitutionRules();
}
