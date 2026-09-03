package com.shevay.knowledge.generation;

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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

class GeminiGenerationProviderTest {

    private static String createMockJsonResponse(String text) {
        return "{\n" +
                "  \"id\": \"interactions/12345\",\n" +
                "  \"status\": \"completed\",\n" +
                "  \"outputs\": [\n" +
                "    {\n" +
                "      \"text\": \"" + text + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    @Test
    @DisplayName("Should format Interactions API request, include x-goog-api-key header, and parse response text")
    void testSuccessfulGeneration() {
        MockHttpClient mockClient = new MockHttpClient(200, createMockJsonResponse("Maven build lifecycle consists of validate, compile, test, package, verify, install, and deploy."));
        AppConfig config = AppConfig.loadDefaults();
        GeminiGenerationProvider provider = new GeminiGenerationProvider(config, "secret-api-key-999", mockClient);

        String answer = provider.generate("Explain Maven build lifecycle");

        assertNotNull(answer);
        assertTrue(answer.contains("compile, test, package"));
        assertEquals("gemini-3.6-flash", provider.getModelIdentifier());

        HttpRequest request = mockClient.lastRequest;
        assertNotNull(request);
        assertEquals("https://generativelanguage.googleapis.com/v1beta/interactions", request.uri().toString());
        assertEquals("POST", request.method());
        assertEquals("secret-api-key-999", request.headers().firstValue("x-goog-api-key").orElse(null));

        String body = mockClient.lastRequestBody;
        assertTrue(body.contains("\"model\": \"gemini-3.6-flash\""));
        assertTrue(body.contains("\"input\": \"Explain Maven build lifecycle\""));
    }

    @Test
    @DisplayName("Should map HTTP 400 Bad Request to GenerationException")
    void testHttp400BadRequest() {
        MockHttpClient mockClient = new MockHttpClient(400, "{\"error\": {\"message\": \"Invalid content structure\"}}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiGenerationProvider provider = new GeminiGenerationProvider(config, "secret-api-key-999", mockClient);

        GenerationException ex = assertThrows(GenerationException.class, () -> provider.generate("Prompt"));
        assertTrue(ex.getMessage().contains("400"));
    }

    @Test
    @DisplayName("Should map HTTP 401/403 Authentication Failure without exposing secret API key")
    void testHttp401AuthenticationFailure() {
        MockHttpClient mockClient = new MockHttpClient(401, "{\"error\": {\"message\": \"API key invalid\"}}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiGenerationProvider provider = new GeminiGenerationProvider(config, "secret-api-key-999", mockClient);

        GenerationException ex = assertThrows(GenerationException.class, () -> provider.generate("Prompt"));
        assertTrue(ex.getMessage().contains("401"));
        assertFalse(ex.getMessage().contains("secret-api-key-999"));
    }

    @Test
    @DisplayName("Should map HTTP 429 Rate Limit Exceeded to GenerationException")
    void testHttp429RateLimitExceeded() {
        MockHttpClient mockClient = new MockHttpClient(429, "{\"error\": {\"message\": \"Quota exceeded\"}}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiGenerationProvider provider = new GeminiGenerationProvider(config, "secret-api-key-999", mockClient);

        GenerationException ex = assertThrows(GenerationException.class, () -> provider.generate("Prompt"));
        assertTrue(ex.getMessage().contains("429"));
    }

    @Test
    @DisplayName("Should map HTTP 500 Server Error to GenerationException")
    void testHttp500ServerError() {
        MockHttpClient mockClient = new MockHttpClient(500, "{\"error\": {\"message\": \"Internal Error\"}}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiGenerationProvider provider = new GeminiGenerationProvider(config, "secret-api-key-999", mockClient);

        GenerationException ex = assertThrows(GenerationException.class, () -> provider.generate("Prompt"));
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    @DisplayName("Should map request timeout to GenerationException")
    void testTimeoutHandling() {
        MockHttpClient mockClient = new MockHttpClient(true);
        AppConfig config = AppConfig.loadDefaults();
        GeminiGenerationProvider provider = new GeminiGenerationProvider(config, "secret-api-key-999", mockClient);

        GenerationException ex = assertThrows(GenerationException.class, () -> provider.generate("Prompt"));
        assertTrue(ex.getMessage().contains("timed out"));
    }

    @Test
    @DisplayName("Should handle malformed or empty generation response")
    void testMalformedResponse() {
        MockHttpClient mockClient = new MockHttpClient(200, "{\"outputs\": []}");
        AppConfig config = AppConfig.loadDefaults();
        GeminiGenerationProvider provider = new GeminiGenerationProvider(config, "secret-api-key-999", mockClient);

        GenerationException ex = assertThrows(GenerationException.class, () -> provider.generate("Prompt"));
        assertTrue(ex.getMessage().contains("malformed") || ex.getMessage().contains("missing"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException if GEMINI_API_KEY is not set")
    void testMissingApiKey() {
        AppConfig config = AppConfig.loadDefaults();
        assertThrows(IllegalStateException.class, () -> new GeminiGenerationProvider(config, null, null));
        assertThrows(IllegalStateException.class, () -> new GeminiGenerationProvider(config, "  ", null));
    }

    @Test
    @DisplayName("Should reject null or blank prompt string")
    void testNullOrBlankPromptValidation() {
        MockHttpClient mockClient = new MockHttpClient(200, createMockJsonResponse("Answer"));
        AppConfig config = AppConfig.loadDefaults();
        GeminiGenerationProvider provider = new GeminiGenerationProvider(config, "secret-api-key-999", mockClient);

        assertThrows(IllegalArgumentException.class, () -> provider.generate(null));
        assertThrows(IllegalArgumentException.class, () -> provider.generate("   "));
    }

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
