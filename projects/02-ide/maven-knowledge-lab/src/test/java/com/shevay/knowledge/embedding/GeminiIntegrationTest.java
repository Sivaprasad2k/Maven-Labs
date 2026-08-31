package com.shevay.knowledge.embedding;

import com.shevay.knowledge.config.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Opt-in live integration test against Google Gemini API.
 *
 * <p>To execute this test:
 * <code>.\mvnw.cmd test -Dgemini.integration=true</code>
 * (Requires GEMINI_API_KEY environment variable to be set)</p>
 */
@EnabledIfSystemProperty(named = "gemini.integration", matches = "true")
class GeminiIntegrationTest {

    @Test
    @DisplayName("Live Integration: Embed known sentence via Gemini API gemini-embedding-001")
    void testLiveGeminiEmbedding() {
        AppConfig config = AppConfig.loadDefaults();
        String apiKey = config.getGeminiApiKey();

        assertNotNull(apiKey, "GEMINI_API_KEY environment variable must be set to run live integration test");
        assertFalse(apiKey.isBlank(), "GEMINI_API_KEY environment variable must not be blank");

        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(config);
        assertEquals("gemini-embedding-001", provider.getModelIdentifier());
        assertEquals(768, provider.getDimensions());

        Embedding embedding = provider.embed("What is the Maven build lifecycle?", EmbeddingPurpose.QUERY);

        assertNotNull(embedding);
        assertEquals(768, embedding.getDimensions());
        assertEquals("gemini-embedding-001", embedding.getModelIdentifier());

        float[] values = embedding.getValues();
        assertEquals(768, values.length);

        double magnitudeSq = 0.0;
        for (float v : values) {
            assertFalse(Float.isNaN(v), "Vector value must not be NaN");
            assertFalse(Float.isInfinite(v), "Vector value must not be Infinite");
            magnitudeSq += v * v;
        }

        // Verify L2 normalization
        assertEquals(1.0, Math.sqrt(magnitudeSq), 1e-3, "Vector must be L2 normalized to magnitude 1.0");
    }
}
