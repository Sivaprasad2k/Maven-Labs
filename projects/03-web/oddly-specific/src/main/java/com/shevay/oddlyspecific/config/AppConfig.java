package com.shevay.oddlyspecific.config;

public class AppConfig {
    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final int DEFAULT_PORT = 8080;
    public static final String WEB_RESOURCE_ROOT = "/web";

    public static int getPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid PORT environment variable: " + envPort + ". Falling back to " + DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }
}
