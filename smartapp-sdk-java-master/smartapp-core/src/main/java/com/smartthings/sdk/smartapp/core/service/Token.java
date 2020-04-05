package com.smartthings.sdk.smartapp.core.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * This class represents a token used in the SmartApp. It allows the refresh,
 * access token and their expiration dates to be handled as one.
 */
public class Token {
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(30);

    private String accessToken;
    private Instant accessTokenExpiration;
    private String refreshToken;
    private Instant refreshTokenExpiration;

    public Token() {
        // this page intentionally left blank
    }

    public Token(String accessToken, Instant accessTokenExpiration, String refreshToken,
            Instant refreshTokenExpiration) {
        this.accessToken = accessToken;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Token accessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Get the approximate expiration time of the access token.
     */
    public Instant getAccessTokenExpiration() {
        return this.accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Instant accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Token accessTokenExpiration(Instant accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
        return this;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Token refreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    /**
     * Get the approximate expiration time of the refresh token.
     */
    public Instant getRefreshTokenExpiration() {
        return this.refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Instant refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public Token refreshTokenExpiration(Instant refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Token)) {
            return false;
        }
        Token token = (Token) o;
        return Objects.equals(accessToken, token.accessToken)
                && Objects.equals(accessTokenExpiration, token.accessTokenExpiration)
                && Objects.equals(refreshToken, token.refreshToken)
                && Objects.equals(refreshTokenExpiration, token.refreshTokenExpiration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, accessTokenExpiration, refreshToken, refreshTokenExpiration);
    }

    @Override
    public String toString() {
        return "{" + " accessToken='" + getAccessToken() + "'" + ", accessTokenExpiration='"
                + getAccessTokenExpiration() + "'" + ", refreshToken='" + getRefreshToken() + "'"
                + ", refreshTokenExpiration='" + getRefreshTokenExpiration() + "'" + "}";
    }
}
