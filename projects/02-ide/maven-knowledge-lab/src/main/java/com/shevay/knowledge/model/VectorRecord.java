package com.shevay.knowledge.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable domain model representing a vector embedding associated with a chunk and its metadata.
 */
public record VectorRecord(
        String id,
        String chunkId,
        String documentId,
        String sourcePath,
        int chunkIndex,
        String text,
        int tokenCount,
        float[] vector,
        int dimensions
) {
    public VectorRecord {
        Objects.requireNonNull(id, "VectorRecord id must not be null");
        Objects.requireNonNull(chunkId, "chunkId must not be null");
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(text, "text must not be null");
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex cannot be negative: " + chunkIndex);
        }
        if (tokenCount < 0) {
            throw new IllegalArgumentException("tokenCount cannot be negative: " + tokenCount);
        }
        Objects.requireNonNull(vector, "vector array must not be null");
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be positive: " + dimensions);
        }
        if (vector.length != dimensions) {
            throw new IllegalArgumentException("vector length (" + vector.length + ") does not match dimensions (" + dimensions + ")");
        }
        vector = vector.clone();
    }

    /**
     * Backward-compatible 4-arg constructor for Phase 3 compatibility.
     */
    public VectorRecord(String id, String chunkId, float[] vector, int dimensions) {
        this(id, chunkId, "", "", 0, "", 0, vector, dimensions);
    }

    /**
     * Convenience constructor creating VectorRecord from a DocumentChunk and float vector.
     */
    public VectorRecord(String id, DocumentChunk chunk, float[] vector, int dimensions) {
        this(id,
                Objects.requireNonNull(chunk, "chunk must not be null").id(),
                chunk.documentId(),
                chunk.sourcePath(),
                chunk.chunkIndex(),
                chunk.text(),
                chunk.tokenCount(),
                vector,
                dimensions);
    }

    /**
     * Reconstructs a DocumentChunk domain object from stored VectorRecord metadata.
     */
    public DocumentChunk toDocumentChunk() {
        return new DocumentChunk(chunkId, documentId, sourcePath, chunkIndex, text, text.length(), tokenCount);
    }

    @Override
    public float[] vector() {
        return vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorRecord that = (VectorRecord) o;
        return dimensions == that.dimensions &&
                chunkIndex == that.chunkIndex &&
                tokenCount == that.tokenCount &&
                Objects.equals(id, that.id) &&
                Objects.equals(chunkId, that.chunkId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(sourcePath, that.sourcePath) &&
                Objects.equals(text, that.text) &&
                Arrays.equals(vector, that.vector);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, chunkId, documentId, sourcePath, chunkIndex, text, tokenCount, dimensions);
        result = 31 * result + Arrays.hashCode(vector);
        return result;
    }
}
