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

package org.gradle.api.internal.artifacts.transform;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.internal.UncheckedException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Provides a generic transform from a zipped file to an extracted directory.  The extracted directory
 * is located in the output directory of the transform and is named after the zipped file name
 * minus the extension.
 */
public interface UnzipTransform extends TransformAction<TransformParameters.None> {

    @InputArtifact
    Provider<FileSystemLocation> getZippedFile();

    @Override
    default void transform(TransformOutputs outputs) {
        File zippedFile = getZippedFile().get().getAsFile();
        String unzippedDirName = removeExtension(zippedFile.getName());
        File unzipDir = outputs.dir(unzippedDirName);
        try {
            unzipTo(zippedFile, unzipDir);
        } catch (IOException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    static void unzipTo(File headersZip, File unzipDir) throws IOException {
        ZipInputStream inputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(headersZip)));
        try {
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File outFile = new File(unzipDir, entry.getName());
                Files.createParentDirs(outFile);
                FileOutputStream outputStream = new FileOutputStream(outFile);
                try {
                    IOUtils.copyLarge(inputStream, outputStream);
                } finally {
                    outputStream.close();
                }
            }
        } finally {
            inputStream.close();
        }
    }
}
