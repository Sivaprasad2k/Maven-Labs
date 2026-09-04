package com.shevay.knowledge.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.shevay.knowledge.model.RagResponse;
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

class RagServletTest {

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

    private RagService ragService;
    private VectorStore vectorStore;
    private DummyLlmGenerationProvider dummyLlmProvider;

    @BeforeEach
    void setUp() throws Exception {
        Path storePath = tempDir.resolve("vectors.dat");
        AppConfig config = AppConfig.loadDefaults();
        dummyLlmProvider = new DummyLlmGenerationProvider("Maven build phases run sequentially.");
        SimilaritySearchService searchService = new SimilaritySearchService(config);
        vectorStore = new FileVectorStore(storePath, 768);
        ragService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), dummyLlmProvider);

        DocumentChunk chunk = new DocumentChunk("c1", "doc1", "knowledge/maven.md", 0, "Maven lifecycle consists of phases.", 30, 5);
        vectorStore.saveAll(List.of(new VectorRecord("v1", chunk, UNIT_VECTOR, 768)));
    }

    @Test
    @DisplayName("3, 4, 5, 16. Valid RAG request reaches RagService, returns 200, and serializes RagResponse JSON")
    void testValidRagRequest() throws Exception {
        RagServlet servlet = new RagServlet(ragService, vectorStore);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rag/query");
        request.setBody("{\"query\":\"What is the Maven lifecycle?\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().contains("application/json"));
        assertEquals(1, dummyLlmProvider.getCallCount(), "Request must reach RagService and invoke LLM");

        String jsonOutput = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        RagResponse ragResponse = mapper.readValue(jsonOutput, RagResponse.class);

        assertNotNull(ragResponse);
        assertEquals("What is the Maven lifecycle?", ragResponse.query());
        assertEquals("Maven build phases run sequentially.", ragResponse.generatedAnswer());
        assertFalse(ragResponse.retrievedChunks().isEmpty());
        assertFalse(ragResponse.sources().isEmpty());
    }

    @Test
    @DisplayName("6. Missing request body returns 400 Bad Request")
    void testMissingRequestBodyReturns400() throws Exception {
        RagServlet servlet = new RagServlet(ragService, vectorStore);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rag/query");
        request.setBody("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Request body is required"));
    }

    @Test
    @DisplayName("7. Missing query field in JSON returns 400 Bad Request")
    void testMissingQueryFieldReturns400() throws Exception {
        RagServlet servlet = new RagServlet(ragService, vectorStore);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rag/query");
        request.setBody("{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Query field must not be null"));
    }

    @Test
    @DisplayName("8. Null query returns 400 Bad Request")
    void testNullQueryReturns400() throws Exception {
        RagServlet servlet = new RagServlet(ragService, vectorStore);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rag/query");
        request.setBody("{\"query\":null}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Query field must not be null"));
    }

    @Test
    @DisplayName("9. Blank query returns 400 Bad Request")
    void testBlankQueryReturns400() throws Exception {
        RagServlet servlet = new RagServlet(ragService, vectorStore);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rag/query");
        request.setBody("{\"query\":\"   \"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Query must not be blank"));
    }

    @Test
    @DisplayName("10. Malformed JSON returns 400 Bad Request")
    void testMalformedJsonReturns400() throws Exception {
        RagServlet servlet = new RagServlet(ragService, vectorStore);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rag/query");
        request.setBody("{invalid_json: true");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Malformed JSON request"));
    }

    @Test
    @DisplayName("11. Unsupported HTTP method (GET/PUT/DELETE) returns 405 Method Not Allowed")
    void testUnsupportedMethodReturns405() throws Exception {
        RagServlet servlet = new RagServlet(ragService, vectorStore);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rag/query");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        assertEquals(405, response.getStatus());
        assertTrue(response.getContentAsString().contains("Method Not Allowed"));
    }

    @Test
    @DisplayName("12, 13, 14, 15. RAG service failure returns 500 valid JSON without stack traces or credential leaks")
    void testServiceFailureReturns500WithoutLeaks() throws Exception {
        LlmGenerationProvider failingLlm = prompt -> {
            throw new GenerationException("Gemini API Error (500) - secret GEMINI_API_KEY value leaked!");
        };
        RagService failingRagService = new RagService(
                ragService.getSearchService(),
                constantEmbeddingProvider,
                new ContextAssembler(),
                new PromptBuilder(),
                failingLlm
        );

        RagServlet servlet = new RagServlet(failingRagService, vectorStore);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rag/query");
        request.setBody("{\"query\":\"Valid question?\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals(500, response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"error\":"));
        assertFalse(body.contains("GEMINI_API_KEY"), "Error response must NEVER expose API credentials");
        assertFalse(body.contains("at com.shevay"), "Error response must NEVER expose java stack traces");
    }

    @Test
    @DisplayName("RagServlet.init must throw ServletException when ServletContext attributes are missing")
    void testInitThrowsServletExceptionWhenContextAttributesMissing() {
        RagServlet servlet = new RagServlet();
        jakarta.servlet.ServletContext emptyServletContext = (jakarta.servlet.ServletContext) java.lang.reflect.Proxy.newProxyInstance(
                jakarta.servlet.ServletContext.class.getClassLoader(),
                new Class<?>[]{jakarta.servlet.ServletContext.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> null;
                    default -> null;
                }
        );
        jakarta.servlet.ServletConfig mockConfig = (jakarta.servlet.ServletConfig) java.lang.reflect.Proxy.newProxyInstance(
                jakarta.servlet.ServletConfig.class.getClassLoader(),
                new Class<?>[]{jakarta.servlet.ServletConfig.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getServletContext" -> emptyServletContext;
                    case "getServletName" -> "RagServlet";
                    default -> null;
                }
        );

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class, () -> servlet.init(mockConfig));
        assertTrue(ex.getMessage().contains("missing from ServletContext"),
                "RagServlet must fail fast with ServletException instead of instantiating DummyEmbeddingProvider");
    }
}
