package com.shevay.monitor.config;

import java.util.Map;

public class AppConfig {

    public static final int DEFAULT_PORT = 8080;

    private final int port;

    public AppConfig() {
        this(System.getenv());
    }

    public AppConfig(Map<String, String> env) {
        this.port = parsePort(env);
    }

    public AppConfig(int port) {
        this.port = validatePort(port);
    }

    public int getPort() {
        return port;
    }

    private static int parsePort(Map<String, String> env) {
        if (env == null) {
            return DEFAULT_PORT;
        }
        String portEnv = env.get("PORT");
        if (portEnv == null || portEnv.trim().isEmpty()) {
            return DEFAULT_PORT;
        }
        try {
            int parsed = Integer.parseInt(portEnv.trim());
            return validatePort(parsed);
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    private static int validatePort(int port) {
        if (port < 1 || port > 65535) {
            return DEFAULT_PORT;
        }
        return port;
    }
}
