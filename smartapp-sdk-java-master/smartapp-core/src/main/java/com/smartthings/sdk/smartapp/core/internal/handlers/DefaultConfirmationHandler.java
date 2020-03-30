package com.smartthings.sdk.smartapp.core.internal.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.models.AppLifecycle;
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse;
import com.smartthings.sdk.smartapp.core.models.UninstallResponseData;


public class DefaultConfirmationHandler implements ConfirmationHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public ExecutionResponse handle(ExecutionRequest request) throws Exception {
        if (request.getLifecycle() != AppLifecycle.CONFIRMATION) {
            log.error("Invalid lifecycle for CONFIRMATION handler.  lifecycle={}",
                request.getLifecycle());
            throw new IllegalArgumentException("Unsupported lifecycle for ConfirmationHandler");
        }

        if (log.isInfoEnabled()) {
            log.info("Received confirmation lifecycle event. To complete registration," +
                " execute a GET request to " + request.getConfirmationData().getConfirmationUrl());
        }

        return Response.ok(new UninstallResponseData());
    }
}
