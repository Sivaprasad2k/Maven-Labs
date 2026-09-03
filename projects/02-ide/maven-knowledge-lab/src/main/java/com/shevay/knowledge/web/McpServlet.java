package com.shevay.knowledge.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.knowledge.mcp.McpTestService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet handling POST /api/mcp/test requests for testing MCP client/server STDIO interaction.
 */
@WebServlet("/api/mcp/test")
public class McpServlet extends HttpServlet {

    private final ObjectMapper objectMapper;
    private McpTestService mcpTestService;

    public McpServlet() {
        this(null);
    }

    public McpServlet(McpTestService mcpTestService) {
        this.objectMapper = new ObjectMapper();
        this.mcpTestService = mcpTestService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.mcpTestService == null) {
            this.mcpTestService = new McpTestService();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String body = new String(req.getInputStream().readAllBytes()).trim();
        String requestedPath = "java/collections.md";

        if (!body.isEmpty()) {
            try {
                Map<?, ?> map = objectMapper.readValue(body, Map.class);
                if (map.containsKey("path") && map.get("path") instanceof String p && !p.isBlank()) {
                    requestedPath = p.trim();
                }
            } catch (JsonProcessingException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON request");
                return;
            }
        }

        try {
            Map<String, Object> testResult = mcpTestService.runTest(requestedPath);
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), testResult);
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MCP test execution error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), Map.of("error", message));
    }
}
