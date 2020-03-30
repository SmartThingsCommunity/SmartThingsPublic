package com.smartthings.sdk.smartapp.core;

import com.smartthings.sdk.smartapp.core.models.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EventSpec {
    EventSpec onMode(String modeId, Action<Event> action);
    EventSpec onSchedule(String scheduleName, Action<Event> action);
    EventSpec onSubscription(String subscriptionName, Action<Event> action);
    EventSpec onEvent(Predicate<Event> predicate, Action<Event> action);
    EventSpec failOnError(Predicate<Throwable> failOnError);
    EventSpec onError(BiFunction<ExecutionRequest, Throwable, ExecutionResponse> onError);
    EventSpec onSuccess(Function<ExecutionRequest, ExecutionResponse> onSuccess);
}
