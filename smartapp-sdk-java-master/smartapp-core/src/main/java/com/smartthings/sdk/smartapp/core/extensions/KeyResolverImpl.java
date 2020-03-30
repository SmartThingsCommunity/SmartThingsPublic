package com.smartthings.sdk.smartapp.core.extensions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A helper class for HttpVerificationService for retrieving keys (as unparsed
 * Strings) from either a local file or an HTTP request.
 */
public class KeyResolverImpl implements KeyResolver {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CloseableHttpClient httpClient;

    private final String localPublicKeyStr;
    private final String apiHost;

    public KeyResolverImpl(CloseableHttpClient httpClient, String apiHost, String publicKeyPath) {
        this.httpClient = httpClient;
        this.apiHost = apiHost;

        if (publicKeyPath == null) {
            localPublicKeyStr = null;
            return;
        }

        InputStream in = HttpVerificationService.class.getResourceAsStream(publicKeyPath);
        if (in == null) {
            // This is not an error until we are past the PING step.
            log.info("no public key; will only accept PING lifecycle requests");
            localPublicKeyStr = null;
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Looking for public key file: " + publicKeyPath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            localPublicKeyStr = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException ioException) {
            throw new RuntimeException("failed to read " + publicKeyPath + " file", ioException);
        }
    }

    @Override
    public String getKeyString(String keyId) {
        if (keyId.startsWith("/SmartThings")) {
            if (localPublicKeyStr == null) {
                throw new IllegalStateException("Public key file not set up"
                    + " properly. Please see README (Configure Public Key) for"
                    + " directions and don't forget to restart this server.");
            }
            return localPublicKeyStr;
        } else if (httpClient == null) {
            throw new RuntimeException("httpClient is required in HttpVerificationService for this application");
        } else {
            HttpGet request = new HttpGet(apiHost + keyId);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    throw new RuntimeException("got status code " + statusCode + " trying to retrieve public key");
                }
                InputStream content = response.getEntity().getContent();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content, StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            } catch (IOException ioException) {
                throw new RuntimeException("IOException retrieving public key", ioException);
            }
        }
    }
}
