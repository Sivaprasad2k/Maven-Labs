package com.shevay.knowledge.retrieval;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.model.RetrievedChunk;
import com.shevay.knowledge.model.VectorRecord;
import com.shevay.knowledge.vector.FileVectorStore;
import com.shevay.knowledge.vector.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimilaritySearchServiceTest {

    @TempDir
    Path tempDir;

    private VectorStore store;

    @BeforeEach
    void setUp() {
        Path storePath = tempDir.resolve("vectors.dat");
        store = new FileVectorStore(storePath, 3);
    }

    @Test
    @DisplayName("Should rank results by similarity score descending and apply minSimilarity threshold")
    void testRankingAndThresholdFiltering() {
        // Query vector: [1, 0, 0]
        // Stored vectors:
        //  v1 (chunk-A): [1.0, 0.0, 0.0] -> score 1.0
        //  v2 (chunk-B): [0.8, 0.6, 0.0] -> score 0.8
        //  v3 (chunk-C): [0.6, 0.8, 0.0] -> score 0.6 (below threshold 0.7)
        //  v4 (chunk-D): [0.0, 1.0, 0.0] -> score 0.0 (below threshold 0.7)

        store.save(new VectorRecord("v1", new DocumentChunk("chunk-A", "d1", "p1", 0, "A", 1, 1), new float[]{1.0f, 0.0f, 0.0f}, 3));
        store.save(new VectorRecord("v2", new DocumentChunk("chunk-B", "d1", "p1", 1, "B", 1, 1), new float[]{0.8f, 0.6f, 0.0f}, 3));
        store.save(new VectorRecord("v3", new DocumentChunk("chunk-C", "d1", "p1", 2, "C", 1, 1), new float[]{0.6f, 0.8f, 0.0f}, 3));
        store.save(new VectorRecord("v4", new DocumentChunk("chunk-D", "d1", "p1", 3, "D", 1, 1), new float[]{0.0f, 1.0f, 0.0f}, 3));

        SimilaritySearchService searchService = new SimilaritySearchService(3, 0.70, 3);
        Embedding queryEmb = new Embedding(new float[]{1.0f, 0.0f, 0.0f}, 3, "test-model");

        List<RetrievedChunk> results = searchService.search(queryEmb, store);

        assertEquals(2, results.size(), "Only chunk-A and chunk-B qualify above 0.70 threshold");
        assertEquals("chunk-A", results.get(0).chunk().id());
        assertEquals(1.0, results.get(0).similarityScore(), 1e-6);

        assertEquals("chunk-B", results.get(1).chunk().id());
        assertEquals(0.8, results.get(1).similarityScore(), 1e-6);
    }

    @Test
    @DisplayName("Should truncate results to Top-K when more candidates qualify")
    void testTopKLimitTruncation() {
        store.save(new VectorRecord("v1", new DocumentChunk("chunk-1", "d1", "p1", 0, "Text 1", 6, 1), new float[]{1.0f, 0.0f, 0.0f}, 3));
        store.save(new VectorRecord("v2", new DocumentChunk("chunk-2", "d1", "p1", 1, "Text 2", 6, 1), new float[]{0.9f, 0.1f, 0.0f}, 3));
        store.save(new VectorRecord("v3", new DocumentChunk("chunk-3", "d1", "p1", 2, "Text 3", 6, 1), new float[]{0.8f, 0.2f, 0.0f}, 3));

        // Request topK = 2
        SimilaritySearchService searchService = new SimilaritySearchService(2, 0.50, 3);
        Embedding queryEmb = new Embedding(new float[]{1.0f, 0.0f, 0.0f}, 3, "test-model");

        List<RetrievedChunk> results = searchService.search(queryEmb, store);
        assertEquals(2, results.size());
        assertEquals("chunk-1", results.get(0).chunk().id());
        assertEquals("chunk-2", results.get(1).chunk().id());
    }

    @Test
    @DisplayName("Should perform deterministic tie-breaking by chunkId ascending when scores are equal")
    void testDeterministicTieBreaking() {
        // Both chunk-Z and chunk-A have identical similarity score 1.0 against [1,0,0]
        store.save(new VectorRecord("v1", new DocumentChunk("chunk-Z", "d1", "p1", 0, "Z text", 6, 1), new float[]{1.0f, 0.0f, 0.0f}, 3));
        store.save(new VectorRecord("v2", new DocumentChunk("chunk-A", "d1", "p1", 1, "A text", 6, 1), new float[]{1.0f, 0.0f, 0.0f}, 3));

        SimilaritySearchService searchService = new SimilaritySearchService(5, 0.50, 3);
        Embedding queryEmb = new Embedding(new float[]{1.0f, 0.0f, 0.0f}, 3, "test-model");

        List<RetrievedChunk> results = searchService.search(queryEmb, store);
        assertEquals(2, results.size());
        assertEquals("chunk-A", results.get(0).chunk().id(), "chunk-A should come before chunk-Z alphabetically");
        assertEquals("chunk-Z", results.get(1).chunk().id());
    }

    @Test
    @DisplayName("Should return empty list when store is empty or no results qualify")
    void testEmptyStoreAndNoQualifyingResults() {
        SimilaritySearchService searchService = new SimilaritySearchService(3, 0.70, 3);
        Embedding queryEmb = new Embedding(new float[]{1.0f, 0.0f, 0.0f}, 3, "test-model");

        assertTrue(searchService.search(queryEmb, store).isEmpty());

        store.save(new VectorRecord("v1", new DocumentChunk("chunk-1", "d1", "p1", 0, "T", 1, 1), new float[]{0.0f, 1.0f, 0.0f}, 3));
        assertTrue(searchService.search(queryEmb, store).isEmpty());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when query embedding dimension does not match service expectations")
    void testQueryDimensionMismatch() {
        SimilaritySearchService searchService = new SimilaritySearchService(3, 0.70, 768);
        Embedding badQueryEmb = new Embedding(new float[]{1.0f, 0.0f, 0.0f}, 3, "test-model");

        assertThrows(IllegalArgumentException.class, () -> searchService.search(badQueryEmb, store));
    }
}
