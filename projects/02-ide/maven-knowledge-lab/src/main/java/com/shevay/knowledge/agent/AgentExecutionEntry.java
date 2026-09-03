package com.shevay.knowledge.agent;

import java.util.Objects;

/**
 * Single step entry in the agent context execution history.
 *
 * @param step 1-based step index
 * @param decision decision evaluated for this step
 * @param toolResult result of tool execution (null if FINAL_ANSWER)
 */
public record AgentExecutionEntry(int step, AgentDecision decision, ToolResult toolResult) {

    public AgentExecutionEntry {
        if (step <= 0) {
            throw new IllegalArgumentException("step must be positive (> 0)");
        }
        Objects.requireNonNull(decision, "decision must not be null");
    }
}
