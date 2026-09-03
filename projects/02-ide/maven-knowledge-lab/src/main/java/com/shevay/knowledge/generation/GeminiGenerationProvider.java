package com.shevay.knowledge.generation;

import com.shevay.knowledge.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Objects;

/**
 * Production implementation of LlmGenerationProvider interacting directly with
 * Google Gemini REST Interactions API (model `gemini-3.6-flash`) via Java 17 HttpClient.
 *
 * <p>Authentication is performed exclusively via the `x-goog-api-key` HTTP header.
 * API keys are never written to logs, URLs, exceptions, or error messages.</p>
 */
public class GeminiGenerationProvider implements LlmGenerationProvider {

    private static final String GEMINI_INTERACTIONS_API_URL = "https://generativelanguage.googleapis.com/v1beta/interactions";

    private final String model;
    private final Duration timeout;
    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiGenerationProvider(AppConfig config) {
        this(config, null, null);
    }

    public GeminiGenerationProvider(AppConfig config, String apiKeyOverride, HttpClient httpClientOverride) {
        Objects.requireNonNull(config, "AppConfig must not be null");
        this.model = config.getGenerationModel();
        this.timeout = Duration.ofSeconds(config.getGenerationTimeoutSeconds());

        String keyCandidate = (apiKeyOverride != null && !apiKeyOverride.isBlank())
                ? apiKeyOverride
                : config.getGeminiApiKey();

        if (keyCandidate == null || keyCandidate.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not set. Cannot initialize GeminiGenerationProvider.");
        }
        this.apiKey = keyCandidate;

        this.httpClient = (httpClientOverride != null)
                ? httpClientOverride
                : HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
    }

    @Override
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be null or blank");
        }

        String jsonPayload = buildInteractionsJsonPayload(prompt, model);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_INTERACTIONS_API_URL))
                .timeout(timeout)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleHttpStatusErrors(response.statusCode(), response.body());

            return parseGeneratedTextFromJson(response.body());
        } catch (HttpTimeoutException e) {
            throw new GenerationException("Gemini API generation request timed out after " + timeout.toSeconds() + " seconds", e);
        } catch (IOException e) {
            throw new GenerationException("Network I/O failure while calling Gemini API generation endpoint: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GenerationException("Interrupted while awaiting Gemini API generation response", e);
        }
    }

    @Override
    public String getModelIdentifier() {
        return model;
    }

    private static void handleHttpStatusErrors(int statusCode, String responseBody) {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        String safeErrorMsg = extractErrorMessageFromJson(responseBody);
        switch (statusCode) {
            case 400 -> throw new GenerationException("Gemini API Bad Request (400): " + safeErrorMsg);
            case 401, 403 -> throw new GenerationException("Gemini API Authentication Failed (" + statusCode + "). Verify GEMINI_API_KEY environment variable.");
            case 404 -> throw new GenerationException("Gemini API Resource Not Found (404). Verify model identifier.");
            case 429 -> throw new GenerationException("Gemini API Rate Limit Exceeded (429). " + safeErrorMsg);
            default -> {
                if (statusCode >= 500) {
                    throw new GenerationException("Gemini API Server Error (" + statusCode + "). " + safeErrorMsg);
                } else {
                    throw new GenerationException("Gemini API Error (" + statusCode + "): " + safeErrorMsg);
                }
            }
        }
    }

    private static String buildInteractionsJsonPayload(String prompt, String model) {
        return "{\n" +
                "  \"model\": \"" + escapeJson(model) + "\",\n" +
                "  \"input\": \"" + escapeJson(prompt) + "\"\n" +
                "}";
    }

    private static String parseGeneratedTextFromJson(String json) {
        if (json == null || json.isBlank()) {
            throw new GenerationException("Gemini API returned empty response body");
        }

        // 1. Try Interactions API 'outputs' array structure: {"outputs": [{"text": "..."}]} or [{"parts": [{"text": "..."}]}]
        int outputsIndex = json.indexOf("\"outputs\":");
        if (outputsIndex != -1) {
            String extracted = extractTextAfterMarker(json, outputsIndex);
            if (extracted != null && !extracted.isBlank()) {
                return extracted;
            }
        }

        // 2. Try candidates array structure (legacy / fallback)
        int candidatesIndex = json.indexOf("\"candidates\":");
        if (candidatesIndex != -1) {
            String extracted = extractTextAfterMarker(json, candidatesIndex);
            if (extracted != null && !extracted.isBlank()) {
                return extracted;
            }
        }

        // 3. Fallback: Search for top-level "text": "..."
        int textIndex = json.indexOf("\"text\":");
        if (textIndex != -1) {
            String extracted = extractTextFromQuote(json, textIndex + 7);
            if (extracted != null && !extracted.isBlank()) {
                return extracted;
            }
        }

        throw new GenerationException("Malformed Gemini API generation response: missing text output in response body");
    }

    private static String extractTextAfterMarker(String json, int markerIndex) {
        int textIndex = json.indexOf("\"text\":", markerIndex);
        if (textIndex != -1) {
            return extractTextFromQuote(json, textIndex + 7);
        }
        return null;
    }

    private static String extractTextFromQuote(String json, int fromIndex) {
        int startQuote = json.indexOf('"', fromIndex);
        if (startQuote == -1) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int i = startQuote + 1;
        boolean escaped = false;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (escaped) {
                switch (c) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append("\\u").append(hex);
                            }
                        } else {
                            sb.append("\\u");
                        }
                    }
                    default -> sb.append(c);
                }
                escaped = false;
            } else {
                if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            i++;
        }
        return sb.toString().trim();
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
