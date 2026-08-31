package com.shevay.knowledge.embedding;

import com.shevay.knowledge.util.HashUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Deterministic offline EmbeddingProvider implementation used exclusively for unit testing and offline contract validation.
 *
 * <p>Does NOT require network access or API keys. Generates L2-normalized 768-dimensional float vectors derived deterministically from text content hashes.</p>
 */
public class DummyEmbeddingProvider implements EmbeddingProvider {

    private static final int DEFAULT_DIMENSIONS = 768;
    private static final String DUMMY_MODEL_ID = "dummy-embedding-model";

    private final int dimensions;

    public DummyEmbeddingProvider() {
        this(DEFAULT_DIMENSIONS);
    }

    public DummyEmbeddingProvider(int dimensions) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive (> 0), got: " + dimensions);
        }
        this.dimensions = dimensions;
    }

    @Override
    public Embedding embed(String text, EmbeddingPurpose purpose) {
        Objects.requireNonNull(purpose, "EmbeddingPurpose must not be null");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Embedding input text must not be null or blank");
        }

        float[] rawVector = generateDeterministicVector(text + ":" + purpose.name(), dimensions);
        float[] normalizedVector = VectorNormalizer.normalizeL2(rawVector);

        return new Embedding(normalizedVector, dimensions, DUMMY_MODEL_ID);
    }

    @Override
    public List<Embedding> embedBatch(List<String> texts, EmbeddingPurpose purpose) {
        Objects.requireNonNull(texts, "Texts list must not be null");
        Objects.requireNonNull(purpose, "EmbeddingPurpose must not be null");

        List<Embedding> result = new ArrayList<>(texts.size());
        for (String text : texts) {
            result.add(embed(text, purpose));
        }
        return result;
    }

    @Override
    public int getDimensions() {
        return dimensions;
    }

    @Override
    public String getModelIdentifier() {
        return DUMMY_MODEL_ID;
    }

    private float[] generateDeterministicVector(String inputKey, int targetDimensions) {
        long seed = calculateSeed(inputKey);
        Random random = new Random(seed);
        float[] vector = new float[targetDimensions];
        for (int i = 0; i < targetDimensions; i++) {
            // Generate non-zero float values centered around zero
            vector[i] = (random.nextFloat() * 2.0f) - 1.0f;
        }
        return vector;
    }

    private long calculateSeed(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xff);
            }
            return seed;
        } catch (NoSuchAlgorithmException e) {
            return (long) input.hashCode();
        }
    }
}
