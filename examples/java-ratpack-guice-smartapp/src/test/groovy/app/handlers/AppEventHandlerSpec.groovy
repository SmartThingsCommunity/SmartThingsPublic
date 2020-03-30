package app.handlers

import com.smartthings.sdk.smartapp.core.Response
import com.smartthings.sdk.smartapp.core.models.AppLifecycle
import com.smartthings.sdk.smartapp.core.models.EventResponseData
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import spock.lang.Specification


class AppEventHandlerSpec extends Specification {
    private final AppEventHandler eventHandler = new AppEventHandler()

    void "respond with simple 200"() {
        given:
        ExecutionRequest executionRequest = new ExecutionRequest().lifecycle(AppLifecycle.EVENT)
        ExecutionResponse expectedResponse = Response.ok(new EventResponseData())

        when:
        ExecutionResponse executionResponse = eventHandler.handle(executionRequest)

        then:
        executionResponse == expectedResponse
    }
}
