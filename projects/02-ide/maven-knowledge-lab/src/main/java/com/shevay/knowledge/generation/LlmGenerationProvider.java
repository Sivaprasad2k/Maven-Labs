package com.shevay.knowledge.generation;

/**
 * Abstraction interface for LLM text generation providers.
 * Decouples RAG workflow orchestration from specific LLM vendors or APIs.
 */
@FunctionalInterface
public interface LlmGenerationProvider {

    /**
     * Generates a text completion based on the given prompt.
     *
     * @param prompt fully constructed generation prompt
     * @return generated answer text from the model
     * @throws GenerationException if text generation fails or returns empty response
     */
    String generate(String prompt);

    /**
     * Returns the model identifier used for generation.
     *
     * @return string representation of the model name/id
     */
    default String getModelIdentifier() {
        return "generic-llm";
    }
}
