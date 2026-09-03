package com.shevay.knowledge.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.DummyEmbeddingProvider;

import com.shevay.knowledge.generation.ContextAssembler;
import com.shevay.knowledge.generation.DummyLlmGenerationProvider;
import com.shevay.knowledge.generation.GenerationException;
import com.shevay.knowledge.generation.PromptBuilder;
import com.shevay.knowledge.generation.RagService;
import com.shevay.knowledge.model.RagResponse;
import com.shevay.knowledge.retrieval.SimilaritySearchService;
import com.shevay.knowledge.vector.FileVectorStore;
import com.shevay.knowledge.vector.VectorStore;
import com.shevay.knowledge.vector.VectorStoreException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet handling POST /api/rag/query requests.
 * Deserializes JSON query payloads, invokes RagService, and serializes RagResponse objects.
 * Performs strict HTTP validation and returns JSON error responses.
 */
@WebServlet("/api/rag/query")
public class RagServlet extends HttpServlet {

    private final ObjectMapper objectMapper;
    private RagService ragService;
    private VectorStore vectorStore;

    public RagServlet() {
        this(null, null);
    }

    public RagServlet(RagService ragService, VectorStore vectorStore) {
        this.objectMapper = new ObjectMapper();
        this.ragService = ragService;
        this.vectorStore = vectorStore;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.ragService == null) {
            Object serviceObj = getServletContext().getAttribute(WebContextListener.RAG_SERVICE_ATTRIBUTE);
            if (serviceObj instanceof RagService service) {
                this.ragService = service;
            }
        }
        if (this.vectorStore == null) {
            Object storeObj = getServletContext().getAttribute(WebContextListener.VECTOR_STORE_ATTRIBUTE);
            if (storeObj instanceof VectorStore store) {
                this.vectorStore = store;
            }
        }

        // Lazy fallback if initialized outside full web container context
        if (this.ragService == null || this.vectorStore == null) {
            AppConfig config = AppConfig.loadDefaults();
            if (this.vectorStore == null) {
                this.vectorStore = new FileVectorStore(config);
            }
            if (this.ragService == null) {
                this.ragService = new RagService(
                        new SimilaritySearchService(config),
                        new DummyEmbeddingProvider(config.getEmbeddingDimensions()),
                        new ContextAssembler(),
                        new PromptBuilder(),
                        new DummyLlmGenerationProvider()
                );
            }
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
            RagResponse ragResponse = ragService.query(queryRequest.query(), vectorStore);
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), ragResponse);
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, sanitizeErrorMessage(e.getMessage()));
        } catch (GenerationException | VectorStoreException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, sanitizeErrorMessage(e.getMessage()));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred while processing the RAG query");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendMethodNotAllowed(resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendMethodNotAllowed(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendMethodNotAllowed(resp);
    }

    private static void sendMethodNotAllowed(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.getWriter().write("{\"error\":\"Method Not Allowed\"}");
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), Map.of("error", message));
    }

    private static String sanitizeErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Internal error occurred";
        }
        // Ensure no API keys, credentials, or internal raw filesystem paths are exposed
        if (message.contains("GEMINI_API_KEY") || message.contains("x-goog-api-key")) {
            return "Generation service authentication error";
        }
        return message;
    }
}
