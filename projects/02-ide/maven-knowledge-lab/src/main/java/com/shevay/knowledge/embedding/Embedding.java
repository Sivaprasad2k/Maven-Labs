package com.shevay.knowledge.embedding;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable value object encapsulating an L2-normalized vector embedding and its metadata.
 *
 * <p>Invariant Guarantees:
 * <ul>
 *   <li>Vector values array is non-null, non-empty, and protected against external mutation.</li>
 *   <li>Dimensions match exact array length.</li>
 *   <li>All vector values are finite floats (no NaN or Infinity).</li>
 * </ul>
 * </p>
 */
public final class Embedding {

    private final float[] values;
    private final int dimensions;
    private final String modelIdentifier;

    public Embedding(float[] values, int dimensions, String modelIdentifier) {
        Objects.requireNonNull(values, "Vector values array must not be null");
        if (values.length == 0) {
            throw new IllegalArgumentException("Vector values array must not be empty");
        }
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive (> 0), got: " + dimensions);
        }
        if (values.length != dimensions) {
            throw new IllegalArgumentException("Vector length (" + values.length + ") does not match declared dimensions (" + dimensions + ")");
        }
        Objects.requireNonNull(modelIdentifier, "Model identifier must not be null");
        if (modelIdentifier.isBlank()) {
            throw new IllegalArgumentException("Model identifier must not be blank");
        }

        for (int i = 0; i < values.length; i++) {
            float v = values[i];
            if (Float.isNaN(v) || Float.isInfinite(v)) {
                throw new IllegalArgumentException("Embedding vector element at index " + i + " is non-finite: " + v);
            }
        }

        this.values = Arrays.copyOf(values, values.length);
        this.dimensions = dimensions;
        this.modelIdentifier = modelIdentifier;
    }

    /**
     * Returns a defensive copy of the vector float array.
     *
     * @return cloned float array
     */
    public float[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    public int getDimensions() {
        return dimensions;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Embedding embedding = (Embedding) o;
        return dimensions == embedding.dimensions &&
                Arrays.equals(values, embedding.values) &&
                Objects.equals(modelIdentifier, embedding.modelIdentifier);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(dimensions, modelIdentifier);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public String toString() {
        return "Embedding{" +
                "dimensions=" + dimensions +
                ", modelIdentifier='" + modelIdentifier + '\'' +
                ", valuesHead=" + (values.length > 3 ? "[" + values[0] + ", " + values[1] + ", " + values[2] + "...]" : Arrays.toString(values)) +
                '}';
    }
}
