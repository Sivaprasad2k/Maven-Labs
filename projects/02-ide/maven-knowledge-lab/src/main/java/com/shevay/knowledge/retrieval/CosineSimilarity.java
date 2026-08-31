package com.shevay.knowledge.retrieval;

import java.util.Objects;

/**
 * Pure mathematical utility for calculating cosine similarity between float vectors.
 *
 * <p>Formula:
 * <pre>
 * cos(A, B) = (A · B) / (||A||2 * ||B||2)
 * </pre>
 * </p>
 */
public final class CosineSimilarity {

    private static final double EPSILON = 1e-12;

    private CosineSimilarity() {
        // Utility class
    }

    /**
     * Calculates the cosine similarity between two float vectors.
     *
     * @param vectorA first vector array
     * @param vectorB second vector array
     * @return cosine similarity score between -1.0 and 1.0 (inclusive)
     * @throws IllegalArgumentException if either vector is null, empty, mismatched in dimension,
     *                                  contains non-finite values, or has zero magnitude
     */
    public static double compute(float[] vectorA, float[] vectorB) {
        Objects.requireNonNull(vectorA, "vectorA must not be null");
        Objects.requireNonNull(vectorB, "vectorB must not be null");

        if (vectorA.length == 0 || vectorB.length == 0) {
            throw new IllegalArgumentException("Vector must not be empty");
        }
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException(String.format(
                    "Vector dimension mismatch: vectorA has dimension %d but vectorB has dimension %d",
                    vectorA.length, vectorB.length));
        }

        double dotProduct = 0.0;
        double sumSquaresA = 0.0;
        double sumSquaresB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            float a = vectorA[i];
            float b = vectorB[i];

            if (Float.isNaN(a) || Float.isInfinite(a)) {
                throw new IllegalArgumentException("vectorA contains non-finite float at index " + i + ": " + a);
            }
            if (Float.isNaN(b) || Float.isInfinite(b)) {
                throw new IllegalArgumentException("vectorB contains non-finite float at index " + i + ": " + b);
            }

            double da = (double) a;
            double db = (double) b;

            dotProduct += da * db;
            sumSquaresA += da * da;
            sumSquaresB += db * db;
        }

        double magA = Math.sqrt(sumSquaresA);
        double magB = Math.sqrt(sumSquaresB);

        if (magA < EPSILON) {
            throw new IllegalArgumentException("Cannot compute cosine similarity: vectorA has zero or near-zero magnitude (" + magA + ")");
        }
        if (magB < EPSILON) {
            throw new IllegalArgumentException("Cannot compute cosine similarity: vectorB has zero or near-zero magnitude (" + magB + ")");
        }

        double similarity = dotProduct / (magA * magB);

        // Clamp to [-1.0, 1.0] to handle tiny floating-point rounding inaccuracies
        if (similarity > 1.0) {
            return 1.0;
        }
        if (similarity < -1.0) {
            return -1.0;
        }
        return similarity;
    }
}
