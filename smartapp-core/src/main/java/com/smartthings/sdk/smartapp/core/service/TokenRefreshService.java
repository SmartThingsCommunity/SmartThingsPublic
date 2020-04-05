package com.smartthings.sdk.smartapp.core.service;


/**
 * A simple service to refresh a token.:
 */
public interface TokenRefreshService {
    /**
     * Refresh the given token and return new Token object populated with
     * the new refresh and auth tokens.
     */
    Token refresh(Token token);
}
