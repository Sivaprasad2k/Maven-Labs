package com.shevay.monitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SystemInfo {

    private final String hostname;
    private final String osName;
    private final String osVersion;
    private final String architecture;
    private final String javaVersion;
    private final int availableProcessors;

    public SystemInfo(
            @JsonProperty("hostname") String hostname,
            @JsonProperty("osName") String osName,
            @JsonProperty("osVersion") String osVersion,
            @JsonProperty("architecture") String architecture,
            @JsonProperty("javaVersion") String javaVersion,
            @JsonProperty("availableProcessors") int availableProcessors) {
        this.hostname = hostname;
        this.osName = osName;
        this.osVersion = osVersion;
        this.architecture = architecture;
        this.javaVersion = javaVersion;
        this.availableProcessors = availableProcessors;
    }

    public String getHostname() {
        return hostname;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }
}
