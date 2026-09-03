package com.shevay.knowledge.agent.tools;

import com.shevay.knowledge.agent.AgentTool;
import com.shevay.knowledge.agent.ToolResult;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.embedding.EmbeddingProvider;
import com.shevay.knowledge.embedding.EmbeddingPurpose;
import com.shevay.knowledge.model.RetrievedChunk;
import com.shevay.knowledge.retrieval.SimilaritySearchService;
import com.shevay.knowledge.vector.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Agent tool wrapping SimilaritySearchService to search local vector store knowledge chunks.
 */
public class SearchKnowledgeTool implements AgentTool {

    public static final String NAME = "searchKnowledge";

    private final SimilaritySearchService searchService;
    private final EmbeddingProvider embeddingProvider;
    private final VectorStore vectorStore;

    public SearchKnowledgeTool(SimilaritySearchService searchService, EmbeddingProvider embeddingProvider, VectorStore vectorStore) {
        this.searchService = Objects.requireNonNull(searchService, "searchService must not be null");
        this.embeddingProvider = Objects.requireNonNull(embeddingProvider, "embeddingProvider must not be null");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore must not be null");
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Searches the local vector store for knowledge chunks matching a query. Arguments: {\"query\": \"<search string>\"}";
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        if (arguments == null || !arguments.containsKey("query")) {
            return ToolResult.failure(NAME, "Missing required argument 'query'");
        }

        Object queryObj = arguments.get("query");
        if (queryObj == null || !(queryObj instanceof String query) || query.isBlank()) {
            return ToolResult.failure(NAME, "Argument 'query' must be a non-blank string");
        }

        try {
            Embedding queryEmbedding = embeddingProvider.embed(query, EmbeddingPurpose.QUERY);
            List<RetrievedChunk> results = searchService.search(queryEmbedding, vectorStore);

            if (results.isEmpty()) {
                return ToolResult.success(NAME, "No relevant knowledge chunks found for query: " + query);
            }

            String formattedOutput = results.stream()
                    .map(r -> String.format("[%s (Score: %.4f)]\n%s",
                            r.chunk().sourcePath(), r.similarityScore(), r.chunk().text()))
                    .collect(Collectors.joining("\n---\n"));

            return ToolResult.success(NAME, formattedOutput);
        } catch (Exception e) {
            return ToolResult.failure(NAME, "Error performing search: " + e.getMessage());
        }
    }
}
