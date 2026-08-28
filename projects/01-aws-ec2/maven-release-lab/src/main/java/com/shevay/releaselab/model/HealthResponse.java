package com.shevay.releaselab.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthResponse {

    private final String status;

    public HealthResponse(@JsonProperty("status") String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
