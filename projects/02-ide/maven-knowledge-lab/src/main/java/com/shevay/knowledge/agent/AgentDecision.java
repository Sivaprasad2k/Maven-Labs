package com.shevay.knowledge.agent;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a structured decision evaluated by the AgentDecisionProvider.
 *
 * @param type decision type (FINAL_ANSWER or TOOL_CALL)
 * @param answer final answer string (populated if type == FINAL_ANSWER)
 * @param toolCall tool call details (populated if type == TOOL_CALL)
 */
public record AgentDecision(AgentDecisionType type, String answer, ToolCall toolCall) {

    public AgentDecision {
        Objects.requireNonNull(type, "type must not be null");
        if (type == AgentDecisionType.FINAL_ANSWER) {
            Objects.requireNonNull(answer, "answer must not be null for FINAL_ANSWER");
        } else if (type == AgentDecisionType.TOOL_CALL) {
            Objects.requireNonNull(toolCall, "toolCall must not be null for TOOL_CALL");
        }
    }

    public static AgentDecision finalAnswer(String answer) {
        return new AgentDecision(AgentDecisionType.FINAL_ANSWER, answer, null);
    }

    public static AgentDecision toolCall(String toolName, Map<String, Object> arguments) {
        return new AgentDecision(AgentDecisionType.TOOL_CALL, null, new ToolCall(toolName, arguments));
    }
}
