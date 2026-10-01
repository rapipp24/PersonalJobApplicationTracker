package com.example.service;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrationServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        registrationService = new RegistrationService(userRepository, passwordEncoder);
    }

    @Test
    void testRegisterApplicantBerhasil() {
        String name = "Test Applicant";
        String email = "applicant@example.com";
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPasswordHash";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = registrationService.register(name, email, rawPassword, Role.PELAMAR);

        assertNotNull(registeredUser);
        assertEquals(name, registeredUser.getName());
        assertEquals(email, registeredUser.getEmail());
        assertEquals(encodedPassword, registeredUser.getPassword());
        assertEquals(Role.PELAMAR, registeredUser.getRole());

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterEmployerBerhasil() {
        String name = "Test Employer";
        String email = "employer@example.com";
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPasswordHash";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = registrationService.register(name, email, rawPassword, Role.PEMBERI_LAMARAN);

        assertNotNull(registeredUser);
        assertEquals(name, registeredUser.getName());
        assertEquals(email, registeredUser.getEmail());
        assertEquals(encodedPassword, registeredUser.getPassword());
        assertEquals(Role.PEMBERI_LAMARAN, registeredUser.getRole());

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmailDitolak() {
        String email = "duplicate@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> registrationService.register("User Baru", email, "password123", Role.PELAMAR)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterAdminDitolak() {
        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.register("Admin Baru", "admin2@example.com", "password123", Role.ADMIN)
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
