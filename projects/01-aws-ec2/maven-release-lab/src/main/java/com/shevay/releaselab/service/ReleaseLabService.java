package com.shevay.releaselab.service;

import com.shevay.releaselab.config.AppConfig;
import com.shevay.releaselab.model.BuildResponse;
import com.shevay.releaselab.model.HealthResponse;
import com.shevay.releaselab.model.InfoResponse;

public class ReleaseLabService {

    public HealthResponse getHealth() {
        return new HealthResponse("UP");
    }

    public InfoResponse getInfo(AppConfig config) {
        String appName = config.getAppName();
        String appVersion = config.getAppVersion();
        String environment = config.getAppEnvironment();
        String javaVersion = System.getProperty("java.version", "Unknown");
        String operatingSystem = System.getProperty("os.name", "Unknown");

        return new InfoResponse(appName, appVersion, environment, javaVersion, operatingSystem);
    }

    public BuildResponse getBuild(AppConfig config) {
        String appName = config.getAppName();
        String appVersion = config.getAppVersion();
        String buildProfile = config.getAppProfile();
        String environment = config.getAppEnvironment();

        return new BuildResponse(appName, appVersion, buildProfile, environment);
    }
}
