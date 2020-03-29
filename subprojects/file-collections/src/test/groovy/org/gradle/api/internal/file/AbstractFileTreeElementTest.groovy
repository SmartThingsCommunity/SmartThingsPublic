/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.internal.file

import org.gradle.api.file.RelativePath
import org.gradle.internal.file.Chmod
import org.gradle.internal.nativeintegration.filesystem.FileSystem
import org.gradle.test.fixtures.AbstractProjectBuilderSpec
import org.gradle.test.fixtures.file.TestFile
import org.gradle.util.GFileUtils

class AbstractFileTreeElementTest extends AbstractProjectBuilderSpec {
    final chmod = Mock(Chmod)

    def canCopyToOutputStream() {
        given:
        def src = writeToFile("src", "content")

        when:
        def outstr = new ByteArrayOutputStream()
        new TestFileTreeElement(src, chmod).copyTo(outstr)

        then:
        new String(outstr.toByteArray()) == "content"
    }

    def canCopyToFile() {
        given:
        def src = writeToFile("src", "content")
        def dest = temporaryFolder.file("dir/dest")

        when:
        new TestFileTreeElement(src, chmod).copyTo(dest)

        then:
        dest.assertIsFile()
        dest.getText() == "content"
    }

    def copiedFileHasExpectedPermissions() throws Exception {
        given:
        def src = writeToFile("src", "")
        final dest = temporaryFolder.file("dest")

        when:
        new TestFileTreeElement(src, 0666, chmod).copyTo(dest)

        then:
        1 * chmod.chmod(dest, 0666)
    }

    def defaultPermissionValuesAreUsed() {
        given:
        def dir = new TestFileTreeElement(temporaryFolder.getTestDirectory(), chmod)
        def file = new TestFileTreeElement(temporaryFolder.file("someFile"), chmod)

        expect:
        dir.getMode() == FileSystem.DEFAULT_DIR_MODE
        file.getMode() == FileSystem.DEFAULT_FILE_MODE
    }

    private TestFile writeToFile(String name, String content) {
        TestFile result = temporaryFolder.file(name)
        result.write(content)
        return result
    }

    private class TestFileTreeElement extends AbstractFileTreeElement {
        private final TestFile file
        private final Integer mode

        public TestFileTreeElement(TestFile file, Chmod chmod) {
            this(file, null, chmod)
        }

        public TestFileTreeElement(TestFile file, Integer mode, Chmod chmod) {
            super(chmod)
            this.file = file
            this.mode = mode
        }

        public String getDisplayName() {
            return "display name"
        }

        public File getFile() {
            return file
        }

        public long getLastModified() {
            return file.lastModified()
        }

        public boolean isDirectory() {
            return file.isDirectory()
        }

        public long getSize() {
            return file.length()
        }

        public RelativePath getRelativePath() {
            throw new UnsupportedOperationException()
        }

        public InputStream open() {
            return GFileUtils.openInputStream(file)
        }

        public int getMode() {
            return mode == null ? super.getMode() : mode
        }
    }
}
