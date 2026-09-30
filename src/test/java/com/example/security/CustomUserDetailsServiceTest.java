package com.example.security;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CustomUserDetailsServiceTest {

    @Test
    void harusBisaMengambilUserDetailsKetikaEmailDitemukan() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);

        String email = "pelamar@example.com";
        String passwordHash = "$2a$10$CuKxLwNt5am7A2RTutE3T.vsYLv8S8PH3ghxydKgvHxSYde1rHK.C";
        User user = new User("Budi Pelamar", email, passwordHash, Role.PELAMAR);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(passwordHash, userDetails.getPassword());
        assertTrue(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .matches("password123", userDetails.getPassword())
        );
        assertTrue(
                userDetails.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_PELAMAR"))
        );

        verify(userRepository).findByEmail(email);
    }

    @Test
    void harusThrowExceptionKetikaEmailTidakDitemukan() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);

        String email = "tidakada@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        verify(userRepository).findByEmail(email);
    }
}
