package com.smartthings.sdk.smartapp.core.extensions.contextstore;

import java.time.Instant;
import java.util.Objects;

import com.smartthings.sdk.smartapp.core.models.InstallData;
import com.smartthings.sdk.smartapp.core.models.InstalledApp;
import com.smartthings.sdk.smartapp.core.models.UpdateData;
import com.smartthings.sdk.smartapp.core.service.Token;


/**
 * This is the default implementation of InstalledAppContext which stores
 * basic information related to installed apps. In particular, this includes
 * token information to allow for keeping them up-to-date for out of band
 * API requests.
 *
 * Instances of this class are created and managed by the context store.
 */
public class DefaultInstalledAppContext implements InstalledAppContext {
    private InstalledApp installedApp;
    private Token token;

    /**
     * Helper method for creating the context from the install data.
     */
    public static DefaultInstalledAppContext from(InstallData installData) {
        return new DefaultInstalledAppContext(installData.getInstalledApp(),
            installData.getAuthToken(), installData.getRefreshToken());
    }

    /**
     * Helper method for creating the context from the update data.
     */
    public static DefaultInstalledAppContext from(UpdateData updateData) {
        return new DefaultInstalledAppContext(updateData.getInstalledApp(),
            updateData.getAuthToken(), updateData.getRefreshToken());
    }

    public DefaultInstalledAppContext() {
        // Include empty constructor to make this class as amenable to
        // persistence engines as possible.
    }

    public DefaultInstalledAppContext(InstalledApp installedApp, Token token) {
        this.installedApp = installedApp;
        this.token = token;
    }

    public DefaultInstalledAppContext(InstalledApp installedApp,
            String accessToken, String refreshToken) {
        this.installedApp = installedApp;

        Instant now = Instant.now();
        token = new Token()
            .accessToken(accessToken)
            .accessTokenExpiration(now.plus(Token.ACCESS_TOKEN_DURATION))
            .refreshToken(refreshToken)
            .refreshTokenExpiration(now.plus(Token.REFRESH_TOKEN_DURATION));
    }

    @Override
    public String getInstalledAppId() {
        return installedApp != null ? installedApp.getInstalledAppId() : null;
    }

    public InstalledApp getInstalledApp() {
        return installedApp;
    }
    public void setInstalledApp(InstalledApp installedApp) {
        this.installedApp = installedApp;
    }
    public DefaultInstalledAppContext installedApp(InstalledApp installedApp) {
        this.installedApp = installedApp;
        return this;
    }

    public Token getToken() {
        return token;
    }
    public void setToken(Token token) {
        this.token = token;
    }
    public DefaultInstalledAppContext token(Token token) {
        this.token = token;
        return this;
    }

    public String getAuth() {
        if (token == null || token.getAccessToken() == null) {
            throw new NullPointerException("context is missing access token");
        }
        return "Bearer " + token.getAccessToken();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DefaultInstalledAppContext)) {
            return false;
        }
        DefaultInstalledAppContext defaultInstalledAppContext = (DefaultInstalledAppContext) o;
        return Objects.equals(installedApp, defaultInstalledAppContext.installedApp)
            && Objects.equals(token, defaultInstalledAppContext.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(installedApp, token);
    }

    @Override
    public String toString() {
        return "{" +
            " installedApp='" + getInstalledApp() + "'" +
            ", token='" + getToken() + "'" +
            "}";
    }
}
