package com.shevay.knowledge.document;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.model.Document;
import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.util.HashUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for deterministically chunking Document content into DocumentChunk models
 * using configurable character size and overlap.
 */
public class TextChunker {

    private final int chunkSize;
    private final int chunkOverlap;

    public TextChunker(AppConfig config) {
        Objects.requireNonNull(config, "AppConfig must not be null");
        this.chunkSize = config.getChunkSize();
        this.chunkOverlap = config.getChunkOverlap();
        validateConfig(chunkSize, chunkOverlap);
    }

    public TextChunker(int chunkSize, int chunkOverlap) {
        validateConfig(chunkSize, chunkOverlap);
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    private static void validateConfig(int chunkSize, int chunkOverlap) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive (> 0), got: " + chunkSize);
        }
        if (chunkOverlap < 0) {
            throw new IllegalArgumentException("chunkOverlap must be non-negative (>= 0), got: " + chunkOverlap);
        }
        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("chunkOverlap (" + chunkOverlap + ") must be strictly less than chunkSize (" + chunkSize + ")");
        }
    }

    /**
     * Splits a Document into deterministic chunks based on configured chunk size and overlap.
     *
     * @param document target Document model
     * @return ordered list of DocumentChunk instances
     */
    public List<DocumentChunk> chunkDocument(Document document) {
        Objects.requireNonNull(document, "Document must not be null");
        String content = document.content();
        if (content == null || content.isBlank()) {
            return List.of();
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        int length = content.length();
        int start = 0;
        int chunkIndex = 0;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);

            // Attempt to break at a whitespace boundary if near end limit
            if (end < length) {
                int boundaryLookback = Math.min(chunkOverlap, 50);
                int lastSpace = -1;
                for (int i = end; i > end - boundaryLookback && i > start; i--) {
                    char c = content.charAt(i - 1);
                    if (Character.isWhitespace(c)) {
                        lastSpace = i;
                        break;
                    }
                }
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            String chunkText = content.substring(start, end);
            String chunkId = HashUtil.sha256(document.id() + ":" + chunkIndex + ":" + chunkText);
            int tokenCount = estimateTokenCount(chunkText);

            DocumentChunk chunk = new DocumentChunk(
                    chunkId,
                    document.id(),
                    document.sourcePath(),
                    chunkIndex,
                    chunkText,
                    chunkText.length(),
                    tokenCount
            );
            chunks.add(chunk);
            chunkIndex++;

            if (end >= length) {
                break;
            }

            int nextStart = end - chunkOverlap;
            // Guarantee progress to prevent infinite loop
            start = Math.max(nextStart, start + 1);
        }

        return chunks;
    }

    private int estimateTokenCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }
}
