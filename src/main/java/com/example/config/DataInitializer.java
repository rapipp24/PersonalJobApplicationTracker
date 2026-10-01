package com.example.config;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String testUserPassword;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${TEST_USER_PASSWORD}") String testUserPassword) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.testUserPassword = testUserPassword;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("pelamar@example.com")) {
            User pelamar = new User();
            pelamar.setName("Test Pelamar");
            pelamar.setEmail("pelamar@example.com");
            pelamar.setPassword(passwordEncoder.encode(testUserPassword));
            pelamar.setRole(Role.PELAMAR);

            userRepository.save(pelamar);
            System.out.println("Created development user: pelamar@example.com");
        }

        if (!userRepository.existsByEmail("pemberi@example.com")) {
            User pemberi = new User();
            pemberi.setName("Test Employer");
            pemberi.setEmail("pemberi@example.com");
            pemberi.setPassword(passwordEncoder.encode(testUserPassword));
            pemberi.setRole(Role.PEMBERI_LAMARAN);

            userRepository.save(pemberi);
            System.out.println("Created development user: pemberi@example.com");
        }

        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode(testUserPassword));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            System.out.println("Created development user: admin@example.com");
        }
    }
}
