package com.shevay.knowledge.retrieval;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CosineSimilarityTest {

    @Test
    @DisplayName("Should compute 1.0 for identical parallel vectors")
    void testIdenticalVectors() {
        float[] a = new float[]{1.0f, 0.0f, 0.0f};
        float[] b = new float[]{1.0f, 0.0f, 0.0f};
        assertEquals(1.0, CosineSimilarity.compute(a, b), 1e-6);
    }

    @Test
    @DisplayName("Should compute 0.0 for orthogonal vectors")
    void testOrthogonalVectors() {
        float[] a = new float[]{1.0f, 0.0f, 0.0f};
        float[] b = new float[]{0.0f, 1.0f, 0.0f};
        assertEquals(0.0, CosineSimilarity.compute(a, b), 1e-6);
    }

    @Test
    @DisplayName("Should compute -1.0 for opposite vectors")
    void testOppositeVectors() {
        float[] a = new float[]{1.0f, 0.0f, 0.0f};
        float[] b = new float[]{-1.0f, 0.0f, 0.0f};
        assertEquals(-1.0, CosineSimilarity.compute(a, b), 1e-6);
    }

    @Test
    @DisplayName("Should be symmetric compute(A, B) == compute(B, A)")
    void testSymmetry() {
        float[] a = new float[]{0.6f, 0.8f, 0.0f};
        float[] b = new float[]{0.0f, 0.6f, 0.8f};
        double simAB = CosineSimilarity.compute(a, b);
        double simBA = CosineSimilarity.compute(b, a);
        assertEquals(simAB, simBA, 1e-12);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on null or empty vectors")
    void testNullOrEmptyVectors() {
        assertThrows(NullPointerException.class, () -> CosineSimilarity.compute(null, new float[]{1f}));
        assertThrows(NullPointerException.class, () -> CosineSimilarity.compute(new float[]{1f}, null));
        assertThrows(IllegalArgumentException.class, () -> CosineSimilarity.compute(new float[0], new float[0]));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on dimension mismatch")
    void testDimensionMismatch() {
        float[] a = new float[]{1.0f, 0.0f};
        float[] b = new float[]{1.0f, 0.0f, 0.0f};
        assertThrows(IllegalArgumentException.class, () -> CosineSimilarity.compute(a, b));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on zero magnitude vector")
    void testZeroMagnitude() {
        float[] a = new float[]{0.0f, 0.0f, 0.0f};
        float[] b = new float[]{1.0f, 0.0f, 0.0f};
        assertThrows(IllegalArgumentException.class, () -> CosineSimilarity.compute(a, b));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on non-finite floats")
    void testNonFiniteFloats() {
        float[] a = new float[]{Float.NaN, 0.0f, 0.0f};
        float[] b = new float[]{1.0f, 0.0f, 0.0f};
        assertThrows(IllegalArgumentException.class, () -> CosineSimilarity.compute(a, b));

        float[] c = new float[]{Float.POSITIVE_INFINITY, 0.0f, 0.0f};
        assertThrows(IllegalArgumentException.class, () -> CosineSimilarity.compute(b, c));
    }
}
