package com.shevay.knowledge.config;

import java.util.Objects;
import java.util.Properties;

/**
 * Configuration holder for Maven Knowledge Lab.
 * Defines configuration properties with safe defaults, strict bounds assertions, and support for override loading.
 */
public final class AppConfig {

    public static final String DEFAULT_KNOWLEDGE_PATH = "knowledge";
    public static final String DEFAULT_DATA_PATH = "data";
    public static final String DEFAULT_VECTOR_STORE_PATH = "data/vectors.dat";
    public static final int DEFAULT_TOP_K = 3;
    public static final double DEFAULT_MIN_SIMILARITY = 0.7;
    public static final int DEFAULT_CHUNK_SIZE = 800;
    public static final int DEFAULT_CHUNK_OVERLAP = 100;

    public static final String DEFAULT_EMBEDDING_PROVIDER = "gemini";
    public static final String DEFAULT_EMBEDDING_MODEL = "gemini-embedding-001";
    public static final int DEFAULT_EMBEDDING_DIMENSIONS = 768;
    public static final int DEFAULT_EMBEDDING_TIMEOUT_SECONDS = 30;

    public static final String DEFAULT_GENERATION_PROVIDER = "gemini";
    public static final String DEFAULT_GENERATION_MODEL = "gemini-3.6-flash";
    public static final int DEFAULT_GENERATION_TIMEOUT_SECONDS = 30;

    private final String knowledgePath;
    private final String dataPath;
    private final String vectorStorePath;
    private final int topK;
    private final double minSimilarity;
    private final int chunkSize;
    private final int chunkOverlap;

    private final String embeddingProvider;
    private final String embeddingModel;
    private final int embeddingDimensions;
    private final int embeddingTimeoutSeconds;

    private final String generationProvider;
    private final String generationModel;
    private final int generationTimeoutSeconds;

