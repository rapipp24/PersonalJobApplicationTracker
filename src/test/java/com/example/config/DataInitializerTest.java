package com.example.config;

import com.example.entity.Company;
import com.example.entity.JobPosting;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.JobPostingRepository;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataInitializerTest {

    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private JobPostingRepository jobPostingRepository;
    private PasswordEncoder passwordEncoder;
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        companyRepository = Mockito.mock(CompanyRepository.class);
        jobPostingRepository = Mockito.mock(JobPostingRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);

        dataInitializer = new DataInitializer(
                userRepository,
                companyRepository,
                jobPostingRepository,
                passwordEncoder,
                "dummyPassword123"
        );
    }

    @Test
    void testLengkapiCompanyDanNormalisasiJobPostingUntukUserLama() {
        // Arrange
        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(null); // Kondisi lama belum ada Company

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(companyRepository.findByName("PT Test Employer")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobPosting job1 = new JobPosting();
        job1.setPosition("Cleaning Service");
        job1.setCompanyName("PT OY");
        job1.setEmployer(pemberi);

        JobPosting job2 = new JobPosting();
        job2.setPosition("Mobile Developer");
        job2.setCompanyName("PT SAJAK");
        job2.setEmployer(pemberi);

        JobPosting job3 = new JobPosting();
        job3.setPosition("Vaadin Developer");
        job3.setCompanyName("PT Test Employer"); // Sudah sesuai
        job3.setEmployer(pemberi);

        List<JobPosting> testJobPostings = new ArrayList<>();
        testJobPostings.add(job1);
        testJobPostings.add(job2);
        testJobPostings.add(job3);

        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(testJobPostings);

        // Act
        dataInitializer.run();

        // Assert
        assertNotNull(pemberi.getCompany());
        assertEquals("PT Test Employer", pemberi.getCompany().getName());
        verify(userRepository).save(pemberi);

        // Job 1 dan 2 harus dinormalisasi
        assertEquals("PT Test Employer", job1.getCompanyName());
        assertEquals("PT Test Employer", job2.getCompanyName());
        assertEquals("PT Test Employer", job3.getCompanyName());

        verify(jobPostingRepository).save(job1);
        verify(jobPostingRepository).save(job2);
        verify(jobPostingRepository, never()).save(job3); // Tidak perlu save jika sudah sesuai
    }

    @Test
    void testTidakDuplikasiCompanyJikaCompanySudahAda() {
        // Arrange
        Company existingCompany = new Company();
        existingCompany.setName("PT Test Employer");

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(null);

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(companyRepository.findByName("PT Test Employer")).thenReturn(Optional.of(existingCompany));
        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(new ArrayList<>());

        // Act
        dataInitializer.run();

        // Assert
        assertEquals(existingCompany, pemberi.getCompany());
        verify(companyRepository, never()).save(any(Company.class)); // Tidak membuat Company baru
        verify(userRepository).save(pemberi);
    }
}
