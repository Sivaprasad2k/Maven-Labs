package com.shevay.monitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthResponse {

    private final String status;
    private final String timestamp;
    private final String service;

    public HealthResponse(
            @JsonProperty("status") String status,
            @JsonProperty("timestamp") String timestamp,
            @JsonProperty("service") String service) {
        this.status = status;
        this.timestamp = timestamp;
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getService() {
        return service;
    }
}
