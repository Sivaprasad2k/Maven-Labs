package com.shevay.knowledge.agent.tools;

import com.shevay.knowledge.agent.AgentTool;
import com.shevay.knowledge.agent.ToolResult;
import com.shevay.knowledge.generation.RagService;
import com.shevay.knowledge.model.RagResponse;
import com.shevay.knowledge.vector.VectorStore;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Higher-level agent tool wrapping RagService to explain Maven concepts using grounded RAG generation.
 */
public class ExplainMavenConceptTool implements AgentTool {

    public static final String NAME = "explainMavenConcept";

    private final RagService ragService;
    private final VectorStore vectorStore;

    public ExplainMavenConceptTool(RagService ragService, VectorStore vectorStore) {
        this.ragService = Objects.requireNonNull(ragService, "ragService must not be null");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore must not be null");
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Generates a grounded RAG explanation for a Maven concept using the knowledge base. Arguments: {\"concept\": \"<maven topic>\"}";
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        if (arguments == null || (!arguments.containsKey("concept") && !arguments.containsKey("query"))) {
            return ToolResult.failure(NAME, "Missing required argument 'concept'");
        }

        Object conceptObj = arguments.getOrDefault("concept", arguments.get("query"));
        if (conceptObj == null || !(conceptObj instanceof String concept) || concept.isBlank()) {
            return ToolResult.failure(NAME, "Argument 'concept' must be a non-blank string");
        }

        try {
            RagResponse response = ragService.query(concept, vectorStore);

            String sourcesSummary = response.sources().isEmpty()
                    ? "No sources referenced"
                    : response.sources().stream()
                            .map(s -> s.sourcePath() + " (relevance: " + String.format("%.2f", s.relevanceScore()) + ")")
                            .collect(Collectors.joining(", "));

            String output = String.format("Concept: %s\nAnswer: %s\nSources: [%s]",
                    response.query(), response.generatedAnswer(), sourcesSummary);

            return ToolResult.success(NAME, output);
        } catch (Exception e) {
            return ToolResult.failure(NAME, "Error explaining concept: " + e.getMessage());
        }
    }
}
