package com.shevay.knowledge.embedding;

import com.shevay.knowledge.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Production implementation of EmbeddingProvider interacting directly with Google Gemini REST API
 * model `gemini-embedding-001` via Java 17 HttpClient.
 *
 * <p>Requirements & Behavior:
 * <ul>
 *   <li>Model: `gemini-embedding-001`</li>
 *   <li>Output Dimensions: 768</li>
 *   <li>Purpose Mapping: DOCUMENT -> RETRIEVAL_DOCUMENT, QUERY -> RETRIEVAL_QUERY</li>
 *   <li>Authentication: `x-goog-api-key` HTTP header (never printed, logged, or committed)</li>
 *   <li>Request Format: `embedContentConfig` object wrapping taskType and outputDimensionality</li>
 *   <li>L2 Normalization: Automatically applied to all returned vectors via VectorNormalizer</li>
 * </ul>
 * </p>
 */
public class GeminiEmbeddingProvider implements EmbeddingProvider {

    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    private final String model;
    private final int dimensions;
    private final Duration timeout;
    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiEmbeddingProvider(AppConfig config) {
        this(config, null, null);
    }

    public GeminiEmbeddingProvider(AppConfig config, String apiKeyOverride, HttpClient httpClientOverride) {
        Objects.requireNonNull(config, "AppConfig must not be null");
        this.model = config.getEmbeddingModel();
        this.dimensions = config.getEmbeddingDimensions();
        this.timeout = Duration.ofSeconds(config.getEmbeddingTimeoutSeconds());

        String keyCandidate = (apiKeyOverride != null && !apiKeyOverride.isBlank())
                ? apiKeyOverride
                : config.getGeminiApiKey();

        if (keyCandidate == null || keyCandidate.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not set. Cannot initialize GeminiEmbeddingProvider.");
        }
        this.apiKey = keyCandidate;

        this.httpClient = (httpClientOverride != null)
                ? httpClientOverride
                : HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
    }

    @Override
    public Embedding embed(String text, EmbeddingPurpose purpose) {
        Objects.requireNonNull(purpose, "EmbeddingPurpose must not be null");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Embedding input text must not be null or blank");
        }

        String taskType = mapPurposeToTaskType(purpose);
        String jsonPayload = buildSingleEmbedJsonPayload(text, taskType, model, dimensions);
        String requestUrl = GEMINI_API_BASE_URL + "/" + model + ":embedContent";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(timeout)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleHttpStatusErrors(response.statusCode(), response.body());

            float[] rawVector = parseSingleEmbeddingValuesFromJson(response.body(), dimensions);
            float[] normalizedVector = VectorNormalizer.normalizeL2(rawVector);

