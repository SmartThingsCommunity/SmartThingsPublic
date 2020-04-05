package com.smartthings.sdk.smartapp.core;

import java.util.List;
import java.util.function.Predicate;

import com.smartthings.sdk.smartapp.core.extensions.*;
import com.smartthings.sdk.smartapp.core.models.*;


public interface SmartAppDefinitionSpec {
    SmartAppDefinitionSpec install(InstallHandler handler);
    SmartAppDefinitionSpec update(UpdateHandler handler);
    SmartAppDefinitionSpec uninstall(UninstallHandler handler);
    SmartAppDefinitionSpec event(EventHandler handler);
    SmartAppDefinitionSpec ping(PingHandler handler);
    SmartAppDefinitionSpec confirmation(ConfirmationHandler handler);
    SmartAppDefinitionSpec configuration(ConfigurationHandler handler);
    SmartAppDefinitionSpec oauthCallback(OAuthCallbackHandler handler);
    SmartAppDefinitionSpec when(Predicate<ExecutionRequest> predicate, Handler handler);
    SmartAppDefinitionSpec requestPreprocessors(List<RequestPreprocessor> requestPreprocessor);
    SmartAppDefinitionSpec addRequestPreprocessor(RequestPreprocessor requestPreprocessor);
}
