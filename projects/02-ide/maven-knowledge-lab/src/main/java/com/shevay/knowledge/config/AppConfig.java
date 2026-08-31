package com.shevay.knowledge.config;

import java.util.Objects;
import java.util.Properties;

/**
 * Configuration holder for Maven Knowledge Lab.
 * Defines configuration properties with safe defaults and support for override loading.
 */
public final class AppConfig {

    public static final String DEFAULT_KNOWLEDGE_PATH = "knowledge";
    public static final String DEFAULT_DATA_PATH = "data";
    public static final int DEFAULT_TOP_K = 3;
    public static final double DEFAULT_MIN_SIMILARITY = 0.7;
    public static final int DEFAULT_CHUNK_SIZE = 800;
    public static final int DEFAULT_CHUNK_OVERLAP = 100;

    private final String knowledgePath;
    private final String dataPath;
    private final int topK;
    private final double minSimilarity;
    private final int chunkSize;
    private final int chunkOverlap;

    public AppConfig(String knowledgePath, String dataPath, int topK, double minSimilarity) {
        this(knowledgePath, dataPath, topK, minSimilarity, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }

    public AppConfig(String knowledgePath, String dataPath, int topK, double minSimilarity, int chunkSize, int chunkOverlap) {
        this.knowledgePath = Objects.requireNonNull(knowledgePath, "knowledgePath must not be null");
        this.dataPath = Objects.requireNonNull(dataPath, "dataPath must not be null");
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be positive, got: " + topK);
        }
        if (minSimilarity < 0.0 || minSimilarity > 1.0) {
            throw new IllegalArgumentException("minSimilarity must be between 0.0 and 1.0, got: " + minSimilarity);
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive (> 0), got: " + chunkSize);
        }
        if (chunkOverlap < 0) {
            throw new IllegalArgumentException("chunkOverlap must be non-negative (>= 0), got: " + chunkOverlap);
        }
        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("chunkOverlap (" + chunkOverlap + ") must be strictly less than chunkSize (" + chunkSize + ")");
        }
        this.topK = topK;
        this.minSimilarity = minSimilarity;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    /**
     * Loads AppConfig with standard default values.
     *
     * @return AppConfig populated with default settings.
     */
    public static AppConfig loadDefaults() {
        return loadFromProperties(new Properties());
    }

    /**
     * Loads AppConfig checking explicit properties, system properties, environment variables,
     * and finally falling back to safe defaults.
     *
     * @param props explicit property overrides
     * @return populated AppConfig
     */
    public static AppConfig loadFromProperties(Properties props) {
        String kPath = getPropertyOrEnv(props, "knowledge.path", "KNOWLEDGE_PATH", DEFAULT_KNOWLEDGE_PATH);
        String dPath = getPropertyOrEnv(props, "data.path", "DATA_PATH", DEFAULT_DATA_PATH);
        int k = parseIntOrDefault(getPropertyOrEnv(props, "retrieval.top-k", "RETRIEVAL_TOP_K", null), DEFAULT_TOP_K);
        double minSim = parseDoubleOrDefault(getPropertyOrEnv(props, "retrieval.min-similarity", "RETRIEVAL_MIN_SIMILARITY", null), DEFAULT_MIN_SIMILARITY);
        int cSize = parseIntOrDefault(getPropertyOrEnv(props, "chunking.chunk-size", "CHUNKING_CHUNK_SIZE", null), DEFAULT_CHUNK_SIZE);
        int cOverlap = parseIntOrDefault(getPropertyOrEnv(props, "chunking.chunk-overlap", "CHUNKING_CHUNK_OVERLAP", null), DEFAULT_CHUNK_OVERLAP);

        return new AppConfig(kPath, dPath, k, minSim, cSize, cOverlap);
    }

    private static String getPropertyOrEnv(Properties props, String propKey, String envKey, String defaultValue) {
        if (props != null && props.containsKey(propKey)) {
            return props.getProperty(propKey);
        }
        String sysProp = System.getProperty(propKey);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal;
        }
        return defaultValue;
    }

    private static int parseIntOrDefault(String val, int defaultVal) {
        if (val == null || val.isBlank()) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static double parseDoubleOrDefault(String val, double defaultVal) {
        if (val == null || val.isBlank()) {
            return defaultVal;
        }
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public String getKnowledgePath() {
        return knowledgePath;
    }

    public String getDataPath() {
        return dataPath;
    }

    public int getTopK() {
        return topK;
    }

    public double getMinSimilarity() {
        return minSimilarity;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "knowledge.path='" + knowledgePath + '\'' +
                ", data.path='" + dataPath + '\'' +
                ", retrieval.top-k=" + topK +
                ", retrieval.min-similarity=" + minSimilarity +
                ", chunking.chunk-size=" + chunkSize +
                ", chunking.chunk-overlap=" + chunkOverlap +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppConfig appConfig = (AppConfig) o;
        return topK == appConfig.topK &&
                Double.compare(appConfig.minSimilarity, minSimilarity) == 0 &&
                chunkSize == appConfig.chunkSize &&
                chunkOverlap == appConfig.chunkOverlap &&
                Objects.equals(knowledgePath, appConfig.knowledgePath) &&
                Objects.equals(dataPath, appConfig.dataPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knowledgePath, dataPath, topK, minSimilarity, chunkSize, chunkOverlap);
    }
}
