package com.shevay.knowledge.generation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTest {

    @Test
    @DisplayName("Should build grounded prompt containing user question and context")
    void testPromptConstruction() {
        PromptBuilder builder = new PromptBuilder();
        String question = "What is the compile phase?";
        String context = "--- Source [1] ---\nThe compile phase compiles the source code of the project.";

        String prompt = builder.buildPrompt(question, context);

        assertNotNull(prompt);
        assertTrue(prompt.contains("USER QUESTION: What is the compile phase?"));
        assertTrue(prompt.contains("--- KNOWLEDGE CONTEXT BEGIN ---"));
        assertTrue(prompt.contains("The compile phase compiles the source code of the project."));
        assertTrue(prompt.contains("--- KNOWLEDGE CONTEXT END ---"));
        assertTrue(prompt.contains("Answer the user's question strictly using ONLY the provided knowledge context"));
        assertTrue(prompt.contains("Do NOT invent, assume, or extrapolate facts"));
    }

    @Test
    @DisplayName("Should handle empty context gracefully")
    void testEmptyContextHandling() {
        PromptBuilder builder = new PromptBuilder();
        String prompt = builder.buildPrompt("What is Maven?", "");

        assertTrue(prompt.contains("USER QUESTION: What is Maven?"));
        assertTrue(prompt.contains("No context provided."));
    }

    @Test
    @DisplayName("Should reject null or blank query string")
    void testNullOrBlankQueryValidation() {
        PromptBuilder builder = new PromptBuilder();

        assertThrows(IllegalArgumentException.class, () -> builder.buildPrompt(null, "context"));
        assertThrows(IllegalArgumentException.class, () -> builder.buildPrompt("   ", "context"));
    }
}
