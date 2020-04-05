package com.smartthings.sdk.smartapp.core.extensions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.common.testing.FakeTicker;
import net.adamcin.httpsig.api.Algorithm;
import net.adamcin.httpsig.api.Authorization;
import net.adamcin.httpsig.api.Challenge;
import net.adamcin.httpsig.api.DefaultKeychain;
import net.adamcin.httpsig.api.RequestContent;
import net.adamcin.httpsig.api.Signer;
import net.adamcin.httpsig.ssh.jce.KeyFormat;
import net.adamcin.httpsig.ssh.jce.SSHKey;
import net.adamcin.httpsig.ssh.jce.UserFingerprintKeyId;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class HttpVerificationServiceTests {
    private KeyResolver keyResolver = mock(KeyResolver.class);
    private FakeTicker ticker = new FakeTicker();
    private String goodPEMContents;
    private String goodCertContents;

    public HttpVerificationServiceTests() throws IOException {
        goodPEMContents = readFile("good_public.pem");
        goodCertContents = readFile("good.crt");
    }

    private String readFile(String filename) throws IOException {
        return Resources.asCharSource(Resources.getResource(filename), Charset.forName("UTF-8")).read();
    }

    private static class PseudoRequest {
        String method;
        String uri;
        Map<String, String> headers;
        String jsonBody;

        PseudoRequest(String method, String uri, Map<String, String> headers, String jsonBody) {
            this.method = method;
            this.uri = uri;
            this.headers = headers;
            this.jsonBody = jsonBody;
        }
    }

    private String getPath(URL url) {
        if (url.getPath() == null || url.getPath().isEmpty()) {
            return "/";
        }
        if (url.getQuery() == null || url.getQuery().isEmpty()) {
            return url.getPath();
        }
        return url.getPath() + "?" + url.getQuery();
    }

    private String getDigest(String jsonBody) throws NoSuchAlgorithmException {
        String body = jsonBody != null ? jsonBody : "";
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8));
        return "SHA-256" + "=" + Base64.getEncoder().encodeToString(digest);
    }

    private RequestContent getRequestContent(String method, String path, String digest) {
        return new RequestContent.Builder().setRequestTarget(method, path).addHeader("Digest", digest).addDateNow()
                .build();
    }

    private PublicKey getPublicKey() throws Exception {
        SubjectPublicKeyInfo info;
        try (InputStream is = HttpVerificationServiceTests.class.getResourceAsStream("/good_public.pem");
                PEMParser parser = new PEMParser(new BufferedReader(new InputStreamReader(is, "UTF-8")))) {
            Object object = parser.readObject();
            if (object instanceof SubjectPublicKeyInfo) {
                info = (SubjectPublicKeyInfo) object;
            } else if (object instanceof PEMKeyPair) {
                PEMKeyPair keyPair = (PEMKeyPair) object;
                info = keyPair.getPublicKeyInfo();
            } else {
                throw new RuntimeException("Public key not found");
            }
        }

        RSAKeyParameters param = (RSAKeyParameters) PublicKeyFactory.createKey(info);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(param.getModulus(), param.getExponent());
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private PrivateKey getPrivateKey() throws Exception {
        PrivateKeyInfo info;
        try (InputStream is = this.getClass().getResourceAsStream("/good_private.pem");
                PEMParser parser = new PEMParser(new BufferedReader(new InputStreamReader(is, "UTF-8")))) {
            Object object = parser.readObject();
            if (object instanceof PrivateKeyInfo) {
                info = (PrivateKeyInfo) object;
            } else if (object instanceof PEMKeyPair) {
                PEMKeyPair keyPair = (PEMKeyPair) object;
                info = keyPair.getPrivateKeyInfo();
            } else {
                throw new RuntimeException("Private key not found");
            }
        }
        RSAKeyParameters param = (RSAKeyParameters) PrivateKeyFactory.createKey(info);
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(param.getModulus(), param.getExponent());
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private KeyPair getRSAKeyPair(String privateKeyStr) throws Exception {
        PublicKey publicKey = getPublicKey();
        PrivateKey privateKey = null;
        if (privateKeyStr != null) {
            privateKey = getPrivateKey();
        }
        return new KeyPair(publicKey, privateKey);
    }

    private Authorization getAuthorization(String privateKey, RequestContent content, String keyId) throws Exception {
        DefaultKeychain keychain = new DefaultKeychain();
        KeyPair pair = getRSAKeyPair(privateKey);
        Challenge challenge = new Challenge("<preemptive>", ImmutableList.of("(request-target)", "date", "digest"),
                ImmutableList.of(Algorithm.RSA_SHA256));
        keychain.add(new SSHKey(KeyFormat.SSH_RSA, pair));
        Signer signer = new Signer(keychain, new UserFingerprintKeyId(keyId));
        signer.rotateKeys(challenge);

        return signer.sign(content);
    }

    private String readKeyStringFromResource(String resourceName) {
        InputStream privateIS = this.getClass().getResourceAsStream(resourceName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(privateIS, "UTF-8"))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException ioException) {
            throw new RuntimeException("failed to read " + resourceName + " file", ioException);
        }
    }

    private PseudoRequest signPsuedoRequest(PseudoRequest request) {
        return signPsuedoRequest(request, "SmartThings");
    }

    private PseudoRequest signPsuedoRequest(PseudoRequest request, String keyId) {
        String privateKey = readKeyStringFromResource("/good_private.pem");

        String existingAuthHeader = request.headers.get("Authorization");
        try {
            String message;
            switch (request.method) {
            case "GET":
                message = getPath(new URL(request.uri));
                break;
            default:
                message = request.jsonBody;
                break;
            }

            String digest = getDigest(message);
            RequestContent requestContent = getRequestContent(request.method, getPath(new URL(request.uri)), digest);
            Authorization authorization = getAuthorization(privateKey, requestContent, keyId);

            StringBuilder resultingAuthHeader = new StringBuilder();
            if (existingAuthHeader != null) {
                resultingAuthHeader.append(existingAuthHeader).append(", ");
            }
            resultingAuthHeader.append(authorization.getHeaderValue());

            Map<String, String> updatedHeaders = new HashMap<String, String>();
            updatedHeaders.putAll(request.headers);
            updatedHeaders.put("Authorization", resultingAuthHeader.toString());
            updatedHeaders.put("Date", requestContent.getDate());
            updatedHeaders.put("Digest", digest);
            return new PseudoRequest(request.method, request.uri, updatedHeaders, request.jsonBody);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to sign request: %s", e.getMessage()), e);
        }
    }

    @Test
    public void failsWithMissingKeyFile() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("host", "here.example.com");
        builder.put("content-type", "application/json");
        Map<String, String> headers = builder.build();
        PseudoRequest unsigned = new PseudoRequest("POST", "https://there.example.com/smartapp", headers, "{}");
        when(keyResolver.getKeyString(any())).thenThrow(new IllegalStateException("Public key..."));

        HttpVerificationService tester = new HttpVerificationService(Duration.ofMinutes(1), keyResolver, ticker);

        PseudoRequest signed = signPsuedoRequest(unsigned);

        boolean result = tester.verify(signed.method, "/smartapp", signed.headers);

        assertFalse(result);
    }

    @Test
    public void failsWithMismatchedKeyFile() throws IOException {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("host", "here.example.com");
        builder.put("content-type", "application/json");
        Map<String, String> headers = builder.build();
        PseudoRequest unsigned = new PseudoRequest("POST", "https://there.example.com/smartapp", headers, "{}");
        when(keyResolver.getKeyString(any())).thenReturn(readFile("wrong_rsa.pub"));

        HttpVerificationService tester = new HttpVerificationService(Duration.ofMinutes(1), keyResolver, ticker);

        PseudoRequest signed = signPsuedoRequest(unsigned);

        boolean result = tester.verify(signed.method, "/smartapp", signed.headers);

        assertFalse(result);
    }

    @Test
    public void failsWithMissingAuthorizationHeader() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("host", "here.example.com");
        builder.put("content-type", "application/json");
        Map<String, String> headers = builder.build();
        PseudoRequest unsigned = new PseudoRequest("POST", "https://there.example.com/smartapp", headers, "{}");

        HttpVerificationService tester = new HttpVerificationService(Duration.ofMinutes(1), keyResolver, ticker);

        PseudoRequest signed = signPsuedoRequest(unsigned);
        signed.headers.remove("Authorization");

        boolean result = tester.verify(signed.method, "/smartapp", signed.headers);

        assertFalse(result);
    }

    @Test
    public void testVerification() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("host", "here.example.com");
        builder.put("content-type", "application/json");
        Map<String, String> headers = builder.build();
        PseudoRequest unsigned = new PseudoRequest("POST", "https://there.example.com/smartapp", headers, "{}");

        when(keyResolver.getKeyString(any())).thenReturn(goodPEMContents);

        HttpVerificationService tester = new HttpVerificationService(Duration.ofMinutes(1), keyResolver, ticker);

        PseudoRequest signed = signPsuedoRequest(unsigned);

        boolean result = tester.verify(signed.method, "/smartapp", signed.headers);

        verify(keyResolver).getKeyString("/SmartThings/dd:b9:e6:0b:be:63:6e:ae:ca:98:89:15:fd:40:b3:da");

        assertTrue(result);
    }

    @Test
    public void verificationWithRemoteKey() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("host", "here.example.com");
        builder.put("content-type", "application/json");
        Map<String, String> headers = builder.build();
        PseudoRequest unsigned = new PseudoRequest("POST", "https://there.example.com/smartapp", headers, "{}");

        when(keyResolver.getKeyString(any())).thenReturn(goodPEMContents);

        HttpVerificationService tester = new HttpVerificationService(
            Duration.ofMinutes(1), keyResolver, ticker);

        PseudoRequest signed = signPsuedoRequest(unsigned, "other_key");

        boolean result = tester.verify(signed.method, "/smartapp", signed.headers);

        verify(keyResolver).getKeyString("/other_key/dd:b9:e6:0b:be:63:6e:ae:ca:98:89:15:fd:40:b3:da");

        assertTrue(result);
    }

    @Test
    public void lowercaseAuthHeaderWorksToo() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("host", "here.example.com");
        builder.put("content-type", "application/json");
        Map<String, String> headers = builder.build();
        PseudoRequest unsigned = new PseudoRequest("POST", "https://there.example.com/smartapp", headers, "{}");

        when(keyResolver.getKeyString(any())).thenReturn(goodPEMContents);

        HttpVerificationService tester = new HttpVerificationService(Duration.ofMinutes(1), keyResolver, ticker);

        PseudoRequest signed = signPsuedoRequest(unsigned);
        signed.headers.put("authorization", signed.headers.remove("Authorization"));

        boolean result = tester.verify(signed.method, "/smartapp", signed.headers);

        verify(keyResolver, times(1)).getKeyString("/SmartThings/dd:b9:e6:0b:be:63:6e:ae:ca:98:89:15:fd:40:b3:da");

        assertTrue(result);
    }

    @Test
    public void testCache() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("Host", "here.example.com");
        builder.put("Content-Type", "application/json");
        Map<String, String> headers = builder.build();
        PseudoRequest unsigned = new PseudoRequest("POST",
            "https://there.example.com/smartapp", headers, "{}");

        when(keyResolver.getKeyString(any())).thenReturn(goodPEMContents);

        HttpVerificationService tester = new HttpVerificationService(Duration.ofDays(1), keyResolver, ticker);

        PseudoRequest signed = signPsuedoRequest(unsigned);

        boolean result = tester.verify(signed.method, "/smartapp", signed.headers);
        assertTrue(result);

        ticker.advance(Duration.ofHours(23));

        PseudoRequest unsigned2 = new PseudoRequest("POST",
            "https://there.example.com/smartapp", headers, "different body");
        PseudoRequest signed2 = signPsuedoRequest(unsigned2);

        boolean result2 = tester.verify(signed2.method, "/smartapp", signed2.headers);
        assertTrue(result2);

        verify(keyResolver, times(1)).getKeyString(any());
    }

    @Test
    public void canParseCert() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("host", "here.example.com");
        builder.put("content-type", "application/json");
        Map<String, String> headers = builder.build();
        PseudoRequest unsigned = new PseudoRequest("POST", "https://there.example.com/smartapp", headers, "{}");

        when(keyResolver.getKeyString(any())).thenReturn(goodCertContents);

        HttpVerificationService tester = new HttpVerificationService(Duration.ofMinutes(1), keyResolver, ticker);

        PseudoRequest signed = signPsuedoRequest(unsigned);

        boolean result = tester.verify(signed.method, "/smartapp", signed.headers);

        verify(keyResolver).getKeyString("/SmartThings/dd:b9:e6:0b:be:63:6e:ae:ca:98:89:15:fd:40:b3:da");

        assertFalse(result);
    }
}
