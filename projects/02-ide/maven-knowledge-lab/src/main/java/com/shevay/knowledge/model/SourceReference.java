package com.shevay.knowledge.model;

import java.util.Objects;

/**
 * Immutable domain model representing source attribution for a generated RAG response.
 */
public record SourceReference(
        String documentId,
        String sourcePath,
        String snippet,
        double relevanceScore
) {
    public SourceReference {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(snippet, "snippet must not be null");
    }
}
