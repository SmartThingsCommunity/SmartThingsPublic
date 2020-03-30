package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.SmartApp;
import com.smartthings.sdk.smartapp.core.internal.handlers.DefaultPingHandler;
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse;
import com.smartthings.sdk.smartapp.core.models.UninstallResponseData;
import app.handlers.AppConfigurationHandler;
import app.handlers.AppEventHandler;
import app.handlers.AppUpdateInstallHandler;


public class App implements RequestHandler<ExecutionRequest, ExecutionResponse> {
    private final Logger LOG = LoggerFactory.getLogger(App.class);

    private final SmartApp smartApp = SmartApp.of(spec ->
        spec
            // We don't need to include a PingHandler because the default is sufficient. This is included
            // here to help with debugging a new SmartApp.
            .ping(new DefaultPingHandler() {
                @Override
                public ExecutionResponse handle(ExecutionRequest executionRequest) throws Exception {
                    LOG.debug("PING: executionRequest = " + executionRequest);
                    return super.handle(executionRequest);
                }
            })
            .configuration(new AppConfigurationHandler())
            .install(new AppUpdateInstallHandler.AppInstallHandler())
            .update(new AppUpdateInstallHandler.AppUpdateHandler())
            .event(new AppEventHandler())
            .uninstall(request -> {
                LOG.debug("UNINSTALL: executionRequest = " + request);
                return Response.ok(new UninstallResponseData());
            })
            );

    @Override
    public ExecutionResponse handleRequest(ExecutionRequest request, Context context) {
        LOG.info("got request with input " + request);
        ExecutionResponse response = smartApp.execute(request);
        LOG.info("returning " + response);
        return response;
    }
}
