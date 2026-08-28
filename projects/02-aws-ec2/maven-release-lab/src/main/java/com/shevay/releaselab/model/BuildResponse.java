package com.shevay.releaselab.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BuildResponse {

    private final String application;
    private final String version;
    private final String buildProfile;
    private final String environment;

    public BuildResponse(
            @JsonProperty("application") String application,
            @JsonProperty("version") String version,
            @JsonProperty("buildProfile") String buildProfile,
            @JsonProperty("environment") String environment) {
        this.application = application;
        this.version = version;
        this.buildProfile = buildProfile;
        this.environment = environment;
    }

    public String getApplication() {
        return application;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildProfile() {
        return buildProfile;
    }

    public String getEnvironment() {
        return environment;
    }
}
