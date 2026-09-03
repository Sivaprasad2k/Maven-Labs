package com.shevay.knowledge.agent;

import com.shevay.knowledge.agent.tools.ExplainMavenConceptTool;
import com.shevay.knowledge.agent.tools.GetDocumentTool;
import com.shevay.knowledge.agent.tools.SearchKnowledgeTool;
import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.embedding.EmbeddingProvider;
import com.shevay.knowledge.embedding.EmbeddingPurpose;
import com.shevay.knowledge.generation.ContextAssembler;
import com.shevay.knowledge.generation.DummyLlmGenerationProvider;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentToolsTest {

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

    private AppConfig config;
    private VectorStore vectorStore;
    private SimilaritySearchService searchService;
    private RagService ragService;

    @BeforeEach
    void setUp() throws Exception {
        config = AppConfig.loadDefaults();
        Path storePath = tempDir.resolve("vectors.dat");
        vectorStore = new FileVectorStore(storePath, 768);
        searchService = new SimilaritySearchService(config);
        ragService = new RagService(
                searchService,
                constantEmbeddingProvider,
                new ContextAssembler(),
                new PromptBuilder(),
                new DummyLlmGenerationProvider("Maven lifecycle consists of validate, compile, test, package.")
        );

        DocumentChunk chunk = new DocumentChunk("c1", "doc1", "knowledge/maven/lifecycle.md", 0, "Maven lifecycle content.", 25, 4);
        vectorStore.saveAll(List.of(new VectorRecord("v1", chunk, UNIT_VECTOR, 768)));
    }

    @Test
    @DisplayName("4. SearchKnowledgeTool execution returns search results")
    void testSearchKnowledgeToolExecution() {
        SearchKnowledgeTool tool = new SearchKnowledgeTool(searchService, constantEmbeddingProvider, vectorStore);
        assertEquals("searchKnowledge", tool.name());

        ToolResult result = tool.execute(Map.of("query", "Maven lifecycle"));
        assertTrue(result.success());
        assertNotNull(result.output());
        assertNull(result.errorMessage());
    }

    @Test
    @DisplayName("8. SearchKnowledgeTool rejects missing or blank query argument")
    void testSearchKnowledgeToolRejectsInvalidArgs() {
        SearchKnowledgeTool tool = new SearchKnowledgeTool(searchService, constantEmbeddingProvider, vectorStore);

        ToolResult r1 = tool.execute(Map.of());
        assertFalse(r1.success());
        assertTrue(r1.errorMessage().contains("Missing required argument"));

        ToolResult r2 = tool.execute(Map.of("query", "   "));
        assertFalse(r2.success());
        assertTrue(r2.errorMessage().contains("must be a non-blank string"));
    }

    @Test
    @DisplayName("5 & Security: GetDocumentTool rejects path traversal, absolute paths, and unknown files")
    void testGetDocumentToolPathTraversalProtection() {
        GetDocumentTool tool = new GetDocumentTool(config);

        ToolResult r1 = tool.execute(Map.of("documentPath", "../pom.xml"));
        assertFalse(r1.success());
        assertTrue(r1.errorMessage().contains("Path traversal or absolute path rejected"));

        ToolResult r2 = tool.execute(Map.of("documentPath", "C:\\Windows\\System32\\config"));
        assertFalse(r2.success());
        assertTrue(r2.errorMessage().contains("Path traversal or absolute path rejected"));

        ToolResult r3 = tool.execute(Map.of("documentPath", "/etc/passwd"));
        assertFalse(r3.success());
        assertTrue(r3.errorMessage().contains("Path traversal or absolute path rejected"));

        ToolResult r4 = tool.execute(Map.of("documentPath", "non_existent_file.md"));
        assertFalse(r4.success());
        assertTrue(r4.errorMessage().contains("Document not found"));
    }

    @Test
    @DisplayName("5. GetDocumentTool retrieves valid corpus document")
    void testGetDocumentToolValidRetrieval() {
        GetDocumentTool tool = new GetDocumentTool(config);

        ToolResult result = tool.execute(Map.of("documentPath", "maven/lifecycle.md"));
        assertTrue(result.success());
        assertTrue(result.output().contains("Document Title"));
    }

    @Test
    @DisplayName("6. ExplainMavenConceptTool execution delegates to RagService")
    void testExplainMavenConceptToolExecution() {
        ExplainMavenConceptTool tool = new ExplainMavenConceptTool(ragService, vectorStore);
        assertEquals("explainMavenConcept", tool.name());

        ToolResult result = tool.execute(Map.of("concept", "Maven build phases"));
        assertTrue(result.success());
        assertTrue(result.output().contains("Maven lifecycle consists of validate"));
    }
}
