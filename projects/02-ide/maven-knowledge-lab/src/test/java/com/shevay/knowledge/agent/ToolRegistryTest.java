package com.shevay.knowledge.agent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ToolRegistryTest {

    private static class DummyTool implements AgentTool {
        private final String name;

        DummyTool(String name) {
            this.name = name;
        }

        @Override public String name() { return name; }
        @Override public String description() { return "Dummy description for " + name; }
        @Override public ToolResult execute(Map<String, Object> args) { return ToolResult.success(name, "OK"); }
    }

    @Test
    @DisplayName("15. ToolRegistry only exposes explicitly registered tools")
    void testRegisterAndLookup() {
        ToolRegistry registry = new ToolRegistry();
        DummyTool tool1 = new DummyTool("tool1");
        registry.register(tool1);

        assertTrue(registry.isRegistered("tool1"));
        Optional<AgentTool> found = registry.getTool("tool1");
        assertTrue(found.isPresent());
        assertEquals("tool1", found.get().name());

        assertFalse(registry.isRegistered("deleteEverything"));
        assertTrue(registry.getTool("deleteEverything").isEmpty());
    }

    @Test
    @DisplayName("7. Unknown/unregistered tools are rejected")
    void testUnregisteredToolLookupReturnsEmpty() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(new DummyTool("searchKnowledge"));

        assertTrue(registry.getTool("execProcess").isEmpty());
        assertTrue(registry.getTool("").isEmpty());
        assertTrue(registry.getTool(null).isEmpty());
    }

    @Test
    @DisplayName("16. Tool descriptions are included in tool registry output")
    void testGetToolDescriptionsFormat() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(new DummyTool("t1"));
        registry.register(new DummyTool("t2"));

        String desc = registry.getToolDescriptions();
        assertTrue(desc.contains("t1"));
        assertTrue(desc.contains("Dummy description for t1"));
        assertTrue(desc.contains("t2"));
        assertTrue(desc.contains("Dummy description for t2"));
    }
}
