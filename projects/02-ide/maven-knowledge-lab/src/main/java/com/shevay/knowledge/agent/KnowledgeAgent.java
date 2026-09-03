package com.shevay.knowledge.agent;

import java.util.Objects;
import java.util.Optional;

/**
 * Orchestrator implementing a bounded observe-decide-act loop for controlled knowledge agent execution.
 * Operates strictly via allowlisted tools in ToolRegistry.
 */
public class KnowledgeAgent {

    public static final int DEFAULT_MAX_ITERATIONS = 3;

    private final ToolRegistry toolRegistry;
    private final AgentDecisionProvider decisionProvider;
    private final int maxIterations;

    public KnowledgeAgent(ToolRegistry toolRegistry, AgentDecisionProvider decisionProvider) {
        this(toolRegistry, decisionProvider, DEFAULT_MAX_ITERATIONS);
    }

    public KnowledgeAgent(ToolRegistry toolRegistry, AgentDecisionProvider decisionProvider, int maxIterations) {
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "toolRegistry must not be null");
        this.decisionProvider = Objects.requireNonNull(decisionProvider, "decisionProvider must not be null");
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive (> 0), got: " + maxIterations);
        }
        this.maxIterations = maxIterations;
    }

    /**
     * Executes the agent loop for the given user query.
     *
     * @param userQuery input question or command string
     * @return final answer string or controlled fallback message
     */
    public String execute(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return "User query must not be blank.";
        }
        AgentContext context = new AgentContext(userQuery, toolRegistry.getTools(), maxIterations);
        return execute(context);
    }

    /**
     * Executes the agent loop for the given AgentContext.
     *
     * @param context agent context holding query, tool definitions, and history
     * @return final answer string or controlled fallback message
     */
    public String execute(AgentContext context) {
        if (context == null || context.getUserQuery().isBlank()) {
            return "User query must not be blank.";
        }

        for (int i = 0; i < maxIterations; i++) {
            AgentDecision decision;
            try {
                decision = decisionProvider.decide(context);
            } catch (Exception e) {
                return "Agent failed to evaluate decision: " + sanitizeError(e.getMessage());
            }

            if (decision == null) {
                return "Agent received null decision from provider.";
            }

            if (decision.type() == AgentDecisionType.FINAL_ANSWER) {
                // Return final answer immediately without executing any tools
                context.addEntry(decision, null);
                return decision.answer();
            }

            if (decision.type() == AgentDecisionType.TOOL_CALL) {
                ToolCall call = decision.toolCall();
                Optional<AgentTool> toolOpt = toolRegistry.getTool(call.tool());

                ToolResult result;
                if (toolOpt.isEmpty()) {
                    result = ToolResult.failure(call.tool(), "Tool '" + call.tool() + "' is not registered in the allowlist.");
                } else {
                    try {
                        result = toolOpt.get().execute(call.arguments());
                    } catch (Exception e) {
                        result = ToolResult.failure(call.tool(), "Exception during tool execution: " + sanitizeError(e.getMessage()));
                    }
                }

                context.addEntry(decision, result);
            }
        }

        return "Agent stopped after reaching maximum iteration limit of " + maxIterations + " without producing a final answer.";
    }

    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }

    public AgentDecisionProvider getDecisionProvider() {
        return decisionProvider;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    private static String sanitizeError(String msg) {
        if (msg == null || msg.isBlank()) return "Unknown error";
        if (msg.contains("GEMINI_API_KEY") || msg.contains("x-goog-api-key")) {
            return "API key authentication failure";
        }
        return msg;
    }
}
