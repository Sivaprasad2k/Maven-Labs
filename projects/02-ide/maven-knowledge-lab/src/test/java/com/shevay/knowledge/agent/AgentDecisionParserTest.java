package com.shevay.knowledge.agent;

import com.shevay.knowledge.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentDecisionParserTest {

    private GeminiAgentDecisionProvider provider;

    @BeforeEach
    void setUp() {
        provider = new GeminiAgentDecisionProvider(AppConfig.loadDefaults());
    }

    @Test
    @DisplayName("1. Parse valid final_answer JSON")
    void testParseValidFinalAnswer() {
        String json = "{\"type\": \"final_answer\", \"answer\": \"Maven lifecycle has 3 built-in lifecycles.\"}";
        AgentDecision decision = provider.parseAgentDecision(json);

        assertEquals(AgentDecisionType.FINAL_ANSWER, decision.type());
        assertEquals("Maven lifecycle has 3 built-in lifecycles.", decision.answer());
        assertNull(decision.toolCall());
    }

    @Test
    @DisplayName("2. Parse valid tool_call JSON")
    void testParseValidToolCall() {
        String json = "{\"type\": \"tool_call\", \"tool\": \"searchKnowledge\", \"arguments\": {\"query\": \"Maven lifecycle\"}}";
        AgentDecision decision = provider.parseAgentDecision(json);

        assertEquals(AgentDecisionType.TOOL_CALL, decision.type());
        assertNull(decision.answer());
        assertNotNull(decision.toolCall());
        assertEquals("searchKnowledge", decision.toolCall().tool());
        assertEquals("Maven lifecycle", decision.toolCall().arguments().get("query"));
    }

    @Test
    @DisplayName("3. Parse Markdown fenced JSON decision")
    void testParseMarkdownFencedJson() {
        String json = "```json\n{\"type\": \"final_answer\", \"answer\": \"Answer inside code block.\"}\n```";
        AgentDecision decision = provider.parseAgentDecision(json);

        assertEquals(AgentDecisionType.FINAL_ANSWER, decision.type());
        assertEquals("Answer inside code block.", decision.answer());
    }

    @Test
    @DisplayName("9. Malformed agent decision JSON should throw AgentException")
    void testMalformedJsonThrowsException() {
        String badJson = "{type: final_answer";
        assertThrows(AgentException.class, () -> provider.parseAgentDecision(badJson));
    }

    @Test
    @DisplayName("10. Missing decision type should throw AgentException")
    void testMissingTypeThrowsException() {
        String missingTypeJson = "{\"answer\": \"No type specified\"}";
        assertThrows(AgentException.class, () -> provider.parseAgentDecision(missingTypeJson));
    }

    @Test
    @DisplayName("Tool call without tool name should throw AgentException")
    void testMissingToolNameThrowsException() {
        String missingToolJson = "{\"type\": \"tool_call\", \"arguments\": {}}";
        assertThrows(AgentException.class, () -> provider.parseAgentDecision(missingToolJson));
    }

    @Test
    @DisplayName("Final answer without answer field should throw AgentException")
    void testMissingAnswerFieldThrowsException() {
        String missingAnswerJson = "{\"type\": \"final_answer\"}";
        assertThrows(AgentException.class, () -> provider.parseAgentDecision(missingAnswerJson));
    }

    @Test
    @DisplayName("Unsupported decision type should throw AgentException")
    void testUnsupportedDecisionTypeThrowsException() {
        String unknownTypeJson = "{\"type\": \"execute_shell\"}";
        assertThrows(AgentException.class, () -> provider.parseAgentDecision(unknownTypeJson));
    }
}
