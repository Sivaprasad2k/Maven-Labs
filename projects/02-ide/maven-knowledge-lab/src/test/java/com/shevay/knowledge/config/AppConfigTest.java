package com.shevay.knowledge.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    @DisplayName("Should load default configuration values including Phase 3 embedding settings")
    void testLoadDefaults() {
        AppConfig config = AppConfig.loadDefaults();
        assertEquals(AppConfig.DEFAULT_KNOWLEDGE_PATH, config.getKnowledgePath());
        assertEquals(AppConfig.DEFAULT_DATA_PATH, config.getDataPath());
        assertEquals(AppConfig.DEFAULT_TOP_K, config.getTopK());
        assertEquals(AppConfig.DEFAULT_MIN_SIMILARITY, config.getMinSimilarity());
        assertEquals("gemini", config.getEmbeddingProvider());
        assertEquals("gemini-embedding-001", config.getEmbeddingModel());
        assertEquals(768, config.getEmbeddingDimensions());
        assertEquals(30, config.getEmbeddingTimeoutSeconds());
    }

    @Test
    @DisplayName("Should load custom values from properties")
    void testLoadFromProperties() {
        Properties props = new Properties();
        props.setProperty("knowledge.path", "custom/knowledge");
        props.setProperty("data.path", "custom/data");
        props.setProperty("retrieval.top-k", "10");
        props.setProperty("retrieval.min-similarity", "0.85");
        props.setProperty("embedding.provider", "gemini");
        props.setProperty("embedding.model", "gemini-embedding-001");
        props.setProperty("embedding.dimensions", "768");
        props.setProperty("embedding.timeout-seconds", "60");

        AppConfig config = AppConfig.loadFromProperties(props);
        assertEquals("custom/knowledge", config.getKnowledgePath());
        assertEquals("custom/data", config.getDataPath());
        assertEquals(10, config.getTopK());
        assertEquals(0.85, config.getMinSimilarity());
        assertEquals("gemini", config.getEmbeddingProvider());
        assertEquals("gemini-embedding-001", config.getEmbeddingModel());
        assertEquals(768, config.getEmbeddingDimensions());
        assertEquals(60, config.getEmbeddingTimeoutSeconds());
    }

    @Test
    @DisplayName("Should throw exception when topK is non-positive")
    void testInvalidTopK() {
        assertThrows(IllegalArgumentException.class, () ->
                new AppConfig("knowledge", "data", 0, 0.7)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new AppConfig("knowledge", "data", -5, 0.7)
        );
    }

    @Test
    @DisplayName("Should throw exception when minSimilarity is out of bounds [0.0, 1.0]")
    void testInvalidMinSimilarity() {
        assertThrows(IllegalArgumentException.class, () ->
                new AppConfig("knowledge", "data", 3, -0.1)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new AppConfig("knowledge", "data", 3, 1.1)
        );
    }

    @Test
    @DisplayName("Should validate embedding configuration parameters strictly")
    void testEmbeddingConfigValidations() {
        // Invalid provider
        assertThrows(IllegalArgumentException.class, () ->
                new AppConfig("k", "d", 3, 0.7, 800, 100, "openai", "gemini-embedding-001", 768, 30));

        // Invalid model
        assertThrows(IllegalArgumentException.class, () ->
                new AppConfig("k", "d", 3, 0.7, 800, 100, "gemini", "text-embedding-004", 768, 30));

        // Invalid dimensions
        assertThrows(IllegalArgumentException.class, () ->
                new AppConfig("k", "d", 3, 0.7, 800, 100, "gemini", "gemini-embedding-001", 1536, 30));

        // Invalid timeout
        assertThrows(IllegalArgumentException.class, () ->
                new AppConfig("k", "d", 3, 0.7, 800, 100, "gemini", "gemini-embedding-001", 768, 0));
    }

    @Test
    @DisplayName("Should satisfy equals, hashCode, and toString contracts")
    void testEqualsAndToString() {
        AppConfig config1 = new AppConfig("kPath", "dPath", 5, 0.8);
        AppConfig config2 = new AppConfig("kPath", "dPath", 5, 0.8);

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertTrue(config1.toString().contains("kPath"));
        assertTrue(config1.toString().contains("gemini-embedding-001"));
    }
}
