package com.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final String serverApiToken;

    public ApiKeyAuthenticationFilter(@Value("${app.security.api-token:}") String serverApiToken) {
        this.serverApiToken = serverApiToken != null ? serverApiToken.trim() : "";
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

       
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

   
        String clientToken = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (serverApiToken.isEmpty() || clientToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] serverTokenBytes = serverApiToken.getBytes(StandardCharsets.UTF_8);
        byte[] clientTokenBytes = clientToken.getBytes(StandardCharsets.UTF_8);

        if (MessageDigest.isEqual(serverTokenBytes, clientTokenBytes)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "external-api-client",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
                    );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }

        filterChain.doFilter(request, response);
    }
}