    public AppConfig(String knowledgePath, String dataPath, int topK, double minSimilarity) {
        this(knowledgePath, dataPath, DEFAULT_VECTOR_STORE_PATH, topK, minSimilarity, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }

    public AppConfig(String knowledgePath, String dataPath, int topK, double minSimilarity, int chunkSize, int chunkOverlap) {
        this(knowledgePath, dataPath, DEFAULT_VECTOR_STORE_PATH, topK, minSimilarity, chunkSize, chunkOverlap,
                DEFAULT_EMBEDDING_PROVIDER, DEFAULT_EMBEDDING_MODEL, DEFAULT_EMBEDDING_DIMENSIONS, DEFAULT_EMBEDDING_TIMEOUT_SECONDS);
    }

    public AppConfig(String knowledgePath, String dataPath, String vectorStorePath, int topK, double minSimilarity,
                     int chunkSize, int chunkOverlap) {
        this(knowledgePath, dataPath, vectorStorePath, topK, minSimilarity, chunkSize, chunkOverlap,
                DEFAULT_EMBEDDING_PROVIDER, DEFAULT_EMBEDDING_MODEL, DEFAULT_EMBEDDING_DIMENSIONS, DEFAULT_EMBEDDING_TIMEOUT_SECONDS);
    }

    public AppConfig(String knowledgePath, String dataPath, String vectorStorePath, int topK, double minSimilarity,
                     int chunkSize, int chunkOverlap,
                     String embeddingProvider, String embeddingModel, int embeddingDimensions, int embeddingTimeoutSeconds) {
        this(knowledgePath, dataPath, vectorStorePath, topK, minSimilarity, chunkSize, chunkOverlap,
                embeddingProvider, embeddingModel, embeddingDimensions, embeddingTimeoutSeconds,
                DEFAULT_GENERATION_PROVIDER, DEFAULT_GENERATION_MODEL, DEFAULT_GENERATION_TIMEOUT_SECONDS);
    }

    public AppConfig(String knowledgePath, String dataPath, String vectorStorePath, int topK, double minSimilarity,
                     int chunkSize, int chunkOverlap,
                     String embeddingProvider, String embeddingModel, int embeddingDimensions, int embeddingTimeoutSeconds,
                     String generationProvider, String generationModel, int generationTimeoutSeconds) {
        this.knowledgePath = Objects.requireNonNull(knowledgePath, "knowledgePath must not be null");
        this.dataPath = Objects.requireNonNull(dataPath, "dataPath must not be null");
        this.vectorStorePath = Objects.requireNonNull(vectorStorePath, "vectorStorePath must not be null");
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

        Objects.requireNonNull(embeddingProvider, "embeddingProvider must not be null");
        if (!"gemini".equalsIgnoreCase(embeddingProvider.trim())) {
            throw new IllegalArgumentException("Unsupported embedding provider: '" + embeddingProvider + "'. Only 'gemini' is supported in this phase.");
        }

        Objects.requireNonNull(embeddingModel, "embeddingModel must not be null");
        if (!"gemini-embedding-001".equalsIgnoreCase(embeddingModel.trim())) {
            throw new IllegalArgumentException("Unsupported embedding model: '" + embeddingModel + "'. Only 'gemini-embedding-001' is supported in this phase.");
        }

        if (embeddingDimensions != 768) {
            throw new IllegalArgumentException("Unsupported embedding dimensions: " + embeddingDimensions + ". Only 768 is supported in this phase.");
        }

        if (embeddingTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("embeddingTimeoutSeconds must be positive (> 0), got: " + embeddingTimeoutSeconds);
        }

        Objects.requireNonNull(generationProvider, "generationProvider must not be null");
        if (generationProvider.isBlank()) {
            throw new IllegalArgumentException("generationProvider must not be blank");
        }

        Objects.requireNonNull(generationModel, "generationModel must not be null");
        if (generationModel.isBlank()) {
            throw new IllegalArgumentException("generationModel must not be blank");
        }

        if (generationTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("generationTimeoutSeconds must be positive (> 0), got: " + generationTimeoutSeconds);
        }

        this.topK = topK;
        this.minSimilarity = minSimilarity;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.embeddingProvider = embeddingProvider.trim().toLowerCase();
        this.embeddingModel = embeddingModel.trim().toLowerCase();
        this.embeddingDimensions = embeddingDimensions;
        this.embeddingTimeoutSeconds = embeddingTimeoutSeconds;
        this.generationProvider = generationProvider.trim().toLowerCase();
        this.generationModel = generationModel.trim().toLowerCase();
        this.generationTimeoutSeconds = generationTimeoutSeconds;
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
        String vPath = getPropertyOrEnv(props, "vector.store.path", "VECTOR_STORE_PATH", DEFAULT_VECTOR_STORE_PATH);
        int k = parseIntOrDefault(getPropertyOrEnv(props, "retrieval.top-k", "RETRIEVAL_TOP_K", null), DEFAULT_TOP_K);
        double minSim = parseDoubleOrDefault(getPropertyOrEnv(props, "retrieval.min-similarity", "RETRIEVAL_MIN_SIMILARITY", null), DEFAULT_MIN_SIMILARITY);
        int cSize = parseIntOrDefault(getPropertyOrEnv(props, "chunking.chunk-size", "CHUNKING_CHUNK_SIZE", null), DEFAULT_CHUNK_SIZE);
        int cOverlap = parseIntOrDefault(getPropertyOrEnv(props, "chunking.chunk-overlap", "CHUNKING_CHUNK_OVERLAP", null), DEFAULT_CHUNK_OVERLAP);

        String embProvider = getPropertyOrEnv(props, "embedding.provider", "EMBEDDING_PROVIDER", DEFAULT_EMBEDDING_PROVIDER);
        String embModel = getPropertyOrEnv(props, "embedding.model", "EMBEDDING_MODEL", DEFAULT_EMBEDDING_MODEL);
        int embDimensions = parseIntOrDefault(getPropertyOrEnv(props, "embedding.dimensions", "EMBEDDING_DIMENSIONS", null), DEFAULT_EMBEDDING_DIMENSIONS);
        int embTimeout = parseIntOrDefault(getPropertyOrEnv(props, "embedding.timeout-seconds", "EMBEDDING_TIMEOUT_SECONDS", null), DEFAULT_EMBEDDING_TIMEOUT_SECONDS);

        String genProvider = getPropertyOrEnv(props, "generation.provider", "GENERATION_PROVIDER", DEFAULT_GENERATION_PROVIDER);
        String genModel = getPropertyOrEnv(props, "generation.model", "GENERATION_MODEL", DEFAULT_GENERATION_MODEL);
        int genTimeout = parseIntOrDefault(getPropertyOrEnv(props, "generation.timeout-seconds", "GENERATION_TIMEOUT_SECONDS", null), DEFAULT_GENERATION_TIMEOUT_SECONDS);

        return new AppConfig(kPath, dPath, vPath, k, minSim, cSize, cOverlap, embProvider, embModel, embDimensions, embTimeout, genProvider, genModel, genTimeout);
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

    private static volatile String servletContextRealPath = null;

    public static void setServletContextRealPath(String realPath) {
        if (realPath != null && !realPath.isBlank()) {
            servletContextRealPath = realPath;
        }
    }

    public static java.nio.file.Path resolvePath(String pathStr) {
        if (pathStr == null || pathStr.isBlank()) {
            return java.nio.file.Paths.get(".");
        }
        java.nio.file.Path p = java.nio.file.Paths.get(pathStr);
        if (p.isAbsolute() && java.nio.file.Files.exists(p)) {
            return p;
        }
        // 1. Check relative to current working directory (CLI execution)
        if (java.nio.file.Files.exists(p)) {
            return p.toAbsolutePath().normalize();
        }
        // 2. Check ServletContext real path if bound
        if (servletContextRealPath != null) {
            java.nio.file.Path webPath = java.nio.file.Paths.get(servletContextRealPath, pathStr);
            if (java.nio.file.Files.exists(webPath)) {
                return webPath.toAbsolutePath().normalize();
            }
        }
        // 3. Check code source location parent (WAR unpacked location or target/classes)
        try {
            java.net.URL codeLoc = AppConfig.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeLoc != null) {
                java.nio.file.Path classPathLoc = java.nio.file.Paths.get(codeLoc.toURI());
                java.nio.file.Path webappRoot = classPathLoc.getParent() != null && classPathLoc.getParent().getParent() != null
                        ? classPathLoc.getParent().getParent()
                        : classPathLoc.getParent();
                if (webappRoot != null) {
                    java.nio.file.Path candidate = webappRoot.resolve(pathStr);
                    if (java.nio.file.Files.exists(candidate)) {
                        return candidate.toAbsolutePath().normalize();
                    }
                }
            }
        } catch (Exception ignored) {}

        // 4. Fallback to normalized absolute path
        return p.toAbsolutePath().normalize();
    }

    public String getKnowledgePath() {
        return knowledgePath;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getVectorStorePath() {
        return vectorStorePath;
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

    public String getEmbeddingProvider() {
        return embeddingProvider;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public int getEmbeddingDimensions() {
        return embeddingDimensions;
    }

    public int getEmbeddingTimeoutSeconds() {
        return embeddingTimeoutSeconds;
    }

    public String getGenerationProvider() {
        return generationProvider;
    }

    public String getGenerationModel() {
        return generationModel;
    }

    public int getGenerationTimeoutSeconds() {
        return generationTimeoutSeconds;
    }

    /**
     * Reads GEMINI_API_KEY environment variable.
     * NEVER prints, logs, or exposes the API key.
     *
     * @return API key string or null if not set
     */
    public String getGeminiApiKey() {
        return System.getenv("GEMINI_API_KEY");
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "knowledge.path='" + knowledgePath + '\'' +
                ", data.path='" + dataPath + '\'' +
                ", vector.store.path='" + vectorStorePath + '\'' +
                ", retrieval.top-k=" + topK +
                ", retrieval.min-similarity=" + minSimilarity +
                ", chunking.chunk-size=" + chunkSize +
                ", chunking.chunk-overlap=" + chunkOverlap +
                ", embedding.provider='" + embeddingProvider + '\'' +
                ", embedding.model='" + embeddingModel + '\'' +
                ", embedding.dimensions=" + embeddingDimensions +
                ", embedding.timeout-seconds=" + embeddingTimeoutSeconds +
                ", generation.provider='" + generationProvider + '\'' +
                ", generation.model='" + generationModel + '\'' +
                ", generation.timeout-seconds=" + generationTimeoutSeconds +
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
                embeddingDimensions == appConfig.embeddingDimensions &&
                embeddingTimeoutSeconds == appConfig.embeddingTimeoutSeconds &&
                generationTimeoutSeconds == appConfig.generationTimeoutSeconds &&
                Objects.equals(knowledgePath, appConfig.knowledgePath) &&
                Objects.equals(dataPath, appConfig.dataPath) &&
                Objects.equals(vectorStorePath, appConfig.vectorStorePath) &&
                Objects.equals(embeddingProvider, appConfig.embeddingProvider) &&
                Objects.equals(embeddingModel, appConfig.embeddingModel) &&
                Objects.equals(generationProvider, appConfig.generationProvider) &&
                Objects.equals(generationModel, appConfig.generationModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knowledgePath, dataPath, vectorStorePath, topK, minSimilarity, chunkSize, chunkOverlap,
                embeddingProvider, embeddingModel, embeddingDimensions, embeddingTimeoutSeconds,
                generationProvider, generationModel, generationTimeoutSeconds);
    }
}
