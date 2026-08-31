package com.shevay.knowledge.embedding;

/**
 * Domain-neutral abstraction representing the semantic purpose of an embedding request.
 */
public enum EmbeddingPurpose {
    /**
     * Embedding for a document or text chunk to be indexed.
     */
    DOCUMENT,

    /**
     * Embedding for a search query to retrieve relevant documents.
     */
    QUERY
}
