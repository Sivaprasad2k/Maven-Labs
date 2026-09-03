package com.shevay.knowledge.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.knowledge.agent.AgentContext;
import com.shevay.knowledge.agent.AgentDecisionProvider;
import com.shevay.knowledge.agent.AgentExecutionEntry;
import com.shevay.knowledge.agent.DummyAgentDecisionProvider;
import com.shevay.knowledge.agent.GeminiAgentDecisionProvider;
import com.shevay.knowledge.agent.KnowledgeAgent;
import com.shevay.knowledge.agent.ToolRegistry;
import com.shevay.knowledge.agent.tools.ExplainMavenConceptTool;
import com.shevay.knowledge.agent.tools.GetDocumentTool;
import com.shevay.knowledge.agent.tools.SearchKnowledgeTool;
import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.DummyEmbeddingProvider;
import com.shevay.knowledge.generation.ContextAssembler;
import com.shevay.knowledge.generation.DummyLlmGenerationProvider;
import com.shevay.knowledge.generation.PromptBuilder;
import com.shevay.knowledge.generation.RagService;
import com.shevay.knowledge.retrieval.SimilaritySearchService;
import com.shevay.knowledge.vector.FileVectorStore;
import com.shevay.knowledge.vector.VectorStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servlet handling POST /api/agent/query requests for controlled KnowledgeAgent execution.
 */
@WebServlet("/api/agent/query")
public class AgentServlet extends HttpServlet {

    private final ObjectMapper objectMapper;
    private KnowledgeAgent agent;

    public AgentServlet() {
        this(null);
    }

    public AgentServlet(KnowledgeAgent agent) {
        this.objectMapper = new ObjectMapper();
        this.agent = agent;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.agent == null) {
            AppConfig config = AppConfig.loadDefaults();
            VectorStore vectorStore = new FileVectorStore(config);
            SimilaritySearchService searchService = new SimilaritySearchService(config);

            String apiKey = config.getGeminiApiKey();
            var embeddingProvider = (apiKey != null && !apiKey.isBlank())
                    ? new com.shevay.knowledge.embedding.GeminiEmbeddingProvider(config)
                    : new DummyEmbeddingProvider(768);

            var genProvider = (apiKey != null && !apiKey.isBlank())
                    ? new com.shevay.knowledge.generation.GeminiGenerationProvider(config)
                    : new DummyLlmGenerationProvider();

            RagService ragService = new RagService(searchService, embeddingProvider, new ContextAssembler(), new PromptBuilder(), genProvider);

            ToolRegistry registry = new ToolRegistry();
            registry.register(new SearchKnowledgeTool(searchService, embeddingProvider, vectorStore));
            registry.register(new GetDocumentTool(config));
            registry.register(new ExplainMavenConceptTool(ragService, vectorStore));

            AgentDecisionProvider decisionProvider = (apiKey != null && !apiKey.isBlank())
                    ? new GeminiAgentDecisionProvider(config)
                    : new DummyAgentDecisionProvider((ctx) -> {
                        if (ctx.getHistory().isEmpty()) {
                            return com.shevay.knowledge.agent.AgentDecision.toolCall("explainMavenConcept", Map.of("concept", ctx.getUserQuery()));
                        } else {
                            String toolOut = ctx.getHistory().get(0).toolResult() != null ? ctx.getHistory().get(0).toolResult().output() : "Default agent answer";
                            return com.shevay.knowledge.agent.AgentDecision.finalAnswer(toolOut);
                        }
                    });

            this.agent = new KnowledgeAgent(registry, decisionProvider);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String body = new String(req.getInputStream().readAllBytes()).trim();
        if (body.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body is required");
            return;
        }

        RagQueryRequest queryRequest;
        try {
            queryRequest = objectMapper.readValue(body, RagQueryRequest.class);
        } catch (JsonProcessingException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON request");
            return;
        }

        if (queryRequest == null || queryRequest.query() == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query field must not be null");
            return;
        }

        if (queryRequest.query().isBlank()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query must not be blank");
            return;
        }

        try {
            AgentContext context = new AgentContext(queryRequest.query(), agent.getToolRegistry().getTools(), agent.getMaxIterations());
            String answer = agent.execute(context);

            List<Map<String, Object>> traceList = new ArrayList<>();
            for (AgentExecutionEntry entry : context.getHistory()) {
                Map<String, Object> stepMap = Map.of(
                        "step", entry.step(),
                        "decisionType", entry.decision().type().name(),
                        "toolName", entry.decision().toolCall() != null ? entry.decision().toolCall().tool() : "N/A",
                        "toolResultSuccess", entry.toolResult() != null && entry.toolResult().success(),
                        "toolOutput", entry.toolResult() != null ? (entry.toolResult().success() ? entry.toolResult().output() : entry.toolResult().errorMessage()) : "N/A"
                );
                traceList.add(stepMap);
            }

            Map<String, Object> responseMap = Map.of(
                    "query", queryRequest.query(),
                    "answer", answer,
                    "trace", traceList
            );

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), responseMap);

        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, sanitizeError(e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        sendError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), Map.of("error", message));
    }

    private static String sanitizeError(String msg) {
        if (msg == null || msg.isBlank()) return "Internal server error";
        if (msg.contains("GEMINI_API_KEY") || msg.contains("x-goog-api-key")) {
            return "Agent service authentication failure";
        }
        return msg;
    }
}
