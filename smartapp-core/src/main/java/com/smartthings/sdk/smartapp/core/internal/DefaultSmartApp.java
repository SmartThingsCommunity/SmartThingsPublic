package com.smartthings.sdk.smartapp.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartthings.sdk.smartapp.core.Handler;
import com.smartthings.sdk.smartapp.core.PredicateHandler;
import com.smartthings.sdk.smartapp.core.RequestPreprocessor;
import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.SmartApp;
import com.smartthings.sdk.smartapp.core.SmartAppDefinition;
import com.smartthings.sdk.smartapp.core.SmartAppDefinitionSpec;
import com.smartthings.sdk.smartapp.core.internal.handlers.DefaultConfirmationHandler;
import com.smartthings.sdk.smartapp.core.internal.handlers.DefaultPingHandler;
import com.smartthings.sdk.smartapp.core.internal.handlers.NoopUninstallHandler;
import com.smartthings.sdk.smartapp.core.models.AppLifecycle;
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse;


public class DefaultSmartApp implements SmartApp {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSmartApp.class);

    private static final String HANDLER_MISSING_MSG = "Required handler for %s lifecycle was found.";

    private static final String HANDLER_NOT_FOUND_MSG =
        "No matching handler for requested lifecycle was found.  " +
            "If unintended check SmartApp definition for binding. lifecycle={}";

    private static final Handler NOT_FOUND_HANDLER = (request) -> {
        LOG.warn(HANDLER_NOT_FOUND_MSG, request.getLifecycle());
        return Response.notFound();
    };

    private final List<PredicateHandler> handlers;
    private final List<RequestPreprocessor> requestPreprocessors = new ArrayList<>();

    public DefaultSmartApp(Consumer<SmartAppDefinitionSpec> spec) {
        this(DefaultSmartAppDefinition.build(spec));
    }

    public DefaultSmartApp(SmartAppDefinition definition) {
        this.handlers = createHandlerChain(definition);
        if (definition.getRequestPreprocessors() != null) {
            this.requestPreprocessors.addAll(definition.getRequestPreprocessors());
        }
    }

    @Override
    public ExecutionResponse execute(ExecutionRequest request) {
        try {
            Optional<PredicateHandler> maybeHandler = handlers.stream()
                .filter((handler) -> handler.test(request))
                .findFirst();

            if (!maybeHandler.isPresent()) {
                return Response.notFound();
            }

            for (RequestPreprocessor requestPreprocessor : requestPreprocessors) {
                requestPreprocessor.process(request);
            }
            return maybeHandler.get().handle(request);
        } catch (Exception e) {
            LOG.error("Unexpected error in request handler", e);
            return Response.status(500);
        }
    }

    private static PredicateHandler handler(
        AppLifecycle lifecycle,
        Handler handler
    ) {

        if (handler == null) {
            LOG.error("missing_required_handler lifecycle={}", lifecycle);
            throw new IllegalStateException(String.format(HANDLER_MISSING_MSG, lifecycle));
        }

        return PredicateHandler.of(
            (request) -> request.getLifecycle() == lifecycle,
            handler
        );
    }

    private static List<PredicateHandler> createHandlerChain(SmartAppDefinition definition) {
        List<PredicateHandler> chain = new ArrayList<>();

        chain.add(handler(AppLifecycle.EVENT, definition.getEventHandler()));
        chain.add(handler(AppLifecycle.CONFIGURATION, definition.getConfigurationHandler()));
        chain.add(handler(AppLifecycle.INSTALL, definition.getInstallHandler()));
        chain.add(handler(AppLifecycle.UPDATE, definition.getUpdateHandler()));
        chain.add(
            handler(AppLifecycle.UNINSTALL, getOrDefault(definition.getUninstallHandler(), new NoopUninstallHandler()))
        );
        chain.add(
            handler(AppLifecycle.PING, getOrDefault(definition.getPingHandler(), new DefaultPingHandler()))
        );
        chain.add(
            handler(AppLifecycle.CONFIRMATION, getOrDefault(definition.getConfirmationHandler(),
                new DefaultConfirmationHandler()))
        );
        chain.add(
            handler(AppLifecycle.OAUTH_CALLBACK, getOrDefault(definition.getOauthCallbackHandler(), NOT_FOUND_HANDLER))
        );

        if (definition.getPredicateHandlers() != null && !definition.getPredicateHandlers().isEmpty()) {
            chain.addAll(definition.getPredicateHandlers());
        }

        return Collections.unmodifiableList(chain);
    }

    private static Handler getOrDefault(Handler handler, Handler defaultHandler) {
        if (handler == null) {
            return defaultHandler;
        }
        return handler;
    }
}
