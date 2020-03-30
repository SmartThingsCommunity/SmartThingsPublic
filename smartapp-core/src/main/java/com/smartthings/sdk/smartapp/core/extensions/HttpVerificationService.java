package com.smartthings.sdk.smartapp.core.extensions;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.adamcin.httpsig.api.Authorization;
import net.adamcin.httpsig.api.Challenge;
import net.adamcin.httpsig.api.DefaultKeychain;
import net.adamcin.httpsig.api.DefaultVerifier;
import net.adamcin.httpsig.api.Key;
import net.adamcin.httpsig.api.KeyId;
import net.adamcin.httpsig.api.RequestContent;
import net.adamcin.httpsig.api.Verifier;
import net.adamcin.httpsig.api.VerifyResult;
import net.adamcin.httpsig.ssh.jce.KeyFormat;
import net.adamcin.httpsig.ssh.jce.SSHKey;
import net.adamcin.httpsig.ssh.jce.UserFingerprintKeyId;


/**
 * All requests should have their HTTP signature verified to ensure if request
 * is actually from SmartThings. This class has the implementation of signature
 * authentication and verification from https://github.com/adamcin/httpsig-java.
 * The public key that is generated when registering a smartapp; is used to
 * verify the request.
 */
public class HttpVerificationService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final LoadingCache<String, PublicKey> publicKeysCache;

    /**
     * Create a basic instance that only supports local public key files.
     */
    public HttpVerificationService() {
        this(null);
    }

    /**
     * Create a basic instance that can get public key files remotely where
     * supported.
     *
     * @param httpClient An Apache CloseableHttpClient instance to use when
     * requesting remote keys.
     */
    public HttpVerificationService(CloseableHttpClient httpClient) {
        this(httpClient, "/smartthings_rsa.pub", "https://key.smartthings.com", Duration.ofDays(1));
    }

    /**
     * Build a verification service. This constructor is primarily for testing.
     */
    public HttpVerificationService(CloseableHttpClient httpClient, String publicKeyPath,
            String apiHost, Duration keyCacheTTL) {
        this(keyCacheTTL, new KeyResolverImpl(httpClient, apiHost, publicKeyPath), Ticker.systemTicker());
    }

    /**
     * Build a verification service. This constructor is primarily for testing.
     */
    HttpVerificationService(Duration keyCacheTTL, KeyResolver keyResolver, Ticker ticker) {
        publicKeysCache = CacheBuilder.newBuilder()
            .expireAfterWrite(keyCacheTTL)
            .ticker(ticker)
            .build(new CacheLoader<String, PublicKey>() {
                @Override
                public PublicKey load(String keyId) throws Exception {
                    return parsePublicKey(keyResolver.getKeyString(keyId));
                }
            });
    }

    public boolean verify(String method, String uri, Map<String, String> headers) {
        String authorizationHeader = headers.get("Authorization");
        if (authorizationHeader == null) {
            // Spring lower-cases header names
            authorizationHeader = headers.get("authorization");
        }
        Authorization authorization = Authorization.parse(authorizationHeader);

        if (authorization == null) {
            log.error("Request contains no authorization header");
            return false;
        }

        KeyPair pair;
        try {
            pair = new KeyPair(publicKeysCache.getUnchecked(authorization.getKeyId()), null);
        } catch (UncheckedExecutionException exception) {
            log.error("error retrieving public key", exception);
            return false;
        }

        VerifyResult verifyResult = verifyRequest(authorization, pair, method, uri, headers);
        if (log.isDebugEnabled() && verifyResult != VerifyResult.SUCCESS) {
            log.debug("verification failed with result " + verifyResult);
        } else if (log.isTraceEnabled()) {
            log.trace("verification result: " + verifyResult);
        }
        return verifyResult == VerifyResult.SUCCESS;
    }

    private static class FixedKeyId implements KeyId {
        Authorization authorization;

        FixedKeyId(Authorization authorization) {
            this.authorization = authorization;
        }

        @Override
        public String getId(Key key) {
            // DefaultVerifier calls this method to get the map key for
            // a map which is then used to look up the Key. The map
            // key used to look for the Key in the map is retrieved
            // via a call to authorization.getKeyId().
            // There is nothing in the passed in Key that contains
            // this value so we have to use this value directly.
            return authorization.getKeyId();
        }
    }
    private VerifyResult verifyRequest(Authorization authorization, KeyPair pair, String method,
            String uri, Map<String, String> headers) {
        DefaultKeychain keychain = new DefaultKeychain();
        String fpKeyId = authorization.getKeyId().substring(1); // remove slash from front
        KeyId keyId;
        if (fpKeyId.startsWith("SmartThings")) {
            keyId = new UserFingerprintKeyId("SmartThings");
        } else {
            keyId = new FixedKeyId(authorization);
        }
        keychain.add(new SSHKey(KeyFormat.SSH_RSA, pair));
        Verifier verifier = new DefaultVerifier(keychain, keyId);

        Challenge challenge = new Challenge("<preemptive>", authorization.getHeaders(),
            Collections.unmodifiableList(Arrays.asList(authorization.getAlgorithm())));

        Set<String> signedHeaders = authorization.getHeaders().stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        if (log.isDebugEnabled()) {
            log.debug("requestURI: " + uri);
        }

        RequestContent.Builder content = new RequestContent.Builder()
            .setRequestTarget(method, uri);

        headers.keySet().stream()
            .filter(headerName -> signedHeaders.contains(headerName.toLowerCase(Locale.ENGLISH)))
            .forEach(headerName -> content.addHeader(headerName, headers.get(headerName)));

        RequestContent requestContent = content.build();
        return verifier.verifyWithResult(challenge, requestContent, authorization);
    }

    private PublicKey parsePublicKey(String publicKeyString) {
        try (PEMParser parser = new PEMParser(new StringReader(publicKeyString))) {
            SubjectPublicKeyInfo info;
            Object object = parser.readObject();
            if (object instanceof X509CertificateHolder) {
                X509CertificateHolder cert = (X509CertificateHolder) object;
                info = cert.getSubjectPublicKeyInfo();
            } else if (object instanceof SubjectPublicKeyInfo) {
                info = (SubjectPublicKeyInfo) object;
            } else {
                throw new RuntimeException("Public key not found or unknown type");
            }
            RSAKeyParameters param = (RSAKeyParameters) PublicKeyFactory.createKey(info);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(param.getModulus(), param.getExponent());
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (IOException e) {
            throw new RuntimeException("IOException trying to create public key from PEM", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException trying to create public key from PEM", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("InvalidKeySpecException trying to create public key from PEM", e);
        }
    }
}
