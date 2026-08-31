package com.shevay.knowledge.embedding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingNormalizationTest {

    @Test
    @DisplayName("Should correctly perform L2 normalization resulting in unit length magnitude")
    void testStandardL2Normalization() {
        float[] raw = new float[]{3.0f, 4.0f}; // magnitude = sqrt(9 + 16) = 5.0
        float[] normalized = VectorNormalizer.normalizeL2(raw);

        assertEquals(0.6f, normalized[0], 1e-6f);
        assertEquals(0.8f, normalized[1], 1e-6f);

        double magnitude = Math.sqrt(normalized[0] * normalized[0] + normalized[1] * normalized[1]);
        assertEquals(1.0, magnitude, 1e-6);
    }

    @Test
    @DisplayName("Already normalized unit vector should remain unchanged")
    void testAlreadyNormalizedVector() {
        float[] unit = new float[]{0.6f, 0.8f};
        float[] normalized = VectorNormalizer.normalizeL2(unit);

        assertEquals(0.6f, normalized[0], 1e-5f);
        assertEquals(0.8f, normalized[1], 1e-5f);
    }

    @Test
    @DisplayName("Should handle negative values and large numbers stably")
    void testNegativeAndLargeValues() {
        float[] raw = new float[]{-3000.0f, 4000.0f};
        float[] normalized = VectorNormalizer.normalizeL2(raw);

        assertEquals(-0.6f, normalized[0], 1e-5f);
        assertEquals(0.8f, normalized[1], 1e-5f);

        double magnitude = Math.sqrt(normalized[0] * normalized[0] + normalized[1] * normalized[1]);
        assertEquals(1.0, magnitude, 1e-6);
    }

    @Test
    @DisplayName("Zero or near-zero vector should raise IllegalArgumentException explicitly without NaN or Infinity")
    void testZeroVectorHandling() {
        float[] zero = new float[]{0.0f, 0.0f, 0.0f};
        assertThrows(IllegalArgumentException.class, () -> VectorNormalizer.normalizeL2(zero));

        float[] nearZero = new float[]{1e-15f, 1e-15f};
        assertThrows(IllegalArgumentException.class, () -> VectorNormalizer.normalizeL2(nearZero));
    }

    @Test
    @DisplayName("Normalization should be completely deterministic across multiple invocations")
    void testDeterminism() {
        float[] raw = new float[]{1.23f, -4.56f, 7.89f, 0.12f};
        float[] pass1 = VectorNormalizer.normalizeL2(raw);
        float[] pass2 = VectorNormalizer.normalizeL2(raw);

        assertArrayEquals(pass1, pass2);
    }
}
