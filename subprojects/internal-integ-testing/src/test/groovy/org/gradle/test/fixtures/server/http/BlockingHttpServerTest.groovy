/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.test.fixtures.server.http

import org.gradle.test.fixtures.concurrent.ConcurrentSpec
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.util.TestUtil
import org.hamcrest.CoreMatchers
import org.junit.Rule

class BlockingHttpServerTest extends ConcurrentSpec {
    @Rule
    TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider(getClass())

    def server = new BlockingHttpServer(1000)

    def "serves HTTP resources"() {
        server.expect("a")
        server.expect("b")
        server.start()

        when:
        def a = server.uri("a")
        def b = server.uri("b")

        then:
        a.scheme == "http"
        b.scheme == "http"
        a.toURL().text == "hi"
        b.toURL().text == "hi"

        when:
        server.stop()

        then:
        noExceptionThrown()
    }

    def "succeeds when expected serial requests are made"() {
        given:
        server.expect("a")
        server.expect("b")
        server.expect("c")
        server.start()

        when:
        server.uri("a").toURL().text
        server.uri("b").toURL().text
        server.uri("c").toURL().text
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can specify the content to return in response to a GET request"() {
        def file = tmpDir.createFile("thing.txt")
        file.text = "123"

        given:
        server.expect(server.get("a"))
        server.expect(server.get("b").sendFile(file))
        server.expect(server.get("c").send("this is the content"))
        server.start()

        when:
        server.uri("a").toURL().text == ""
        server.uri("b").toURL().text == "123"
        server.uri("c").toURL().text == "this is the content"
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can specify to return 404 response to a GET request"() {
        given:
        server.expect(server.get("a").missing())
        server.start()

        when:
        def connection = server.uri("a").toURL().openConnection()
        connection.inputStream.text

        then:
        thrown(FileNotFoundException)
        connection.responseCode == 404

        when:
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can specify to return 500 response to a GET request"() {
        given:
        server.expect(server.get("a").broken())
        server.start()

        when:
        def connection = server.uri("a").toURL().openConnection()
        connection.inputStream.text

        then:
        thrown(IOException)
        connection.responseCode == 500

        when:
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can specify an action to run to generate response for a GET request"() {
        given:
        server.expect(server.get("a", { e ->
            e.sendResponseHeaders(200, 0)
        }))
        server.expect(server.get("b", { e ->
            def str = "this is the content"
            e.sendResponseHeaders(200, str.bytes.length)
            e.responseBody.write(str.bytes)
        }))
        server.start()

        when:
        server.uri("a").toURL().text == ""
        server.uri("b").toURL().text == "this is the content"
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can verify that user agent matches particular criteria"() {
        def criteria = CoreMatchers.equalTo("some-agent")

        given:
        server.expect(server.get("a").expectUserAgent(criteria))
        server.expect(server.get("b").expectUserAgent(criteria))
        server.expect(server.get("c").expectUserAgent(criteria))
        server.start()

        when:
        def connection = server.uri("a").toURL().openConnection()
        connection.setRequestProperty("user-agent", "some-agent")

        then:
        connection.responseCode == 200
        connection.inputStream.text

        when:
        def connection2 = server.uri("b").toURL().openConnection()
        connection2.setRequestProperty("user-agent", "not-correct")
        connection2.inputStream.text

        then:
        thrown(IOException)
        connection2.responseCode == 500

        when:
        def connection3 = server.uri("c").toURL().openConnection()
        connection3.inputStream.text

        then:
        thrown(IOException)
        connection3.responseCode == 500

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes[0].message == 'Failed to handle GET /b'
        e.causes[0].cause.message == 'Unexpected user agent in request'
        e.causes.message == [
            'Failed to handle GET /b',
            'Failed to handle GET /c'
        ]
    }

    def "can expect a PUT request"() {
        given:
        server.expect(server.put("a"))
        server.start()

        when:
        def connection = server.uri("a").toURL().openConnection()
        connection.requestMethod = 'PUT'
        connection.doOutput = true
        connection.outputStream << "123".bytes
        connection.inputStream.text

        server.stop()

        then:
        noExceptionThrown()
    }

