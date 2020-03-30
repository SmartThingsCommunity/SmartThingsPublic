package com.smartthings.sdk.smartapp.core.internal.handlers;

import com.smartthings.sdk.smartapp.core.Action;
import com.smartthings.sdk.smartapp.core.models.*;
import java.util.function.Predicate;

public final class PredicateSingleEventHandler {

    private final Predicate<Event> predicate;
    private final Action<Event> action;

    public PredicateSingleEventHandler(Predicate<Event> predicate, Action<Event> action) {
        this.predicate = predicate;
        this.action = action;
    }

    public boolean test(Event event) {
        return predicate.test(event);
    }

    public void execute(Event event) throws Exception {
        action.execute(event);
    }
}
