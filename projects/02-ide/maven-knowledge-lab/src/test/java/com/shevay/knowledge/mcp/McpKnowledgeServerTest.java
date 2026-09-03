package com.shevay.knowledge.mcp;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentLoader;
import com.shevay.knowledge.mcp.server.McpKnowledgeServer;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class McpKnowledgeServerTest {

    private McpKnowledgeServer server;

    @BeforeEach
    void setUp() {
        AppConfig config = AppConfig.loadDefaults();
        DocumentLoader documentLoader = new DocumentLoader();
        server = new McpKnowledgeServer(config, documentLoader);
    }

    @Test
    @DisplayName("1. McpKnowledgeServer handleGetKnowledgeDocument returns structured document result for valid path")
    void testHandleGetKnowledgeDocumentValid() {
        CallToolResult result = server.handleGetKnowledgeDocument(Map.of("path", "java/collections.md"));
        assertNotNull(result);
        assertFalse(result.isError(), "Result must not be an error for valid document");
        assertNotNull(result.content());
        assertFalse(result.content().isEmpty());

        TextContent content = (TextContent) result.content().get(0);
        assertTrue(content.text().contains("java/collections.md") || content.text().contains("Java Collections"));
    }

    @Test
    @DisplayName("2. Security: McpKnowledgeServer handleGetKnowledgeDocument rejects path traversal and absolute paths")
    void testHandleGetKnowledgeDocumentSecurityRejections() {
        CallToolResult r1 = server.handleGetKnowledgeDocument(Map.of("path", "../pom.xml"));
        assertTrue(r1.isError());

        CallToolResult r2 = server.handleGetKnowledgeDocument(Map.of("path", "C:\\Windows\\System32\\config"));
        assertTrue(r2.isError());

        CallToolResult r3 = server.handleGetKnowledgeDocument(Map.of("path", "/etc/passwd"));
        assertTrue(r3.isError());
    }

    @Test
    @DisplayName("3. McpKnowledgeServer handleGetKnowledgeDocument handles missing or invalid arguments safely")
    void testHandleGetKnowledgeDocumentMissingArgs() {
        CallToolResult r1 = server.handleGetKnowledgeDocument(null);
        assertTrue(r1.isError());

        CallToolResult r2 = server.handleGetKnowledgeDocument(Map.of());
        assertTrue(r2.isError());

        CallToolResult r3 = server.handleGetKnowledgeDocument(Map.of("path", "   "));
        assertTrue(r3.isError());

        CallToolResult r4 = server.handleGetKnowledgeDocument(Map.of("path", "non_existent.md"));
        assertTrue(r4.isError());
    }
}
