package com.shevay.monitor.service;

import com.shevay.monitor.model.DiskInfo;
import com.shevay.monitor.model.HealthResponse;
import com.shevay.monitor.model.ResourceMetrics;
import com.shevay.monitor.model.SystemInfo;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class SystemMonitorService {

    public HealthResponse getHealth(String serviceName) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        return new HealthResponse("UP", timestamp, serviceName);
    }

    public SystemInfo getSystemInfo() {
        String hostname = resolveHostname();
        String osName = System.getProperty("os.name", "Unknown");
        String osVersion = System.getProperty("os.version", "Unknown");
        String architecture = System.getProperty("os.arch", "Unknown");
        String javaVersion = System.getProperty("java.version", "Unknown");
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        return new SystemInfo(hostname, osName, osVersion, architecture, javaVersion, availableProcessors);
    }

    public ResourceMetrics getResourceMetrics() {
        Runtime runtime = Runtime.getRuntime();
        long committedMemoryBytes = runtime.totalMemory();
        long freeJvmMemoryBytes = runtime.freeMemory();
        long usedMemoryBytes = committedMemoryBytes - freeJvmMemoryBytes;
        long maxMemoryBytes = runtime.maxMemory();

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double systemLoadAverage = osBean.getSystemLoadAverage();
        double systemCpuLoad = resolveSystemCpuLoad(osBean);

        ProcessHandle processHandle = ProcessHandle.current();
        long pid = processHandle.pid();
        long processUptimeSeconds = processHandle.info()
                .startInstant()
                .map(start -> Duration.between(start, Instant.now()).toSeconds())
                .orElse(0L);

        return new ResourceMetrics(
                systemCpuLoad,
                systemLoadAverage,
                usedMemoryBytes,
                committedMemoryBytes,
                maxMemoryBytes,
                pid,
                processUptimeSeconds
        );
    }

    public DiskInfo getDiskInfo() {
        File applicationDirectory = new File(".");
        String absolutePath = applicationDirectory.getAbsolutePath();

        long totalBytes = applicationDirectory.getTotalSpace();
        long freeBytes = applicationDirectory.getFreeSpace();
        long usableBytes = applicationDirectory.getUsableSpace();
        long usedBytes = totalBytes - freeBytes;

        return new DiskInfo(absolutePath, totalBytes, freeBytes, usableBytes, usedBytes);
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            String envHost = System.getenv("HOSTNAME");
            if (envHost != null && !envHost.isBlank()) {
                return envHost;
            }
            return "unknown-host";
        }
    }

    private double resolveSystemCpuLoad(OperatingSystemMXBean osBean) {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            return sunOsBean.getCpuLoad();
        }
        return -1.0;
    }
}
