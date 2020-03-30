package com.smartthings.sdk.smartapp.core.extensions.contextstore;

import com.smartthings.sdk.smartapp.core.RequestPreprocessor;
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;


/**
 * The default implementation of InstalledAppContextStore which stores
 * instances of DefaultInstalledAppContext as an instance of a
 * RequestPreprocessor, allowing hook-up into SmartApp lifecycle events with
 * minimal effort.
 */
public interface DefaultInstalledAppContextStore
        extends InstalledAppContextStore<DefaultInstalledAppContext>, RequestPreprocessor {
    @Override
    default void process(ExecutionRequest executionRequest) {
        switch (executionRequest.getLifecycle()) {
            case INSTALL:
                add(DefaultInstalledAppContext.from(executionRequest.getInstallData()));
                break;
            case UPDATE:
                update(DefaultInstalledAppContext.from(executionRequest.getUpdateData()));
                break;
            case UNINSTALL:
                String installedAppId = executionRequest.getUninstallData().getInstalledApp().getInstalledAppId();
                remove(installedAppId);
                break;
            default:
                // only need to handle install, update and uninstall
        }
    }
}
