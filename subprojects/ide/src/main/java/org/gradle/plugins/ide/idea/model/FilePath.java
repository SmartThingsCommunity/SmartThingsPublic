/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.plugins.ide.idea.model;

import java.io.File;

/**
 * A Path that keeps the reference to the File
 */
public class FilePath extends Path {

    private final File file;

    public FilePath(File file, String url, String canonicalUrl, String relPath) {
        super(url, canonicalUrl, relPath);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
