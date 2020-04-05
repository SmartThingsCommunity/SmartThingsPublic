package com.smartthings.sdk.smartapp.core;

import com.smartthings.sdk.smartapp.core.extensions.*;

import java.util.List;

public interface SmartAppDefinition {
    InstallHandler getInstallHandler();
    UpdateHandler getUpdateHandler();
    UninstallHandler getUninstallHandler();
    EventHandler getEventHandler();
    PingHandler getPingHandler();
    ConfirmationHandler getConfirmationHandler();
    ConfigurationHandler getConfigurationHandler();
    OAuthCallbackHandler getOauthCallbackHandler();
    List<PredicateHandler> getPredicateHandlers();
    List<RequestPreprocessor> getRequestPreprocessors();
}
