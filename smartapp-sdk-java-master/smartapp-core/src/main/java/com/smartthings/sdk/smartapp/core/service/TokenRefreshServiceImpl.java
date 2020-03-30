package com.smartthings.sdk.smartapp.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TokenRefreshServiceImpl implements TokenRefreshService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String clientId;
    private final String clientSecret;
    private final String tokenRefreshUrl;

    private final CloseableHttpClient httpClient;
    private final Clock clock;

    private static final ObjectMapper mapper = new ObjectMapper();

    public TokenRefreshServiceImpl(String clientId, String clientSecret,
            CloseableHttpClient httpClient) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        tokenRefreshUrl = "https://auth-global.api.smartthings.com/oauth/token";
        this.httpClient = httpClient;
        clock = Clock.systemUTC();
    }

    public TokenRefreshServiceImpl(String clientId, String clientSecret,
            String tokenRefreshUrl, CloseableHttpClient httpClient, Clock clock) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenRefreshUrl = tokenRefreshUrl;
        this.httpClient = httpClient;
        this.clock = clock;
    }

    @Override
    public Token refresh(Token token) {
        Instant now = clock.instant();

        HttpPost request = new HttpPost(tokenRefreshUrl);

        String basicAuth = Base64.getEncoder().encodeToString((clientId + ":"
            + clientSecret).getBytes(StandardCharsets.UTF_8));
        request.addHeader("Authorization", "Basic " + basicAuth);
        request.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());

        List<NameValuePair> formData = Arrays.asList(
            new BasicNameValuePair("grant_type", "refresh_token"),
            new BasicNameValuePair("client_id", clientId),
            new BasicNameValuePair("client_secret", clientSecret),
            new BasicNameValuePair("refresh_token", token.getRefreshToken()));
        request.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

        String body = null;
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            InputStream content = response.getEntity().getContent();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(content, StandardCharsets.UTF_8))) {
                body = reader.lines().collect(Collectors.joining("\n"));
            }
            if (response.getStatusLine().getStatusCode() > 299) {
                String msg = "error " + response.getStatusLine().getStatusCode()
                    + " refreshing token " + body;
                log.error(msg);
                throw new TokenRefreshException(msg);
            }
        } catch (IOException e) {
            log.error("exception refreshing token", e);
            throw new RuntimeException(e);
        }

        Map<?, ?> tokenData = null;
        try {
            tokenData = mapper.readValue(body, Map.class);
        } catch (IOException e) {
            log.error("exception parsing JSON", e);
            throw new RuntimeException(e);
        }

        long expiresIn = ((Number) tokenData.get("expires_in")).longValue();
        return new Token()
            .accessToken((String) tokenData.get("access_token"))
            .accessTokenExpiration(now.plusSeconds(expiresIn))
            .refreshToken((String) tokenData.get("refresh_token"))
            .refreshTokenExpiration(now.plus(Token.REFRESH_TOKEN_DURATION));
    }
}
