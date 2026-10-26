package com.example.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyAuthenticationFilterTest {

    private static final String CONFIGURED_TOKEN = "valid-test-opaque-bearer-token-12345";

    private ApiKeyAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new ApiKeyAuthenticationFilter(CONFIGURED_TOKEN);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_whenNoAuthorizationHeader_doesNotAuthenticate() throws ServletException, IOException {
        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_whenAuthorizationHeaderNotBearer_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_whenBearerTokenIsEmpty_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer ");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_whenBearerTokenIsWrong_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer invalid-wrong-token-abc");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_whenBearerTokenIsValid_authenticatesSuccessfullyWithRoleApiClient() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + CONFIGURED_TOKEN);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals("external-api-client", authentication.getName());
        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_API_CLIENT"))
        );
    }

    @Test
    void doFilter_whenServerTokenNotConfigured_failsClosed() throws ServletException, IOException {
        // Inisialisasi filter dengan server token kosong
        ApiKeyAuthenticationFilter unconfiguredFilter = new ApiKeyAuthenticationFilter("");
        request.addHeader("Authorization", "Bearer " + CONFIGURED_TOKEN);

        unconfiguredFilter.doFilter(request, response, filterChain);

        // Harus ditolak (fail-closed)
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_whenServerTokenIsNull_failsClosed() throws ServletException, IOException {
        // Inisialisasi filter dengan server token null
        ApiKeyAuthenticationFilter unconfiguredFilter = new ApiKeyAuthenticationFilter(null);
        request.addHeader("Authorization", "Bearer " + CONFIGURED_TOKEN);

        unconfiguredFilter.doFilter(request, response, filterChain);

        // Harus ditolak (fail-closed)
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
