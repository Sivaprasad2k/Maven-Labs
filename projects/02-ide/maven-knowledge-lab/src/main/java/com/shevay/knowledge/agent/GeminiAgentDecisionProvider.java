package com.shevay.knowledge.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.knowledge.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AgentDecisionProvider implementation using Gemini REST API via Java 17 HttpClient.
 * Parses Gemini text outputs into structured AgentDecision records.
 */
public class GeminiAgentDecisionProvider implements AgentDecisionProvider {

    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final AppConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiAgentDecisionProvider(AppConfig config) {
        this(config, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getGenerationTimeoutSeconds()))
                .build());
    }

    public GeminiAgentDecisionProvider(AppConfig config, HttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AgentDecision decide(AgentContext context) throws AgentException {
        Objects.requireNonNull(context, "context must not be null");

        String apiKey = config.getGeminiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new AgentException("GEMINI_API_KEY environment variable is not configured");
        }

        String prompt = buildAgentPrompt(context);
        String requestBodyJson = buildGeminiRequestBody(prompt);

        URI endpoint = URI.create(GEMINI_API_BASE_URL + config.getGenerationModel() + ":generateContent");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(config.getGenerationTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new AgentException("Gemini API Error (" + httpResponse.statusCode() + "): Generation request failed");
            }

            String responseText = extractCandidateText(httpResponse.body());
            return parseAgentDecision(responseText);

        } catch (IOException e) {
            throw new AgentException("Network I/O failure during agent decision generation", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AgentException("Agent decision generation was interrupted", e);
        }
    }

    private String buildAgentPrompt(AgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a controlled knowledge agent for Maven Knowledge Lab.\n");
        sb.append("You must answer user queries strictly using available tools.\n\n");
        sb.append("Available Tools:\n");
        for (AgentTool tool : context.getAvailableTools()) {
            sb.append("- ").append(tool.name()).append(": ").append(tool.description()).append("\n");
        }

        sb.append("\nDECISION PROTOCOL:\n");
        sb.append("You MUST respond with ONLY a valid JSON object in ONE of two forms:\n");
        sb.append("Form 1 - Tool Call:\n");
        sb.append("{\"type\": \"tool_call\", \"tool\": \"<toolName>\", \"arguments\": {\"<argName>\": \"<argValue>\"}}\n");
        sb.append("Form 2 - Final Answer:\n");
        sb.append("{\"type\": \"final_answer\", \"answer\": \"<your response text>\"}\n\n");
        sb.append("RULES:\n");
        sb.append("1. Do NOT invent tool names. Use ONLY listed tools.\n");
        sb.append("2. Output ONLY the JSON object. Do not include extra text, explanations, or code block markers.\n");
        sb.append("3. If you have enough evidence or no tool can help, return a final_answer decision.\n\n");

        sb.append("Execution History:\n");
        if (context.getHistory().isEmpty()) {
            sb.append("(No tools executed yet)\n");
        } else {
            for (AgentExecutionEntry entry : context.getHistory()) {
                sb.append("Step ").append(entry.step()).append(":\n");
                sb.append("  Decision: ").append(entry.decision().type());
                if (entry.decision().type() == AgentDecisionType.TOOL_CALL) {
                    sb.append(" (").append(entry.decision().toolCall().tool()).append(")");
                }
                sb.append("\n");
                if (entry.toolResult() != null) {
                    sb.append("  Result: ").append(entry.toolResult().success() ? "SUCCESS" : "FAILURE").append("\n");
                    sb.append("  Output: ").append(entry.toolResult().success() ? entry.toolResult().output() : entry.toolResult().errorMessage()).append("\n");
                }
            }
        }

        sb.append("\nUser Query: ").append(context.getUserQuery()).append("\n");
        sb.append("What is your next decision?\n");

        return sb.toString();
    }

    private String buildGeminiRequestBody(String prompt) {
        try {
            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> contentObj = Map.of("parts", List.of(textPart));
            Map<String, Object> rootObj = Map.of("contents", List.of(contentObj));
            return objectMapper.writeValueAsString(rootObj);
        } catch (Exception e) {
            throw new AgentException("Failed to construct Gemini request payload JSON", e);
        }
    }

    private String extractCandidateText(String responseBodyJson) {
        try {
            JsonNode root = objectMapper.readTree(responseBodyJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText().trim();
                }
            }
            throw new AgentException("Invalid or empty candidate text in Gemini response");
        } catch (Exception e) {
            throw new AgentException("Failed to parse Gemini HTTP response payload", e);
        }
    }

    public AgentDecision parseAgentDecision(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new AgentException("Empty or null raw model output");
        }

        String jsonText = rawText.trim();
        if (jsonText.startsWith("```")) {
            int firstNewline = jsonText.indexOf('\n');
            int lastBackticks = jsonText.lastIndexOf("```");
            if (firstNewline != -1 && lastBackticks > firstNewline) {
                jsonText = jsonText.substring(firstNewline + 1, lastBackticks).trim();
            }
        }

        try {
            JsonNode root = objectMapper.readTree(jsonText);
            if (!root.isObject()) {
                throw new AgentException("Decision output must be a JSON object");
            }

            JsonNode typeNode = root.path("type");
            if (typeNode.isMissingNode() || typeNode.isNull() || !typeNode.isTextual()) {
                throw new AgentException("Missing or invalid 'type' field in decision JSON");
            }

            String typeStr = typeNode.asText().trim().toLowerCase();
            if ("final_answer".equals(typeStr) || "finalanswer".equals(typeStr)) {
                JsonNode answerNode = root.path("answer");
                if (answerNode.isMissingNode() || answerNode.isNull() || !answerNode.isTextual()) {
                    throw new AgentException("final_answer decision must contain a non-null 'answer' text string");
                }
                return AgentDecision.finalAnswer(answerNode.asText());
            } else if ("tool_call".equals(typeStr) || "toolcall".equals(typeStr)) {
                JsonNode toolNode = root.path("tool");
                if (toolNode.isMissingNode() || toolNode.isNull() || !toolNode.isTextual() || toolNode.asText().isBlank()) {
                    throw new AgentException("tool_call decision must contain a non-blank 'tool' name");
                }
                String toolName = toolNode.asText().trim();
                JsonNode argsNode = root.path("arguments");
                Map<String, Object> argsMap = new HashMap<>();
                if (argsNode.isObject()) {
                    argsNode.fields().forEachRemaining(entry -> {
                        JsonNode val = entry.getValue();
                        if (val.isTextual()) argsMap.put(entry.getKey(), val.asText());
                        else if (val.isNumber()) argsMap.put(entry.getKey(), val.numberValue());
                        else if (val.isBoolean()) argsMap.put(entry.getKey(), val.booleanValue());
                        else argsMap.put(entry.getKey(), val.toString());
                    });
                }
                return AgentDecision.toolCall(toolName, argsMap);
            } else {
                throw new AgentException("Unsupported decision type: '" + typeStr + "'");
            }
        } catch (AgentException e) {
            throw e;
        } catch (Exception e) {
            throw new AgentException("Malformed agent decision JSON: " + e.getMessage(), e);
        }
    }
}
