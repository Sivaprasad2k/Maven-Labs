package com.shevay.monitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceMetrics {

    private final double systemCpuLoad;
    private final double systemLoadAverage;
    private final long jvmUsedMemoryBytes;
    private final long jvmCommittedMemoryBytes;
    private final long jvmMaxMemoryBytes;
    private final long pid;
    private final long processUptimeSeconds;

    public ResourceMetrics(
            @JsonProperty("systemCpuLoad") double systemCpuLoad,
            @JsonProperty("systemLoadAverage") double systemLoadAverage,
            @JsonProperty("jvmUsedMemoryBytes") long jvmUsedMemoryBytes,
            @JsonProperty("jvmCommittedMemoryBytes") long jvmCommittedMemoryBytes,
            @JsonProperty("jvmMaxMemoryBytes") long jvmMaxMemoryBytes,
            @JsonProperty("pid") long pid,
            @JsonProperty("processUptimeSeconds") long processUptimeSeconds) {
        this.systemCpuLoad = systemCpuLoad;
        this.systemLoadAverage = systemLoadAverage;
        this.jvmUsedMemoryBytes = jvmUsedMemoryBytes;
        this.jvmCommittedMemoryBytes = jvmCommittedMemoryBytes;
        this.jvmMaxMemoryBytes = jvmMaxMemoryBytes;
        this.pid = pid;
        this.processUptimeSeconds = processUptimeSeconds;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public long getJvmUsedMemoryBytes() {
        return jvmUsedMemoryBytes;
    }

    public long getJvmCommittedMemoryBytes() {
        return jvmCommittedMemoryBytes;
    }

    public long getJvmMaxMemoryBytes() {
        return jvmMaxMemoryBytes;
    }

    public long getPid() {
        return pid;
    }

    public long getProcessUptimeSeconds() {
        return processUptimeSeconds;
    }
}
