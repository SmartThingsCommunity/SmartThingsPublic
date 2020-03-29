/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.integtests.resolve.http

import org.gradle.integtests.fixtures.TestResources
import org.gradle.test.fixtures.keystore.TestKeyStore
import org.gradle.test.fixtures.server.http.MavenHttpRepository
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition
import org.junit.Rule

// Remove when https://bugs.openjdk.java.net/browse/JDK-8219658 is fixed in JDK 12
@Requires(TestPrecondition.JDK11_OR_EARLIER)
class HttpsProxyResolveIntegrationTest extends AbstractProxyResolveIntegrationTest {
    @Override
    MavenHttpRepository getRepo() {
        return mavenHttpRepo
    }

    @Rule TestResources resources = new TestResources(temporaryFolder)
    TestKeyStore keyStore

    @Override
    String getProxyScheme() {
        return 'https'
    }

    @Override
    String getRepoServerUrl() {
        "https://localhost:${server.sslPort}/repo"
    }

    @Override
    boolean isTunnel() { true }

    @Override
    void setupServer() {
        keyStore = TestKeyStore.init(resources.dir)
        keyStore.enableSslWithServerCert(server)
        keyStore.configureServerCert(executer)
    }
}
