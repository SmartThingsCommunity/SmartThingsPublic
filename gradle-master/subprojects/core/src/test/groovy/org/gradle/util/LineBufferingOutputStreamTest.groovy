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
package org.gradle.util

import org.gradle.internal.SystemProperties
import org.gradle.internal.io.LineBufferingOutputStream
import org.gradle.internal.io.TextStream
import spock.lang.Specification

class LineBufferingOutputStreamTest extends Specification {
    private TextStream action = Mock(TextStream)
    private String eol

    def setup() {
        eol = SystemProperties.getInstance().getLineSeparator()
    }

    def cleanup() {
        System.setProperty("line.separator", eol)
    }

    def logsEachLineAsASeparateLogMessage() {
        when:
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)
        outputStream.write(TextUtil.toPlatformLineSeparators("line 1\nline 2\n").getBytes())

        then:
        1 * action.text(TextUtil.toPlatformLineSeparators("line 1\n"))
        1 * action.text(TextUtil.toPlatformLineSeparators("line 2\n"))
    }

    def buffersTextUntilEndOfLineReached() {
        when:
        System.setProperty("line.separator", "-")
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)
        outputStream.write("line ".getBytes())
        outputStream.write("1-line 2".getBytes())

        then:
        1 * action.text("line 1-")

        when:
        outputStream.write("-".getBytes())

        then:
        1 * action.text("line 2-")
    }

    def logsEmptyLines() {
        when:
        System.setProperty("line.separator", "-")
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)

        outputStream.write("--".getBytes())

        then:
        2 * action.text("-")
    }

    def handlesSingleCharacterLineSeparator() {
        when:
        System.setProperty("line.separator", "-")
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)

        outputStream.write(String.format("line 1-line 2-").getBytes())

        then:
        1 * action.text("line 1-")
        1 * action.text("line 2-")
    }

    def handlesMultiCharacterLineSeparator() {
        final String separator = "\r\n"
        System.setProperty("line.separator", separator)
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)

        when:
        outputStream.write(("line 1" + separator + "line 2" + separator).getBytes())

        then:
        1 * action.text("line 1" + separator)
        1 * action.text("line 2" + separator)
    }

    def logsLineWhichIsLongerThanInitialBufferLength() {
        when:
        System.setProperty("line.separator", "-")
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)
        outputStream.write("a line longer than 8 bytes long-".getBytes())
        outputStream.write("line 2".getBytes())
        outputStream.flush()

        then:
        1 * action.text("a line longer than 8 bytes long-")
        1 * action.text("line 2")
    }

    def logsPartialLineOnFlush() {
        given:
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)
        outputStream.write("line 1".getBytes())

        when:
        outputStream.flush()

        then:
        action.text("line 1")
    }

    def logsNothingOnCloseWhenNothingHasBeenWrittenToStream() {
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)

        when:
        outputStream.close()

        then:
        1 * action.endOfStream(null)
        0 * action._
    }

    def logsNothingOnCloseWhenCompleteLineHasBeenWrittenToStream() {
        System.setProperty("line.separator", "-")
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)

        when:
        outputStream.write("line 1-".getBytes())
        outputStream.close()

        then:
        action.text("line 1-")
        action.endOfStream(null)
    }

    def logsPartialLineOnClose()  {
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)

        when:
        outputStream.write("line 1".getBytes())
        outputStream.close()

        then:
        action.text("line 1")
        action.endOfStream(null)
    }

    def cannotWriteAfterClose() {
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8)

        when:
        outputStream.close()

        then:
        action.endOfStream(null)

        when:
        outputStream.write("ignore me".getBytes())

        then:
        thrown(IOException)
    }

    def splitsLongLines() {
        when:
        LineBufferingOutputStream outputStream = new LineBufferingOutputStream(action, 8, 13)
        outputStream.write("12345678901234567890123456789".getBytes())
        outputStream.close()

        then:
        action.text("1234567890123")
        action.text("4567890123456")
        action.text("789")
        action.endOfStream(null)
    }
}
