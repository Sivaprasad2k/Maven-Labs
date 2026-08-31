package com.shevay.knowledge.embedding;

import java.util.Objects;

/**
 * Utility component performing L2 normalization on float vectors.
 *
 * <p>Formula: normalized[i] = vector[i] / ||vector||2
 * where ||vector||2 = sqrt(sum(vector[i]^2))</p>
 */
public final class VectorNormalizer {

    private static final float EPSILON = 1e-12f;

    private VectorNormalizer() {
        // Utility class
    }

    /**
     * Performs L2 normalization on a float vector and returns a new normalized float array.
     *
     * @param vector raw float array
     * @return new L2-normalized float array
     * @throws IllegalArgumentException if vector is null, empty, contains non-finite values, or has zero magnitude
     */
    public static float[] normalizeL2(float[] vector) {
        Objects.requireNonNull(vector, "Vector must not be null");
        if (vector.length == 0) {
            throw new IllegalArgumentException("Vector must not be empty");
        }

        double sumSquares = 0.0;
        for (int i = 0; i < vector.length; i++) {
            float v = vector[i];
            if (Float.isNaN(v) || Float.isInfinite(v)) {
                throw new IllegalArgumentException("Vector element at index " + i + " is non-finite: " + v);
            }
            sumSquares += (double) v * (double) v;
        }

        double magnitude = Math.sqrt(sumSquares);
        if (magnitude < EPSILON) {
            throw new IllegalArgumentException("Cannot perform L2 normalization on zero or near-zero vector (magnitude=" + magnitude + ")");
        }

        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = (float) (vector[i] / magnitude);
        }

        return normalized;
    }
}
