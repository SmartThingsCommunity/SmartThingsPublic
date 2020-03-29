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

package org.gradle.integtests.resolve.rules

import groovy.transform.CompileStatic
import org.gradle.api.internal.artifacts.ivyservice.NamespaceId

@CompileStatic
trait ComponentMetadataRulesSupport {
    NamespaceId ns(String name) {
        return new NamespaceId("http://my.extra.info/${name}", name)
    }

    String declareNS(String name) {
        "(new javax.xml.namespace.QName('http://my.extra.info/${name}', '${name}'))"
    }

    String sq(String input) {
        escapeForSingleQuoting(input)
    }

    String escapeForSingleQuoting(String input) {
        input.replace('\\', '\\\\').replace('\'', '\\\'')
    }
}
