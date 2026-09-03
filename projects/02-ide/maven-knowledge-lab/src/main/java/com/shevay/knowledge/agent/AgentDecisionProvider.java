package com.shevay.knowledge.agent;

/**
 * Interface contract for evaluating an AgentContext and deciding the next AgentDecision.
 */
public interface AgentDecisionProvider {

    /**
     * Evaluates current execution context and returns the next AgentDecision (TOOL_CALL or FINAL_ANSWER).
     *
     * @param context current agent context
     * @return evaluated AgentDecision
     * @throws AgentException if evaluation or LLM communication fails
     */
    AgentDecision decide(AgentContext context) throws AgentException;
}
