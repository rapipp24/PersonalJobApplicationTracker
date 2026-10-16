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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
        assertEquals(CompanyMembershipStatus.ACTIVE, pemberi.getMembershipStatus());
        assertEquals(CompanyPermission.COMPANY_ADMIN, pemberi.getCompanyPermission());
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
        existingCompany.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(null);
        pemberi.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        pemberi.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);

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

    @Test
    void testFreshTestEmployerDanCompanyDiberiStatusVerifiedActiveAdmin() {
        // Arrange
        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.empty());

        when(companyRepository.findByName("PT Test Employer")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

        // Act
        dataInitializer.run();

        // Assert
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        Company savedCompany = companyCaptor.getValue();
        assertEquals("PT Test Employer", savedCompany.getName());
        assertEquals(CompanyVerificationStatus.VERIFIED, savedCompany.getVerificationStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("pemberi@example.com", savedUser.getEmail());
        assertEquals(Role.PEMBERI_LAMARAN, savedUser.getRole());
        assertEquals(savedCompany, savedUser.getCompany());
        assertEquals(CompanyMembershipStatus.ACTIVE, savedUser.getMembershipStatus());
        assertEquals(CompanyPermission.COMPANY_ADMIN, savedUser.getCompanyPermission());
    }

    @Test
    void testExistingLegacyEmployerDiberiActiveDanCompanyAdminJikaNull() {
        // Arrange
        Company existingCompany = new Company();
        existingCompany.setName("PT Test Employer");
        existingCompany.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(existingCompany);
        pemberi.setMembershipStatus(null);
        pemberi.setCompanyPermission(null);

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(new ArrayList<>());

        // Act
        dataInitializer.run();

        // Assert
        assertEquals(CompanyMembershipStatus.ACTIVE, pemberi.getMembershipStatus());
        assertEquals(CompanyPermission.COMPANY_ADMIN, pemberi.getCompanyPermission());
        verify(userRepository).save(pemberi);
    }

    @Test
    void testExistingValidMembershipJanganDitimpa() {
        // Arrange
        Company existingCompany = new Company();
        existingCompany.setName("PT Test Employer");
        existingCompany.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(existingCompany);
        pemberi.setMembershipStatus(CompanyMembershipStatus.PENDING);
        pemberi.setCompanyPermission(CompanyPermission.MEMBER);

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(new ArrayList<>());

        // Act
        dataInitializer.run();

        // Assert: PENDING dan MEMBER tidak boleh di-overwrite
        assertEquals(CompanyMembershipStatus.PENDING, pemberi.getMembershipStatus());
        assertEquals(CompanyPermission.MEMBER, pemberi.getCompanyPermission());
        verify(userRepository, never()).save(pemberi);
    }

    @Test
    void testExistingRejectedMembershipJanganDitimpa() {
        // Arrange
        Company existingCompany = new Company();
        existingCompany.setName("PT Test Employer");
        existingCompany.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(existingCompany);
        pemberi.setMembershipStatus(CompanyMembershipStatus.REJECTED);
        pemberi.setCompanyPermission(CompanyPermission.MEMBER);

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(new ArrayList<>());

        // Act
        dataInitializer.run();

        // Assert: REJECTED tidak boleh di-overwrite
        assertEquals(CompanyMembershipStatus.REJECTED, pemberi.getMembershipStatus());
        assertEquals(CompanyPermission.MEMBER, pemberi.getCompanyPermission());
        verify(userRepository, never()).save(pemberi);
    }

    @Test
    void testExistingLegacyCompanyDenganStatusNullDiberiVerified() {
        // Arrange: pemberi sudah ada + sudah memiliki PT Test Employer + verificationStatus = null
        Company legacyCompany = new Company();
        legacyCompany.setName("PT Test Employer");
        legacyCompany.setVerificationStatus(null);

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(legacyCompany);
        pemberi.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        pemberi.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(new ArrayList<>());

        // Act
        dataInitializer.run();

        // Assert: verificationStatus null dibackfill menjadi VERIFIED dan disimpan
        assertEquals(CompanyVerificationStatus.VERIFIED, legacyCompany.getVerificationStatus());
        verify(companyRepository).save(legacyCompany);
    }

    @Test
    void testExistingCompanyStatusPendingTidakDitimpa() {
        // Arrange: pemberi sudah memiliki PT Test Employer dengan status PENDING
        Company pendingCompany = new Company();
        pendingCompany.setName("PT Test Employer");
        pendingCompany.setVerificationStatus(CompanyVerificationStatus.PENDING);

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(pendingCompany);
        pemberi.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        pemberi.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(new ArrayList<>());

        // Act
        dataInitializer.run();

        // Assert: Company status PENDING tidak boleh diubah dan tidak disimpan ulang
        assertEquals(CompanyVerificationStatus.PENDING, pendingCompany.getVerificationStatus());
        verify(companyRepository, never()).save(pendingCompany);
    }

    @Test
    void testExistingCompanyStatusRejectedTidakDitimpa() {
        // Arrange: pemberi sudah memiliki PT Test Employer dengan status REJECTED
        Company rejectedCompany = new Company();
        rejectedCompany.setName("PT Test Employer");
        rejectedCompany.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(rejectedCompany);
        pemberi.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        pemberi.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(new ArrayList<>());

        // Act
        dataInitializer.run();

        // Assert: Company status REJECTED tidak boleh diubah dan tidak disimpan ulang
        assertEquals(CompanyVerificationStatus.REJECTED, rejectedCompany.getVerificationStatus());
        verify(companyRepository, never()).save(rejectedCompany);
    }

    @Test
    void testExistingCompanyPendingDiRepositoryTidakDitimpa() {
        // Arrange: pemberi belum punya Company, tapi di repository ada PT Test Employer dengan status PENDING
        Company pendingCompany = new Company();
        pendingCompany.setName("PT Test Employer");
        pendingCompany.setVerificationStatus(CompanyVerificationStatus.PENDING);

        User pemberi = new User();
        pemberi.setName("Test Employer");
        pemberi.setEmail("pemberi@example.com");
        pemberi.setRole(Role.PEMBERI_LAMARAN);
        pemberi.setCompany(null);
        pemberi.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        pemberi.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);

        when(userRepository.existsByEmail("pelamar@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);
        when(userRepository.findByEmail("pemberi@example.com")).thenReturn(Optional.of(pemberi));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(companyRepository.findByName("PT Test Employer")).thenReturn(Optional.of(pendingCompany));
        when(jobPostingRepository.findByEmployer(pemberi)).thenReturn(new ArrayList<>());

        // Act
        dataInitializer.run();

        // Assert: Company status PENDING di repository tidak boleh dipaksa menjadi VERIFIED
        assertEquals(CompanyVerificationStatus.PENDING, pendingCompany.getVerificationStatus());
        verify(companyRepository, never()).save(pendingCompany);
    }
}
