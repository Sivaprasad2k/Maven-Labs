package com.shevay.knowledge.embedding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingTest {

    @Test
    @DisplayName("Should create immutable Embedding value object and enforce defensive copies")
    void testImmutabilityAndDefensiveCopy() {
        float[] original = new float[]{0.6f, 0.8f};
        Embedding embedding = new Embedding(original, 2, "gemini-embedding-001");

        assertEquals(2, embedding.getDimensions());
        assertEquals("gemini-embedding-001", embedding.getModelIdentifier());

        // Mutating original input array must not affect embedding instance
        original[0] = 999.0f;
        assertEquals(0.6f, embedding.getValues()[0], 1e-6);

        // Mutating array returned by getter must not affect internal embedding state
        float[] getterArray = embedding.getValues();
        getterArray[0] = 888.0f;
        assertEquals(0.6f, embedding.getValues()[0], 1e-6);
    }

    @Test
    @DisplayName("Should reject null, empty, or dimension mismatch vectors")
    void testInvalidVectorArguments() {
        assertThrows(NullPointerException.class, () -> new Embedding(null, 3, "model"));
        assertThrows(IllegalArgumentException.class, () -> new Embedding(new float[0], 0, "model"));
        assertThrows(IllegalArgumentException.class, () -> new Embedding(new float[]{0.1f, 0.2f}, 3, "model"));
        assertThrows(IllegalArgumentException.class, () -> new Embedding(new float[]{0.1f, 0.2f}, 1, "model"));
    }

    @Test
    @DisplayName("Should reject non-finite vector float values (NaN, Infinity)")
    void testNonFiniteValueRejection() {
        assertThrows(IllegalArgumentException.class, () ->
                new Embedding(new float[]{0.5f, Float.NaN}, 2, "model"));

        assertThrows(IllegalArgumentException.class, () ->
                new Embedding(new float[]{Float.POSITIVE_INFINITY, 0.5f}, 2, "model"));

        assertThrows(IllegalArgumentException.class, () ->
                new Embedding(new float[]{-Float.MAX_VALUE * 2, 0.5f}, 2, "model"));
    }

    @Test
    @DisplayName("Should reject null or blank model identifiers")
    void testInvalidModelIdentifier() {
        assertThrows(NullPointerException.class, () -> new Embedding(new float[]{0.6f, 0.8f}, 2, null));
        assertThrows(IllegalArgumentException.class, () -> new Embedding(new float[]{0.6f, 0.8f}, 2, "   "));
    }
}
