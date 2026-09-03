package com.shevay.knowledge.web;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.embedding.EmbeddingProvider;
import com.shevay.knowledge.embedding.EmbeddingPurpose;
import com.shevay.knowledge.generation.ContextAssembler;
import com.shevay.knowledge.generation.DummyLlmGenerationProvider;
import com.shevay.knowledge.generation.GenerationException;
import com.shevay.knowledge.generation.LlmGenerationProvider;
import com.shevay.knowledge.generation.PromptBuilder;
import com.shevay.knowledge.generation.RagService;
import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.model.VectorRecord;
import com.shevay.knowledge.retrieval.SimilaritySearchService;
import com.shevay.knowledge.vector.FileVectorStore;
import com.shevay.knowledge.vector.VectorStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManualWebVerificationTest {

    @TempDir
    Path tempDir;

    private static final float[] UNIT_VECTOR = createUnitVector(768);

    private static float[] createUnitVector(int dim) {
        float[] v = new float[dim];
        v[0] = 1.0f;
        return v;
    }

    private final EmbeddingProvider constantEmbeddingProvider = new EmbeddingProvider() {
        @Override public Embedding embed(String text, EmbeddingPurpose purpose) { return new Embedding(UNIT_VECTOR, 768, "constant-model"); }
        @Override public List<Embedding> embedBatch(List<String> texts, EmbeddingPurpose purpose) { return texts.stream().map(t -> embed(t, purpose)).toList(); }
        @Override public int getDimensions() { return 768; }
        @Override public String getModelIdentifier() { return "constant-model"; }
    };

    private HealthServlet healthServlet;
    private RagServlet ragServlet;
    private RagServlet failingRagServlet;

    @BeforeEach
    void setUp() throws Exception {
        healthServlet = new HealthServlet();

        AppConfig config = AppConfig.loadDefaults();
        Path storePath = tempDir.resolve("vectors.dat");
        VectorStore store = new FileVectorStore(storePath, 768);

        DummyLlmGenerationProvider llmProvider = new DummyLlmGenerationProvider("Maven lifecycle executes validate, compile, test, package, install, deploy.");
        SimilaritySearchService searchService = new SimilaritySearchService(config);

        RagService ragService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), llmProvider);

        DocumentChunk chunk = new DocumentChunk("c1", "doc1", "knowledge/maven.md", 0, "Maven build lifecycle consists of phases.", 40, 6);
        store.saveAll(List.of(new VectorRecord("v1", chunk, UNIT_VECTOR, 768)));

        ragServlet = new RagServlet(ragService, store);

        LlmGenerationProvider failingLlm = prompt -> {
            throw new GenerationException("Simulated internal generation error - GEMINI_API_KEY=secret_123");
        };
        RagService failingRagService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), failingLlm);
        failingRagServlet = new RagServlet(failingRagService, store);
    }

    @Test
    @DisplayName("Manual Endpoint Test 1: GET /api/health -> 200 {\"status\":\"UP\"}")
    void test1HealthEndpoint() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        healthServlet.doGet(req, resp);

        assertEquals(200, resp.getStatus());
        assertEquals("{\"status\":\"UP\"}", resp.getContentAsString());
    }

    @Test
    @DisplayName("Manual Endpoint Test 2: Valid RAG request -> 200 OK RagResponse")
    void test2ValidRagQuery() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/rag/query");
        req.setBody("{\"query\":\"What is the Maven lifecycle?\"}");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ragServlet.doPost(req, resp);

        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("generatedAnswer"));
    }

    @Test
    @DisplayName("Manual Endpoint Test 3 & 4: Empty store / zero chunks preserves no-relevant-context behavior")
    void test3UnrelatedQueryNoContext() throws Exception {
        VectorStore emptyStore = new FileVectorStore(tempDir.resolve("empty.dat"), 768);
        RagServlet emptyRagServlet = new RagServlet(ragServletInitService(), emptyStore);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/rag/query");
        req.setBody("{\"query\":\"Quantum physics in space?\"}");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        emptyRagServlet.doPost(req, resp);

        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("No relevant knowledge context found"));
    }

    private RagService ragServletInitService() {
        AppConfig config = AppConfig.loadDefaults();
        return new RagService(new SimilaritySearchService(config), constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), new DummyLlmGenerationProvider());
    }

    @Test
    @DisplayName("Manual Endpoint Test 5: Empty JSON {} -> 400 Bad Request")
    void test5EmptyJson() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/rag/query");
        req.setBody("{}");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ragServlet.doPost(req, resp);

        assertEquals(400, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("Query field must not be null"));
    }

    @Test
    @DisplayName("Manual Endpoint Test 6: Blank query -> 400 Bad Request")
    void test6BlankQuery() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/rag/query");
        req.setBody("{\"query\":\"    \"}");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ragServlet.doPost(req, resp);

        assertEquals(400, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("Query must not be blank"));
    }

    @Test
    @DisplayName("Manual Endpoint Test 7: Malformed JSON -> 400 Bad Request")
    void test7MalformedJson() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/rag/query");
        req.setBody("{bad-json");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ragServlet.doPost(req, resp);

        assertEquals(400, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("Malformed JSON request"));
    }

    @Test
    @DisplayName("Manual Endpoint Test 8: Unsupported HTTP method -> 405 Method Not Allowed")
    void test8UnsupportedMethod() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("PUT", "/api/rag/query");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ragServlet.doPut(req, resp);

        assertEquals(405, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("Method Not Allowed"));
    }

    @Test
    @DisplayName("Manual Endpoint Test 10: Trigger controlled internal failure -> 500 without exposing secrets")
    void test10InternalFailureNoLeaks() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/rag/query");
        req.setBody("{\"query\":\"Trigger failure\"}");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        failingRagServlet.doPost(req, resp);

        assertEquals(500, resp.getStatus());
        String body = resp.getContentAsString();
        assertTrue(body.contains("\"error\":"));
        assertFalse(body.contains("GEMINI_API_KEY"));
    }
}
