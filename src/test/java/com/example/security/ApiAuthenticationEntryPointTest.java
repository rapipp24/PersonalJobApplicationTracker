package com.example.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAuthenticationEntryPointTest {

    @Test
    void commence_writes401Status_applicationJson_andWwwAuthenticateHeader() throws IOException {
        ApiAuthenticationEntryPoint entryPoint = new ApiAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/jobs");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Authentication required"));

        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertEquals("Bearer", response.getHeader("WWW-Authenticate"));

        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":401"));
        assertTrue(body.contains("\"error\":\"Unauthorized\""));
        assertTrue(body.contains("\"message\":\"Bearer token is missing or invalid\""));
        assertTrue(body.contains("\"path\":\"/api/v1/jobs\""));
    }
}
