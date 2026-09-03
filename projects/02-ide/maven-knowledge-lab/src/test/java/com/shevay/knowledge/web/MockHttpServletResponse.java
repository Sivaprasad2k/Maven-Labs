package com.shevay.knowledge.web;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight mock implementation of HttpServletResponse for isolated Servlet unit testing.
 */
public class MockHttpServletResponse implements HttpServletResponse {

    private int status = SC_OK;
    private String contentType;
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(stringWriter);
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final Map<String, String> headers = new HashMap<>();

    @Override public void setStatus(int sc) { this.status = sc; }
    @Override public int getStatus() { return status; }
    @Override public void setContentType(String type) { this.contentType = type; }
    @Override public String getContentType() { return contentType; }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
            @Override public void write(int b) { outputStream.write(b); }
        };
    }

    public String getContentAsString() {
        printWriter.flush();
        String writerContent = stringWriter.toString();
        if (!writerContent.isEmpty()) {
            return writerContent;
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    @Override public void addCookie(Cookie cookie) {}
    @Override public boolean containsHeader(String name) { return headers.containsKey(name); }
    @Override public String encodeURL(String url) { return url; }
    @Override public String encodeRedirectURL(String url) { return url; }
    @Override public void sendError(int sc, String msg) throws IOException { this.status = sc; printWriter.write("{\"error\":\"" + msg + "\"}"); }
    @Override public void sendError(int sc) throws IOException { this.status = sc; }
    @Override public void sendRedirect(String location) throws IOException {}
    @Override public void setDateHeader(String name, long date) {}
    @Override public void addDateHeader(String name, long date) {}
    @Override public void setHeader(String name, String value) { headers.put(name, value); }
    @Override public void addHeader(String name, String value) { headers.put(name, value); }
    @Override public void setIntHeader(String name, int value) { headers.put(name, String.valueOf(value)); }
    @Override public void addIntHeader(String name, int value) { headers.put(name, String.valueOf(value)); }
    @Override public String getHeader(String name) { return headers.get(name); }
    @Override public Collection<String> getHeaders(String name) { return Collections.singletonList(headers.get(name)); }
    @Override public Collection<String> getHeaderNames() { return headers.keySet(); }
    @Override public void setCharacterEncoding(String charset) {}
    @Override public String getCharacterEncoding() { return "UTF-8"; }
    @Override public void setContentLength(int len) {}
    @Override public void setContentLengthLong(long len) {}
    @Override public void setBufferSize(int size) {}
    @Override public int getBufferSize() { return 8192; }
    @Override public void flushBuffer() throws IOException {}
    @Override public void resetBuffer() {}
    @Override public boolean isCommitted() { return false; }
    @Override public void reset() {}
    @Override public void setLocale(java.util.Locale loc) {}
    @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
}
