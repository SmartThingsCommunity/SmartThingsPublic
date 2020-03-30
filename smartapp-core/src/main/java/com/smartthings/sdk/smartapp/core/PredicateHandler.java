package com.smartthings.sdk.smartapp.core;


import com.smartthings.sdk.smartapp.core.models.*;

import java.util.function.Predicate;

public final class PredicateHandler {

    private final Predicate<ExecutionRequest> predicate;
    private final Handler handler;

    private PredicateHandler(Predicate<ExecutionRequest> predicate, Handler handler) {
        this.predicate = predicate;
        this.handler = handler;
    }

    public boolean test(ExecutionRequest request) {
        return predicate.test(request);
    }

    public ExecutionResponse handle(ExecutionRequest request) throws Exception {
        return handler.handle(request);
    }

    public static  PredicateHandler of(Predicate<ExecutionRequest> predicate, Handler handler) {
        return new PredicateHandler(predicate, handler);
    }
}
