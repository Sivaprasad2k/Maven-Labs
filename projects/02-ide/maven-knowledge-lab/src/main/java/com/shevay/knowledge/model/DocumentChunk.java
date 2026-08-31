package com.shevay.knowledge.model;

import java.util.Objects;

/**
 * Immutable domain model representing a chunk of a document.
 */
public record DocumentChunk(
        String id,
        String documentId,
        String sourcePath,
        int chunkIndex,
        String text,
        int contentLength,
        int tokenCount
) {
    public DocumentChunk {
        Objects.requireNonNull(id, "Chunk id must not be null");
        Objects.requireNonNull(documentId, "DocumentId must not be null");
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(text, "Chunk text must not be null");
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex cannot be negative: " + chunkIndex);
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength cannot be negative: " + contentLength);
        }
        if (tokenCount < 0) {
            throw new IllegalArgumentException("tokenCount cannot be negative: " + tokenCount);
        }
    }

    /**
     * Backward-compatible 5-arg constructor for Stage 1 compatibility.
     */
    public DocumentChunk(String id, String documentId, int chunkIndex, String text, int tokenCount) {
        this(id, documentId, "", chunkIndex, text, text.length(), tokenCount);
    }
}
