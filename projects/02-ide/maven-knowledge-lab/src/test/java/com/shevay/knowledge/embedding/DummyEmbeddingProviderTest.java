package com.shevay.knowledge.embedding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DummyEmbeddingProviderTest {

    @Test
    @DisplayName("Should produce deterministic 768-dimensional L2-normalized vectors for identical input text")
    void testDeterministicOutput() {
        DummyEmbeddingProvider provider = new DummyEmbeddingProvider();
        assertEquals(768, provider.getDimensions());
        assertEquals("dummy-embedding-model", provider.getModelIdentifier());

        Embedding emb1 = provider.embed("Maven lifecycle phases", EmbeddingPurpose.DOCUMENT);
        Embedding emb2 = provider.embed("Maven lifecycle phases", EmbeddingPurpose.DOCUMENT);

        assertEquals(768, emb1.getDimensions());
        assertEquals(768, emb2.getDimensions());
        assertArrayEquals(emb1.getValues(), emb2.getValues());

        // Verify magnitude is L2 normalized (approx 1.0)
        float[] v = emb1.getValues();
        double sumSq = 0.0;
        for (float f : v) {
            sumSq += f * f;
        }
        assertEquals(1.0, Math.sqrt(sumSq), 1e-4);
    }

    @Test
    @DisplayName("Should produce different vectors for different input texts")
    void testDifferentInputsProduceDifferentVectors() {
        DummyEmbeddingProvider provider = new DummyEmbeddingProvider();

        Embedding embA = provider.embed("Text A", EmbeddingPurpose.QUERY);
        Embedding embB = provider.embed("Text B", EmbeddingPurpose.QUERY);

        assertFalse(java.util.Arrays.equals(embA.getValues(), embB.getValues()));
    }

    @Test
    @DisplayName("Should accept both DOCUMENT and QUERY purposes")
    void testPurposeSupport() {
        DummyEmbeddingProvider provider = new DummyEmbeddingProvider();

        Embedding docEmb = provider.embed("Content", EmbeddingPurpose.DOCUMENT);
        Embedding queryEmb = provider.embed("Content", EmbeddingPurpose.QUERY);

        assertNotNull(docEmb);
        assertNotNull(queryEmb);
    }

    @Test
    @DisplayName("Should preserve input order in embedBatch")
    void testBatchOrderPreservation() {
        DummyEmbeddingProvider provider = new DummyEmbeddingProvider();
        List<String> inputs = List.of("First sentence.", "Second sentence.", "Third sentence.");

        List<Embedding> embeddings = provider.embedBatch(inputs, EmbeddingPurpose.DOCUMENT);

        assertEquals(3, embeddings.size());
        for (int i = 0; i < inputs.size(); i++) {
            Embedding expectedSingle = provider.embed(inputs.get(i), EmbeddingPurpose.DOCUMENT);
            assertArrayEquals(expectedSingle.getValues(), embeddings.get(i).getValues());
        }
    }
}
