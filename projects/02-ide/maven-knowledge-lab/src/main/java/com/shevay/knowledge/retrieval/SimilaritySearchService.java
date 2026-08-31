package com.shevay.knowledge.retrieval;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.model.RetrievedChunk;
import com.shevay.knowledge.model.VectorRecord;
import com.shevay.knowledge.vector.VectorStore;
import com.shevay.knowledge.vector.VectorStoreException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Service performing exact linear similarity search over stored VectorRecords.
 * Applies minimum similarity filtering, Top-K truncation, and deterministic secondary tie-breaking.
 */
public class SimilaritySearchService {

    private final int topK;
    private final double minSimilarity;
    private final int expectedDimensions;

    public SimilaritySearchService(AppConfig config) {
        Objects.requireNonNull(config, "AppConfig must not be null");
        this.topK = config.getTopK();
        this.minSimilarity = config.getMinSimilarity();
        this.expectedDimensions = config.getEmbeddingDimensions();
    }

    public SimilaritySearchService(int topK, double minSimilarity, int expectedDimensions) {
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be positive (> 0), got: " + topK);
        }
        if (minSimilarity < 0.0 || minSimilarity > 1.0) {
            throw new IllegalArgumentException("minSimilarity must be between 0.0 and 1.0, got: " + minSimilarity);
        }
        if (expectedDimensions <= 0) {
            throw new IllegalArgumentException("expectedDimensions must be positive (> 0), got: " + expectedDimensions);
        }
        this.topK = topK;
        this.minSimilarity = minSimilarity;
        this.expectedDimensions = expectedDimensions;
    }

    /**
     * Searches for stored vector records matching the query embedding using default config parameters.
     *
     * @param queryEmbedding target query embedding vector
     * @param vectorStore    source VectorStore
     * @return List of ranked RetrievedChunks qualifying above minSimilarity threshold up to Top-K
     */
    public List<RetrievedChunk> search(Embedding queryEmbedding, VectorStore vectorStore) {
        return search(queryEmbedding, vectorStore, this.topK, this.minSimilarity);
    }

    /**
     * Searches for stored vector records with explicit topK and minSimilarity overrides.
     *
     * @param queryEmbedding        target query embedding vector
     * @param vectorStore           source VectorStore
     * @param topKOverride          maximum results count
     * @param minSimilarityOverride minimum similarity score cutoff threshold
     * @return List of ranked RetrievedChunks
     */
    public List<RetrievedChunk> search(Embedding queryEmbedding, VectorStore vectorStore, int topKOverride, double minSimilarityOverride) {
        Objects.requireNonNull(queryEmbedding, "queryEmbedding must not be null");
        Objects.requireNonNull(vectorStore, "vectorStore must not be null");

        if (topKOverride <= 0) {
            throw new IllegalArgumentException("topKOverride must be positive (> 0), got: " + topKOverride);
        }
        if (minSimilarityOverride < 0.0 || minSimilarityOverride > 1.0) {
            throw new IllegalArgumentException("minSimilarityOverride must be between 0.0 and 1.0, got: " + minSimilarityOverride);
        }

        if (queryEmbedding.getDimensions() != expectedDimensions) {
            throw new IllegalArgumentException(String.format(
                    "Query embedding dimension mismatch: query has %d dimensions but service expects %d",
                    queryEmbedding.getDimensions(), expectedDimensions));
        }

        List<VectorRecord> storedRecords = vectorStore.findAll();
        if (storedRecords.isEmpty()) {
            return List.of();
        }

        float[] queryVector = queryEmbedding.getValues();
        List<RetrievedChunk> candidates = new ArrayList<>();

        for (VectorRecord record : storedRecords) {
            if (record.dimensions() != expectedDimensions) {
                throw new VectorStoreException(String.format(
                        "Stored record dimension mismatch: record '%s' has %d dimensions but service expects %d",
                        record.id(), record.dimensions(), expectedDimensions));
            }

            double score = CosineSimilarity.compute(queryVector, record.vector());

            if (score >= minSimilarityOverride) {
                candidates.add(new RetrievedChunk(record.toDocumentChunk(), score));
            }
        }

        if (candidates.isEmpty()) {
            return List.of();
        }

        // Sort primary score descending, secondary chunkId ascending for deterministic tie-breaking
        candidates.sort(Comparator
                .<RetrievedChunk>comparingDouble(RetrievedChunk::similarityScore).reversed()
                .thenComparing(rc -> rc.chunk().id()));

        int limit = Math.min(topKOverride, candidates.size());
        return new ArrayList<>(candidates.subList(0, limit));
    }

    public int getTopK() {
        return topK;
    }

    public double getMinSimilarity() {
        return minSimilarity;
    }

    public int getExpectedDimensions() {
        return expectedDimensions;
    }
}
