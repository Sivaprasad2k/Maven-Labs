package com.shevay.knowledge.agent;

import com.shevay.knowledge.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeAgentTest {

    private ToolRegistry toolRegistry;

    private static class CounterTool implements AgentTool {
        private final AtomicInteger count = new AtomicInteger(0);

        @Override public String name() { return "counterTool"; }
        @Override public String description() { return "Counts invocations."; }
        @Override
        public ToolResult execute(Map<String, Object> arguments) {
            count.incrementAndGet();
            return ToolResult.success("counterTool", "Count: " + count.get());
        }

        public int getCount() { return count.get(); }
    }

    @BeforeEach
    void setUp() {
        toolRegistry = new ToolRegistry();
        toolRegistry.register(new CounterTool());
    }

    @Test
    @DisplayName("1 & 14. Direct final answer returns immediately without executing any tools")
    void testDirectFinalAnswer() {
        CounterTool counter = (CounterTool) toolRegistry.getTool("counterTool").get();
        DummyAgentDecisionProvider decisionProvider = new DummyAgentDecisionProvider("Maven dependency scopes are compile, provided, runtime, test, system, import.");
        KnowledgeAgent agent = new KnowledgeAgent(toolRegistry, decisionProvider);

        String answer = agent.execute("Explain Maven dependency scopes");
        assertEquals("Maven dependency scopes are compile, provided, runtime, test, system, import.", answer);
        assertEquals(0, counter.getCount(), "No tool must execute when final answer is selected");
    }

    @Test
    @DisplayName("2. One tool call followed by final answer")
    void testOneToolCallFollowedByFinalAnswer() {
        CounterTool counter = (CounterTool) toolRegistry.getTool("counterTool").get();
        List<AgentDecision> decisions = List.of(
                AgentDecision.toolCall("counterTool", Map.of()),
                AgentDecision.finalAnswer("Final answer after counter tool.")
        );
        DummyAgentDecisionProvider decisionProvider = new DummyAgentDecisionProvider(decisions);
        KnowledgeAgent agent = new KnowledgeAgent(toolRegistry, decisionProvider);

        String answer = agent.execute("Test query");
        assertEquals("Final answer after counter tool.", answer);
        assertEquals(1, counter.getCount());
    }

    @Test
    @DisplayName("3 & 17. Multiple tool calls preserve execution history correctly")
    void testMultipleToolCallsPreserveHistory() {
        CounterTool counter = (CounterTool) toolRegistry.getTool("counterTool").get();
        List<AgentDecision> decisions = List.of(
                AgentDecision.toolCall("counterTool", Map.of("step", 1)),
                AgentDecision.toolCall("counterTool", Map.of("step", 2)),
                AgentDecision.finalAnswer("Finished 2 tool steps.")
        );
        DummyAgentDecisionProvider decisionProvider = new DummyAgentDecisionProvider(decisions);
        KnowledgeAgent agent = new KnowledgeAgent(toolRegistry, decisionProvider);

        String answer = agent.execute("Multi-step query");
        assertEquals("Finished 2 tool steps.", answer);
        assertEquals(2, counter.getCount());
    }

    @Test
    @DisplayName("7 & 11. Unknown tool rejected as ToolResult failure without crashing agent")
    void testUnknownToolRejectionHandledGracefully() {
        List<AgentDecision> decisions = List.of(
                AgentDecision.toolCall("unauthorizedTool", Map.of()),
                AgentDecision.finalAnswer("Recovered after unauthorized tool rejection.")
        );
        DummyAgentDecisionProvider decisionProvider = new DummyAgentDecisionProvider(decisions);
        KnowledgeAgent agent = new KnowledgeAgent(toolRegistry, decisionProvider);

        String answer = agent.execute("Run forbidden command");
        assertEquals("Recovered after unauthorized tool rejection.", answer);
    }

    @Test
    @DisplayName("12. Maximum iteration limit enforced when decision loop does not produce final answer")
    void testMaximumIterationLimitEnforced() {
        // Always return a tool call decision
        DummyAgentDecisionProvider decisionProvider = new DummyAgentDecisionProvider(
                ctx -> AgentDecision.toolCall("counterTool", Map.of())
        );
        KnowledgeAgent agent = new KnowledgeAgent(toolRegistry, decisionProvider, 3);

        String answer = agent.execute("Infinite loop test");
        assertTrue(answer.contains("Agent stopped after reaching maximum iteration limit of 3"));
    }

    @Test
    @DisplayName("18. No persistent agent state across separate CLI executions")
    void testNoPersistentStateBetweenExecutions() {
        DummyAgentDecisionProvider decisionProvider = new DummyAgentDecisionProvider("Independent answer.");
        KnowledgeAgent agent = new KnowledgeAgent(toolRegistry, decisionProvider);

        String answer1 = agent.execute("Query 1");
        String answer2 = agent.execute("Query 2");

        assertEquals("Independent answer.", answer1);
        assertEquals("Independent answer.", answer2);
    }

    @Test
    @DisplayName("Security: Malformed API key error messages are sanitized without exposing credentials")
    void testErrorSanitizationNoApiKeyLeak() {
        AgentDecisionProvider failingProvider = ctx -> {
            throw new AgentException("Gemini API Error - GEMINI_API_KEY=secret_key_12345");
        };
        KnowledgeAgent agent = new KnowledgeAgent(toolRegistry, failingProvider);

        String answer = agent.execute("Test error leak");
        assertTrue(answer.contains("Agent failed to evaluate decision"));
        assertFalse(answer.contains("secret_key_12345"), "API keys must NEVER be leaked in error responses");
    }
}
