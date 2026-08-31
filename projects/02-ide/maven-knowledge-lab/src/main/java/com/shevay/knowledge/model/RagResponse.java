package com.shevay.knowledge.model;

import java.util.List;
import java.util.Objects;

/**
 * Immutable domain model representing the complete RAG response object.
 */
public record RagResponse(
        String query,
        String generatedAnswer,
        List<RetrievedChunk> retrievedChunks,
        List<SourceReference> sources
) {
    public RagResponse {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(generatedAnswer, "generatedAnswer must not be null");
        retrievedChunks = retrievedChunks == null ? List.of() : List.copyOf(retrievedChunks);
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
