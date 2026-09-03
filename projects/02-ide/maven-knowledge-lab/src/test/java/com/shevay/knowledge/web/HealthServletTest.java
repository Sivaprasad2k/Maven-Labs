package com.shevay.knowledge.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthServletTest {

    @Test
    @DisplayName("GET /api/health should return 200 OK and status UP JSON")
    void testHealthEndpointReturns200AndValidJson() throws Exception {
        HealthServlet servlet = new HealthServlet();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().contains("application/json"));
        assertEquals("{\"status\":\"UP\"}", response.getContentAsString());
    }

    @Test
    @DisplayName("POST /api/health should return 405 Method Not Allowed")
    void testHealthEndpointRejectsPost() throws Exception {
        HealthServlet servlet = new HealthServlet();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals(405, response.getStatus());
        assertTrue(response.getContentAsString().contains("Method Not Allowed"));
    }
}
