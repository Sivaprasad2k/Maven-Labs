package com.shevay.oddlyspecific.privacy;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpResolverTest {

    @Test
    void testResolveConnectionIpFromXForwardedFor() {
        TestHttpExchange exchange = new TestHttpExchange();
        exchange.getRequestHeaders().set("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");

        String ip = IpResolver.resolveConnectionIp(exchange);
        assertEquals("203.0.113.195", ip);
    }

    @Test
    void testResolveConnectionIpFromXRealIp() {
        TestHttpExchange exchange = new TestHttpExchange();
        exchange.getRequestHeaders().set("X-Real-IP", "198.51.100.1");

        String ip = IpResolver.resolveConnectionIp(exchange);
        assertEquals("198.51.100.1", ip);
    }

    @Test
    void testResolveConnectionIpFallbackToRemoteAddress() {
        TestHttpExchange exchange = new TestHttpExchange();
        exchange.setRemoteAddr(new InetSocketAddress("192.168.1.50", 54321));

        String ip = IpResolver.resolveConnectionIp(exchange);
        assertEquals("192.168.1.50", ip);
    }

    @Test
    void testResolveConnectionIpIPv6Loopback() {
        TestHttpExchange exchange = new TestHttpExchange();
        exchange.setRemoteAddr(new InetSocketAddress("0:0:0:0:0:0:0:1", 54321));

        String ip = IpResolver.resolveConnectionIp(exchange);
        assertEquals("127.0.0.1", ip);
    }

    // Lightweight mock-free test exchange stub
    private static class TestHttpExchange extends HttpExchange {
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 8080);

        public void setRemoteAddr(InetSocketAddress addr) {
            this.remoteAddress = addr;
        }

        @Override public Headers getRequestHeaders() { return requestHeaders; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public URI getRequestURI() { return URI.create("/"); }
        @Override public String getRequestMethod() { return "GET"; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {}
        @Override public InputStream getRequestBody() { return null; }
        @Override public OutputStream getResponseBody() { return null; }
        @Override public void sendResponseHeaders(int rCode, long responseLength) {}
        @Override public InetSocketAddress getRemoteAddress() { return remoteAddress; }
        @Override public int getResponseCode() { return 200; }
        @Override public InetSocketAddress getLocalAddress() { return null; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream i, OutputStream o) {}
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}
