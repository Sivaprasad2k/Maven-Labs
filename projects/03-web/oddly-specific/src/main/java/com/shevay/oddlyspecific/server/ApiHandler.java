package com.shevay.oddlyspecific.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.oddlyspecific.challenge.Challenge;
import com.shevay.oddlyspecific.challenge.ChallengeEngine;
import com.shevay.oddlyspecific.privacy.IpGeolocationService;
import com.shevay.oddlyspecific.privacy.IpResolver;
import com.shevay.oddlyspecific.session.Session;
import com.shevay.oddlyspecific.session.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ApiHandler implements HttpHandler {

    private final SessionManager sessionManager;
    private final ChallengeEngine challengeEngine;
    private final IpGeolocationService ipGeolocationService;
    private final ObjectMapper objectMapper;

    public ApiHandler(SessionManager sessionManager, ChallengeEngine challengeEngine) {
        this.sessionManager = sessionManager;
        this.challengeEngine = challengeEngine;
        this.ipGeolocationService = new IpGeolocationService();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String method = exchange.getRequestMethod();

        // Enable CORS headers for API calls
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            if ("/api/session/start".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleStartSession(exchange);
            } else if ("/api/challenge/complete".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleCompleteChallenge(exchange);
            } else if ("/api/session/state".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetSessionState(exchange);
            } else if ("/api/challenges".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetAllChallenges(exchange);
            } else {
                sendJsonResponse(exchange, 404, Map.of("error", "Endpoint Not Found"));
            }
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, Map.of("error", "Internal Server Error"));
        }
    }

    private void handleStartSession(HttpExchange exchange) throws IOException {
        String connectionIp = IpResolver.resolveConnectionIp(exchange);
        Challenge challenge = challengeEngine.getRandomChallenge();
        Session session = sessionManager.createSession(challenge.getId(), connectionIp);

        // Perform IP Geolocation lookup
        IpGeolocationService.IpLocationResult ipLoc = ipGeolocationService.resolveIpLocation(connectionIp);
        sessionManager.updateIpLocation(session.getSessionId(), ipLoc.getCity(), ipLoc.getRegion(), ipLoc.getCountry(), ipLoc.getLatitude(), ipLoc.getLongitude(), ipLoc.getIsp());

        String userAgentHeader = exchange.getRequestHeaders().getFirst("User-Agent");

        // Optional request body parsing for browser geolocation & client environment data
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        if (!body.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(body);
                boolean locationGranted = root.path("locationGranted").asBoolean(false);
                Double lat = root.hasNonNull("latitude") ? root.path("latitude").asDouble() : null;
                Double lng = root.hasNonNull("longitude") ? root.path("longitude").asDouble() : null;
                Double acc = root.hasNonNull("accuracy") ? root.path("accuracy").asDouble() : null;
                sessionManager.updateLocation(session.getSessionId(), locationGranted, lat, lng, acc);

                String ua = root.path("userAgent").asText(userAgentHeader != null ? userAgentHeader : "Unknown UA");
                String platform = root.path("platform").asText("Unknown Platform");
                String language = root.path("language").asText("en-US");
                String timezone = root.path("timezone").asText("UTC");
                String resolution = root.path("screenResolution").asText("Unknown Resolution");
                sessionManager.updateClientEnvironment(session.getSessionId(), ua, platform, language, timezone, resolution);
            } catch (Exception e) {
                sessionManager.updateClientEnvironment(session.getSessionId(), userAgentHeader != null ? userAgentHeader : "Unknown UA", "Unknown Platform", "en-US", "UTC", "Unknown Resolution");
            }
        } else {
            sessionManager.updateClientEnvironment(session.getSessionId(), userAgentHeader != null ? userAgentHeader : "Unknown UA", "Unknown Platform", "en-US", "UTC", "Unknown Resolution");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("sessionId", session.getSessionId());
        response.put("selectedChallenge", challenge);

        sendJsonResponse(exchange, 200, response);
    }

    private void handleCompleteChallenge(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        if (body.isBlank()) {
            sendJsonResponse(exchange, 400, Map.of("error", "Missing request body"));
            return;
        }

        JsonNode root = objectMapper.readTree(body);
        String sessionId = root.path("sessionId").asText(null);

        if (sessionId == null || sessionId.isBlank()) {
            sendJsonResponse(exchange, 400, Map.of("error", "sessionId is required"));
            return;
        }

        Optional<Session> opt = sessionManager.getSession(sessionId);
        if (opt.isEmpty()) {
            sendJsonResponse(exchange, 404, Map.of("error", "Session not found or expired"));
            return;
        }

        // Update location if provided in completion request as well
        if (root.has("locationGranted")) {
            boolean locationGranted = root.path("locationGranted").asBoolean(false);
            Double lat = root.hasNonNull("latitude") ? root.path("latitude").asDouble() : null;
            Double lng = root.hasNonNull("longitude") ? root.path("longitude").asDouble() : null;
            Double acc = root.hasNonNull("accuracy") ? root.path("accuracy").asDouble() : null;
            sessionManager.updateLocation(sessionId, locationGranted, lat, lng, acc);
        }

        sessionManager.completeSession(sessionId);
        sendJsonResponse(exchange, 200, Map.of("status", "SUCCESS", "message", "Challenge completed"));
    }

    private void handleGetSessionState(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String sessionId = getQueryParam(query, "sessionId");

        if (sessionId == null || sessionId.isBlank()) {
            sendJsonResponse(exchange, 400, Map.of("error", "sessionId parameter is required"));
            return;
        }

        Optional<Session> opt = sessionManager.getSession(sessionId);
        if (opt.isEmpty()) {
            sendJsonResponse(exchange, 404, Map.of("error", "Session not found or expired"));
            return;
        }

        Session session = opt.get();
        Map<String, Object> state = new HashMap<>();
        state.put("sessionId", session.getSessionId());
        state.put("selectedChallengeId", session.getSelectedChallengeId());
        state.put("connectionIp", session.getConnectionIp());
        state.put("locationGranted", session.isLocationGranted());
        state.put("latitude", session.getLatitude());
        state.put("longitude", session.getLongitude());
        state.put("accuracy", session.getAccuracy());
        state.put("completed", session.isCompleted());

        // IP Geolocation metadata
        state.put("ipCity", session.getIpCity());
        state.put("ipRegion", session.getIpRegion());
        state.put("ipCountry", session.getIpCountry());
        state.put("ipLatitude", session.getIpLatitude());
        state.put("ipLongitude", session.getIpLongitude());
        state.put("isp", session.getIsp());

        sendJsonResponse(exchange, 200, state);
    }

    private void handleGetAllChallenges(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, challengeEngine.getAllChallenges());
    }

    private String getQueryParam(String query, String name) {
        if (query == null || query.isBlank()) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(name)) {
                return pair[1];
            }
        }
        return null;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, jsonBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonBytes);
            os.flush();
        }
    }
}
