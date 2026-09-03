package com.shevay.knowledge.agent;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a tool call requested by the LLM agent decision.
 *
 * @param tool name of the tool to execute
 * @param arguments key-value map of arguments passed to the tool
 */
public record ToolCall(String tool, Map<String, Object> arguments) {

    public ToolCall {
        Objects.requireNonNull(tool, "tool name must not be null");
        if (tool.isBlank()) {
            throw new IllegalArgumentException("tool name must not be blank");
        }
        arguments = arguments == null ? Collections.emptyMap() : Collections.unmodifiableMap(arguments);
    }
}
