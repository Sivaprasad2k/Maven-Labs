package com.shevay.knowledge.generation;

import com.shevay.knowledge.config.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Opt-in live integration test against Google Gemini REST API generation endpoint.
 *
 * <p>To execute this test:
 * <code>.\mvnw.cmd test -Dgemini.integration=true</code>
 * (Requires GEMINI_API_KEY environment variable to be set)</p>
 */
@EnabledIfSystemProperty(named = "gemini.integration", matches = "true")
class GeminiGenerationIntegrationTest {

    @Test
    @DisplayName("Live Integration: Generate text via Gemini REST API")
    void testLiveGeminiGeneration() {
        AppConfig config = AppConfig.loadDefaults();
        String apiKey = config.getGeminiApiKey();

        assertNotNull(apiKey, "GEMINI_API_KEY environment variable must be set to run live integration test");
        assertFalse(apiKey.isBlank(), "GEMINI_API_KEY environment variable must not be blank");

        GeminiGenerationProvider provider = new GeminiGenerationProvider(config);
        assertEquals("gemini-1.5-flash", provider.getModelIdentifier());

        String prompt = "Respond with 'Hello from Maven Knowledge Lab Phase 5!' if you receive this message.";
        String answer = provider.generate(prompt);

        assertNotNull(answer, "Generated answer must not be null");
        assertFalse(answer.isBlank(), "Generated answer must not be blank");
    }
}
