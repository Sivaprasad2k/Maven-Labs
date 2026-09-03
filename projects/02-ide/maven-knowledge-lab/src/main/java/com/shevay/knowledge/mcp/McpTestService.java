package com.shevay.knowledge.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentLoader;
import com.shevay.knowledge.mcp.server.McpKnowledgeServer;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service orchestrating deterministic request-scoped MCP test executions for the Servlet layer and CLI.
 */
public class McpTestService {

    private final McpKnowledgeServer server;
    private final ObjectMapper objectMapper;

    public McpTestService() {
        this(new McpKnowledgeServer(AppConfig.loadDefaults(), new DocumentLoader()));
    }

    public McpTestService(McpKnowledgeServer server) {
        this.server = Objects.requireNonNull(server, "server must not be null");
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> runTest(String path) {
        String documentPath = (path == null || path.isBlank()) ? "java/collections.md" : path.trim();

        CallToolResult toolResult = server.handleGetKnowledgeDocument(Map.of("path", documentPath));

        String contentText = "";
        if (toolResult.content() != null && !toolResult.content().isEmpty()) {
            if (toolResult.content().get(0) instanceof TextContent textContent) {
                contentText = textContent.text();
            }
        }

        Object parsedOutput;
        try {
            parsedOutput = objectMapper.readValue(contentText, Map.class);
        } catch (Exception e) {
            parsedOutput = contentText;
        }

        return Map.of(
                "success", !toolResult.isError(),
                "status", "CONNECTED",
                "discoveredTools", List.of(McpKnowledgeServer.TOOL_NAME),
                "toolName", McpKnowledgeServer.TOOL_NAME,
                "requestedPath", documentPath,
                "output", parsedOutput
        );
    }
}
