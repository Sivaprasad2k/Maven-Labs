package com.shevay.oddlyspecific.server;

import com.shevay.oddlyspecific.session.Session;
import com.shevay.oddlyspecific.session.SessionManager;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class AdminHandlerTest {

    private SessionManager sessionManager;
    private AdminHandler adminHandler;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        adminHandler = new AdminHandler(sessionManager);
    }

    @Test
    void testGetActiveSessionsEmpty() throws Exception {
        TestHttpExchange exchange = new TestHttpExchange("/api/admin/sessions", "GET");
        adminHandler.handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        assertTrue(exchange.getResponseBodyAsString().contains("[]"));
    }

    @Test
    void testGetActiveSessionsWithSession() throws Exception {
        Session session = sessionManager.createSession("REACTION_TEST", "192.168.1.50");

        TestHttpExchange exchange = new TestHttpExchange("/api/admin/sessions", "GET");
        adminHandler.handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        String body = exchange.getResponseBodyAsString();
        assertTrue(body.contains(session.getSessionId()));
        assertTrue(body.contains("192.168.1.50"));
        assertTrue(body.contains("REACTION_TEST"));
    }

    @Test
    void testManualSessionExpirationAndGetExpiredSessions() throws Exception {
        Session session = sessionManager.createSession("DONT_CLICK", "10.0.0.5");

        // Manually expire session
        TestHttpExchange expireExchange = new TestHttpExchange("/api/admin/sessions/" + session.getSessionId() + "/expire", "POST");
        adminHandler.handle(expireExchange);
        assertEquals(200, expireExchange.getResponseCode());

        // Get Expired Sessions
        TestHttpExchange getExpiredExchange = new TestHttpExchange("/api/admin/sessions/expired", "GET");
        adminHandler.handle(getExpiredExchange);
        assertEquals(200, getExpiredExchange.getResponseCode());

        String body = getExpiredExchange.getResponseBodyAsString();
        assertTrue(body.contains(session.getSessionId()));
        assertTrue(body.contains("EXPIRED"));
    }

    @Test
    void testDeleteSession() throws Exception {
        Session session = sessionManager.createSession("MOVING_BUTTON", "172.16.0.2");

        TestHttpExchange deleteExchange = new TestHttpExchange("/api/admin/sessions/" + session.getSessionId(), "DELETE");
        adminHandler.handle(deleteExchange);

        assertEquals(200, deleteExchange.getResponseCode());
        assertTrue(sessionManager.getSession(session.getSessionId()).isEmpty());
    }

    private static class TestHttpExchange extends HttpExchange {
        private final String path;
        private final String method;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBodyStream = new ByteArrayOutputStream();
        private int responseCode = 200;

        public TestHttpExchange(String path, String method) {
            this.path = path;
            this.method = method;
        }

        public String getResponseBodyAsString() {
            return responseBodyStream.toString();
        }

        @Override public Headers getRequestHeaders() { return requestHeaders; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public URI getRequestURI() { return URI.create(path); }
        @Override public String getRequestMethod() { return method; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {}
        @Override public InputStream getRequestBody() { return InputStream.nullInputStream(); }
        @Override public OutputStream getResponseBody() { return responseBodyStream; }
        @Override public void sendResponseHeaders(int rCode, long responseLength) { this.responseCode = rCode; }
        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress("127.0.0.1", 12345); }
        @Override public int getResponseCode() { return responseCode; }
        @Override public InetSocketAddress getLocalAddress() { return null; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream i, OutputStream o) {}
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}
