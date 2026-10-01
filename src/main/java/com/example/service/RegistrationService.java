package com.example.service;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User register(
            String name,
            String email,
            String rawPassword,
            Role role) {

        if (role == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Admin cannot register through public registration."
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "Email is already registered."
            );
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(role);

        return userRepository.save(user);
    }
}
