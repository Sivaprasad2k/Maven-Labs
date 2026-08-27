package com.shevay.monitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiskInfo {

    private final String path;
    private final long totalBytes;
    private final long freeBytes;
    private final long usableBytes;
    private final long usedBytes;

    public DiskInfo(
            @JsonProperty("path") String path,
            @JsonProperty("totalBytes") long totalBytes,
            @JsonProperty("freeBytes") long freeBytes,
            @JsonProperty("usableBytes") long usableBytes,
            @JsonProperty("usedBytes") long usedBytes) {
        this.path = path;
        this.totalBytes = totalBytes;
        this.freeBytes = freeBytes;
        this.usableBytes = usableBytes;
        this.usedBytes = usedBytes;
    }

    public String getPath() {
        return path;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getFreeBytes() {
        return freeBytes;
    }

    public long getUsableBytes() {
        return usableBytes;
    }

    public long getUsedBytes() {
        return usedBytes;
    }
}