    def "can expect a POST request and provide action to generate response"() {
        given:
        server.expect(server.post("a", { e ->
            def str = "this is the content"
            e.sendResponseHeaders(200, str.bytes.length)
            e.responseBody.write(str.bytes)
        }))
        server.start()

        when:
        def connection = server.uri("a").toURL().openConnection()
        connection.requestMethod = 'POST'
        connection.doOutput = true
        connection.outputStream << "123".bytes
        def result = connection.inputStream.text

        server.stop()

        then:
        result == "this is the content"
        noExceptionThrown()
    }

    def "can send partial response and block"() {
        given:
        def request1 = server.get("a").sendSomeAndBlock(new byte[2048])
        def request2 = server.get("b").sendSomeAndBlock(new byte[2048])
        server.expect(request1)
        server.expect(request2)
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
                instant.aDone
                server.uri("b").toURL().text
            }
            request1.waitUntilBlocked()
            instant.aBlocked
            request1.release()
            request2.waitUntilBlocked()
            request2.release()
        }
        server.stop()

        then:
        instant.aDone > instant.aBlocked
    }

    def "can call from client code"() {
        server.start()
        def script = TestUtil.createScript """
            def prefix = "a"
            ${server.callFromBuild("a1")}
            ${server.callFromBuildUsingExpression("prefix + '2'")}
        """

        given:
        server.expect("a1")
        server.expect("a2")

        when:
        script.run()
        server.stop()

        then:
        noExceptionThrown()
    }

    def "client code fails when making unexpected request"() {
        server.start()
        def script = TestUtil.createScript """
            def prefix = "a"
            ${server.callFromBuild("a1")}
            ${server.callFromBuildUsingExpression("prefix + '2'")}
        """

        given:
        server.expect("a1")
        server.expect("other")

        when:
        script.run()

        then:
        def e = thrown(RuntimeException)
        e.message == "Received error response from ${server.uri}/a2"

        when:
        server.stop()

        then:
        def e2 = thrown(RuntimeException)
        e2.message == 'Failed to handle all HTTP requests.'
        e2.causes.message == ['Unexpected request GET /a2 received. Waiting for [GET /other], already received []']
    }

    def "succeeds when expected concurrent requests are made"() {
        given:
        server.expectConcurrent("a", "b", "c")
        server.start()

        when:
        async {
            start { server.uri("a").toURL().text }
            start { server.uri("b").toURL().text }
            start { server.uri("c").toURL().text }
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can wait for multiple concurrent requests to same URL"() {
        given:
        server.expectConcurrent("a", "a", "a")
        server.start()

        when:
        async {
            start { server.uri("a").toURL().text }
            start { server.uri("a").toURL().text }
            start { server.uri("a").toURL().text }
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can wait for multiple concurrent requests to same URL using request objects"() {
        given:
        def expected = server.get("a")
        server.expectConcurrent(expected, expected, expected)
        server.start()

        when:
        async {
            start { server.uri("a").toURL().text }
            start { server.uri("a").toURL().text }
            start { server.uri("a").toURL().text }
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can send partial response and block for concurrent requests"() {
        given:
        def request1 = server.get("a").sendSomeAndBlock(new byte[2048])
        def request2 = server.get("b").sendSomeAndBlock(new byte[2048])
        server.expectConcurrent(request1, request2)
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
                instant.aDone
            }
            start {
                server.uri("b").toURL().text
                instant.bDone
            }
            request1.waitUntilBlocked()
            instant.aBlocked
            request1.release()
            request2.waitUntilBlocked()
            instant.bBlocked
            request2.release()
        }
        server.stop()

        then:
        instant.aDone > instant.aBlocked
        instant.bDone > instant.bBlocked
    }

    def "can wait for and release n concurrent requests"() {
        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "b", "c")
        server.start()

        when:
        async {
            start { server.uri("a").toURL().text }
            start { server.uri("b").toURL().text }
            handle.waitForAllPendingCalls()
            handle.release(1)
            start { server.uri("c").toURL().text }
            handle.waitForAllPendingCalls()
            handle.release(1)
            handle.release(1)
            handle.waitForAllPendingCalls()
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can wait for and release n concurrent requests to same URL"() {
        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "a", "a")
        server.start()

        when:
        async {
            start { server.uri("a").toURL().text }
            start { server.uri("a").toURL().text }
            handle.waitForAllPendingCalls()
            handle.release(1)
            start { server.uri("a").toURL().text }
            handle.waitForAllPendingCalls()
            handle.release(1)
            handle.release(1)
            handle.waitForAllPendingCalls()
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can wait for and release a specific concurrent request"() {
        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "b", "c")
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
                server.uri("b").toURL().text
            }
            start { server.uri("c").toURL().text }
            handle.waitForAllPendingCalls()
            handle.release("a")
            handle.waitForAllPendingCalls()
            handle.release("b")
            handle.waitForAllPendingCalls()
            handle.release("c")
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can wait for and release all concurrent requests"() {
        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "b", "c")
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
                server.uri("b").toURL().text
            }
            start { server.uri("c").toURL().text }
            handle.waitForAllPendingCalls()
            handle.release("a")
            handle.waitForAllPendingCalls()
            handle.releaseAll()
            handle.waitForAllPendingCalls()
            handle.releaseAll()
            handle.waitForAllPendingCalls()
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can wait for and release all concurrent requests to same URL"() {
        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "a", "b", "b")
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
                server.uri("b").toURL().text
            }
            start {
                server.uri("a").toURL().text
                server.uri("b").toURL().text
            }
            handle.waitForAllPendingCalls()
            handle.release("a")
            handle.release("a")
            handle.waitForAllPendingCalls()
            handle.release(1)
            handle.waitForAllPendingCalls()
            handle.releaseAll()
            handle.waitForAllPendingCalls()
            handle.releaseAll()
            handle.waitForAllPendingCalls()
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "can send partial response and block again after blocking for concurrent requests"() {
        given:
        def request1 = server.get("a").sendSomeAndBlock(new byte[2048])
        def request2 = server.get("b").sendSomeAndBlock(new byte[2048])
        def handle = server.expectConcurrentAndBlock(2, request1, request2)
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
                instant.aDone
            }
            start {
                server.uri("b").toURL().text
                instant.bDone
            }
            handle.waitForAllPendingCalls()
            handle.releaseAll()
            request1.waitUntilBlocked()
            instant.aBlocked
            request1.release()
            request2.waitUntilBlocked()
            instant.bBlocked
            request2.release()
        }
        server.stop()

        then:
        instant.aDone > instant.aBlocked
        instant.bDone > instant.bBlocked
    }

    def "can chain expectations"() {
        given:
        server.expectConcurrent("a", "b")
        server.expect("c")
        def handle = server.expectConcurrentAndBlock(2, "d", "e")
        server.expect("f")
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
            }
            start {
                server.uri("b").toURL().text
            }
            server.waitForRequests(2)
            server.uri("c").toURL().text
            start {
                server.uri("d").toURL().text
            }
            start {
                server.uri("e").toURL().text
            }
            handle.waitForAllPendingCalls()
            handle.releaseAll()
            server.uri("f").toURL().text
        }
        server.stop()

        then:
        noExceptionThrown()
    }

    def "fails on stop when expected serial request is not made"() {
        given:
        server.expect("a")
        server.expect("b")
        server.start()

        when:
        server.uri("a").toURL().text
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message.sort() == ['Did not receive expected requests. Waiting for [GET /b], already received []']
    }

    def "fails when request is received after serial expectations met"() {
        given:
        server.expect("a")
        server.start()

        when:
        server.uri("a").toURL().text
        server.uri("a").toURL().text

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message.sort() == ['Unexpected request GET /a received']
    }

    def "fails when request path does not match expected serial request"() {
        given:
        server.expect("a")
        server.start()

        when:
        def connection = server.uri("b").toURL().openConnection()

        then:
        connection.responseCode == 400
        connection.responseMessage == "Bad Request"

        when:
        connection.inputStream

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Unexpected request GET /b received. Waiting for [GET /a], already received []'
        ]
    }

    def "fails when request method does not match expected serial GET request"() {
        given:
        server.expect("a")
        server.start()

        when:
        def connection = server.uri("a").toURL().openConnection()
        connection.requestMethod = 'HEAD'

        then:
        connection.responseCode == 400
        connection.responseMessage == "Bad Request"

        when:
        connection.inputStream

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Unexpected request HEAD /a received. Waiting for [GET /a], already received []'
        ]
    }

    def "fails when request method does not match expected serial PUT request"() {
        given:
        server.expect(server.put("a"))
        server.start()

        when:
        server.uri("a").toURL().text

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Unexpected request GET /a received. Waiting for [PUT /a], already received []'
        ]
    }

    def "fails when request method does not match expected serial POST request"() {
        given:
        server.expect(server.post("a", { e -> e.sendResponseHeaders(200, 0) }))
        server.start()

        when:
        server.uri("a").toURL().text

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message.sort() == [
            'Unexpected request GET /a received. Waiting for [POST /a], already received []'
        ]
    }

    def "fails when some but not all expected parallel requests received"() {
        given:
        server.expectConcurrent("a", "b")
        server.start()

        when:
        server.uri("a").toURL().text

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message.sort() == [
            'Failed to handle GET /a due to a timeout waiting for other requests. Waiting for [GET /b], already received [GET /a]'
        ]
    }

    def "fails when expected parallel request received after other request has failed"() {
        given:
        server.expectConcurrent("a", "b")
        server.start()

        when:
        server.uri("a").toURL().text

        then:
        thrown(IOException)

        when:
        server.uri("b").toURL().text

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Failed to handle GET /a due to a timeout waiting for other requests. Waiting for [GET /b], already received [GET /a]',
            'Failed to handle GET /b due to a previous timeout. Waiting for [], already received [GET /a, GET /b]'
        ]
    }

    def "fails when some but not all expected parallel requests received when stop called"() {
        given:
        server.expectConcurrent("a", "b")
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
                // TODO - probably should fail
            }
            server.waitForRequests(1)
            server.stop()
        }

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Did not receive expected requests. Waiting for [GET /b], already received [GET /a]'
        ]
    }

    def "fails when request path does not match expected parallel request"() {
        given:
        server.expectConcurrent("a", "b")
        server.start()

        when:
        server.uri("c").toURL().text

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message.sort() == [
            'Unexpected request GET /c received. Waiting for [GET /a, GET /b], already received []'
        ]
    }

    def "fails when request path does not match expected blocking parallel request"() {
        def requestFailure = null

        given:
        def handle = server.expectConcurrentAndBlock("a", "b")
        server.start()

        when:
        async {
            start {
                try {
                    server.uri("c").toURL().text
                } catch (IOException e) {
                    requestFailure = e
                }
            }
            handle.waitForAllPendingCalls()
        }

        then:
        def waitException = thrown(RuntimeException)
        waitException.message == 'Unexpected request GET /c received. Waiting for 2 further requests, received [], released [], not yet received [GET /a, GET /b]'

        requestFailure instanceof IOException

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message.sort() == [
            'Unexpected request GET /c received. Waiting for 2 further requests, received [], released [], not yet received [GET /a, GET /b]'
        ]
    }

    def "fails when request method does not match expected parallel request"() {
        def failure1 = null
        def failure2 = null
        def failure3 = null

        given:
        server.expectConcurrent(server.get("a"), server.get("b"), server.put("c"))
        server.start()

        when:
        async {
            start {
                try {
                    def connection = server.uri("a").toURL().openConnection()
                    connection.requestMethod = 'HEAD'
                    connection.inputStream.text
                } catch (Throwable t) {
                    failure1 = t
                }
            }
            start {
                server.waitForRequests(1)
                try {
                    server.uri("b").toURL().text
                } catch (Throwable t) {
                    failure2 = t
                }
            }
            start {
                server.waitForRequests(2)
                try {
                    server.uri("c").toURL().text
                } catch (Throwable t) {
                    failure3 = t
                }
            }
        }

        then:
        failure1 instanceof IOException
        failure2 instanceof IOException
        failure3 instanceof IOException

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Unexpected request HEAD /a received. Waiting for [GET /a, GET /b, PUT /c], already received []',
            'Failed to handle GET /b due to unexpected request HEAD /a. Waiting for [GET /a, PUT /c], already received [GET /b]',
            'Unexpected request GET /c received. Waiting for [GET /a, PUT /c], already received [GET /b]'
        ]
    }

    def "fails when request method does not match expected blocking parallel request"() {
        def failure1 = null
        def failure2 = null
        def failure3 = null

        given:
        def handle = server.expectConcurrentAndBlock(server.get("a"), server.get("b"), server.put("c"))
        server.start()

        when:
        async {
            start {
                try {
                    def connection = server.uri("a").toURL().openConnection()
                    connection.requestMethod = 'HEAD'
                    connection.inputStream.text
                } catch (Throwable t) {
                    failure1 = t
                }
            }
            start {
                server.waitForRequests(1)
                try {
                    server.uri("b").toURL().text
                } catch (Throwable t) {
                    failure2 = t
                }
            }
            start {
                server.waitForRequests(2)
                try {
                    server.uri("c").toURL().text
                } catch (Throwable t) {
                    failure3 = t
                }
            }
            server.waitForRequests(3)
            handle.waitForAllPendingCalls()
        }

        then:
        def waitException = thrown(RuntimeException)
        waitException.message == 'Unexpected request HEAD /a received. Waiting for 2 further requests, received [GET /b], released [], not yet received [GET /a, PUT /c]'

        failure1 instanceof IOException
        failure2 instanceof IOException
        failure3 instanceof IOException

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Unexpected request HEAD /a received. Waiting for 3 further requests, received [], released [], not yet received [GET /a, GET /b, PUT /c]',
            'Failed to handle GET /b due to unexpected request HEAD /a. Waiting for 2 further requests, received [GET /b], released [], not yet received [GET /a, PUT /c]',
            'Unexpected request GET /c received. Waiting for 2 further requests, received [GET /b], released [], not yet received [GET /a, PUT /c]',
        ]
    }

    def "fails when additional requests are made after parallel expectations are met"() {
        given:
        server.expectConcurrent("a", "b")
        server.start()

        when:
        async {
            start { server.uri("a").toURL().text }
            start { server.uri("b").toURL().text }
        }
        server.uri("c").toURL().text

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.cause.message == 'Unexpected request GET /c received'
        e.causes.message.sort() == ['Unexpected request GET /c received']
    }

    def "fails when some but not all expected parallel requests received while waiting"() {
        def requestFailure = null

        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "b", "c")
        server.start()

        when:
        async {
            start {
                try {
                    server.uri("a").toURL().text
                } catch (Throwable e) {
                    requestFailure = e
                }
            }
            handle.waitForAllPendingCalls()
        }

        then:
        def waitError = thrown(RuntimeException)
        waitError.message == 'Timeout waiting for expected requests. Waiting for 1 further requests, received [GET /a], released [], not yet received [GET /b, GET /c]'

        requestFailure instanceof IOException

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Failed to handle GET /a due to a timeout waiting for other requests. Waiting for 1 further requests, received [GET /a], released [], not yet received [GET /b, GET /c]'
        ]
    }

    def "fails when expected parallel request received after waiting has failed"() {
        def requestFailure = null

        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "b", "c")
        server.start()

        when:
        async {
            start {
                try {
                    server.uri("a").toURL().text
                } catch (Throwable e) {
                    requestFailure = e
                }
            }
            handle.waitForAllPendingCalls()
        }

        then:
        def waitError = thrown(RuntimeException)
        waitError.message == 'Timeout waiting for expected requests. Waiting for 1 further requests, received [GET /a], released [], not yet received [GET /b, GET /c]'

        requestFailure instanceof IOException

        when:
        server.uri("b").toURL().text

        then:
        thrown(IOException)

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Failed to handle GET /a due to a timeout waiting for other requests. Waiting for 1 further requests, received [GET /a], released [], not yet received [GET /b, GET /c]',
            'Failed to handle GET /b due to a previous timeout. Waiting for 0 further requests, received [GET /a, GET /b], released [], not yet received [GET /c]'
        ]
    }

    def "fails when unexpected request received while other request is waiting "() {
        def failure1 = null
        def failure2 = null

        given:
        server.expectConcurrent("a", "b")
        server.start()

        when:
        async {
            start {
                try {
                    server.uri("a").toURL().text
                } catch (Throwable t) {
                    failure1 = t
                }
            }
            start {
                server.waitForRequests(1)
                try {
                    server.uri("c").toURL().text
                } catch (Throwable t) {
                    failure2 = t
                }
            }
        }
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Failed to handle GET /a due to unexpected request GET /c. Waiting for [GET /b], already received [GET /a]',
            'Unexpected request GET /c received. Waiting for [GET /b], already received [GET /a]'
        ]

        failure1 instanceof IOException
        failure2 instanceof IOException
    }

    def "fails when unexpected request received while waiting"() {
        def failure1 = null
        def failure2 = null

        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "b", "c")
        server.start()

        when:
        async {
            start {
                try {
                    server.uri("a").toURL().text
                } catch (Throwable t) {
                    failure1 = t
                }
            }
            start {
                server.waitForRequests(1)
                try {
                    server.uri("d").toURL().text
                } catch (Throwable t) {
                    failure2 = t
                }
            }
            handle.waitForAllPendingCalls()
        }

        then:
        def waitError = thrown(RuntimeException)
        waitError.message == "Unexpected request GET /d received. Waiting for 1 further requests, received [GET /a], released [], not yet received [GET /b, GET /c]"

        failure1 instanceof IOException
        failure2 instanceof IOException

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Failed to handle GET /a due to unexpected request GET /d. Waiting for 1 further requests, received [GET /a], released [], not yet received [GET /b, GET /c]',
            'Unexpected request GET /d received. Waiting for 1 further requests, received [GET /a], released [], not yet received [GET /b, GET /c]'
        ]
    }

    def "fails when too many concurrent requests received while waiting"() {
        def failure1 = null
        def failure2 = null
        def failure3 = null

        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "b", "c")
        server.start()

        when:
        async {
            start {
                try {
                    server.uri("a").toURL().text
                } catch (Throwable t) {
                    failure1 = t
                }
            }
            start {
                server.waitForRequests(1)
                try {
                    server.uri("b").toURL().text
                } catch (Throwable t) {
                    failure2 = t
                }
            }
            start {
                server.waitForRequests(2)
                try {
                    server.uri("c").toURL().text
                } catch (Throwable t) {
                    failure3 = t
                }
            }
            server.waitForRequests(3)
            handle.waitForAllPendingCalls()
        }

        then:
        def waitError = thrown(RuntimeException)
        waitError.message == "Unexpected request GET /c received. Waiting for 0 further requests, received [GET /a, GET /b], released [], not yet received [GET /c]"

        failure1 instanceof IOException
        failure2 instanceof IOException
        failure3 instanceof IOException

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        // TODO - message should indicate that /c was expected but there were too many concurrent requests
        e.causes.message.sort() == [
            'Failed to handle GET /a due to unexpected request GET /c. Waiting for 0 further requests, received [GET /a, GET /b], released [], not yet received [GET /c]',
            'Failed to handle GET /b due to unexpected request GET /c. Waiting for 0 further requests, received [GET /a, GET /b], released [], not yet received [GET /c]',
            'Unexpected request GET /c received. Waiting for 0 further requests, received [GET /a, GET /b], released [], not yet received [GET /c]',
        ]
    }

    def "fails when attempting to wait before server is started"() {
        given:
        def handle1 = server.expectConcurrentAndBlock("a", "b")
        def handle2 = server.expectConcurrentAndBlock(2, "c", "d")
        def request1 = server.get("e").sendSomeAndBlock(new byte[2048])
        server.expect(request1)

        when:
        handle1.waitForAllPendingCalls()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Cannot wait as the server is not running."

        when:
        handle2.waitForAllPendingCalls()

        then:
        def e2 = thrown(IllegalStateException)
        e2.message == "Cannot wait as the server is not running."

        when:
        request1.waitUntilBlocked()

        then:
        def e3 = thrown(IllegalStateException)
        e3.message == "Cannot wait as the server is not running."

        when:
        server.stop()

        then:
        def e4 = thrown(RuntimeException)
        e4.message == 'Failed to handle all HTTP requests.'
        e4.causes.message == [
            'Did not handle all expected requests. Waiting for 2 further requests, received [], released [], not yet received [GET /a, GET /b]',
            'Did not handle all expected requests. Waiting for 2 further requests, received [], released [], not yet received [GET /c, GET /d]',
            'Did not receive expected requests. Waiting for [GET /e], already received []'
        ]
    }

    def "fails when attempting to wait before previous requests released"() {
        given:
        server.expectConcurrentAndBlock(2, "a", "b")
        def handle2 = server.expectConcurrentAndBlock(2, "c", "d")
        server.start()

        when:
        handle2.waitForAllPendingCalls()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Cannot wait as no requests have been released. Waiting for [GET /a, GET /b], received []."

        when:
        server.stop()

        then:
        def e2 = thrown(RuntimeException)
        e2.message == 'Failed to handle all HTTP requests.'
        e2.causes.message == [
            'Did not handle all expected requests. Waiting for 2 further requests, received [], released [], not yet received [GET /a, GET /b]',
            'Did not handle all expected requests. Waiting for 2 further requests, received [], released [], not yet received [GET /c, GET /d]',
        ]
    }

    def "fails when request is not released when stop called"() {
        def failure1 = null
        def failure2 = null

        given:
        def handle = server.expectConcurrentAndBlock(2, "a", "b", "c")
        server.start()

        when:
        async {
            start {
                try {
                    server.uri("a").toURL().text
                } catch (Throwable t) {
                    failure1 = t
                }
            }
            start {
                server.waitForRequests(1)
                try {
                    server.uri("b").toURL().text
                } catch (Throwable t) {
                    failure2 = t
                }
            }
            handle.waitForAllPendingCalls()
            // Do not release the requests here
        }

        then:
        failure1 instanceof IOException
        failure2 instanceof IOException

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.causes.message == [
            'Failed to handle GET /a due to a timeout waiting to be released. Waiting for 0 further requests, received [GET /a, GET /b], released [], not yet received [GET /c]',
            'Failed to handle GET /b due to a timeout waiting to be released. Waiting for 0 further requests, received [GET /a, GET /b], released [], not yet received [GET /c]'
        ]
    }

    def "fails when request is not released after sending partial response"() {
        given:
        def request = server.get("a").sendSomeAndBlock(new byte[2048])
        def handle = server.expectConcurrentAndBlock(1, request)
        server.start()

        when:
        async {
            start {
                server.uri("a").toURL().text
            }
            start {
                handle.waitForAllPendingCalls()
                handle.releaseAll()
                // Should release the request here
            }
        }

        then:
        // TODO - reading from the URL should fail
        noExceptionThrown()

        when:
        server.stop()

        then:
        def e = thrown(RuntimeException)
        e.message == 'Failed to handle all HTTP requests.'
        e.cause.message == 'Failed to handle GET /a'
        e.cause.cause.message == 'Timeout waiting to be released after sending some content.'
    }

    def "fails when attempting to wait for a request that has not been released to send partial response"() {
        given:
        server.expect("a")
        def request1 = server.get("b").sendSomeAndBlock(new byte[2048])
        def request2 = server.get("c").sendSomeAndBlock(new byte[2048])
        server.expectConcurrentAndBlock(1, request1)
        server.expect(request2)
        server.start()

        when:
        request1.waitUntilBlocked()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Cannot wait as the request to 'b' has not been released yet."

        when:
        request2.waitUntilBlocked()

        then:
        def e2 = thrown(IllegalStateException)
        e2.message == "Cannot wait as no requests have been released. Waiting for [GET /b], received []."

        when:
        server.stop()

        then:
        def e3 = thrown(RuntimeException)
        e3.message == 'Failed to handle all HTTP requests.'
        e3.causes.message == [
            'Did not receive expected requests. Waiting for [GET /a], already received []',
            'Did not handle all expected requests. Waiting for 1 further requests, received [], released [], not yet received [GET /b]',
            'Did not receive expected requests. Waiting for [GET /c], already received []'
        ]
    }

}

