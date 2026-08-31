package com.shevay.knowledge.model;

import java.util.Objects;

/**
 * Immutable domain model representing a chunk retrieved during similarity search,
 * accompanied by its similarity score.
 */
public record RetrievedChunk(
        DocumentChunk chunk,
        double similarityScore
) {
    public RetrievedChunk {
        Objects.requireNonNull(chunk, "chunk must not be null");
        if (similarityScore < -1.0 || similarityScore > 1.0) {
            throw new IllegalArgumentException("similarityScore must be between -1.0 and 1.0, got: " + similarityScore);
        }
    }
}
