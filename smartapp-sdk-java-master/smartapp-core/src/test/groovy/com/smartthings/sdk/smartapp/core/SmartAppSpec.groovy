package com.smartthings.sdk.smartapp.core

import com.smartthings.sdk.smartapp.core.extensions.EventHandler
import com.smartthings.sdk.smartapp.core.models.*
import spock.lang.Specification
import spock.lang.Unroll

class SmartAppSpec extends Specification {

    @Unroll
    void 'it should execute an #request.lifecycle smartapp request using default handlers and receive a #expectedStatus status'() {
        given:
        SmartApp smartApp = SmartApp.of { spec ->
            spec
                .install({ req -> Response.ok() })
                .update({ req -> Response.ok() })
                .configuration({ req -> Response.ok() })
                .event(EventHandler.of { eventSpec ->
                    eventSpec
                        .onSubscription('switch', { event ->
                            // do something
                        })
                        .onSchedule('nightly', { event ->
                            // do something
                        })
                        .onEvent(
                            { event ->
                                // test event
                                true
                            },
                            { event ->
                                // do something
                            }
                        )
                        .onSuccess({ request ->
                            Response.ok(new EventResponseData())
                        })
                })
        }

        when:
        ExecutionResponse response = smartApp.execute(request)

        then:
        assert response.statusCode == expectedStatus

        where:
        request << [
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.INSTALL)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.UNINSTALL)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.UPDATE)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.CONFIGURATION)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.EVENT)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.PING)
                    .executionId(UUID.randomUUID().toString())
                    .pingData(
                        new PingData()
                            .challenge(UUID.randomUUID().toString())
                    ),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.CONFIRMATION)
                    .confirmationData(new ConfirmationData().confirmationUrl("https://example.com/confirm"))
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.OAUTH_CALLBACK)
                    .executionId(UUID.randomUUID().toString())
        ]

        expectedStatus << [
            200, // INSTALL
            200, // UNINSTALL
            200, // UPDATE
            200, // CONFIGURATION
            200, // EVENT
            200, // PING
            200, // CONFIRMATION
            404  // OAUTH_CALLBACK
        ]
    }

    @Unroll
    void 'request preprocessor should get called for #request.lifecycle lifecycle event'() {
        given:
        RequestPreprocessor requestPreprocessor = Mock(RequestPreprocessor.class);
        SmartApp smartApp = SmartApp.of { spec ->
            spec
                .install({ req -> Response.ok() })
                .update({ req -> Response.ok() })
                .configuration({ req -> Response.ok() })
                .event(EventHandler.of { eventSpec ->
                    eventSpec
                        .onSubscription('switch', { event ->
                            // do something
                        })
                        .onSchedule('nightly', { event ->
                            // do something
                        })
                        .onEvent(
                            { event ->
                                // test event
                                true
                            },
                            { event ->
                                // do something
                            }
                        )
                        .onSuccess({ request ->
                            Response.ok(new EventResponseData())
                        })
                })
                .addRequestPreprocessor(requestPreprocessor)
        }

        when:
        ExecutionResponse response = smartApp.execute(request)

        then:
        assert response.statusCode == expectedStatus
        1 * requestPreprocessor.process(request)

        where:
        request << [
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.INSTALL)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.UNINSTALL)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.UPDATE)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.CONFIGURATION)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.EVENT)
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.PING)
                    .executionId(UUID.randomUUID().toString())
                    .pingData(
                        new PingData()
                            .challenge(UUID.randomUUID().toString())
                    ),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.CONFIRMATION)
                    .confirmationData(new ConfirmationData().confirmationUrl("https://example.com/confirm"))
                    .executionId(UUID.randomUUID().toString()),
                new ExecutionRequest()
                    .lifecycle(AppLifecycle.OAUTH_CALLBACK)
                    .executionId(UUID.randomUUID().toString())
        ]

        expectedStatus << [
            200, // INSTALL
            200, // UNINSTALL
            200, // UPDATE
            200, // CONFIGURATION
            200, // EVENT
            200, // PING
            200, // CONFIRMATION
            404  // OAUTH_CALLBACK
        ]
    }

    void 'request preprocessors should get called for in the order they are added'() {
        given:
        RequestPreprocessor requestPreprocessor1 = Mock(RequestPreprocessor.class);
        RequestPreprocessor requestPreprocessor2 = Mock(RequestPreprocessor.class);
        RequestPreprocessor requestPreprocessor3 = Mock(RequestPreprocessor.class);
        SmartApp smartApp = SmartApp.of { spec ->
            spec
                .install({ req -> Response.ok() })
                .update({ req -> Response.ok() })
                .configuration({ req -> Response.ok() })
                .event(EventHandler.of { eventSpec ->
                    eventSpec
                        .onSubscription('switch', { event ->
                            // do something
                        })
                        .onSchedule('nightly', { event ->
                            // do something
                        })
                        .onEvent(
                            { event ->
                                // test event
                                true
                            },
                            { event ->
                                // do something
                            }
                        )
                        .onSuccess({ request ->
                            Response.ok(new EventResponseData())
                        })
                })
                .addRequestPreprocessor(requestPreprocessor1)
                .addRequestPreprocessor(requestPreprocessor2)
                .addRequestPreprocessor(requestPreprocessor3)
        }

        ExecutionRequest request = new ExecutionRequest()
                .lifecycle(AppLifecycle.INSTALL)
                .executionId(UUID.randomUUID().toString())

        when:
        ExecutionResponse response = smartApp.execute(request)

        then:
        1 * requestPreprocessor1.process(request)

        then:
        1 * requestPreprocessor2.process(request)

        then:
        1 * requestPreprocessor3.process(request)
        assert response.statusCode == 200
    }
}
