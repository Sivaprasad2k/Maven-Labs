package com.shevay.knowledge.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    @DisplayName("Should load default configuration values")
    void testLoadDefaults() {
        AppConfig config = AppConfig.loadDefaults();
        assertEquals(AppConfig.DEFAULT_KNOWLEDGE_PATH, config.getKnowledgePath());
        assertEquals(AppConfig.DEFAULT_DATA_PATH, config.getDataPath());
        assertEquals(AppConfig.DEFAULT_TOP_K, config.getTopK());
        assertEquals(AppConfig.DEFAULT_MIN_SIMILARITY, config.getMinSimilarity());
    }

    @Test
    @DisplayName("Should load custom values from properties")
    void testLoadFromProperties() {
        Properties props = new Properties();
        props.setProperty("knowledge.path", "custom/knowledge");
        props.setProperty("data.path", "custom/data");
        props.setProperty("retrieval.top-k", "10");
        props.setProperty("retrieval.min-similarity", "0.85");

        AppConfig config = AppConfig.loadFromProperties(props);
        assertEquals("custom/knowledge", config.getKnowledgePath());
        assertEquals("custom/data", config.getDataPath());
        assertEquals(10, config.getTopK());
        assertEquals(0.85, config.getMinSimilarity());
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
    @DisplayName("Should satisfy equals, hashCode, and toString contracts")
    void testEqualsAndToString() {
        AppConfig config1 = new AppConfig("kPath", "dPath", 5, 0.8);
        AppConfig config2 = new AppConfig("kPath", "dPath", 5, 0.8);

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertTrue(config1.toString().contains("kPath"));
    }
}
