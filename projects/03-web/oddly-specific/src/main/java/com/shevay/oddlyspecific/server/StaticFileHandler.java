package com.shevay.oddlyspecific.server;

import com.shevay.oddlyspecific.config.AppConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            sendResponse(exchange, 405, "Method Not Allowed".getBytes(), "text/plain");
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        if (path == null || path.equals("/") || path.isBlank()) {
            path = "/index.html";
        }

        // Normalize path to prevent path traversal
        Path normalizedPath = Paths.get(path).normalize();
        String safePath = normalizedPath.toString().replace('\\', '/');
        if (safePath.startsWith("..") || safePath.contains("../")) {
            sendResponse(exchange, 400, "Bad Request".getBytes(), "text/plain");
            return;
        }

        String resourcePath = AppConfig.WEB_RESOURCE_ROOT + (safePath.startsWith("/") ? safePath : "/" + safePath);

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                String notFoundMsg = "404 Not Found";
                sendResponse(exchange, 404, notFoundMsg.getBytes(), "text/plain");
                return;
            }

            byte[] bytes = readAllBytes(is);
            String contentType = getContentType(safePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");

            if ("HEAD".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
            } else {
                sendResponse(exchange, 200, bytes, contentType);
            }
        } catch (Exception e) {
            String errorMsg = "Internal Server Error";
            sendResponse(exchange, 500, errorMsg.getBytes(), "text/plain");
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

    private String getContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        } else if (path.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (path.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (path.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        } else if (path.endsWith(".ico")) {
            return "image/x-icon";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
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
