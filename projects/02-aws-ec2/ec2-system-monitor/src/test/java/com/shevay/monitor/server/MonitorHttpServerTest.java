package com.shevay.monitor.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.monitor.config.AppConfig;
import com.shevay.monitor.service.SystemMonitorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class MonitorHttpServerTest {

    private static final int TEST_PORT = 18080;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MonitorHttpServer server;
    private HttpClient httpClient;
    private String baseUrl;

    @BeforeEach
    public void setUp() throws IOException {
        AppConfig config = new AppConfig(TEST_PORT);
        SystemMonitorService service = new SystemMonitorService();
        server = new MonitorHttpServer(config, service);
        server.start();

        httpClient = HttpClient.newHttpClient();
        baseUrl = "http://localhost:" + TEST_PORT;
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testHealthEndpointReturns200AndJson() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/health"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));

        JsonNode json = MAPPER.readTree(response.body());
        assertEquals("UP", json.get("status").asText());
        assertEquals("ec2-system-monitor", json.get("service").asText());
        assertNotNull(json.get("timestamp").asText());
    }

    @Test
    public void testSystemEndpointReturns200AndSystemMetrics() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/system"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonNode json = MAPPER.readTree(response.body());
        assertNotNull(json.get("hostname").asText());
        assertNotNull(json.get("osName").asText());
        assertTrue(json.get("availableProcessors").asInt() > 0);
    }

    @Test
    public void testMetricsEndpointReturns200AndJvmMetrics() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/metrics"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonNode json = MAPPER.readTree(response.body());
        assertTrue(json.get("pid").asLong() > 0);
        assertTrue(json.get("jvmMaxMemoryBytes").asLong() > 0);
    }

    @Test
    public void testDiskEndpointReturns200AndDiskInfo() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/disk"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonNode json = MAPPER.readTree(response.body());
        assertTrue(json.get("totalBytes").asLong() > 0);
        assertNotNull(json.get("path").asText());
    }

    @Test
    public void testUnknownEndpointReturns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/does-not-exist"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        JsonNode json = MAPPER.readTree(response.body());
        assertEquals(404, json.get("status").asInt());
        assertTrue(json.get("error").asText().contains("Endpoint Not Found"));
    }

    @Test
    public void testUnsupportedMethodReturns405() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/health"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
        JsonNode json = MAPPER.readTree(response.body());
        assertEquals(405, json.get("status").asInt());
        assertTrue(json.get("error").asText().contains("Method Not Allowed"));
    }
}
