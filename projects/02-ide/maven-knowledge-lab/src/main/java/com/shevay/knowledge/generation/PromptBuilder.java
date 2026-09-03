package com.shevay.knowledge.generation;

import java.util.Objects;

/**
 * Component responsible for constructing deterministic, grounded-generation prompts.
 * Explicitly instructs the LLM to rely strictly on supplied knowledge context
 * and avoid hallucinating facts.
 */
public class PromptBuilder {

    private static final String SYSTEM_INSTRUCTIONS =
            "You are a precise technical knowledge assistant.\n" +
            "Answer the user's question strictly using ONLY the provided knowledge context below.\n\n" +
            "Rules:\n" +
            "1. Base your answer ONLY on the supplied knowledge context.\n" +
            "2. Do NOT invent, assume, or extrapolate facts beyond what is explicitly stated in the context.\n" +
            "3. If the context does not contain sufficient information to answer the question, state clearly: \"I am unable to answer based on the provided knowledge context.\"\n" +
            "4. Keep the answer clear, accurate, and concise.\n";

    /**
     * Constructs the generation prompt combining system instructions, assembled context, and user question.
     *
     * @param query            User query text
     * @param assembledContext Formatted source context from ContextAssembler
     * @return Deterministic prompt string
     */
    public String buildPrompt(String query, String assembledContext) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query must not be null or blank");
        }

        String safeContext = (assembledContext == null || assembledContext.isBlank())
                ? "No context provided."
                : assembledContext.strip();

        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEM_INSTRUCTIONS).append("\n");
        sb.append("--- KNOWLEDGE CONTEXT BEGIN ---\n");
        sb.append(safeContext).append("\n");
        sb.append("--- KNOWLEDGE CONTEXT END ---\n\n");
        sb.append("USER QUESTION: ").append(query.strip()).append("\n\n");
        sb.append("ANSWER:");

        return sb.toString();
    }
}
