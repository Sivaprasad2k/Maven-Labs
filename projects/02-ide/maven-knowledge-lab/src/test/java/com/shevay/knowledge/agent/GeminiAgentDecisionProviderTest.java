package com.shevay.knowledge.agent;

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

class GeminiAgentDecisionProviderTest {

    private static String createMockInteractionsResponse(String decisionJson) {
        return "{\n" +
                "  \"id\": \"interactions/998877\",\n" +
                "  \"status\": \"completed\",\n" +
                "  \"outputs\": [\n" +
                "    {\n" +
                "      \"text\": \"" + escapeJson(decisionJson) + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    @Test
    @DisplayName("Should send Interactions API request with model gemini-3.6-flash and x-goog-api-key header")
    void testDecideInteractionsApiFormat() throws Exception {
        String decisionJson = "{\"type\": \"final_answer\", \"answer\": \"Dependency scopes define artifact visibility.\"}";
        MockHttpClient mockClient = new MockHttpClient(200, createMockInteractionsResponse(decisionJson));

        AppConfig config = AppConfig.loadDefaults();
        GeminiAgentDecisionProvider provider = new GeminiAgentDecisionProvider(config, "secret-api-key-999", mockClient);

        AgentContext context = new AgentContext("Explain dependency scopes", List.of(), 5);
        AgentDecision decision = provider.decide(context);

        assertNotNull(decision);
        assertEquals(AgentDecisionType.FINAL_ANSWER, decision.type());
        assertEquals("Dependency scopes define artifact visibility.", decision.answer());

        HttpRequest request = mockClient.lastRequest;
        assertNotNull(request);
        assertEquals("https://generativelanguage.googleapis.com/v1beta/interactions", request.uri().toString());
        assertEquals("POST", request.method());
        assertEquals("secret-api-key-999", request.headers().firstValue("x-goog-api-key").orElse(null));

        String body = mockClient.lastRequestBody;
        assertTrue(body.contains("\"model\":\"gemini-3.6-flash\"") || body.contains("\"model\": \"gemini-3.6-flash\""));
        assertTrue(body.contains("Explain dependency scopes"));
    }

    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    static class MockHttpClient extends HttpClient {
        private final int statusCode;
        private final String responseBody;
        HttpRequest lastRequest;
        String lastRequestBody;

        MockHttpClient(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
            this.lastRequest = request;
            this.lastRequestBody = extractBodyString(request);
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
