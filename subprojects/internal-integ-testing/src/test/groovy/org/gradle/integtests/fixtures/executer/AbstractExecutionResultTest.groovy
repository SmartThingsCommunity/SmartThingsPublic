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

package org.gradle.integtests.fixtures.executer

import org.gradle.util.TextUtil
import spock.lang.Specification


abstract class AbstractExecutionResultTest extends Specification {
    String error(String text) {
        return TextUtil.normaliseLineSeparators(text.stripIndent().trim())
    }

    String error(Throwable failure) {
        return TextUtil.normaliseLineSeparators(failure.message)
    }
}
