package com.smartthings.sdk.smartapp.core.extensions;

/**
 * KeyResolver
 */
public interface KeyResolver {
    /**
     * Get the appropriate unparsed (PEM file text) for the given key id.
     * If the keyId is "SmartThings", this will return the contents of the local
     * file. Otherwise it will use the key lookup service.
     */
    String getKeyString(String keyId);
}
