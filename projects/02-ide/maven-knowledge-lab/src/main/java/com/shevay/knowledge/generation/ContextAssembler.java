package com.shevay.knowledge.generation;

import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.model.RetrievedChunk;

import java.util.List;

/**
 * Component responsible for converting retrieved vector chunks into clearly delimited,
 * source-aware context text suitable for LLM prompt insertion.
 */
public class ContextAssembler {

    /**
     * Assembles a list of RetrievedChunk objects into a formatted context string.
     *
     * @param chunks List of retrieved chunks
     * @return Formatted context string, or empty string if chunks is null or empty
     */
    public String assembleContext(List<RetrievedChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk retrieved = chunks.get(i);
            DocumentChunk chunk = retrieved.chunk();

            sb.append(String.format("--- Source [%d]: %s (Path: %s, Score: %.4f) ---\n",
                    i + 1,
                    chunk.documentId(),
                    chunk.sourcePath().isBlank() ? "N/A" : chunk.sourcePath(),
                    retrieved.similarityScore()));
            sb.append(chunk.text().strip());
            sb.append("\n\n");
        }

        return sb.toString().stripTrailing();
    }
}
