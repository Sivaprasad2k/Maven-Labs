package com.shevay.knowledge.agent;

import java.util.Map;

/**
 * Interface contract for tools registered in the ToolRegistry and executable by the KnowledgeAgent.
 */
public interface AgentTool {

    /**
     * Unique identifier name for the tool (e.g., "searchKnowledge").
     *
     * @return tool name
     */
    String name();

    /**
     * Short description of the tool purpose and expected arguments for prompt construction.
     *
     * @return tool description
     */
    String description();

    /**
     * Executes the tool with the provided arguments.
     *
     * @param arguments map of argument keys to values
     * @return ToolResult containing output or error message
     */
    ToolResult execute(Map<String, Object> arguments);
}
