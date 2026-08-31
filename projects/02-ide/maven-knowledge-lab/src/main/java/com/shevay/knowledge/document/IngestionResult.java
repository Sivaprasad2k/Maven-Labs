package com.shevay.knowledge.document;

import com.shevay.knowledge.model.Document;
import com.shevay.knowledge.model.DocumentChunk;

import java.util.List;
import java.util.Objects;

/**
 * Immutable result object capturing document ingestion metrics and generated chunks.
 */
public record IngestionResult(
        int documentsDiscovered,
        int documentsProcessed,
        int chunksCreated,
        int skippedFiles,
        int failures,
        List<Document> documents,
        List<DocumentChunk> chunks
) {
    public IngestionResult {
        documents = documents == null ? List.of() : List.copyOf(documents);
        chunks = chunks == null ? List.of() : List.copyOf(chunks);
        if (documentsDiscovered < 0 || documentsProcessed < 0 || chunksCreated < 0 || skippedFiles < 0 || failures < 0) {
            throw new IllegalArgumentException("Ingestion result counts cannot be negative");
        }
    }
}
