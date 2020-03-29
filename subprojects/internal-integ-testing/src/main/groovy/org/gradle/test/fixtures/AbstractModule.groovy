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

package org.gradle.test.fixtures

import org.apache.commons.io.FilenameUtils
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.internal.IoActions
import org.gradle.internal.hash.HashUtil
import org.gradle.test.fixtures.file.TestFile
import org.gradle.util.TextUtil

abstract class AbstractModule implements Module {
    /**
     Last modified date for writeZipped to be able to create zipFiles with identical hashes
     */
    private static Date lmd = new Date(0)

    private boolean hasModuleMetadata
    private Closure<?> onEveryFile

    Map<String, String> attributes = [:]

    /**
     * @param cl A closure that is passed a writer to use to generate the content.
     */
    protected void publish(TestFile file, @DelegatesTo(value=Writer, strategy=Closure.DELEGATE_FIRST) Closure cl, byte[] content = null) {
        file.parentFile.mkdirs()
        def hashBefore = file.exists() ? getHash(file, "sha1") : null
        def tmpFile = file.parentFile.file("${file.name}.tmp")

        if (content) {
            tmpFile.bytes = content
        } else if (isJarFile(file)) {
            writeZipped(tmpFile, cl)
        } else {
            writeContents(tmpFile, cl)
            // normalize line endings
            tmpFile.setText(TextUtil.normaliseLineSeparators(tmpFile.getText("utf-8")), "utf-8")
        }

        def hashAfter = getHash(tmpFile, "sha1")
        if (hashAfter == hashBefore) {
            // Already published
            return
        }

        assert !file.exists() || file.delete()
        assert tmpFile.renameTo(file)
        onPublish(file)
    }

    private void writeContents(output, Closure cl) {
        output.withWriter("utf-8", cl)
    }

    private void writeZipped(TestFile testFile, Closure cl) {
        def bos = new ByteArrayOutputStream()
        writeContents(bos, cl)

        ZipOutputStream zipStream = new ZipOutputStream(testFile)
        try {
            def entry = new ZipEntry(testFile.name)
            entry.setTime(lmd.getTime())
            zipStream.putNextEntry(entry)
            zipStream << bos.toByteArray()
            zipStream.closeEntry()
            zipStream.finish()
        } finally {
            IoActions.closeQuietly(zipStream)
        }
    }

    protected boolean isJarFile(TestFile testFile) {
        return FilenameUtils.getExtension(testFile.getName()) == 'jar'
    }

    protected abstract onPublish(TestFile file)

    protected void postPublish(TestFile file) {
        if (onEveryFile) {
            Closure cl = onEveryFile.clone()
            cl.delegate = file
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl(file)
        }
    }

    TestFile getSha1File(TestFile file) {
        getHashFile(file, "sha1")
    }

    TestFile sha1File(TestFile file) {
        hashFile(file, "sha1", 40)
    }

    TestFile sha256File(TestFile file) {
        hashFile(file, "sha-256", 64)
    }

    TestFile sha512File(TestFile file) {
        hashFile(file, "sha-512", 128)
    }

    TestFile getMd5File(TestFile file) {
        getHashFile(file, "md5")
    }

    TestFile md5File(TestFile file) {
        hashFile(file, "md5", 32)
    }

    TestFile getSha256File(TestFile file) {
        getHashFile(file, "sha-256")
    }

    TestFile getSha512File(TestFile file) {
        getHashFile(file, "sha-512")
    }

    private TestFile hashFile(TestFile file, String algorithm, int len) {
        def hashFile = getHashFile(file, algorithm)
        def hash = getHash(file, algorithm)
        hashFile.text = String.format("%0${len}x", hash)
        return hashFile
    }

    private TestFile getHashFile(TestFile file, String algorithm) {
        file.parentFile.file("${file.name}.${algorithm.replaceAll('-', '')}")
    }

    protected BigInteger getHash(TestFile file, String algorithm) {
        HashUtil.createHash(file, algorithm.toUpperCase()).asBigInteger()
    }

    Module withModuleMetadata() {
        hasModuleMetadata = true
        return this
    }

    boolean isHasModuleMetadata() {
        hasModuleMetadata
    }

    @Override
    Module withSignature(@DelegatesTo(value = File, strategy = Closure.DELEGATE_FIRST) Closure<?> signer) {
        onEveryFile = signer
        this
    }
}
