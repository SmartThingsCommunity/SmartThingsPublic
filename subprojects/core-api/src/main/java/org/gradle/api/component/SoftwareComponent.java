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

package org.gradle.api.component;

import org.gradle.api.Named;
import org.gradle.internal.HasInternalProtocol;

/**
 * A software component produced by a Gradle software project.
 *
 * <p>An implementation of this interface may also implement:</p>
 *
 * <ul>
 *
 * <li>{@link ComponentWithVariants} to provide information about the variants that the component provides.</li>
 *
 * </ul>
 */
@HasInternalProtocol
public interface SoftwareComponent extends Named {
}
