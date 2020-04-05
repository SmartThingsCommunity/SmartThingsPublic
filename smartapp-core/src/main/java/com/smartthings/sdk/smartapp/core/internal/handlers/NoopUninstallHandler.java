package com.smartthings.sdk.smartapp.core.internal.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.extensions.UninstallHandler;
import com.smartthings.sdk.smartapp.core.models.*;

public class NoopUninstallHandler implements UninstallHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NoopUninstallHandler.class);

    @Override
    public ExecutionResponse handle(ExecutionRequest request) throws Exception {
        if (AppLifecycle.UNINSTALL != request.getLifecycle()) {
            LOG.error("Invalid lifecycle for UNINSTALL handler.  lifecycle={}", request.getLifecycle());
            throw new IllegalArgumentException("Unsupported lifecycle for UninstallHandler");
        }

        return Response.ok(
            new UninstallResponseData()
        );
    }
}

