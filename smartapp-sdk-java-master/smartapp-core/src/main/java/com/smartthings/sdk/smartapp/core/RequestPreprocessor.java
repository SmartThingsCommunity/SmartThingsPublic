package com.smartthings.sdk.smartapp.core;

import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;


/**
 * This class represents functions that act on requests before they are passed
 * on to the request handler.
 */
public interface RequestPreprocessor {
    void process(ExecutionRequest executionRequest);
}
