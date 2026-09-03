package com.shevay.knowledge.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Ephemeral execution context for a single KnowledgeAgent invocation.
 * Holds user query, available tool descriptions, execution history, and iteration bounds.
 */
public class AgentContext {

    private final String userQuery;
    private final List<AgentTool> availableTools;
    private final List<AgentExecutionEntry> history;
    private final int maxIterations;

    public AgentContext(String userQuery, List<AgentTool> availableTools, int maxIterations) {
        this.userQuery = Objects.requireNonNull(userQuery, "userQuery must not be null").trim();
        if (this.userQuery.isBlank()) {
            throw new IllegalArgumentException("userQuery must not be blank");
        }
        this.availableTools = availableTools == null ? Collections.emptyList() : List.copyOf(availableTools);
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive (> 0)");
        }
        this.maxIterations = maxIterations;
        this.history = new ArrayList<>();
    }

    public String getUserQuery() {
        return userQuery;
    }

    public List<AgentTool> getAvailableTools() {
        return availableTools;
    }

    public List<AgentExecutionEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getCurrentIteration() {
        return history.size() + 1;
    }

    public void addEntry(AgentDecision decision, ToolResult result) {
        int step = history.size() + 1;
        history.add(new AgentExecutionEntry(step, decision, result));
    }
}
