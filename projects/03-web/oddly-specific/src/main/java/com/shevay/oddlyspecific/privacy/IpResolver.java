package com.shevay.oddlyspecific.privacy;

import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;

public class IpResolver {

    /**
     * Resolves the "Connection IP" associated with the HTTP connection.
     * Note: Per specification, this is explicitly called "Connection IP",
     * never "Real IP", "Original IP", or "Actual IP".
     */
    public static String resolveConnectionIp(HttpExchange exchange) {
        if (exchange == null) {
            return "127.0.0.1";
        }

        // Check proxy headers (e.g. Railway / Cloudflare / Nginx reverse proxy)
        String xForwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] ips = xForwardedFor.split(",");
            String firstIp = ips[0].trim();
            if (!firstIp.isEmpty()) {
                return sanitizeIp(firstIp);
            }
        }

        String xRealIp = exchange.getRequestHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return sanitizeIp(xRealIp.trim());
        }

        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            String hostAddress = remoteAddress.getAddress().getHostAddress();
            return sanitizeIp(hostAddress);
        }

        return "127.0.0.1";
    }

    private static String sanitizeIp(String rawIp) {
        if (rawIp == null || rawIp.isBlank()) {
            return "127.0.0.1";
        }
        // Normalize IPv6 loopback
        if ("0:0:0:0:0:0:0:1".equals(rawIp) || "::1".equals(rawIp)) {
            return "127.0.0.1";
        }
        // If IPv6 link-local with scope id (e.g. fe80::1%12)
        int percentIdx = rawIp.indexOf('%');
        if (percentIdx > 0) {
            return rawIp.substring(0, percentIdx);
        }
        return rawIp;
    }
}
