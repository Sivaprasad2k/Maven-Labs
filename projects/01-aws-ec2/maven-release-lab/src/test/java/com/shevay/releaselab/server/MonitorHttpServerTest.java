package com.shevay.releaselab.server;

import com.shevay.releaselab.config.AppConfig;
import com.shevay.releaselab.service.ReleaseLabService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class MonitorHttpServerTest {

    private static final int TEST_PORT = 8990;
    private MonitorHttpServer server;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws IOException {
        AppConfig config = new AppConfig(TEST_PORT, "maven-release-lab", "1.0.0", "dev", "development");
        ReleaseLabService service = new ReleaseLabService();
        server = new MonitorHttpServer(config, service);
        server.start();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void testHealthEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/api/health"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertTrue(response.body().contains("\"status\":\"UP\""));
    }

    @Test
    void testInfoEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/api/info"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"application\":\"maven-release-lab\""));
        assertTrue(response.body().contains("\"version\":\"1.0.0\""));
        assertTrue(response.body().contains("\"environment\":\"development\""));
    }

    @Test
    void testBuildEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/api/build"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"application\":\"maven-release-lab\""));
        assertTrue(response.body().contains("\"buildProfile\":\"dev\""));
    }

    @Test
    void testNotFoundEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/api/unknown"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("\"error\":\"Endpoint Not Found: /api/unknown\""));
    }

    @Test
    void testMethodNotAllowed() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/api/health"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
        assertTrue(response.body().contains("\"error\":\"Method Not Allowed. Only GET is supported.\""));
    }
}
