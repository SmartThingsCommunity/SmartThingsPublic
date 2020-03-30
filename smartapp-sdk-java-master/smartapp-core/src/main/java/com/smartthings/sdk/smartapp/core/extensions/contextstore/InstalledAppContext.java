package com.smartthings.sdk.smartapp.core.extensions.contextstore;


/**
 * This interface represents the context of an installed application. More than
 * likely, you will just want to use DefaultInstalledAppContext.
 *
 * If you want to add your own data to the context store, you will need to:
 *   1) extend DefaultInstalledAppContext or implement this interface directly
 *   2) create your own InstalledAppContextStore implementation
 */
public interface InstalledAppContext {
    String getInstalledAppId();
}
