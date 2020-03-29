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

package org.gradle.integtests.resolve.api

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.resolve.support.RepositoryDslSupport
import spock.lang.Unroll

@Mixin(RepositoryDslSupport)
class CredentialsDslIntegrationTest extends AbstractIntegrationSpec {

    @Unroll
    def "can configure repositories with [#scenario] DSL "() {
        expect:
        buildFile << dsl
        succeeds 'help'

        where:
        scenario          | dsl
        'simple'          | simplestDsl()
        'awsCredentials'  | awsCredentials()
        'ivyWithPassword' | ivyPasswordCredentials()
        'passwordTyped'   | passwordCredentialsTyped()
    }
}
