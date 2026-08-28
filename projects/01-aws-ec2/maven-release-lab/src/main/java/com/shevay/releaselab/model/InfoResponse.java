package com.shevay.releaselab.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InfoResponse {

    private final String application;
    private final String version;
    private final String environment;
    private final String javaVersion;
    private final String operatingSystem;

    public InfoResponse(
            @JsonProperty("application") String application,
            @JsonProperty("version") String version,
            @JsonProperty("environment") String environment,
            @JsonProperty("javaVersion") String javaVersion,
            @JsonProperty("operatingSystem") String operatingSystem) {
        this.application = application;
        this.version = version;
        this.environment = environment;
        this.javaVersion = javaVersion;
        this.operatingSystem = operatingSystem;
    }

    public String getApplication() {
        return application;
    }

    public String getVersion() {
        return version;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }
}
