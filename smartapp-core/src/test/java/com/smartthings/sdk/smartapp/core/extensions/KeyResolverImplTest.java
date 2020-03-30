package com.smartthings.sdk.smartapp.core.extensions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Resources;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
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
public class KeyResolverImplTest {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    HttpEntity entity = mock(HttpEntity.class);

    StatusLine okStatus = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, null);
    StatusLine notFoundStatus = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 404, null);

    @Test
    public void constructorHandlesSpecifiedButMissingKeyFile() {
        KeyResolver result = new KeyResolverImpl(null, null, "/invalid/path");

        assertNotNull(result);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionWithNoPublicKey() {
        KeyResolver tester = new KeyResolverImpl(null, null, null);

        tester.getKeyString("/SmartThings");
    }

    @Test
    public void returnsLocalKeyStr() throws IOException {
        KeyResolver tester = new KeyResolverImpl(null, null, "/good_public.pem");
        URL url = Resources.getResource("good_public.pem");
        String goodPEMContents = Resources.asCharSource(url, Charset.forName("UTF-8")).read();

        String result = tester.getKeyString("/SmartThings/more:stuff");
        assertEquals(goodPEMContents, result + "\n");
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionWHenHttpClientMissing() {
        KeyResolver tester = new KeyResolverImpl(null, "https://key-server.example.com", null);

        tester.getKeyString("/key-to-look-up");
    }

    @Test
    public void resolvesRemoteKey() throws ClientProtocolException, IOException {
        KeyResolver tester = new KeyResolverImpl(httpClient, "https://key-server.example.com", null);

        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(okStatus);
        when(response.getEntity()).thenReturn(entity);
        String responseBodyStr = "multi\nline\nfile contents";
        InputStream responseBodyStream =
            new ByteArrayInputStream(responseBodyStr.getBytes(StandardCharsets.UTF_8));
        when(entity.getContent()).thenReturn(responseBodyStream);

        String result = tester.getKeyString("/key-to-look-up");
        ArgumentCaptor<HttpUriRequest> requestCaptor =
            ArgumentCaptor.forClass(HttpUriRequest.class);
        verify(httpClient).execute(requestCaptor.capture());
        assertEquals("https://key-server.example.com/key-to-look-up",
            requestCaptor.getValue().getURI().toString());

        assertEquals(responseBodyStr, result);
    }

    @Test(expected = RuntimeException.class)
    public void handlesBadStatusCode() throws ClientProtocolException, IOException {
        KeyResolver tester = new KeyResolverImpl(httpClient, "https://key-server.example.com", null);

        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(notFoundStatus);

        tester.getKeyString("/key-to-look-up");
    }

    @Test(expected = RuntimeException.class)
    public void bubblesUpIOException() throws ClientProtocolException, IOException {
        KeyResolver tester = new KeyResolverImpl(httpClient, "https://key-server.example.com", null);

        when(httpClient.execute(any())).thenThrow(new IOException("it isn't here"));

        tester.getKeyString("/key-to-look-up");
    }
}
