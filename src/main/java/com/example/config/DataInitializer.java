package com.example.config;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.JobPosting;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.JobPostingRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final PasswordEncoder passwordEncoder;
    private final String testUserPassword;

    public DataInitializer(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            JobPostingRepository jobPostingRepository,
            PasswordEncoder passwordEncoder,
            @Value("${TEST_USER_PASSWORD}") String testUserPassword) {

        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
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

        // Test user Employer: pemberi@example.com
        Optional<User> pemberiOptional = userRepository.findByEmail("pemberi@example.com");
        User pemberi;

        if (pemberiOptional.isEmpty()) {
            Company testCompany = getOrCreateTestCompany();

            pemberi = new User();
            pemberi.setName("Test Employer");
            pemberi.setEmail("pemberi@example.com");
            pemberi.setPassword(passwordEncoder.encode(testUserPassword));
            pemberi.setRole(Role.PEMBERI_LAMARAN);
            pemberi.setCompany(testCompany);
            pemberi.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
            pemberi.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);

            pemberi = userRepository.save(pemberi);
            System.out.println("Created development user: pemberi@example.com");
        } else {
            pemberi = pemberiOptional.get();
            boolean pemberiUpdated = false;

            // Ambil Company milik pemberi atau buat jika belum ada
            Company company = pemberi.getCompany();
            if (company == null) {
                company = getOrCreateTestCompany();
                pemberi.setCompany(company);
                pemberiUpdated = true;
            }

            // Backfill legacy null verificationStatus untuk development Test Company tanpa menimpa status non-null
            if (company != null && "PT Test Employer".equals(company.getName()) && company.getVerificationStatus() == null) {
                company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);
                companyRepository.save(company);
            }

            // Backfill legacy null membershipStatus tanpa menimpa status non-null
            if (pemberi.getMembershipStatus() == null) {
                pemberi.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
                pemberiUpdated = true;
            }

            // Backfill legacy null companyPermission tanpa menimpa permission non-null
            if (pemberi.getCompanyPermission() == null) {
                pemberi.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);
                pemberiUpdated = true;
            }

            if (pemberiUpdated) {
                pemberi = userRepository.save(pemberi);
                System.out.println("Updated development user baseline: pemberi@example.com");
            }
        }

        // Normalisasi seluruh JobPosting lama khusus milik pemberi@example.com
        List<JobPosting> jobPostings = jobPostingRepository.findByEmployer(pemberi);
        for (JobPosting jobPosting : jobPostings) {
            if (!"PT Test Employer".equals(jobPosting.getCompanyName())) {
                jobPosting.setCompanyName("PT Test Employer");
                jobPostingRepository.save(jobPosting);
                System.out.println("Updated job posting companyName to PT Test Employer: " + jobPosting.getPosition());
            }
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

    private Company getOrCreateTestCompany() {
        Optional<Company> existingCompany = companyRepository.findByName("PT Test Employer");
        if (existingCompany.isPresent()) {
            Company company = existingCompany.get();
            if (company.getVerificationStatus() == null) {
                company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);
                return companyRepository.save(company);
            }
            return company;
        }

        Company newCompany = new Company();
        newCompany.setName("PT Test Employer");
        newCompany.setVerificationStatus(CompanyVerificationStatus.VERIFIED);
        return companyRepository.save(newCompany);
    }
}

