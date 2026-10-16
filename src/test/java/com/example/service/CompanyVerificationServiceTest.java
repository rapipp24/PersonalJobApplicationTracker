package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompanyVerificationServiceTest {

    private CompanyRepository companyRepository;
    private UserRepository userRepository;
    private CompanyVerificationService companyVerificationService;

    @BeforeEach
    void setUp() {
        companyRepository = Mockito.mock(CompanyRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        companyVerificationService = new CompanyVerificationService(companyRepository, userRepository);
    }

    @Test
    void testGetPendingCompaniesMemanggilRepositoryDanMengembalikanList() {
        // Arrange
        Company company1 = new Company();
        company1.setName("PT Satu");
        company1.setVerificationStatus(CompanyVerificationStatus.PENDING);

        Company company2 = new Company();
        company2.setName("PT Dua");
        company2.setVerificationStatus(CompanyVerificationStatus.PENDING);

        when(companyRepository.findByVerificationStatus(CompanyVerificationStatus.PENDING))
                .thenReturn(List.of(company1, company2));

        // Act
        List<Company> result = companyVerificationService.getPendingCompanies();

        // Assert
        assertEquals(2, result.size());
        verify(companyRepository).findByVerificationStatus(CompanyVerificationStatus.PENDING);
    }

    @Test
    void testApproveCompanyBerhasilMengubahStatusCompanyDanEmployer() {
        // Arrange
        Long companyId = 100L;
        Company company = new Company();
        company.setId(companyId);
        company.setName("PT Sukses Makmur");
        company.setVerificationStatus(CompanyVerificationStatus.PENDING);

        User employer = new User();
        employer.setName("Budi Employer");
        employer.setEmail("budi@example.com");
        employer.setRole(Role.PEMBERI_LAMARAN);
        employer.setCompany(company);
        employer.setCompanyPermission(null);
        employer.setMembershipStatus(CompanyMembershipStatus.PENDING);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN))
                .thenReturn(List.of(employer));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        companyVerificationService.approveCompany(companyId);

        // Assert
        assertEquals(CompanyVerificationStatus.VERIFIED, company.getVerificationStatus());
        assertEquals(CompanyPermission.COMPANY_ADMIN, employer.getCompanyPermission());
        assertEquals(CompanyMembershipStatus.ACTIVE, employer.getMembershipStatus());

        verify(companyRepository).save(company);
        verify(userRepository).save(employer);
    }

    @Test
    void testApproveGagalJikaTidakAdaEmployerPendaftar() {
        // Arrange: 0 Employer
        Long companyId = 100L;
        Company company = new Company();
        company.setId(companyId);
        company.setVerificationStatus(CompanyVerificationStatus.PENDING);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN))
                .thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> companyVerificationService.approveCompany(companyId));

        assertEquals(CompanyVerificationStatus.PENDING, company.getVerificationStatus());
        verify(companyRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testApproveGagalJikaTerdapatLebihDariSatuEmployerPendaftar() {
        // Arrange: > 1 Employer
        Long companyId = 100L;
        Company company = new Company();
        company.setId(companyId);
        company.setVerificationStatus(CompanyVerificationStatus.PENDING);

        User employer1 = new User();
        employer1.setName("Employer 1");
        employer1.setRole(Role.PEMBERI_LAMARAN);

        User employer2 = new User();
        employer2.setName("Employer 2");
        employer2.setRole(Role.PEMBERI_LAMARAN);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN))
                .thenReturn(List.of(employer1, employer2));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> companyVerificationService.approveCompany(companyId));

        assertEquals(CompanyVerificationStatus.PENDING, company.getVerificationStatus());
        verify(companyRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testApproveGagalJikaCompanySudahVerified() {
        // Arrange: Company sudah VERIFIED
        Long companyId = 100L;
        Company company = new Company();
        company.setId(companyId);
        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> companyVerificationService.approveCompany(companyId));

        verify(companyRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testApproveGagalJikaCompanySudahRejected() {
        // Arrange: Company sudah REJECTED
        Long companyId = 100L;
        Company company = new Company();
        company.setId(companyId);
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> companyVerificationService.approveCompany(companyId));

        verify(companyRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testApproveGagalJikaCompanyTidakDitemukan() {
        // Arrange
        Long companyId = 999L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> companyVerificationService.approveCompany(companyId));

        verify(companyRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testApproveGagalJikaCompanyIdNull() {
        assertThrows(IllegalArgumentException.class, () -> companyVerificationService.approveCompany(null));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testRejectCompanyBerhasilMenyimpanStatusRejectedDanRejectionReason() {
        // Arrange
        Long companyId = 100L;
        Company company = new Company();
        company.setId(companyId);
        company.setName("PT Tidak Lolos");
        company.setVerificationStatus(CompanyVerificationStatus.PENDING);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String reason = "Website perusahaan tidak dapat diverifikasi dan kontak tidak aktif.";

        // Act
        companyVerificationService.rejectCompany(companyId, reason);

        // Assert
        assertEquals(CompanyVerificationStatus.REJECTED, company.getVerificationStatus());
        assertEquals(reason, company.getRejectionReason());

        verify(companyRepository).save(company);
        verify(userRepository, never()).save(any()); // User tidak diubah saat reject
    }

    @Test
    void testRejectGagalJikaRejectionReasonNull() {
        Long companyId = 100L;
        assertThrows(IllegalArgumentException.class, () -> companyVerificationService.rejectCompany(companyId, null));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testRejectGagalJikaRejectionReasonKosong() {
        Long companyId = 100L;
        assertThrows(IllegalArgumentException.class, () -> companyVerificationService.rejectCompany(companyId, ""));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testRejectGagalJikaRejectionReasonHanyaSpasi() {
        Long companyId = 100L;
        assertThrows(IllegalArgumentException.class, () -> companyVerificationService.rejectCompany(companyId, "    "));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testRejectGagalJikaCompanySudahVerified() {
        Long companyId = 100L;
        Company company = new Company();
        company.setId(companyId);
        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertThrows(IllegalStateException.class, () -> companyVerificationService.rejectCompany(companyId, "Alasan valid"));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testRejectGagalJikaCompanySudahRejected() {
        Long companyId = 100L;
        Company company = new Company();
        company.setId(companyId);
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertThrows(IllegalStateException.class, () -> companyVerificationService.rejectCompany(companyId, "Alasan valid"));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testRejectGagalJikaCompanyTidakDitemukan() {
        Long companyId = 999L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> companyVerificationService.rejectCompany(companyId, "Alasan valid"));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testRejectGagalJikaCompanyIdNull() {
        assertThrows(IllegalArgumentException.class, () -> companyVerificationService.rejectCompany(null, "Alasan valid"));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testGetCompanyRegistrantBerhasil() {
        Company company = new Company();
        company.setName("PT Registrant");

        User employer = new User();
        employer.setName("Pendaftar Asli");
        employer.setEmail("pendaftar@example.com");
        employer.setRole(Role.PEMBERI_LAMARAN);

        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN))
                .thenReturn(List.of(employer));

        User result = companyVerificationService.getCompanyRegistrant(company);

        assertNotNull(result);
        assertEquals("Pendaftar Asli", result.getName());
        assertEquals("pendaftar@example.com", result.getEmail());
    }

    @Test
    void testGetCompanyRegistrantNullCompanyDitolak() {
        assertThrows(IllegalArgumentException.class, () -> companyVerificationService.getCompanyRegistrant(null));
    }
}
