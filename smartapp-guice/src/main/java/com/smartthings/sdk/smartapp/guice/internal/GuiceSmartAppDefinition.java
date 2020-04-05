package com.smartthings.sdk.smartapp.guice.internal;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.smartthings.sdk.smartapp.core.PredicateHandler;
import com.smartthings.sdk.smartapp.core.RequestPreprocessor;
import com.smartthings.sdk.smartapp.core.SmartAppDefinition;
import com.smartthings.sdk.smartapp.core.extensions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiceSmartAppDefinition implements SmartAppDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(GuiceSmartAppDefinition.class);

    private final InstallHandler installHandler;
    private final UpdateHandler updateHandler;
    private final UninstallHandler uninstallHandler;
    private final EventHandler eventHandler;
    private final PingHandler pingHandler;
    private final ConfirmationHandler confirmationHandler;
    private final ConfigurationHandler configurationHandler;
    private final OAuthCallbackHandler oAuthCallbackHandler;
    private final List<PredicateHandler> predicateHandlers;
    private final List<RequestPreprocessor> requestPreprocessors;

    public GuiceSmartAppDefinition(Injector injector) {
        this.installHandler = findOne(injector, InstallHandler.class);
        this.updateHandler = findOne(injector, UpdateHandler.class);
        this.uninstallHandler = findOne(injector, UninstallHandler.class);
        this.eventHandler = findOne(injector, EventHandler.class);
        this.pingHandler = findOne(injector, PingHandler.class);
        this.confirmationHandler = findOne(injector, ConfirmationHandler.class);
        this.configurationHandler = findOne(injector, ConfigurationHandler.class);
        this.oAuthCallbackHandler = findOne(injector, OAuthCallbackHandler.class);
        this.predicateHandlers = search(injector, PredicateHandler.class);
        this.requestPreprocessors = search(injector, RequestPreprocessor.class);
    }

    @Override
    public InstallHandler getInstallHandler() {
        return installHandler;
    }

    @Override
    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    @Override
    public UninstallHandler getUninstallHandler() {
        return uninstallHandler;
    }

    @Override
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    public PingHandler getPingHandler() {
        return pingHandler;
    }

    @Override
    public ConfirmationHandler getConfirmationHandler() {
        return confirmationHandler;
    }

    @Override
    public ConfigurationHandler getConfigurationHandler() {
        return configurationHandler;
    }

    @Override
    public OAuthCallbackHandler getOauthCallbackHandler() {
        return oAuthCallbackHandler;
    }

    @Override
    public List<PredicateHandler> getPredicateHandlers() {
        return predicateHandlers;
    }

    @Override
    public List<RequestPreprocessor> getRequestPreprocessors() {
        return requestPreprocessors;
    }

    private static <O> O findOne(Injector injector, Class<O> type) {
        try {
            return injector.getInstance(type);
        } catch (ConfigurationException e) {
            LOG.trace("Missed direct resolution of type={}", type.getSimpleName());
        }

        List<O> instances = search(injector, type);
        if (!instances.isEmpty()) {
            return instances.get(0);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <O> List<O> search(Injector injector, Class<O> type) {
        List<O> instances = new ArrayList<>();
        for (Key<?> key: injector.getAllBindings().keySet()) {
            if (type.isAssignableFrom(key.getTypeLiteral().getRawType())) {
                instances.add((O) injector.getInstance(key));
            }
        }
        return Collections.unmodifiableList(instances);
    }

}
