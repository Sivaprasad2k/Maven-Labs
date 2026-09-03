package com.shevay.knowledge.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Deterministic AgentDecisionProvider implementation for offline unit testing and dry runs.
 */
public class DummyAgentDecisionProvider implements AgentDecisionProvider {

    private final List<AgentDecision> decisionSequence = new ArrayList<>();
    private Function<AgentContext, AgentDecision> decisionFunction;
    private int callCount = 0;

    public DummyAgentDecisionProvider() {
        this.decisionFunction = null;
    }

    public DummyAgentDecisionProvider(String defaultAnswer) {
        this.decisionSequence.add(AgentDecision.finalAnswer(defaultAnswer));
    }

    public DummyAgentDecisionProvider(List<AgentDecision> sequence) {
        if (sequence != null) {
            this.decisionSequence.addAll(sequence);
        }
    }

    public DummyAgentDecisionProvider(Function<AgentContext, AgentDecision> decisionFunction) {
        this.decisionFunction = Objects.requireNonNull(decisionFunction, "decisionFunction must not be null");
    }

    public void addDecision(AgentDecision decision) {
        this.decisionSequence.add(Objects.requireNonNull(decision, "decision must not be null"));
    }

    @Override
    public AgentDecision decide(AgentContext context) throws AgentException {
        callCount++;
        if (decisionFunction != null) {
            return decisionFunction.apply(context);
        }
        if (decisionSequence.isEmpty()) {
            return AgentDecision.finalAnswer("Default dummy agent final answer.");
        }
        int index = Math.min(callCount - 1, decisionSequence.size() - 1);
        return decisionSequence.get(index);
    }

    public int getCallCount() {
        return callCount;
    }
}
