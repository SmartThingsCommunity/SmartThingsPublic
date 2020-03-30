package com.smartthings.sdk.smartapp.core.internal.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartthings.sdk.smartapp.core.Action;
import com.smartthings.sdk.smartapp.core.EventSpec;
import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.extensions.EventHandler;
import com.smartthings.sdk.smartapp.core.models.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;


@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public class DefaultEventHandler implements EventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventHandler.class);

    private final List<PredicateSingleEventHandler> eventHandlers;
    private final Predicate<Throwable> failOnError;
    private final BiFunction<ExecutionRequest, Throwable, ExecutionResponse> onError;
    private final Function<ExecutionRequest, ExecutionResponse> onSuccess;

    private DefaultEventHandler(
        List<PredicateSingleEventHandler> eventHandlers,
        Predicate<Throwable> failOnError,
        BiFunction<ExecutionRequest, Throwable, ExecutionResponse> onError,
        Function<ExecutionRequest, ExecutionResponse> onSuccess
    ) {
        this.eventHandlers = eventHandlers;
        this.failOnError = failOnError;
        this.onError = onError;
        this.onSuccess = onSuccess;
    }

    @Override
    public ExecutionResponse handle(ExecutionRequest request) throws Exception {
        if (AppLifecycle.EVENT != request.getLifecycle()) {
            LOG.error("Invalid lifecycle for EXECUTE handler.  lifecycle={}", request.getLifecycle());
            throw new IllegalArgumentException("Unsupported lifecycle for ExecuteHandler");
        }

        EventData data = request.getEventData();
        List<Event> events = data != null && data.getEvents() != null ? data.getEvents() : Collections.emptyList();

        for (Event event : events) {
            PredicateSingleEventHandler handler = findHandler(event);
            if (handler != null) {
                try {
                    handler.execute(event);
                } catch (Exception e) {
                    if (failOnError.test(e)) {
                        return onError.apply(request, e);
                    }
                }
            }
        }

        return onSuccess.apply(request);
    }

    private PredicateSingleEventHandler findHandler(Event event) {
        return eventHandlers.stream()
            .filter(handler -> handler.test(event))
            .findFirst()
            .orElse(null);
    }

    public static class EventSpecImpl implements EventSpec {
        private List<PredicateSingleEventHandler> eventHandlers = new ArrayList<>();
        private Predicate<Throwable> failOnError = t -> true;
        private BiFunction<ExecutionRequest, Throwable, ExecutionResponse> onError = (request, t) -> {
            EventData data = request.getEventData();
            InstalledApp app = data.getInstalledApp();
            String locationId = app.getLocationId();
            String installedAppId = app.getInstalledAppId();
            LOG.error("event_handler_failed locationId={}, installedAppId={}", locationId, installedAppId, t);
            return Response.status(500);
        };
        private Function<ExecutionRequest, ExecutionResponse> onSuccess = request -> Response.ok(
            new EventResponseData()
        );

        @Override
        public EventSpec onMode(String modeId, Action<Event> action) {
            if (modeId == null || modeId.isEmpty()) {
                throw new IllegalArgumentException("Mode ID must not be null or empty.");
            }

            if (action == null) {
                throw new IllegalArgumentException("Action must not be null or empty.");
            }

            Predicate<Event> predicate = (event) ->
                event.getEventType() == EventType.MODE_EVENT &&
                    modeId.equals(event.getModeEvent().getModeId());

            this.eventHandlers.add(new PredicateSingleEventHandler(predicate, action));
            return this;
        }

        @Override
        public EventSpec onSchedule(String scheduleName, Action<Event> action) {
            if (scheduleName == null || scheduleName.isEmpty()) {
                throw new IllegalArgumentException("Schedule name must not be null or empty.");
            }

            if (action == null) {
                throw new IllegalArgumentException("Action must not be null or empty.");
            }

            Predicate<Event> predicate = (event) ->
                event.getEventType() == EventType.TIMER_EVENT &&
                    scheduleName.equals(event.getTimerEvent().getName());

            this.eventHandlers.add(new PredicateSingleEventHandler(predicate, action));
            return this;
        }

        @Override
        public EventSpec onSubscription(String subscriptionName, Action<Event> action) {
            if (subscriptionName == null || subscriptionName.isEmpty()) {
                throw new IllegalArgumentException("Subscription name must not be null or empty.");
            }

            if (action == null) {
                throw new IllegalArgumentException("Action must not be null or empty.");
            }

            Predicate<Event> predicate = (event) ->
                event.getEventType() == EventType.DEVICE_EVENT &&
                subscriptionName.equals(event.getDeviceEvent().getSubscriptionName());

            this.eventHandlers.add(new PredicateSingleEventHandler(predicate, action));
            return this;
        }

        @Override
        public EventSpec onEvent(Predicate<Event> predicate, Action<Event> action) {
            this.eventHandlers.add(new PredicateSingleEventHandler(predicate, action));
            return this;
        }

        @Override
        public EventSpec failOnError(Predicate<Throwable> failOnError) {
            this.failOnError = failOnError;
            return this;
        }

        @Override
        public EventSpec onError(BiFunction<ExecutionRequest, Throwable, ExecutionResponse> onError) {
            this.onError = onError;
            return this;
        }

        @Override
        public EventSpec onSuccess(Function<ExecutionRequest, ExecutionResponse> onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }

        public EventHandler build() {
            return new DefaultEventHandler(eventHandlers, failOnError, onError, onSuccess);
        }
    }
}
