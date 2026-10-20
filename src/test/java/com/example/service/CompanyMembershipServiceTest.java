package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompanyMembershipServiceTest {

    private UserRepository userRepository;
    private CompanyMembershipService companyMembershipService;

    private Company companyA;
    private Company companyB;
    private User companyAdminA;
    private User candidateA;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        companyMembershipService = new CompanyMembershipService(userRepository);

        companyA = new Company();
        companyA.setId(1L);
        companyA.setName("PT Sinar Jaya");
        companyA.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        companyB = new Company();
        companyB.setId(2L);
        companyB.setName("PT Bintang Timur");
        companyB.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        companyAdminA = new User();
        companyAdminA.setId(10L);
        companyAdminA.setName("Admin A");
        companyAdminA.setEmail("admin@sinarjaya.com");
        companyAdminA.setRole(Role.PEMBERI_LAMARAN);
        companyAdminA.setCompany(companyA);
        companyAdminA.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        companyAdminA.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);

        candidateA = new User();
        candidateA.setId(20L);
        candidateA.setName("Calon Member A");
        candidateA.setEmail("candidate@sinarjaya.com");
        candidateA.setRole(Role.PEMBERI_LAMARAN);
        candidateA.setCompany(companyA);
        candidateA.setMembershipStatus(CompanyMembershipStatus.PENDING);
        candidateA.setCompanyPermission(CompanyPermission.MEMBER);
        candidateA.setMembershipRequestedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testApproveMemberSukses() {
        // Arrange
        when(userRepository.findById(20L)).thenReturn(Optional.of(candidateA));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = companyMembershipService.approveMember(20L, companyAdminA);

        // Assert
        assertNotNull(result);
        assertEquals(CompanyMembershipStatus.ACTIVE, result.getMembershipStatus());
        assertEquals(CompanyPermission.MEMBER, result.getCompanyPermission());
        verify(userRepository).save(candidateA);
    }

    @Test
    void testRejectMemberSukses() {
        // Arrange
        when(userRepository.findById(20L)).thenReturn(Optional.of(candidateA));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = companyMembershipService.rejectMember(20L, companyAdminA);

        // Assert
        assertNotNull(result);
        assertEquals(CompanyMembershipStatus.REJECTED, result.getMembershipStatus());
        assertEquals(CompanyPermission.MEMBER, result.getCompanyPermission());
        verify(userRepository).save(candidateA);
    }

    @Test
    void testPermissionTetapMemberSetelahApprove() {
        // Arrange
        when(userRepository.findById(20L)).thenReturn(Optional.of(candidateA));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = companyMembershipService.approveMember(20L, companyAdminA);

        // Assert: permission HARUS tetap MEMBER, tidak boleh berubah menjadi COMPANY_ADMIN
        assertEquals(CompanyPermission.MEMBER, result.getCompanyPermission());
    }

    @Test
    void testMemberBiasaMencobaApproveDitolak() {
        // Arrange: User dengan permission MEMBER
        User regularMember = new User();
        regularMember.setId(11L);
        regularMember.setRole(Role.PEMBERI_LAMARAN);
        regularMember.setCompany(companyA);
        regularMember.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        regularMember.setCompanyPermission(CompanyPermission.MEMBER);

        // Act & Assert
        SecurityException ex = assertThrows(
                SecurityException.class,
                () -> companyMembershipService.approveMember(20L, regularMember)
        );
        assertEquals("Hanya COMPANY_ADMIN yang berhak mengakses fitur ini", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testMemberBiasaMencobaRejectDitolak() {
        // Arrange: User dengan permission MEMBER
        User regularMember = new User();
        regularMember.setId(11L);
        regularMember.setRole(Role.PEMBERI_LAMARAN);
        regularMember.setCompany(companyA);
        regularMember.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        regularMember.setCompanyPermission(CompanyPermission.MEMBER);

        // Act & Assert
        SecurityException ex = assertThrows(
                SecurityException.class,
                () -> companyMembershipService.rejectMember(20L, regularMember)
        );
        assertEquals("Hanya COMPANY_ADMIN yang berhak mengakses fitur ini", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCompanyAdminDenganMembershipTidakActiveDitolak() {
        // Arrange
        companyAdminA.setMembershipStatus(CompanyMembershipStatus.PENDING);

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> companyMembershipService.approveMember(20L, companyAdminA)
        );
        assertEquals("Status membership admin belum aktif", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCompanyAdminDenganCompanyTidakVerifiedDitolak() {
        // Arrange
        companyA.setVerificationStatus(CompanyVerificationStatus.PENDING);

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> companyMembershipService.approveMember(20L, companyAdminA)
        );
        assertEquals("Perusahaan belum terverifikasi", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCompanyAdminCompanyAMencobaApproveMemberCompanyBDitolak() {
        // Arrange: Candidate dari Company B
        User candidateB = new User();
        candidateB.setId(30L);
        candidateB.setName("Calon Member B");
        candidateB.setRole(Role.PEMBERI_LAMARAN);
        candidateB.setCompany(companyB);
        candidateB.setMembershipStatus(CompanyMembershipStatus.PENDING);
        candidateB.setCompanyPermission(CompanyPermission.MEMBER);

        when(userRepository.findById(30L)).thenReturn(Optional.of(candidateB));

        // Act & Assert: Admin A mencoba approve candidate B -> SecurityException
        SecurityException ex = assertThrows(
                SecurityException.class,
                () -> companyMembershipService.approveMember(30L, companyAdminA)
        );
        assertEquals("Anda hanya dapat mengelola keanggotaan perusahaan Anda sendiri", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCompanyAdminCompanyAMencobaRejectMemberCompanyBDitolak() {
        // Arrange: Candidate dari Company B
        User candidateB = new User();
        candidateB.setId(30L);
        candidateB.setName("Calon Member B");
        candidateB.setRole(Role.PEMBERI_LAMARAN);
        candidateB.setCompany(companyB);
        candidateB.setMembershipStatus(CompanyMembershipStatus.PENDING);
        candidateB.setCompanyPermission(CompanyPermission.MEMBER);

        when(userRepository.findById(30L)).thenReturn(Optional.of(candidateB));

        // Act & Assert: Admin A mencoba reject candidate B -> SecurityException
        SecurityException ex = assertThrows(
                SecurityException.class,
                () -> companyMembershipService.rejectMember(30L, companyAdminA)
        );
        assertEquals("Anda hanya dapat mengelola keanggotaan perusahaan Anda sendiri", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCandidateSudahActiveTidakDapatDiprosesLagi() {
        // Arrange: Candidate status sudah ACTIVE
        candidateA.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        when(userRepository.findById(20L)).thenReturn(Optional.of(candidateA));

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> companyMembershipService.approveMember(20L, companyAdminA)
        );
        assertEquals("Hanya calon anggota berstatus PENDING yang dapat diproses", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCandidateSudahRejectedTidakDapatDiprosesLagi() {
        // Arrange: Candidate status sudah REJECTED
        candidateA.setMembershipStatus(CompanyMembershipStatus.REJECTED);
        when(userRepository.findById(20L)).thenReturn(Optional.of(candidateA));

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> companyMembershipService.rejectMember(20L, companyAdminA)
        );
        assertEquals("Hanya calon anggota berstatus PENDING yang dapat diproses", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCandidateUserIdTidakDitemukanDitolak() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> companyMembershipService.approveMember(999L, companyAdminA)
        );
        assertEquals("Calon anggota tidak ditemukan dengan ID: 999", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetPendingMembersMengambilPendingMemberHanyaDariCompanyAdminTersebut() {
        // Arrange
        when(userRepository.findByCompanyAndMembershipStatus(companyA, CompanyMembershipStatus.PENDING))
                .thenReturn(List.of(candidateA));

        // Act
        List<User> results = companyMembershipService.getPendingMembers(companyAdminA);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Calon Member A", results.get(0).getName());
        verify(userRepository).findByCompanyAndMembershipStatus(companyA, CompanyMembershipStatus.PENDING);
    }

    @Test
    void testCandidatePermissionBukanMemberFailClosed() {
        // Arrange: Candidate memiliki permission COMPANY_ADMIN atau aneh
        candidateA.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);
        when(userRepository.findById(20L)).thenReturn(Optional.of(candidateA));

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> companyMembershipService.approveMember(20L, companyAdminA)
        );
        assertEquals("Calon anggota harus memiliki permission MEMBER", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCompanyAdminNullDitolak() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> companyMembershipService.getPendingMembers(null)
        );
        assertEquals("Company admin tidak boleh null", ex.getMessage());
    }

    @Test
    void testCandidateUserIdNullDitolak() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> companyMembershipService.approveMember(null, companyAdminA)
        );
        assertEquals("ID calon anggota tidak boleh null", ex.getMessage());
    }

    @Test
    void testUserBukanEmployerDitolak() {
        companyAdminA.setRole(Role.PELAMAR);

        SecurityException ex = assertThrows(
                SecurityException.class,
                () -> companyMembershipService.getPendingMembers(companyAdminA)
        );
        assertEquals("User bukan employer", ex.getMessage());
    }
}
