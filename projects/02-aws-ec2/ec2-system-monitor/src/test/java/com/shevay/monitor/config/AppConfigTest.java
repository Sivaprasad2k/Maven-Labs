package com.shevay.monitor.config;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AppConfigTest {

    @Test
    public void testDefaultPortWhenNoEnvVariable() {
        AppConfig config = new AppConfig(Collections.emptyMap());
        assertEquals(8080, config.getPort());
    }

    @Test
    public void testCustomPortFromEnvVariable() {
        Map<String, String> env = Map.of("PORT", "9090");
        AppConfig config = new AppConfig(env);
        assertEquals(9090, config.getPort());
    }

    @Test
    public void testInvalidPortFallbackToDefault() {
        Map<String, String> env = Map.of("PORT", "not-a-number");
        AppConfig config = new AppConfig(env);
        assertEquals(8080, config.getPort());
    }

    @Test
    public void testOutOfRangePortFallbackToDefault() {
        Map<String, String> env = Map.of("PORT", "70000");
        AppConfig config = new AppConfig(env);
        assertEquals(8080, config.getPort());
    }

    @Test
    public void testExplicitPortConstructor() {
        AppConfig config = new AppConfig(9090);
        assertEquals(9090, config.getPort());
    }
}
