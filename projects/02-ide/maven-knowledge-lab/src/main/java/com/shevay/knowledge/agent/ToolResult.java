package com.shevay.knowledge.agent;

import java.util.Objects;

/**
 * Represents the outcome of an executed AgentTool.
 *
 * @param success whether tool execution succeeded
 * @param toolName name of the executed tool
 * @param output controlled tool output string (if successful)
 * @param errorMessage controlled error message string (if failed)
 */
public record ToolResult(boolean success, String toolName, String output, String errorMessage) {

    public ToolResult {
        Objects.requireNonNull(toolName, "toolName must not be null");
        if (success) {
            output = output == null ? "" : output;
            errorMessage = null;
        } else {
            errorMessage = errorMessage == null ? "Tool execution failed" : errorMessage;
            output = null;
        }
    }

    public static ToolResult success(String toolName, String output) {
        return new ToolResult(true, toolName, output, null);
    }

    public static ToolResult failure(String toolName, String errorMessage) {
        return new ToolResult(false, toolName, null, errorMessage);
    }
}
