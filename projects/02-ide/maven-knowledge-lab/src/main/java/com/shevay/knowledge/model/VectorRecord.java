package com.shevay.knowledge.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable domain model representing a vector embedding associated with a chunk.
 */
public record VectorRecord(
        String id,
        String chunkId,
        float[] vector,
        int dimensions
) {
    public VectorRecord {
        Objects.requireNonNull(id, "VectorRecord id must not be null");
        Objects.requireNonNull(chunkId, "chunkId must not be null");
        Objects.requireNonNull(vector, "vector array must not be null");
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be positive: " + dimensions);
        }
        if (vector.length != dimensions) {
            throw new IllegalArgumentException("vector length (" + vector.length + ") does not match dimensions (" + dimensions + ")");
        }
        vector = vector.clone();
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
                Objects.equals(id, that.id) &&
                Objects.equals(chunkId, that.chunkId) &&
                Arrays.equals(vector, that.vector);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, chunkId, dimensions);
        result = 31 * result + Arrays.hashCode(vector);
        return result;
    }
}
