package com.shevay.knowledge.mcp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class McpTestServiceTest {

    @Test
    @DisplayName("McpTestService executes MCP test and returns structured result")
    void testMcpTestServiceExecution() {
        McpTestService service = new McpTestService();
        Map<String, Object> result = service.runTest("java/collections.md");

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals("CONNECTED", result.get("status"));
        assertEquals(List.of("getKnowledgeDocument"), result.get("discoveredTools"));
        assertEquals("getKnowledgeDocument", result.get("toolName"));
        assertEquals("java/collections.md", result.get("requestedPath"));
        assertNotNull(result.get("output"));
    }
}
