/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.internal.file;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.io.File;
import java.util.List;

public class DefaultReservedFileSystemLocationRegistry implements ReservedFileSystemLocationRegistry {
    private final FileHierarchySet reservedFileSystemLocations;

    public DefaultReservedFileSystemLocationRegistry(List<ReservedFileSystemLocation> registeredReservedFileSystemLocations) {
        this.reservedFileSystemLocations = DefaultFileHierarchySet.of(Iterables.transform(registeredReservedFileSystemLocations, new Function<ReservedFileSystemLocation, File>() {
            @Override
            public File apply(ReservedFileSystemLocation input) {
                return input.getReservedFileSystemLocation().get().getAsFile();
            }
        }));
    }

    @Override
    public boolean isInReservedFileSystemLocation(File location) {
        return reservedFileSystemLocations.contains(location);
    }
}
