package com.shevay.releaselab.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    public static final int DEFAULT_PORT = 8080;

    private final String appName;
    private final String appVersion;
    private final String appProfile;
    private final String appEnvironment;
    private final int port;

    public AppConfig() {
        this(System.getenv(), loadPropertiesFromClasspath());
    }

    public AppConfig(Map<String, String> env, Properties props) {
        this.port = parsePort(env);
        this.appName = props.getProperty("app.name", "maven-release-lab");
        this.appVersion = props.getProperty("app.version", "1.0.0");
        this.appProfile = props.getProperty("app.profile", "dev");
        this.appEnvironment = props.getProperty("app.environment", "development");
    }

    public AppConfig(int port, String appName, String appVersion, String appProfile, String appEnvironment) {
        this.port = validatePort(port);
        this.appName = appName;
        this.appVersion = appVersion;
        this.appProfile = appProfile;
        this.appEnvironment = appEnvironment;
    }

    public int getPort() {
        return port;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getAppProfile() {
        return appProfile;
    }

    public String getAppEnvironment() {
        return appEnvironment;
    }

    private static Properties loadPropertiesFromClasspath() {
        Properties props = new Properties();
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                LOGGER.warning("application.properties not found on classpath, using default configuration.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load application.properties", e);
        }
        return props;
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
