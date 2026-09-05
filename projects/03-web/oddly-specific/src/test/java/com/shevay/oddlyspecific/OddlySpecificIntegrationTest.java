package com.shevay.oddlyspecific;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.oddlyspecific.server.WebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class OddlySpecificIntegrationTest {

    private WebServer server;
    private int port;
    private HttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        // Bind to port 0 for ephemeral port allocation
        server = new WebServer("127.0.0.1", 0);
        server.start();
        port = server.getPort();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void testStaticFileServingIndexHtml() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("ODDLY SPECIFIC"));
        assertTrue(response.body().contains("[ START EXPERIENCE ]"));
    }

    @Test
    void testStaticFileServingCssAndJs() throws Exception {
        HttpRequest cssRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/css/style.css"))
                .GET()
                .build();
        HttpResponse<String> cssResponse = client.send(cssRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, cssResponse.statusCode());
        assertTrue(cssResponse.headers().firstValue("Content-Type").orElse("").contains("text/css"));

        HttpRequest jsRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/js/app.js"))
                .GET()
                .build();
        HttpResponse<String> jsResponse = client.send(jsRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, jsResponse.statusCode());
        assertTrue(jsResponse.headers().firstValue("Content-Type").orElse("").contains("javascript"));
    }

    @Test
    void testFullSessionFlow() throws Exception {
        // 1. Start Session
        String startPayload = """
                {
                  "locationGranted": true,
                  "latitude": 37.7749,
                  "longitude": -122.4194,
                  "accuracy": 12.5
                }
                """;

        HttpRequest startRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/session/start"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(startPayload))
                .build();

        HttpResponse<String> startResponse = client.send(startRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, startResponse.statusCode());

        JsonNode startJson = objectMapper.readTree(startResponse.body());
        assertEquals("SUCCESS", startJson.path("status").asText());
        String sessionId = startJson.path("sessionId").asText();
        assertNotNull(sessionId);
        assertFalse(sessionId.isBlank());
        assertTrue(startJson.has("selectedChallenge"));

        // 2. Complete Challenge
        String completePayload = String.format("{\"sessionId\":\"%s\"}", sessionId);
        HttpRequest completeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/challenge/complete"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(completePayload))
                .build();

        HttpResponse<String> completeResponse = client.send(completeRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, completeResponse.statusCode());

        JsonNode completeJson = objectMapper.readTree(completeResponse.body());
        assertEquals("SUCCESS", completeJson.path("status").asText());

        // 3. Get Session State
        HttpRequest stateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/session/state?sessionId=" + sessionId))
                .GET()
                .build();

        HttpResponse<String> stateResponse = client.send(stateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, stateResponse.statusCode());

        JsonNode stateJson = objectMapper.readTree(stateResponse.body());
        assertEquals(sessionId, stateJson.path("sessionId").asText());
        assertTrue(stateJson.path("locationGranted").asBoolean());
        assertEquals(37.7749, stateJson.path("latitude").asDouble());
        assertEquals(-122.4194, stateJson.path("longitude").asDouble());
        assertTrue(stateJson.path("completed").asBoolean());
        assertNotNull(stateJson.path("connectionIp").asText());
    }

    @Test
    void testNotFoundEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/unknown"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void testAdminPageServing() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/admin"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("DEVELOPER OBSERVABILITY CONSOLE"));
    }

    @Test
    void testAdminApiActiveSessions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/admin/sessions"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
    }
}
