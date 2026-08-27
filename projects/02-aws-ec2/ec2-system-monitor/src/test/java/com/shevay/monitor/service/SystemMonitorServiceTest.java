package com.shevay.monitor.service;

import com.shevay.monitor.model.DiskInfo;
import com.shevay.monitor.model.HealthResponse;
import com.shevay.monitor.model.ResourceMetrics;
import com.shevay.monitor.model.SystemInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemMonitorServiceTest {

    private SystemMonitorService service;

    @BeforeEach
    public void setUp() {
        service = new SystemMonitorService();
    }

    @Test
    public void testGetHealthReturnsUpStatus() {
        HealthResponse health = service.getHealth("ec2-system-monitor");

        assertNotNull(health);
        assertEquals("UP", health.getStatus());
        assertEquals("ec2-system-monitor", health.getService());
        assertNotNull(health.getTimestamp());
        assertFalse(health.getTimestamp().isBlank());
    }

    @Test
    public void testGetSystemInfoReturnsEnvironmentProperties() {
        SystemInfo info = service.getSystemInfo();

        assertNotNull(info);
        assertNotNull(info.getHostname());
        assertFalse(info.getHostname().isBlank());
        assertNotNull(info.getOsName());
        assertFalse(info.getOsName().isBlank());
        assertNotNull(info.getOsVersion());
        assertNotNull(info.getArchitecture());
        assertNotNull(info.getJavaVersion());
        assertTrue(info.getAvailableProcessors() > 0);
    }

    @Test
    public void testGetResourceMetricsReturnsValidJvmAndProcessData() {
        ResourceMetrics metrics = service.getResourceMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.getPid() > 0);
        assertTrue(metrics.getProcessUptimeSeconds() >= 0);
        assertTrue(metrics.getJvmUsedMemoryBytes() >= 0);
        assertTrue(metrics.getJvmCommittedMemoryBytes() > 0);
        assertTrue(metrics.getJvmMaxMemoryBytes() > 0);
    }

    @Test
    public void testGetDiskInfoReturnsValidDiskSpaceMetrics() {
        DiskInfo diskInfo = service.getDiskInfo();

        assertNotNull(diskInfo);
        assertNotNull(diskInfo.getPath());
        assertTrue(diskInfo.getTotalBytes() > 0);
        assertTrue(diskInfo.getFreeBytes() >= 0);
        assertTrue(diskInfo.getUsableBytes() >= 0);
        assertTrue(diskInfo.getUsedBytes() >= 0);
        assertTrue(diskInfo.getTotalBytes() >= diskInfo.getFreeBytes());
    }
}
