package com.shevay.knowledge.generation;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.embedding.EmbeddingProvider;
import com.shevay.knowledge.embedding.EmbeddingPurpose;
import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.model.RagResponse;
import com.shevay.knowledge.model.SourceReference;
import com.shevay.knowledge.model.VectorRecord;
import com.shevay.knowledge.retrieval.SimilaritySearchService;
import com.shevay.knowledge.vector.FileVectorStore;
import com.shevay.knowledge.vector.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RagServiceTest {

    @TempDir
    Path tempDir;

    private static final float[] UNIT_VECTOR = createUnitVector(768);

    private static float[] createUnitVector(int dim) {
        float[] v = new float[dim];
        v[0] = 1.0f;
        return v;
    }

    private final EmbeddingProvider constantEmbeddingProvider = new EmbeddingProvider() {
        @Override
        public Embedding embed(String text, EmbeddingPurpose purpose) {
            return new Embedding(UNIT_VECTOR, 768, "constant-model");
        }

        @Override
        public List<Embedding> embedBatch(List<String> texts, EmbeddingPurpose purpose) {
            return texts.stream().map(t -> embed(t, purpose)).toList();
        }

        @Override
        public int getDimensions() { return 768; }

        @Override
        public String getModelIdentifier() { return "constant-model"; }
    };

    private AppConfig config;
    private DummyLlmGenerationProvider dummyLlmProvider;
    private SimilaritySearchService searchService;
    private VectorStore vectorStore;

    @BeforeEach
    void setUp() throws IOException {
        String storePath = tempDir.resolve("vectors.dat").toString();
        config = new AppConfig("knowledge", tempDir.toString(), storePath, 3, 0.5, 800, 100);
        dummyLlmProvider = new DummyLlmGenerationProvider("The Maven build lifecycle manages project build phases.");
        searchService = new SimilaritySearchService(config);
        vectorStore = new FileVectorStore(config);
    }

    @Test
    @DisplayName("1. Successful RAG flow with matching chunks and LLM completion")
    void testSuccessfulRagFlow() throws IOException {
        DocumentChunk chunk1 = new DocumentChunk("c1", "doc1", "knowledge/maven.md", 0, "Maven build lifecycle consists of validate, compile, test, package.", 60, 10);
        Embedding emb1 = constantEmbeddingProvider.embed(chunk1.text(), EmbeddingPurpose.DOCUMENT);
        VectorRecord record1 = new VectorRecord("v1", chunk1, emb1.getValues(), 768);
        vectorStore.saveAll(List.of(record1));

        RagService ragService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), dummyLlmProvider);

        RagResponse response = ragService.query("What is the Maven lifecycle?", vectorStore);

        assertNotNull(response);
        assertEquals("What is the Maven lifecycle?", response.query());
        assertEquals("The Maven build lifecycle manages project build phases.", response.generatedAnswer());
        assertEquals(1, response.retrievedChunks().size());
        assertEquals(1, response.sources().size());
        assertEquals(1, dummyLlmProvider.getCallCount());

        SourceReference source = response.sources().get(0);
        assertEquals("doc1", source.documentId());
        assertEquals("knowledge/maven.md", source.sourcePath());
        assertEquals("Maven build lifecycle consists of validate, compile, test, package.", source.snippet());
    }

    @Test
    @DisplayName("2. No relevant chunks causes no LLM invocation (short-circuit)")
    void testNoRelevantChunksShortCircuit() {
        // Vector store is empty
        RagService ragService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), dummyLlmProvider);

        RagResponse response = ragService.query("Unknown query topic", vectorStore);

        assertNotNull(response);
        assertEquals(0, dummyLlmProvider.getCallCount(), "LLM provider must NOT be invoked when zero chunks are retrieved");
        assertTrue(response.retrievedChunks().isEmpty());
        assertTrue(response.sources().isEmpty());
        assertTrue(response.generatedAnswer().contains("No relevant knowledge context found"));
    }

    @Test
    @DisplayName("3 & 4. Prompt contains user question and retrieved context")
    void testPromptContainsQuestionAndContext() throws IOException {
        DocumentChunk chunk = new DocumentChunk("c1", "doc1", "knowledge/maven.md", 0, "Maven compiles source code into target classes.", 50, 8);
        Embedding emb = constantEmbeddingProvider.embed(chunk.text(), EmbeddingPurpose.DOCUMENT);
        vectorStore.saveAll(List.of(new VectorRecord("v1", chunk, emb.getValues(), 768)));

        RagService ragService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), dummyLlmProvider);

        ragService.query("How does Maven compile code?", vectorStore);

        String lastPrompt = dummyLlmProvider.getLastPrompt();
        assertNotNull(lastPrompt);
        assertTrue(lastPrompt.contains("USER QUESTION: How does Maven compile code?"));
        assertTrue(lastPrompt.contains("Maven compiles source code into target classes."));
        assertTrue(lastPrompt.contains("--- KNOWLEDGE CONTEXT BEGIN ---"));
    }

    @Test
    @DisplayName("5 & 6. Multiple retrieved chunks included deterministically with exact source mapping")
    void testMultipleChunksAndSourceMapping() throws IOException {
        DocumentChunk chunk1 = new DocumentChunk("c1", "doc1", "knowledge/a.md", 0, "Chunk A text content", 20, 4);
        DocumentChunk chunk2 = new DocumentChunk("c2", "doc2", "knowledge/b.md", 0, "Chunk B text content", 20, 4);

        Embedding emb1 = constantEmbeddingProvider.embed(chunk1.text(), EmbeddingPurpose.DOCUMENT);
        Embedding emb2 = constantEmbeddingProvider.embed(chunk2.text(), EmbeddingPurpose.DOCUMENT);

        vectorStore.saveAll(List.of(
                new VectorRecord("v1", chunk1, emb1.getValues(), 768),
                new VectorRecord("v2", chunk2, emb2.getValues(), 768)
        ));

        RagService ragService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), dummyLlmProvider);

        RagResponse response = ragService.query("Retrieve all chunks", vectorStore);

        assertEquals(2, response.retrievedChunks().size());
        assertEquals(2, response.sources().size());

        SourceReference s1 = response.sources().get(0);
        SourceReference s2 = response.sources().get(1);

        assertEquals(chunk1.documentId(), s1.documentId());
        assertEquals(chunk1.sourcePath(), s1.sourcePath());
        assertEquals(chunk2.documentId(), s2.documentId());
        assertEquals(chunk2.sourcePath(), s2.sourcePath());
    }

    @Test
    @DisplayName("7. Blank/invalid query is rejected before embedding/retrieval")
    void testBlankQueryRejection() {
        RagService ragService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), dummyLlmProvider);

        assertThrows(IllegalArgumentException.class, () -> ragService.query(null, vectorStore));
        assertThrows(IllegalArgumentException.class, () -> ragService.query("   ", vectorStore));
    }

    @Test
    @DisplayName("8 & 10. LLM failure converts into controlled GenerationException without credential leaks")
    void testLlmFailureHandling() throws IOException {
        DocumentChunk chunk = new DocumentChunk("c1", "doc1", "knowledge/a.md", 0, "Content text", 12, 2);
        Embedding emb = constantEmbeddingProvider.embed(chunk.text(), EmbeddingPurpose.DOCUMENT);
        vectorStore.saveAll(List.of(new VectorRecord("v1", chunk, emb.getValues(), 768)));

        LlmGenerationProvider failingLlmProvider = prompt -> {
            throw new GenerationException("Gemini API Error (500): Server error occurred");
        };

        RagService ragService = new RagService(searchService, constantEmbeddingProvider, new ContextAssembler(), new PromptBuilder(), failingLlmProvider);

        GenerationException ex = assertThrows(GenerationException.class, () -> ragService.query("Valid query", vectorStore));
        assertTrue(ex.getMessage().contains("500"));
        assertFalse(ex.getMessage().contains("GEMINI_API_KEY"));
    }
}
