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
package org.gradle.internal.resolve;

import com.google.common.base.Objects;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.internal.logging.text.TreeFormatter;

public abstract class RejectedVersion {
    private final ModuleComponentIdentifier id;

    public RejectedVersion(ModuleComponentIdentifier id) {
        this.id = id;
    }

    public ModuleComponentIdentifier getId() {
        return id;
    }

    public void describeTo(TreeFormatter builder) {
        builder.node(id.getVersion());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RejectedVersion that = (RejectedVersion) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
