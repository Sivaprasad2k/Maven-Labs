package com.shevay.knowledge.web;

import com.shevay.knowledge.agent.KnowledgeAgent;
import com.shevay.knowledge.agent.ToolRegistry;
import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentLoader;
import com.shevay.knowledge.mcp.McpTestService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DeveloperConsoleServletTest {

    private DeveloperConsoleServlet consoleServlet;
    private KnowledgeServlet knowledgeServlet;
    private AgentServlet agentServlet;
    private McpServlet mcpServlet;

    @BeforeEach
    void setUp() {
        consoleServlet = new DeveloperConsoleServlet();
        knowledgeServlet = new KnowledgeServlet(AppConfig.loadDefaults(), new DocumentLoader());

        ToolRegistry registry = new ToolRegistry();
        KnowledgeAgent agent = new KnowledgeAgent(registry, context -> new com.shevay.knowledge.agent.AgentDecision(com.shevay.knowledge.agent.AgentDecisionType.FINAL_ANSWER, "Agent Test Answer", null));
        agentServlet = new AgentServlet(agent);
        mcpServlet = new McpServlet(new McpTestService());
    }

    @Test
    @DisplayName("DeveloperConsoleServlet serves HTML Developer Console on GET /")
    void testDeveloperConsoleServletHtml() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        consoleServlet.doGet(req, resp);

        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentType().contains("text/html"));
        assertTrue(resp.getContentAsString().contains("Maven Knowledge Lab"));
        assertTrue(resp.getContentAsString().contains("Developer Console"));
    }

    @Test
    @DisplayName("KnowledgeServlet lists documents on GET /api/knowledge/documents")
    void testKnowledgeServletListDocuments() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/knowledge/documents");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        knowledgeServlet.doGet(req, resp);

        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("collections.md") || resp.getContentAsString().contains("lifecycle.md"));
    }

    @Test
    @DisplayName("KnowledgeServlet retrieves document details on GET /api/knowledge/document")
    void testKnowledgeServletGetDocument() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/knowledge/document");
        req.setParameter("path", "java/collections.md");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        knowledgeServlet.doGet(req, resp);

        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("Java Collections"));
    }

    @Test
    @DisplayName("AgentServlet executes agent query on POST /api/agent/query")
    void testAgentServletQuery() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/agent/query");
        req.setBody("{\"query\": \"Explain Maven dependency scopes\"}");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        agentServlet.doPost(req, resp);

        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("Agent Test Answer"));
    }

    @Test
    @DisplayName("McpServlet executes MCP test on POST /api/mcp/test")
    void testMcpServletTest() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/mcp/test");
        req.setBody("{\"path\": \"java/collections.md\"}");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        mcpServlet.doPost(req, resp);

        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("getKnowledgeDocument"));
        assertTrue(resp.getContentAsString().contains("CONNECTED"));
    }
}
