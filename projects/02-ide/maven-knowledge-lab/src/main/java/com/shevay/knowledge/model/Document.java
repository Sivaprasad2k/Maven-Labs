package com.shevay.knowledge.model;

import com.shevay.knowledge.util.HashUtil;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable domain model representing an ingested document.
 */
public record Document(
        String id,
        String title,
        String content,
        String sourcePath,
        String contentHash,
        Map<String, String> metadata
) {
    public Document {
        Objects.requireNonNull(id, "Document id must not be null");
        Objects.requireNonNull(title, "Document title must not be null");
        Objects.requireNonNull(content, "Document content must not be null");
        Objects.requireNonNull(sourcePath, "Document sourcePath must not be null");
        Objects.requireNonNull(contentHash, "Document contentHash must not be null");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    /**
     * Backward-compatible helper constructor that automatically calculates contentHash using SHA-256.
     */
    public Document(String id, String title, String content, String sourcePath, Map<String, String> metadata) {
        this(id, title, content, sourcePath, HashUtil.sha256(content), metadata);
    }
}
