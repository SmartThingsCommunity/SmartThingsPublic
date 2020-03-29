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

package org.gradle.api.artifacts.transform;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.internal.exceptions.Contextual;

import java.io.File;

/**
 * An exception to report a problem during transformation execution.
 *
 * @since 3.4
 */
@Deprecated
@Contextual
public class ArtifactTransformException extends GradleException {

    public ArtifactTransformException(File input, AttributeContainer expectedAttributes, @SuppressWarnings("unused") Class<? extends ArtifactTransform> transform, Throwable cause) {
        this(input, expectedAttributes, cause);
    }

    /**
     * Capture a failure during the execution of a file-based transformation.
     *
     * @since 5.1
     */
    public ArtifactTransformException(File file, AttributeContainer expectedAttributes, Throwable cause) {
        super(String.format("Failed to transform file '%s' to match attributes %s",
            file.getName(), expectedAttributes), cause);
    }

    /**
     * Capture a failure during the execution of a file-based transformation.
     *
     * @since 5.1
     */
    public ArtifactTransformException(ComponentArtifactIdentifier artifact, AttributeContainer expectedAttributes, Throwable cause) {
        super(String.format("Failed to transform artifact '%s' to match attributes %s",
            artifact, expectedAttributes), cause);
    }
}
