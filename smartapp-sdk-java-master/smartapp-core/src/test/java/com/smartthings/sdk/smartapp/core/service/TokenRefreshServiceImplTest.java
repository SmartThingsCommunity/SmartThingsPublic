package com.smartthings.sdk.smartapp.core.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TokenRefreshServiceImplTest {
    Instant now = Instant.now();

    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    HttpEntity entity = mock(HttpEntity.class);
    Clock clock = Clock.fixed(now, ZoneId.systemDefault());

    TokenRefreshService tester = new TokenRefreshServiceImpl("clientId",
        "clientSecret", "http://example.com/tokenRefreshUrl", httpClient, clock);

    Token token = new Token()
        .accessToken("access token")
        .accessTokenExpiration(now.plus(Duration.ofMinutes(5)))
        .refreshToken("refresh token")
        .refreshTokenExpiration(now.plus(Duration.ofDays(30)));

    @Test
    public void happyPathWorks() throws ClientProtocolException, IOException {
        String responseBodyStr = "{"
                + "\"access_token\": \"new access token\","
                + "\"token_type\": \"bearer\","
                + "\"refresh_token\": \"new refresh token\","
                + "\"expires_in\": 86399,"
                + "\"scope\": \"x:devices:* i:deviceprofiles r:devices:* w:devices:*\","
                + "\"installed_app_id\": \"installed app id\""
            + "}";
        InputStream responseBodyStream =
            new ByteArrayInputStream(responseBodyStr.getBytes(StandardCharsets.UTF_8));
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine())
            .thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, null));
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(responseBodyStream);

        Token expected = new Token()
            .accessToken("new access token")
            .accessTokenExpiration(now.plusSeconds(86399))
            .refreshToken("new refresh token")
            .refreshTokenExpiration(now.plus(Duration.ofDays(30)));

        Token result = tester.refresh(token);
        assertEquals(result, expected);

        ArgumentCaptor<HttpUriRequest> requestCaptor =
            ArgumentCaptor.forClass(HttpUriRequest.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpUriRequest capturedRequest = requestCaptor.getValue();

        assertEquals("http://example.com/tokenRefreshUrl",
            capturedRequest.getURI().toString());
    }

    @Test(expected = TokenRefreshException.class)
    public void throwsExceptionForRefreshFailure() throws UnsupportedOperationException, IOException {
        String responseBodyStr = "something bad happened";
        InputStream responseBodyStream =
            new ByteArrayInputStream(responseBodyStr.getBytes(StandardCharsets.UTF_8));
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine())
            .thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 401, null));
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(responseBodyStream);

        tester.refresh(token);
    }
}
