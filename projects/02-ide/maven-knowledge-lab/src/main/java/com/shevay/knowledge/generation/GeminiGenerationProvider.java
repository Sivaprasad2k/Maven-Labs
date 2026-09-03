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
 * Google Gemini REST API model (e.g., `gemini-1.5-flash`) via Java 17 HttpClient.
 *
 * <p>Authentication is performed exclusively via the `x-goog-api-key` HTTP header.
 * API keys are never written to logs, URLs, exceptions, or error messages.</p>
 */
public class GeminiGenerationProvider implements LlmGenerationProvider {

    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

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

        String jsonPayload = buildGenerateContentJsonPayload(prompt);
        String requestUrl = GEMINI_API_BASE_URL + "/" + model + ":generateContent";

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

    private static String buildGenerateContentJsonPayload(String prompt) {
        return "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        { \"text\": \"" + escapeJson(prompt) + "\" }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private static String parseGeneratedTextFromJson(String json) {
        if (json == null || json.isBlank()) {
            throw new GenerationException("Gemini API returned empty response body");
        }

        int candidatesIndex = json.indexOf("\"candidates\":");
        if (candidatesIndex == -1) {
            throw new GenerationException("Malformed Gemini API generation response: missing 'candidates' array");
        }

        int textIndex = json.indexOf("\"text\":", candidatesIndex);
        if (textIndex == -1) {
            throw new GenerationException("Malformed Gemini API generation response: missing candidate text content");
        }

        int startQuote = json.indexOf('"', textIndex + 7);
        if (startQuote == -1) {
            throw new GenerationException("Malformed Gemini API generation response: invalid text quote format");
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

        String result = sb.toString().trim();
        if (result.isEmpty()) {
            throw new GenerationException("Gemini API returned empty generated text");
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
