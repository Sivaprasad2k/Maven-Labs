package com.shevay.releaselab.service;

import com.shevay.releaselab.config.AppConfig;
import com.shevay.releaselab.model.BuildResponse;
import com.shevay.releaselab.model.HealthResponse;
import com.shevay.releaselab.model.InfoResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseLabServiceTest {

    private final ReleaseLabService service = new ReleaseLabService();

    @Test
    void testGetHealth() {
        HealthResponse health = service.getHealth();
        assertNotNull(health);
        assertEquals("UP", health.getStatus());
    }

    @Test
    void testGetInfo() {
        AppConfig config = new AppConfig(8080, "maven-release-lab", "1.0.0", "dev", "development");
        InfoResponse info = service.getInfo(config);

        assertNotNull(info);
        assertEquals("maven-release-lab", info.getApplication());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("development", info.getEnvironment());
        assertNotNull(info.getJavaVersion());
        assertNotNull(info.getOperatingSystem());
    }

    @Test
    void testGetBuild() {
        AppConfig config = new AppConfig(8080, "maven-release-lab", "1.0.0", "production", "production");
        BuildResponse build = service.getBuild(config);

        assertNotNull(build);
        assertEquals("maven-release-lab", build.getApplication());
        assertEquals("1.0.0", build.getVersion());
        assertEquals("production", build.getBuildProfile());
        assertEquals("production", build.getEnvironment());
    }
}
