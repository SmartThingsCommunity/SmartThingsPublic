package com.smartthings.sdk.smartapp.spring;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.smartthings.sdk.smartapp.core.PredicateHandler;
import com.smartthings.sdk.smartapp.core.RequestPreprocessor;
import com.smartthings.sdk.smartapp.core.SmartAppDefinition;
import com.smartthings.sdk.smartapp.core.extensions.*;


public class SpringSmartAppDefinition implements SmartAppDefinition {
    private static final Logger LOG = LoggerFactory.getLogger(SpringSmartAppDefinition.class);

    private final PingHandler pingHandler;
    private final ConfirmationHandler confirmationHandler;
    private final ConfigurationHandler configurationHandler;
    private final InstallHandler installHandler;
    private final UpdateHandler updateHandler;
    private final OAuthCallbackHandler oAuthCallbackHandler;
    private final EventHandler eventHandler;
    private final UninstallHandler uninstallHandler;
    private final List<PredicateHandler> predicateHandlers;
    private final List<RequestPreprocessor> requestPreprocessors;

    public SpringSmartAppDefinition(PingHandler pingHandler, ConfirmationHandler confirmationHandler,
            ConfigurationHandler configurationHandler, InstallHandler installHandler,
            UpdateHandler updateHandler, OAuthCallbackHandler oAuthCallbackHandler,
            EventHandler eventHandler, UninstallHandler uninstallHandler,
            List<PredicateHandler> predicateHandlers, List<RequestPreprocessor> requestPreprocessors) {
        this.pingHandler = pingHandler;
        this.confirmationHandler = confirmationHandler;
        this.configurationHandler = configurationHandler;
        this.installHandler = installHandler;
        this.updateHandler = updateHandler;
        this.oAuthCallbackHandler = oAuthCallbackHandler;
        this.eventHandler = eventHandler;
        this.uninstallHandler = uninstallHandler;
        this.predicateHandlers = predicateHandlers;
        this.requestPreprocessors = requestPreprocessors;
    }

    public static SpringSmartAppDefinition of(ApplicationContext applicationContext) {
        PingHandler pingHandler = findHandler(applicationContext, PingHandler.class, false);
        ConfirmationHandler confirmationHandler = findHandler(applicationContext, ConfirmationHandler.class, false);
        ConfigurationHandler configurationHandler = findHandler(applicationContext, ConfigurationHandler.class, true);
        InstallHandler installHandler = findHandler(applicationContext, InstallHandler.class, true);
        UpdateHandler updateHandler = findHandler(applicationContext, UpdateHandler.class, true);
        OAuthCallbackHandler oAuthCallbackHandler = findHandler(applicationContext, OAuthCallbackHandler.class, false);
        EventHandler eventHandler = findHandler(applicationContext, EventHandler.class, true);
        UninstallHandler uninstallHandler = findHandler(applicationContext, UninstallHandler.class, false);
        List<PredicateHandler> predicateHandlers =
            new ArrayList<>(applicationContext.getBeansOfType(PredicateHandler.class).values());
        List<RequestPreprocessor> requestPreprocessors =
            new ArrayList<>(applicationContext.getBeansOfType(RequestPreprocessor.class).values());
        return new SpringSmartAppDefinition(pingHandler, confirmationHandler, configurationHandler,
            installHandler, updateHandler, oAuthCallbackHandler, eventHandler, uninstallHandler,
            predicateHandlers, requestPreprocessors);
    }

    private static <T> T findHandler(ApplicationContext applicationContext, Class<T> klass, boolean required) {
        try {
            return applicationContext.getBean(klass);
        } catch (BeansException beansException) {
            if (required) {
                LOG.error("could not find required " + klass.getSimpleName() + " in Spring ApplicationContext",
                    beansException);
                throw beansException;
            } else {
                LOG.debug("did not find optional {} in Spring ApplicationContext", klass.getSimpleName());
                return null;
            }
        }
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
    public InstallHandler getInstallHandler() {
        return installHandler;
    }

    @Override
    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    @Override
    public OAuthCallbackHandler getOauthCallbackHandler() {
        return oAuthCallbackHandler;
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
    public List<PredicateHandler> getPredicateHandlers() {
        return predicateHandlers;
    }

    @Override
    public List<RequestPreprocessor> getRequestPreprocessors() {
        return requestPreprocessors;
    }
}