            return new Embedding(normalizedVector, dimensions, model);
        } catch (HttpTimeoutException e) {
            throw new EmbeddingException("Gemini API Request Timed Out after " + timeout.toSeconds() + " seconds", e);
        } catch (IOException e) {
            throw new EmbeddingException("Network I/O failure while calling Gemini API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmbeddingException("Interrupted while awaiting Gemini API response", e);
        }
    }

    @Override
    public List<Embedding> embedBatch(List<String> texts, EmbeddingPurpose purpose) {
        Objects.requireNonNull(texts, "Texts list must not be null");
        Objects.requireNonNull(purpose, "EmbeddingPurpose must not be null");
        if (texts.isEmpty()) {
            return List.of();
        }

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("Embedding input text at index " + i + " must not be null or blank");
            }
        }

        String taskType = mapPurposeToTaskType(purpose);
        String jsonPayload = buildBatchEmbedJsonPayload(texts, taskType, model, dimensions);
        String requestUrl = GEMINI_API_BASE_URL + "/" + model + ":batchEmbedContents";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(timeout)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleHttpStatusErrors(response.statusCode(), response.body());

            List<float[]> rawVectors = parseBatchEmbeddingValuesFromJson(response.body(), dimensions, texts.size());
            List<Embedding> result = new ArrayList<>(rawVectors.size());

            for (float[] rawVector : rawVectors) {
                float[] normalized = VectorNormalizer.normalizeL2(rawVector);
                result.add(new Embedding(normalized, dimensions, model));
            }
            return result;
        } catch (HttpTimeoutException e) {
            throw new EmbeddingException("Gemini API Request Timed Out after " + timeout.toSeconds() + " seconds", e);
        } catch (IOException e) {
            throw new EmbeddingException("Network I/O failure while calling Gemini API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmbeddingException("Interrupted while awaiting Gemini API response", e);
        }
    }

    @Override
    public int getDimensions() {
        return dimensions;
    }

    @Override
    public String getModelIdentifier() {
        return model;
    }

    private static String mapPurposeToTaskType(EmbeddingPurpose purpose) {
        return switch (purpose) {
            case DOCUMENT -> "RETRIEVAL_DOCUMENT";
            case QUERY -> "RETRIEVAL_QUERY";
        };
    }

    private static void handleHttpStatusErrors(int statusCode, String responseBody) {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        String safeErrorMsg = extractErrorMessageFromJson(responseBody);
        switch (statusCode) {
            case 400 -> throw new EmbeddingException("Gemini API Bad Request (400): " + safeErrorMsg);
            case 401, 403 -> throw new EmbeddingException("Gemini API Authentication Failed (" + statusCode + "). Verify GEMINI_API_KEY environment variable.");
            case 404 -> throw new EmbeddingException("Gemini API Resource Not Found (404). Verify model identifier.");
            case 429 -> throw new EmbeddingException("Gemini API Rate Limit Exceeded (429). " + safeErrorMsg);
            default -> {
                if (statusCode >= 500) {
                    throw new EmbeddingException("Gemini API Server Error (" + statusCode + "). " + safeErrorMsg);
                } else {
                    throw new EmbeddingException("Gemini API Error (" + statusCode + "): " + safeErrorMsg);
                }
            }
        }
    }

    private static String buildSingleEmbedJsonPayload(String text, String taskType, String model, int outputDimensions) {
        return "{\n" +
                "  \"model\": \"models/" + escapeJson(model) + "\",\n" +
                "  \"content\": {\n" +
                "    \"parts\": [\n" +
                "      { \"text\": \"" + escapeJson(text) + "\" }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"taskType\": \"" + escapeJson(taskType) + "\",\n" +
                "  \"outputDimensionality\": " + outputDimensions + ",\n" +
                "  \"embedContentConfig\": {\n" +
                "    \"taskType\": \"" + escapeJson(taskType) + "\",\n" +
                "    \"outputDimensionality\": " + outputDimensions + "\n" +
                "  }\n" +
                "}";
    }

    private static String buildBatchEmbedJsonPayload(List<String> texts, String taskType, String model, int outputDimensions) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"requests\": [\n");
        for (int i = 0; i < texts.size(); i++) {
            sb.append("    {\n");
            sb.append("      \"model\": \"models/").append(escapeJson(model)).append("\",\n");
            sb.append("      \"content\": { \"parts\": [{ \"text\": \"").append(escapeJson(texts.get(i))).append("\" }] },\n");
            sb.append("      \"taskType\": \"").append(escapeJson(taskType)).append("\",\n");
            sb.append("      \"outputDimensionality\": ").append(outputDimensions).append(",\n");
            sb.append("      \"embedContentConfig\": {\n");
            sb.append("        \"taskType\": \"").append(escapeJson(taskType)).append("\",\n");
            sb.append("        \"outputDimensionality\": ").append(outputDimensions).append("\n");
            sb.append("      }\n");
            sb.append("    }");
            if (i < texts.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }

    private static float[] parseSingleEmbeddingValuesFromJson(String json, int expectedDimensions) {
        int valuesIndex = json.indexOf("\"values\":");
        if (valuesIndex == -1) {
            throw new EmbeddingException("Malformed Gemini API response: missing 'values' array");
        }
        int startBracket = json.indexOf('[', valuesIndex);
        int endBracket = json.indexOf(']', startBracket);
        if (startBracket == -1 || endBracket == -1 || endBracket <= startBracket) {
            throw new EmbeddingException("Malformed Gemini API response: invalid 'values' array format");
        }

        String valuesStr = json.substring(startBracket + 1, endBracket).trim();
        if (valuesStr.isEmpty()) {
            throw new EmbeddingException("Gemini API returned empty embedding vector");
        }

        String[] tokens = valuesStr.split(",");
        if (tokens.length != expectedDimensions) {
            throw new EmbeddingException("Gemini API returned vector dimension " + tokens.length + " but expected " + expectedDimensions);
        }

        float[] vector = new float[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                vector[i] = Float.parseFloat(tokens[i].trim());
            } catch (NumberFormatException e) {
                throw new EmbeddingException("Failed to parse float value at index " + i + ": " + tokens[i], e);
            }
        }
        return vector;
    }

    private static List<float[]> parseBatchEmbeddingValuesFromJson(String json, int expectedDimensions, int expectedBatchSize) {
        List<float[]> result = new ArrayList<>();
        int fromIndex = 0;

        while (true) {
            int valuesIndex = json.indexOf("\"values\":", fromIndex);
            if (valuesIndex == -1) break;
            int startBracket = json.indexOf('[', valuesIndex);
            int endBracket = json.indexOf(']', startBracket);
            if (startBracket == -1 || endBracket == -1 || endBracket <= startBracket) break;

            String valuesStr = json.substring(startBracket + 1, endBracket).trim();
            if (!valuesStr.isEmpty()) {
                String[] tokens = valuesStr.split(",");
                if (tokens.length != expectedDimensions) {
                    throw new EmbeddingException("Gemini API returned batch vector dimension " + tokens.length + " but expected " + expectedDimensions);
                }
                float[] vector = new float[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    vector[i] = Float.parseFloat(tokens[i].trim());
                }
                result.add(vector);
            }
            fromIndex = endBracket + 1;
        }

        if (result.size() != expectedBatchSize) {
            throw new EmbeddingException("Gemini API returned " + result.size() + " embeddings for batch size " + expectedBatchSize);
        }
        return result;
    }

    private static String extractErrorMessageFromJson(String json) {
        if (json == null || json.isBlank()) {
            return "No error details returned";
        }
        int msgIndex = json.indexOf("\"message\":");
        if (msgIndex != -1) {
            int startQuote = json.indexOf('"', msgIndex + 10);
            if (startQuote != -1) {
                int endQuote = json.indexOf('"', startQuote + 1);
                if (endQuote != -1) {
                    return json.substring(startQuote + 1, endQuote);
                }
            }
        }
        return "API response: " + (json.length() > 200 ? json.substring(0, 200) + "..." : json);
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
