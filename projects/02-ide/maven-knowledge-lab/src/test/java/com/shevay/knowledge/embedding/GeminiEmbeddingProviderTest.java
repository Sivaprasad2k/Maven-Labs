package com.shevay.knowledge.embedding;

import com.shevay.knowledge.config.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

class GeminiEmbeddingProviderTest {

    private static String createMockSingleJsonResponse(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"embedding\": {\n    \"values\": [");
        for (int i = 0; i < count; i++) {
            sb.append(0.01f * (i + 1));
            if (i < count - 1) sb.append(",");
        }
        sb.append("]\n  }\n}");
        return sb.toString();
    }

    private static String createMockBatchJsonResponse(int count, int batchSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"embeddings\": [\n");
        for (int b = 0; b < batchSize; b++) {
            sb.append("    { \"values\": [");
            for (int i = 0; i < count; i++) {
                sb.append(0.01f * (i + 1 + b));
                if (i < count - 1) sb.append(",");
            }
            sb.append("] }");
            if (b < batchSize - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }

    @Test
    @DisplayName("Should format single DOCUMENT request cleanly, send x-goog-api-key header, and parse 768-dim response")
    void testDocumentEmbeddingRequestAndBodyFormat() {
        MockHttpClient mockClient = new MockHttpClient(200, createMockSingleJsonResponse(768));
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-xyz", mockClient);

        Embedding embedding = provider.embed("Maven build lifecycle guide", EmbeddingPurpose.DOCUMENT);

        assertNotNull(embedding);
        assertEquals(768, embedding.getDimensions());
        assertEquals("gemini-embedding-001", embedding.getModelIdentifier());

        // Verify request details
        HttpRequest request = mockClient.lastRequest;
        assertNotNull(request);
        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent", request.uri().toString());
        assertEquals("POST", request.method());
        assertEquals("test-api-key-xyz", request.headers().firstValue("x-goog-api-key").orElse(null));
        assertTrue(request.headers().firstValue("Content-Type").orElse("").contains("application/json"));

        // Verify exact JSON body structure
        String body = mockClient.lastRequestBody;
        assertTrue(body.contains("\"model\": \"models/gemini-embedding-001\""));
        assertTrue(body.contains("\"text\": \"Maven build lifecycle guide\""));
        assertTrue(body.contains("\"embedContentConfig\":"));
        assertTrue(body.contains("\"taskType\": \"RETRIEVAL_DOCUMENT\""));
        assertTrue(body.contains("\"outputDimensionality\": 768"));
    }

    @Test
    @DisplayName("Should format single QUERY request with RETRIEVAL_QUERY in embedContentConfig")
    void testQueryEmbeddingRequestAndBodyFormat() {
        MockHttpClient mockClient = new MockHttpClient(200, createMockSingleJsonResponse(768));
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-xyz", mockClient);

        Embedding embedding = provider.embed("How do plugins work?", EmbeddingPurpose.QUERY);

        assertNotNull(embedding);
        assertEquals(768, embedding.getDimensions());

        HttpRequest request = mockClient.lastRequest;
        assertNotNull(request);
        assertEquals("test-api-key-xyz", request.headers().firstValue("x-goog-api-key").orElse(null));

        String body = mockClient.lastRequestBody;
        assertTrue(body.contains("\"model\": \"models/gemini-embedding-001\""));
        assertTrue(body.contains("\"text\": \"How do plugins work?\""));
        assertTrue(body.contains("\"embedContentConfig\":"));
        assertTrue(body.contains("\"taskType\": \"RETRIEVAL_QUERY\""));
        assertTrue(body.contains("\"outputDimensionality\": 768"));
    }

    @Test
    @DisplayName("Should format batch request with embedContentConfig for each request item")
    void testBatchEmbeddingRequestAndBodyFormat() {
        MockHttpClient mockClient = new MockHttpClient(200, createMockBatchJsonResponse(768, 2));
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-xyz", mockClient);

        List<String> texts = List.of("Doc chunk one text", "Doc chunk two text");
        List<Embedding> embeddings = provider.embedBatch(texts, EmbeddingPurpose.DOCUMENT);

        assertEquals(2, embeddings.size());
        assertEquals(768, embeddings.get(0).getDimensions());
        assertEquals(768, embeddings.get(1).getDimensions());

        HttpRequest request = mockClient.lastRequest;
        assertNotNull(request);
        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:batchEmbedContents", request.uri().toString());
        assertEquals("test-api-key-xyz", request.headers().firstValue("x-goog-api-key").orElse(null));

        String body = mockClient.lastRequestBody;
        assertTrue(body.contains("\"requests\": ["));
        assertTrue(body.contains("\"text\": \"Doc chunk one text\""));
        assertTrue(body.contains("\"text\": \"Doc chunk two text\""));
        assertTrue(body.contains("\"embedContentConfig\":"));
        assertTrue(body.contains("\"taskType\": \"RETRIEVAL_DOCUMENT\""));
        assertTrue(body.contains("\"outputDimensionality\": 768"));
    }

    @Test
    @DisplayName("Should map HTTP 400 Bad Request to EmbeddingException")
    void testHttp400BadRequest() {
        MockHttpClient mockClient = new MockHttpClient(400, "{\"error\": {\"message\": \"Invalid JSON\"}}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-123", mockClient);

        EmbeddingException ex = assertThrows(EmbeddingException.class, () ->
                provider.embed("Test text", EmbeddingPurpose.QUERY));
        assertTrue(ex.getMessage().contains("400"));
    }

    @Test
    @DisplayName("Should map HTTP 401/403 Authentication failure safely without leaking API key")
    void testHttp401AuthenticationFailure() {
        MockHttpClient mockClient = new MockHttpClient(401, "{\"error\": {\"message\": \"API key invalid\"}}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-123", mockClient);

        EmbeddingException ex = assertThrows(EmbeddingException.class, () ->
                provider.embed("Test text", EmbeddingPurpose.QUERY));
        assertTrue(ex.getMessage().contains("401"));
        assertFalse(ex.getMessage().contains("test-api-key-123"));
    }

    @Test
    @DisplayName("Should map HTTP 429 Rate Limit to EmbeddingException")
    void testHttp429RateLimitExceeded() {
        MockHttpClient mockClient = new MockHttpClient(429, "{\"error\": {\"message\": \"Quota exceeded\"}}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-123", mockClient);

        EmbeddingException ex = assertThrows(EmbeddingException.class, () ->
                provider.embed("Test text", EmbeddingPurpose.QUERY));
        assertTrue(ex.getMessage().contains("429"));
    }

    @Test
    @DisplayName("Should map HTTP 500 Server Error to EmbeddingException")
    void testHttp500ServerError() {
        MockHttpClient mockClient = new MockHttpClient(500, "{\"error\": {\"message\": \"Internal Server Error\"}}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-123", mockClient);

        EmbeddingException ex = assertThrows(EmbeddingException.class, () ->
                provider.embed("Test text", EmbeddingPurpose.QUERY));
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    @DisplayName("Should map HTTP timeout to EmbeddingException")
    void testTimeoutHandling() {
        MockHttpClient mockClient = new MockHttpClient(true);
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-123", mockClient);

        EmbeddingException ex = assertThrows(EmbeddingException.class, () ->
                provider.embed("Test text", EmbeddingPurpose.QUERY));
        assertTrue(ex.getMessage().contains("Timed Out"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException if GEMINI_API_KEY is not set")
    void testMissingApiKey() {
        AppConfig config = AppConfig.loadDefaults();
        assertThrows(IllegalStateException.class, () -> new GeminiEmbeddingProvider(config, null, null));
        assertThrows(IllegalStateException.class, () -> new GeminiEmbeddingProvider(config, "  ", null));
    }

    @Test
    @DisplayName("Should reject null or blank input text before network execution")
    void testNullOrBlankInputValidation() {
        MockHttpClient mockClient = new MockHttpClient(200, createMockSingleJsonResponse(768));
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-123", mockClient);

        assertThrows(IllegalArgumentException.class, () -> provider.embed(null, EmbeddingPurpose.QUERY));
        assertThrows(IllegalArgumentException.class, () -> provider.embed("   ", EmbeddingPurpose.QUERY));
    }

    @Test
    @DisplayName("Should throw EmbeddingException if returned vector dimension does not match 768")
    void testUnexpectedDimensionHandling() {
        MockHttpClient mockClient = new MockHttpClient(200, createMockSingleJsonResponse(500));
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-123", mockClient);

        EmbeddingException ex = assertThrows(EmbeddingException.class, () ->
                provider.embed("Test text", EmbeddingPurpose.QUERY));
        assertTrue(ex.getMessage().contains("500"));
        assertTrue(ex.getMessage().contains("768"));
    }

    @Test
    @DisplayName("Should reject 3072-dimensional response when provider contract requires 768")
    void test3072DimensionResponseRejection() {
        MockHttpClient mockClient = new MockHttpClient(200, createMockSingleJsonResponse(3072));
        AppConfig config = AppConfig.loadDefaults();
        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config, "test-api-key-123", mockClient);

        EmbeddingException ex = assertThrows(EmbeddingException.class, () ->
                provider.embed("Test text", EmbeddingPurpose.QUERY));
        assertTrue(ex.getMessage().contains("3072"));
        assertTrue(ex.getMessage().contains("768"));
    }

    // Static helper Mock HttpClient
    static class MockHttpClient extends HttpClient {
        private final int statusCode;
        private final String responseBody;
        private final boolean causeTimeout;
        HttpRequest lastRequest;
        String lastRequestBody;

        MockHttpClient(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.causeTimeout = false;
        }

        MockHttpClient(boolean causeTimeout) {
            this.statusCode = 0;
            this.responseBody = null;
            this.causeTimeout = causeTimeout;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
            this.lastRequest = request;
            this.lastRequestBody = extractBodyString(request);
            if (causeTimeout) {
                throw new HttpTimeoutException("Simulated request timeout");
            }
            return new MockHttpResponse<>(statusCode, (T) responseBody);
        }

        private static String extractBodyString(HttpRequest request) {
            if (request == null || request.bodyPublisher().isEmpty()) {
                return "";
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WritableByteChannel channel = Channels.newChannel(out);
            HttpRequest.BodyPublisher publisher = request.bodyPublisher().get();
            publisher.subscribe(new Flow.Subscriber<>() {
                @Override public void onSubscribe(Flow.Subscription subscription) { subscription.request(Long.MAX_VALUE); }
                @Override public void onNext(java.nio.ByteBuffer item) {
                    try { channel.write(item); } catch (IOException ignored) {}
                }
                @Override public void onError(Throwable throwable) {}
                @Override public void onComplete() {}
            });
            return out.toString(StandardCharsets.UTF_8);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }

        @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { return null; }
        @Override public SSLParameters sslParameters() { return new SSLParameters(); }
        @Override public Optional<Authenticator> authenticator() { return Optional.empty(); }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }
    }

    static class MockHttpResponse<T> implements HttpResponse<T> {
        private final int statusCode;
        private final T body;

        MockHttpResponse(int statusCode, T body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        @Override public int statusCode() { return statusCode; }
        @Override public HttpRequest request() { return null; }
        @Override public Optional<HttpResponse<T>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (a, b) -> true); }
        @Override public T body() { return body; }
        @Override public Optional<javax.net.ssl.SSLSession> sslSession() { return Optional.empty(); }
        @Override public URI uri() { return URI.create("http://localhost"); }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }
}
