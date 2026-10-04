package com.example.config;

import com.example.entity.Company;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
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
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final String testUserPassword;

    public DataInitializer(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            @Value("${TEST_USER_PASSWORD}") String testUserPassword) {

        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
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
            Company testCompany = new Company();
            testCompany.setName("PT Test Employer");
            companyRepository.save(testCompany);

            User pemberi = new User();
            pemberi.setName("Test Employer");
            pemberi.setEmail("pemberi@example.com");
            pemberi.setPassword(passwordEncoder.encode(testUserPassword));
            pemberi.setRole(Role.PEMBERI_LAMARAN);
            pemberi.setCompany(testCompany);

            userRepository.save(pemberi);
            System.out.println("Created development user: pemberi@example.com");
        } else {
            // Jika user test sudah ada tapi belum mempunyai Company, lengkapi.
            // Ini menangani kasus DataInitializer dijalankan sebelum fitur Company ada.
            userRepository.findByEmail("pemberi@example.com").ifPresent(pemberi -> {
                if (pemberi.getCompany() == null) {
                    Company testCompany = new Company();
                    testCompany.setName("PT Test Employer");
                    companyRepository.save(testCompany);

                    pemberi.setCompany(testCompany);
                    userRepository.save(pemberi);
                    System.out.println("Updated development user with company: pemberi@example.com");
                }
            });
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
