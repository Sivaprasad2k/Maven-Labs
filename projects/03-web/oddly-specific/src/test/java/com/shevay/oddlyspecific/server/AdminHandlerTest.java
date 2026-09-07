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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AdminHandlerTest {

    private SessionManager sessionManager;
    private AdminHandler adminHandler;
    private static final String TEST_USER = "testadmin";
    private static final String TEST_PASS = "testsecret123";

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        adminHandler = new AdminHandler(sessionManager, TEST_USER, TEST_PASS, true);
    }

    private static String basicAuth(String username, String password) {
        String pair = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testAdminDisabledReturns403() throws Exception {
        AdminHandler disabledHandler = new AdminHandler(sessionManager, TEST_USER, TEST_PASS, false);
        TestHttpExchange exchange = new TestHttpExchange("/admin", "GET");
        disabledHandler.handle(exchange);

        assertEquals(403, exchange.getResponseCode());
        assertTrue(exchange.getResponseBodyAsString().contains("Admin Console is disabled"));
    }

    @Test
    void testAdminEnabledWithoutServerCredentialsReturns401() throws Exception {
        AdminHandler unconfiguredHandler = new AdminHandler(sessionManager, null, null, true);
        TestHttpExchange exchange = new TestHttpExchange("/api/admin/sessions", "GET");
        unconfiguredHandler.handle(exchange);

        assertEquals(401, exchange.getResponseCode());
        assertEquals("Basic realm=\"Oddly Specific Admin Console\"", exchange.getResponseHeaders().getFirst("WWW-Authenticate"));
    }

    @Test
    void testAdminEnabledMissingAuthHeaderReturns401() throws Exception {
        TestHttpExchange exchange = new TestHttpExchange("/api/admin/sessions", "GET");
        adminHandler.handle(exchange);

        assertEquals(401, exchange.getResponseCode());
        assertEquals("Basic realm=\"Oddly Specific Admin Console\"", exchange.getResponseHeaders().getFirst("WWW-Authenticate"));
        assertTrue(exchange.getResponseBodyAsString().contains("Unauthorized"));
    }

    @Test
    void testAdminEnabledInvalidCredentialsReturns401() throws Exception {
        TestHttpExchange exchange = new TestHttpExchange("/api/admin/sessions", "GET");
        exchange.getRequestHeaders().set("Authorization", basicAuth("wronguser", "wrongpass"));
        adminHandler.handle(exchange);

        assertEquals(401, exchange.getResponseCode());
        assertEquals("Basic realm=\"Oddly Specific Admin Console\"", exchange.getResponseHeaders().getFirst("WWW-Authenticate"));
        assertTrue(exchange.getResponseBodyAsString().contains("Unauthorized"));
    }

    @Test
    void testValidCredentialsAccessAdminUi() throws Exception {
        TestHttpExchange exchange = new TestHttpExchange("/admin", "GET");
        exchange.getRequestHeaders().set("Authorization", basicAuth(TEST_USER, TEST_PASS));
        adminHandler.handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        assertTrue(exchange.getResponseBodyAsString().contains("ADMIN CONSOLE"));
    }

    @Test
    void testValidCredentialsAccessAdminApi() throws Exception {
        TestHttpExchange exchange = new TestHttpExchange("/api/admin/sessions", "GET");
        exchange.getRequestHeaders().set("Authorization", basicAuth(TEST_USER, TEST_PASS));
        adminHandler.handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        assertTrue(exchange.getResponseBodyAsString().contains("[]"));
    }

    @Test
    void testGetActiveSessionsWithSessionAndValidAuth() throws Exception {
        Session session = sessionManager.createSession("REACTION_TEST", "192.168.1.50");

        TestHttpExchange exchange = new TestHttpExchange("/api/admin/sessions", "GET");
        exchange.getRequestHeaders().set("Authorization", basicAuth(TEST_USER, TEST_PASS));
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
        expireExchange.getRequestHeaders().set("Authorization", basicAuth(TEST_USER, TEST_PASS));
        adminHandler.handle(expireExchange);
        assertEquals(200, expireExchange.getResponseCode());

        // Get Expired Sessions
        TestHttpExchange getExpiredExchange = new TestHttpExchange("/api/admin/sessions/expired", "GET");
        getExpiredExchange.getRequestHeaders().set("Authorization", basicAuth(TEST_USER, TEST_PASS));
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
        deleteExchange.getRequestHeaders().set("Authorization", basicAuth(TEST_USER, TEST_PASS));
        adminHandler.handle(deleteExchange);

        assertEquals(200, deleteExchange.getResponseCode());
        assertTrue(sessionManager.getSession(session.getSessionId()).isEmpty());
    }

    @Test
    void testAdminApiCannotBypassAuthentication() throws Exception {
        // GET sessions bypass attempt
        TestHttpExchange getExchange = new TestHttpExchange("/api/admin/sessions", "GET");
        adminHandler.handle(getExchange);
        assertEquals(401, getExchange.getResponseCode());

        // POST expire bypass attempt
        TestHttpExchange expireExchange = new TestHttpExchange("/api/admin/sessions/dummy/expire", "POST");
        adminHandler.handle(expireExchange);
        assertEquals(401, expireExchange.getResponseCode());

        // DELETE evict bypass attempt
        TestHttpExchange deleteExchange = new TestHttpExchange("/api/admin/sessions/dummy", "DELETE");
        adminHandler.handle(deleteExchange);
        assertEquals(401, deleteExchange.getResponseCode());
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
