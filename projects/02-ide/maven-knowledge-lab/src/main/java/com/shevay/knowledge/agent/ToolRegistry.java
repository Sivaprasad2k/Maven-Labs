package com.shevay.knowledge.agent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registry maintaining an explicit allowlist of AgentTool instances.
 * Rejects unregistered tools and dynamic reflection calls.
 */
public class ToolRegistry {

    private final Map<String, AgentTool> registeredTools = new LinkedHashMap<>();

    public ToolRegistry() {}

    public ToolRegistry(List<AgentTool> tools) {
        if (tools != null) {
            tools.forEach(this::register);
        }
    }

    public void register(AgentTool tool) {
        Objects.requireNonNull(tool, "tool must not be null");
        Objects.requireNonNull(tool.name(), "tool name must not be null");
        if (tool.name().isBlank()) {
            throw new IllegalArgumentException("tool name must not be blank");
        }
        registeredTools.put(tool.name(), tool);
    }

    public Optional<AgentTool> getTool(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(registeredTools.get(name.trim()));
    }

    public List<AgentTool> getTools() {
        return List.copyOf(registeredTools.values());
    }

    public boolean isRegistered(String name) {
        return name != null && registeredTools.containsKey(name.trim());
    }

    public String getToolDescriptions() {
        if (registeredTools.isEmpty()) {
            return "No tools available.";
        }
        return registeredTools.values().stream()
                .map(t -> "- Tool: " + t.name() + "\n  Description: " + t.description())
                .collect(Collectors.joining("\n"));
    }
}
