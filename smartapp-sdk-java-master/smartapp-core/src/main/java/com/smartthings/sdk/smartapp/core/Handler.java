package com.smartthings.sdk.smartapp.core;

import com.smartthings.sdk.smartapp.core.models.*;

@FunctionalInterface
public interface Handler {
    ExecutionResponse handle(ExecutionRequest request) throws Exception;
}

