package com.shevay.oddlyspecific.privacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IpGeolocationServiceTest {

    private IpGeolocationService ipGeolocationService;

    @BeforeEach
    void setUp() {
        ipGeolocationService = new IpGeolocationService();
    }

    @Test
    void testResolveIpLocationLoopbackIp() {
        IpGeolocationService.IpLocationResult result = ipGeolocationService.resolveIpLocation("127.0.0.1");
        assertNotNull(result);
        assertEquals("Localhost Environment", result.getCity());
        assertEquals("Local Network", result.getRegion());
        assertEquals("Internal Loopback", result.getCountry());
        assertEquals(0.0, result.getLatitude());
        assertEquals(0.0, result.getLongitude());
    }

    @Test
    void testResolveIpLocationPrivateIp() {
        IpGeolocationService.IpLocationResult result = ipGeolocationService.resolveIpLocation("192.168.1.10");
        assertNotNull(result);
        assertEquals("Localhost Environment", result.getCity());
    }

    @Test
    void testResolveIpLocationNullOrBlank() {
        IpGeolocationService.IpLocationResult result = ipGeolocationService.resolveIpLocation(null);
        assertNotNull(result);
        assertEquals("Localhost Environment", result.getCity());
    }
}
