package com.shevay.releaselab.config;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void testDefaultConfiguration() {
        AppConfig config = new AppConfig(null, new Properties());
        assertEquals(8080, config.getPort());
        assertEquals("maven-release-lab", config.getAppName());
        assertEquals("1.0.0", config.getAppVersion());
        assertEquals("dev", config.getAppProfile());
        assertEquals("development", config.getAppEnvironment());
    }

    @Test
    void testCustomProperties() {
        Properties props = new Properties();
        props.setProperty("app.name", "my-test-app");
        props.setProperty("app.version", "2.0.0");
        props.setProperty("app.profile", "production");
        props.setProperty("app.environment", "production");

        AppConfig config = new AppConfig(null, props);
        assertEquals("my-test-app", config.getAppName());
        assertEquals("2.0.0", config.getAppVersion());
        assertEquals("production", config.getAppProfile());
        assertEquals("production", config.getAppEnvironment());
    }

    @Test
    void testCustomPortFromEnvironment() {
        Map<String, String> env = Map.of("PORT", "9090");
        AppConfig config = new AppConfig(env, new Properties());
        assertEquals(9090, config.getPort());
    }

    @Test
    void testInvalidPortFallback() {
        Map<String, String> envInvalid = Map.of("PORT", "invalid");
        AppConfig configInvalid = new AppConfig(envInvalid, new Properties());
        assertEquals(8080, configInvalid.getPort());

        Map<String, String> envOutOfRange = Map.of("PORT", "999999");
        AppConfig configOutOfRange = new AppConfig(envOutOfRange, new Properties());
        assertEquals(8080, configOutOfRange.getPort());
    }
}
