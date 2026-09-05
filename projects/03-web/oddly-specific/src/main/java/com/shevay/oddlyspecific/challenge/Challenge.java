package com.shevay.oddlyspecific.challenge;

import java.util.Map;

public class Challenge {
    private final String id;
    private final String title;
    private final String instructions;
    private final String type;
    private final Map<String, Object> config;

    public Challenge(String id, String title, String instructions, String type, Map<String, Object> config) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
        this.type = type;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getConfig() {
        return config;
    }
}
