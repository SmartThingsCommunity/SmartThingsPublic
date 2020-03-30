package app.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.extensions.EventHandler;
import com.smartthings.sdk.smartapp.core.models.EventData;
import com.smartthings.sdk.smartapp.core.models.EventResponseData;
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse;


public class AppEventHandler implements EventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AppEventHandler.class);

    @Override
    public ExecutionResponse handle(ExecutionRequest executionRequest) throws Exception {
        EventData eventData = executionRequest.getEventData();
        LOG.debug("EVENT: executionRequest = " + executionRequest);
        return Response.ok(new EventResponseData());
    }
}
