package com.example.security;

import com.example.view.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    public SecurityConfig(
            ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
            ApiAuthenticationEntryPoint apiAuthenticationEntryPoint) {
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
        this.apiAuthenticationEntryPoint = apiAuthenticationEntryPoint;
    }

    /**
     * Security Filter Chain khusus untuk REST API eksternal (/api/**).
     * Diberi @Order(1) agar diproses sebelum rantai web Vaadin.
     * Bersifat STATELESS dan hanya menerima autentikasi Bearer Token.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(context -> context.securityContextRepository(new NullSecurityContextRepository()))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(apiAuthenticationEntryPoint));

        return http.build();
    }

    /**
     * Security Filter Chain untuk aplikasi web Vaadin.
     * Diberi @Order(2) sebagai fallback untuk seluruh rute selain /api/**.
     * Mempertahankan seluruh alur login, session, dan proteksi role Vaadin eksisting.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
