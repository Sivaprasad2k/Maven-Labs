package com.shevay.knowledge.web;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import java.util.Map;

/**
 * Lightweight mock implementation of HttpServletRequest for isolated Servlet unit testing.
 */
public class MockHttpServletRequest implements HttpServletRequest {

    private String method = "GET";
    private String requestURI = "/";
    private byte[] bodyBytes = new byte[0];
    private String contentType = "application/json";
    private ServletContext servletContext;

    public MockHttpServletRequest() {}

    public MockHttpServletRequest(String method, String requestURI) {
        this.method = method;
        this.requestURI = requestURI;
    }

    public void setMethod(String method) { this.method = method; }
    public void setRequestURI(String requestURI) { this.requestURI = requestURI; }
    public void setBody(String body) { this.bodyBytes = body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0]; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setServletContext(ServletContext servletContext) { this.servletContext = servletContext; }

    @Override public String getMethod() { return method; }
    @Override public String getRequestURI() { return requestURI; }
    @Override public String getContentType() { return contentType; }
    @Override public ServletContext getServletContext() { return servletContext; }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bodyBytes);
        return new ServletInputStream() {
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(jakarta.servlet.ReadListener readListener) {}
            @Override public int read() { return bais.read(); }
        };
    }

    @Override
    public java.io.BufferedReader getReader() throws IOException {
        return new java.io.BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override public String getAuthType() { return null; }
    @Override public Cookie[] getCookies() { return new Cookie[0]; }
    @Override public long getDateHeader(String name) { return -1; }
    @Override public String getHeader(String name) { return null; }
    @Override public Enumeration<String> getHeaders(String name) { return Collections.emptyEnumeration(); }
    @Override public Enumeration<String> getHeaderNames() { return Collections.emptyEnumeration(); }
    @Override public int getIntHeader(String name) { return -1; }
    @Override public String getPathInfo() { return null; }
    @Override public String getPathTranslated() { return null; }
    @Override public String getContextPath() { return ""; }
    @Override public String getQueryString() { return null; }
    @Override public String getRemoteUser() { return null; }
    @Override public boolean isUserInRole(String role) { return false; }
    @Override public Principal getUserPrincipal() { return null; }
    @Override public String getRequestedSessionId() { return null; }
    @Override public String getServletPath() { return requestURI; }
    @Override public HttpSession getSession(boolean create) { return null; }
    @Override public HttpSession getSession() { return null; }
    @Override public String changeSessionId() { return null; }
    @Override public boolean isRequestedSessionIdValid() { return false; }
    @Override public boolean isRequestedSessionIdFromCookie() { return false; }
    @Override public boolean isRequestedSessionIdFromURL() { return false; }
    @Override public boolean authenticate(HttpServletResponse response) { return false; }
    @Override public void login(String username, String password) {}
    @Override public void logout() {}
    @Override public Collection<Part> getParts() { return Collections.emptyList(); }
    @Override public Part getPart(String name) { return null; }
    @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
    @Override public Object getAttribute(String name) { return null; }
    @Override public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
    @Override public String getCharacterEncoding() { return "UTF-8"; }
    @Override public void setCharacterEncoding(String env) {}
    @Override public int getContentLength() { return bodyBytes.length; }
    @Override public long getContentLengthLong() { return bodyBytes.length; }
    @Override public String getParameter(String name) { return null; }
    @Override public Enumeration<String> getParameterNames() { return Collections.emptyEnumeration(); }
    @Override public String[] getParameterValues(String name) { return null; }
    @Override public Map<String, String[]> getParameterMap() { return Collections.emptyMap(); }
    @Override public String getProtocol() { return "HTTP/1.1"; }
    @Override public String getScheme() { return "http"; }
    @Override public String getServerName() { return "localhost"; }
    @Override public int getServerPort() { return 8080; }
    @Override public void setAttribute(String name, Object o) {}
    @Override public void removeAttribute(String name) {}
    @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
    @Override public Enumeration<java.util.Locale> getLocales() { return Collections.emptyEnumeration(); }
    @Override public boolean isSecure() { return false; }
    @Override public RequestDispatcher getRequestDispatcher(String path) { return null; }
    @Override public String getRemoteAddr() { return "127.0.0.1"; }
    @Override public String getRemoteHost() { return "localhost"; }
    @Override public int getRemotePort() { return 12345; }
    @Override public String getLocalName() { return "localhost"; }
    @Override public String getLocalAddr() { return "127.0.0.1"; }
    @Override public int getLocalPort() { return 8080; }
    @Override public StringBuffer getRequestURL() { return new StringBuffer("http://localhost:8080" + requestURI); }
    @Override public AsyncContext startAsync() { return null; }
    @Override public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) { return null; }
    @Override public boolean isAsyncStarted() { return false; }
    @Override public boolean isAsyncSupported() { return false; }
    @Override public AsyncContext getAsyncContext() { return null; }
    @Override public DispatcherType getDispatcherType() { return DispatcherType.REQUEST; }
    @Override public String getRequestId() { return "req-1"; }
    @Override public String getProtocolRequestId() { return "proto-1"; }
    @Override public ServletConnection getServletConnection() { return null; }
}
