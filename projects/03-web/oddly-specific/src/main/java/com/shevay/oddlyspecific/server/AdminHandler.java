package com.shevay.oddlyspecific.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.oddlyspecific.session.Session;
import com.shevay.oddlyspecific.session.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class AdminHandler implements HttpHandler {

    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final boolean adminEnabled;

    public AdminHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.objectMapper = new ObjectMapper();
        String envFlag = System.getenv("ADMIN_CONSOLE_ENABLED");
        this.adminEnabled = (envFlag == null || envFlag.isBlank()) || Boolean.parseBoolean(envFlag.trim());
    }

    public static class SessionAdminDto {
        public String sessionId;
        public String status;
        public String challengeId;
        public String connectionIp;
        public long createdAt;
        public long sessionAgeSeconds;
        public boolean locationGranted;
        public Double latitude;
        public Double longitude;
        public Double accuracy;
        public boolean completed;

        // IP Geolocation Details
        public String ipCity;
        public String ipRegion;
        public String ipCountry;
        public Double ipLatitude;
        public Double ipLongitude;
        public String isp;

        // Client Environment Details
        public String userAgent;
        public String platform;
        public String language;
        public String timezone;
        public String screenResolution;

        // Privacy Flags
        public boolean persistentStorage = false;
        public boolean locationPersistence = false;
        public boolean trackingEnabled = false;

        public static SessionAdminDto fromSession(Session s, boolean isExpired) {
            SessionAdminDto dto = new SessionAdminDto();
            dto.sessionId = s.getSessionId();
            dto.challengeId = s.getSelectedChallengeId();
            dto.connectionIp = s.getConnectionIp();
            dto.createdAt = s.getCreatedAt();
            dto.sessionAgeSeconds = (System.currentTimeMillis() - s.getCreatedAt()) / 1000L;
            dto.locationGranted = s.isLocationGranted();
            dto.latitude = s.getLatitude();
            dto.longitude = s.getLongitude();
            dto.accuracy = s.getAccuracy();
            dto.completed = s.isCompleted();

            dto.ipCity = s.getIpCity();
            dto.ipRegion = s.getIpRegion();
            dto.ipCountry = s.getIpCountry();
            dto.ipLatitude = s.getIpLatitude();
            dto.ipLongitude = s.getIpLongitude();
            dto.isp = s.getIsp();

            dto.userAgent = s.getUserAgent();
            dto.platform = s.getPlatform();
            dto.language = s.getLanguage();
            dto.timezone = s.getTimezone();
            dto.screenResolution = s.getScreenResolution();

            if (isExpired) {
                dto.status = "EXPIRED";
            } else if (s.isCompleted()) {
                dto.status = "COMPLETED";
            } else {
                dto.status = "ACTIVE";
            }
            return dto;
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!adminEnabled) {
            sendJsonResponse(exchange, 403, Map.of("error", "Admin Console is disabled by configuration."));
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String method = exchange.getRequestMethod();

        // CORS headers for local dev testing
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            if ("/admin".equals(path) || "/admin/".equals(path) || "/admin/expired".equals(path)) {
                serveAdminPage(exchange);
            } else if ("/api/admin/sessions".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetActiveSessions(exchange);
            } else if ("/api/admin/sessions/expired".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetExpiredSessions(exchange);
            } else if (path.startsWith("/api/admin/sessions/") && "GET".equalsIgnoreCase(method)) {
                String sessionId = path.substring("/api/admin/sessions/".length());
                handleGetSingleSession(exchange, sessionId);
            } else if (path.startsWith("/api/admin/sessions/") && path.endsWith("/expire") && "POST".equalsIgnoreCase(method)) {
                String sessionId = path.substring("/api/admin/sessions/".length(), path.length() - "/expire".length());
                handleExpireSession(exchange, sessionId);
            } else if (path.startsWith("/api/admin/sessions/") && "DELETE".equalsIgnoreCase(method)) {
                String sessionId = path.substring("/api/admin/sessions/".length());
                handleDeleteSession(exchange, sessionId);
            } else {
                sendJsonResponse(exchange, 404, Map.of("error", "Admin endpoint not found"));
            }
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, Map.of("error", "Internal server error in Admin Module"));
        }
    }

    private void serveAdminPage(HttpExchange exchange) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/web/admin.html")) {
            if (is == null) {
                String msg = "404 Admin UI Not Found";
                sendResponse(exchange, 404, msg.getBytes(), "text/plain");
                return;
            }
            byte[] bytes = readAllBytes(is);
            sendResponse(exchange, 200, bytes, "text/html; charset=UTF-8");
        }
    }

    private void handleGetActiveSessions(HttpExchange exchange) throws IOException {
        Collection<Session> active = sessionManager.getActiveSessions();
        List<SessionAdminDto> dtos = active.stream()
                .map(s -> SessionAdminDto.fromSession(s, false))
                .sorted(Comparator.comparingLong((SessionAdminDto d) -> d.createdAt).reversed())
                .collect(Collectors.toList());
        sendJsonResponse(exchange, 200, dtos);
    }

    private void handleGetExpiredSessions(HttpExchange exchange) throws IOException {
        Collection<Session> expired = sessionManager.getExpiredSessions();
        List<SessionAdminDto> dtos = expired.stream()
                .map(s -> SessionAdminDto.fromSession(s, true))
                .sorted(Comparator.comparingLong((SessionAdminDto d) -> d.createdAt).reversed())
                .collect(Collectors.toList());
        sendJsonResponse(exchange, 200, dtos);
    }

    private void handleGetSingleSession(HttpExchange exchange, String sessionId) throws IOException {
        Optional<Session> opt = sessionManager.getSession(sessionId);
        if (opt.isEmpty()) {
            sendJsonResponse(exchange, 404, Map.of("error", "Session not found"));
            return;
        }
        boolean isExpired = sessionManager.getExpiredSessions().stream().anyMatch(s -> s.getSessionId().equals(sessionId));
        sendJsonResponse(exchange, 200, SessionAdminDto.fromSession(opt.get(), isExpired));
    }

    private void handleExpireSession(HttpExchange exchange, String sessionId) throws IOException {
        boolean expired = sessionManager.expireSession(sessionId);
        if (expired) {
            sendJsonResponse(exchange, 200, Map.of("status", "SUCCESS", "message", "Session manually expired"));
        } else {
            sendJsonResponse(exchange, 404, Map.of("error", "Active session not found"));
        }
    }

    private void handleDeleteSession(HttpExchange exchange, String sessionId) throws IOException {
        boolean deleted = sessionManager.deleteSession(sessionId);
        if (deleted) {
            sendJsonResponse(exchange, 200, Map.of("status", "SUCCESS", "message", "Session evicted"));
        } else {
            sendJsonResponse(exchange, 404, Map.of("error", "Session not found"));
        }
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

    private void sendResponse(HttpExchange exchange, int statusCode, byte[] body, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
            os.flush();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
