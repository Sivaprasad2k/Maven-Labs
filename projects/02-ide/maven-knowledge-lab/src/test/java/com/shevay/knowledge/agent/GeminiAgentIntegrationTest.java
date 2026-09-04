package com.shevay.knowledge.agent;

import com.shevay.knowledge.config.AppConfig;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Opt-in live integration test against Google Gemini Agent Decision Provider.
 *
 * <p>To execute this test:
 * <code>.\mvnw.cmd test -Dgemini.integration=true</code>
 * (Requires GEMINI_API_KEY environment variable to be set)</p>
 */
@EnabledIfSystemProperty(named = "gemini.integration", matches = "true")
class GeminiAgentIntegrationTest {

    @Test
    @DisplayName("Live Gemini Agent Decision Provider Integration Test (Opt-in)")
    void testLiveGeminiAgentDecision() {
        AppConfig config = AppConfig.loadDefaults();
        String apiKey = config.getGeminiApiKey();

        // Skip test if GEMINI_API_KEY environment variable is missing or blank
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "Skipping live Gemini agent integration test because GEMINI_API_KEY is not set.");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new AgentTool() {
            @Override public String name() { return "searchKnowledge"; }
            @Override public String description() { return "Searches knowledge store. Arguments: {\"query\": \"<search string>\"}"; }
            @Override public ToolResult execute(java.util.Map<String, Object> args) { return ToolResult.success("searchKnowledge", "Maven lifecycle consists of validate, compile, test, package."); }
        });

        GeminiAgentDecisionProvider provider = new GeminiAgentDecisionProvider(config);
        AgentContext context = new AgentContext("What is the Maven lifecycle?", registry.getTools(), 3);

        AgentDecision decision = provider.decide(context);
        assertNotNull(decision, "Gemini decision must not be null");
        assertNotNull(decision.type(), "Gemini decision type must not be null");
    }
}
