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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmployerSettingsServiceTest {

    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private PasswordEncoder passwordEncoder;
    private EmployerSettingsService service;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        companyRepository = Mockito.mock(CompanyRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        service = new EmployerSettingsService(userRepository, companyRepository, passwordEncoder);
    }

    private Company createVerifiedCompany() {
        Company company = new Company();
        company.setId(100L);
        company.setName("PT Teknologi Mandiri");
        company.setIndustry("Teknologi Informasi");
        company.setLocation("Jakarta");
        company.setWebsite("https://mandiri.com");
        company.setOfficialContact("hr@mandiri.com");
        company.setDescription("Perusahaan IT");
        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);
        return company;
    }

    private User createEmployerAdminUser(Company company) {
        User user = new User();
        user.setId(1L);
        user.setName("Admin Employer");
        user.setEmail("admin@mandiri.com");
        user.setPassword("encodedOldPassword");
        user.setRole(Role.PEMBERI_LAMARAN);
        user.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);
        user.setCompany(company);
        return user;
    }

    private User createEmployerMemberUser(Company company) {
        User user = new User();
        user.setId(2L);
        user.setName("Member Employer");
        user.setEmail("member@mandiri.com");
        user.setPassword("encodedOldPassword");
        user.setRole(Role.PEMBERI_LAMARAN);
        user.setCompanyPermission(CompanyPermission.MEMBER);
        user.setCompany(company);
        return user;
    }

    private User createApplicantUser() {
        User user = new User();
        user.setId(3L);
        user.setName("Budi Pelamar");
        user.setEmail("budi@pelamar.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.PELAMAR);
        return user;
    }

    private User createInitialRegistrantUser(Company company) {
        User user = new User();
        user.setId(4L);
        user.setName("Registrant Employer");
        user.setEmail("registrant@mandiri.com");
        user.setPassword("encodedOldPassword");
        user.setRole(Role.PEMBERI_LAMARAN);
        user.setCompanyPermission(null);
        user.setMembershipStatus(CompanyMembershipStatus.PENDING);
        user.setCompany(company);
        return user;
    }

    // ==========================================
    // 1. ACCOUNT SETTINGS TESTS (Section 37)
    // ==========================================

    @Test
    void harusBisaUpdateNamaEmployerMember() {
        User member = createEmployerMemberUser(createVerifiedCompany());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.updateFullName(member, "Member Baru");

        assertEquals("Member Baru", result.getName());
        verify(userRepository).save(member);
    }

    @Test
    void harusBisaUpdateNamaEmployerCompanyAdmin() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.updateFullName(admin, "Admin Baru");

        assertEquals("Admin Baru", result.getName());
        verify(userRepository).save(admin);
    }

    @Test
    void harusTrimNamaEmployer() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.updateFullName(admin, "   Admin Spasi   ");

        assertEquals("Admin Spasi", result.getName());
        verify(userRepository).save(admin);
    }

    @Test
    void harusMenolakNamaEmployerKosong() {
        User admin = createEmployerAdminUser(createVerifiedCompany());

        assertThrows(IllegalArgumentException.class, () -> service.updateFullName(admin, ""));
        assertThrows(IllegalArgumentException.class, () -> service.updateFullName(admin, "   "));
        assertThrows(IllegalArgumentException.class, () -> service.updateFullName(admin, null));

        verify(userRepository, never()).save(any());
    }

    @Test
    void harusMenolakUpdateNamaOlehNonEmployer() {
        User applicant = createApplicantUser();

        assertThrows(SecurityException.class, () -> service.updateFullName(applicant, "Nama Baru"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void harusBisaGantiPasswordEmployerJikaCurrentPasswordBenar() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        when(passwordEncoder.matches("oldPass123", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.changePassword(admin, "oldPass123", "newPass123", "newPass123");

        verify(passwordEncoder).matches("oldPass123", "encodedOldPassword");
        verify(passwordEncoder).encode("newPass123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encodedNewPassword", captor.getValue().getPassword());
    }

    @Test
    void harusMenolakGantiPasswordJikaCurrentPasswordSalah() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        when(passwordEncoder.matches("wrongPass", "encodedOldPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(admin, "wrongPass", "newPass123", "newPass123"));

        assertEquals("Password saat ini salah", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void harusMenolakGantiPasswordJikaConfirmTidakSama() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        when(passwordEncoder.matches("oldPass123", "encodedOldPassword")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(admin, "oldPass123", "newPass123", "bedaPass"));

        assertEquals("Konfirmasi password tidak cocok", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void harusMenolakPasswordKosong() {
        User admin = createEmployerAdminUser(createVerifiedCompany());

        assertThrows(IllegalArgumentException.class, () -> service.changePassword(admin, "", "newPass", "newPass"));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(admin, null, "newPass", "newPass"));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(admin, "oldPass", "", ""));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(admin, "oldPass", "newPass", ""));

        verify(userRepository, never()).save(any());
    }

    @Test
    void harusMenolakGantiPasswordOlehNonEmployer() {
        User applicant = createApplicantUser();

        assertThrows(SecurityException.class,
                () -> service.changePassword(applicant, "oldPass", "newPass", "newPass"));
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // 2. COMPANY READ TESTS (Section 38)
    // ==========================================

    @Test
    void harusBisaMengambilCompanyProfileOlehMember() {
        Company company = createVerifiedCompany();
        User member = createEmployerMemberUser(company);

        Company result = service.getCompanyProfile(member);

        assertNotNull(result);
        assertEquals("PT Teknologi Mandiri", result.getName());
    }

    @Test
    void harusBisaMengambilCompanyProfileOlehCompanyAdmin() {
        Company company = createVerifiedCompany();
        User admin = createEmployerAdminUser(company);

        Company result = service.getCompanyProfile(admin);

        assertNotNull(result);
        assertEquals("PT Teknologi Mandiri", result.getName());
    }

    @Test
    void harusMenolakCompanyProfileJikaEmployerTidakMemilikiCompany() {
        User employerWithoutCompany = createEmployerAdminUser(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.getCompanyProfile(employerWithoutCompany));

        assertEquals("Akun employer belum terhubung dengan perusahaan", ex.getMessage());
    }

    @Test
    void harusMenolakCompanyProfileOlehNonEmployer() {
        User applicant = createApplicantUser();

        assertThrows(SecurityException.class, () -> service.getCompanyProfile(applicant));
    }

    // ==========================================
    // 3. COMPANY UPDATE TESTS (Section 39)
    // ==========================================

    @Test
    void harusBisaUpdateCompanyProfileOlehCompanyAdmin() {
        Company company = createVerifiedCompany();
        User admin = createEmployerAdminUser(company);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company updated = service.updateCompanyProfile(
                admin,
                "PT Inovasi Digital",
                "E-Commerce",
                "Bandung",
                "https://inovasi.com",
                "contact@inovasi.com",
                "Deskripsi baru perusahaan"
        );

        assertEquals("PT Inovasi Digital", updated.getName());
        assertEquals("E-Commerce", updated.getIndustry());
        assertEquals("Bandung", updated.getLocation());
        assertEquals("https://inovasi.com", updated.getWebsite());
        assertEquals("contact@inovasi.com", updated.getOfficialContact());
        assertEquals("Deskripsi baru perusahaan", updated.getDescription());

        verify(companyRepository).save(company);
    }

    @Test
    void harusMenolakUpdateCompanyProfileOlehMember() {
        Company company = createVerifiedCompany();
        User member = createEmployerMemberUser(company);

        SecurityException ex = assertThrows(SecurityException.class, () -> service.updateCompanyProfile(
                member,
                "PT Diubah",
                "Industri",
                "Lokasi",
                "https://web.com",
                "kontak",
                "deskripsi"
        ));

        assertEquals("Hanya COMPANY_ADMIN yang berhak mengubah profil perusahaan", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakUpdateCompanyProfileJikaCompanyNull() {
        User adminWithoutCompany = createEmployerAdminUser(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateCompanyProfile(
                adminWithoutCompany,
                "PT Diubah",
                "Industri",
                "Lokasi",
                "https://web.com",
                "kontak",
                "deskripsi"
        ));

        assertEquals("Akun employer belum terhubung dengan perusahaan", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakUpdateCompanyProfileOlehNonEmployer() {
        User applicant = createApplicantUser();

        assertThrows(SecurityException.class, () -> service.updateCompanyProfile(
                applicant,
                "PT Diubah",
                "Industri",
                "Lokasi",
                "https://web.com",
                "kontak",
                "deskripsi"
        ));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMempertahankanVerificationStatusSaatCompanyProfileDiubah() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);
        User admin = createEmployerAdminUser(company);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company updated = service.updateCompanyProfile(
                admin,
                "PT Nama Baru",
                "Industri Baru",
                "Lokasi Baru",
                "https://baru.com",
                "kontak@baru.com",
                "Deskripsi Baru"
        );

        assertEquals(CompanyVerificationStatus.VERIFIED, updated.getVerificationStatus());
    }

    @Test
    void harusBisaUpdateCompanyProfilePendingTanpaMengubahStatus() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.PENDING);
        User admin = createEmployerAdminUser(company);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company updated = service.updateCompanyProfile(
                admin,
                "PT Pending Baru",
                "Industri",
                "Lokasi",
                null,
                "kontak",
                "deskripsi"
        );

        assertEquals(CompanyVerificationStatus.PENDING, updated.getVerificationStatus());
    }

    @Test
    void harusBisaUpdateCompanyProfileRejectedTanpaMengubahStatus() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        User admin = createEmployerAdminUser(company);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company updated = service.updateCompanyProfile(
                admin,
                "PT Rejected Baru",
                "Industri",
                "Lokasi",
                null,
                "kontak",
                "deskripsi"
        );

        assertEquals(CompanyVerificationStatus.REJECTED, updated.getVerificationStatus());
    }

    // ==========================================
    // 4. VALIDATION TESTS (Section 40 & 41)
    // ==========================================

    @Test
    void harusMenolakCompanyProfileJikaNameBlank() {
        User admin = createEmployerAdminUser(createVerifiedCompany());

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "", "Industri", "Lokasi", null, "kontak", "deskripsi"));
        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "   ", "Industri", "Lokasi", null, "kontak", "deskripsi"));
        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, null, "Industri", "Lokasi", null, "kontak", "deskripsi"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakCompanyProfileJikaIndustryBlank() {
        User admin = createEmployerAdminUser(createVerifiedCompany());

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "", "Lokasi", null, "kontak", "deskripsi"));
        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "   ", "Lokasi", null, "kontak", "deskripsi"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakCompanyProfileJikaLocationBlank() {
        User admin = createEmployerAdminUser(createVerifiedCompany());

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "", null, "kontak", "deskripsi"));
        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "   ", null, "kontak", "deskripsi"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakCompanyProfileJikaOfficialContactBlank() {
        User admin = createEmployerAdminUser(createVerifiedCompany());

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "Lokasi", null, "", "deskripsi"));
        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "Lokasi", null, "   ", "deskripsi"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakCompanyProfileJikaDescriptionBlank() {
        User admin = createEmployerAdminUser(createVerifiedCompany());

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "Lokasi", null, "kontak", ""));
        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "Lokasi", null, "kontak", "   "));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMengizinkanWebsiteBlankDanDisimpanSebagaiNull() {
        Company company = createVerifiedCompany();
        User admin = createEmployerAdminUser(company);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company updated = service.updateCompanyProfile(
                admin,
                "PT Sukses",
                "Industri",
                "Lokasi",
                "",
                "kontak",
                "deskripsi"
        );

        assertNull(updated.getWebsite());
        verify(companyRepository).save(company);
    }

    @Test
    void harusMenolakCompanyProfileJikaIndustryMelebihi100Karakter() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        String longIndustry = "A".repeat(101);

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", longIndustry, "Lokasi", null, "kontak", "deskripsi"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakCompanyProfileJikaLocationMelebihi255Karakter() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        String longLocation = "A".repeat(256);

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", longLocation, null, "kontak", "deskripsi"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakCompanyProfileJikaWebsiteMelebihi255Karakter() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        String longWebsite = "https://example.com/" + "A".repeat(250);

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "Lokasi", longWebsite, "kontak", "deskripsi"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakCompanyProfileJikaOfficialContactMelebihi255Karakter() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        String longContact = "A".repeat(256);

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "Lokasi", null, longContact, "deskripsi"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakCompanyProfileJikaDescriptionMelebihi2000Karakter() {
        User admin = createEmployerAdminUser(createVerifiedCompany());
        String longDescription = "A".repeat(2001);

        assertThrows(IllegalArgumentException.class, () -> service.updateCompanyProfile(
                admin, "PT Sukses", "Industri", "Lokasi", null, "kontak", longDescription));

        verify(companyRepository, never()).save(any());
    }

    // ==========================================
    // 5. RESUBMIT & INITIAL REGISTRANT TESTS (Section 32)
    // ==========================================

    @Test
    void harusBisaUpdateCompanyProfileOlehInitialRegistrantSaatStatusRejected() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        company.setRejectionReason("Website tidak valid");

        User registrant = createInitialRegistrantUser(company);
        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN)).thenReturn(List.of(registrant));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company updated = service.updateCompanyProfile(
                registrant,
                "PT Diperbaiki",
                "Teknologi",
                "Bandung",
                "https://diperbaiki.com",
                "info@diperbaiki.com",
                "Deskripsi diperbaiki"
        );

        assertNotNull(updated);
        assertEquals("PT Diperbaiki", updated.getName());
        assertEquals("Bandung", updated.getLocation());
        assertEquals(CompanyVerificationStatus.REJECTED, updated.getVerificationStatus());
        assertEquals("Website tidak valid", updated.getRejectionReason()); // Alasan penolakan tidak hilang saat save profile
        verify(companyRepository).save(company);
    }

    @Test
    void harusMenolakUpdateCompanyProfileOlehInitialRegistrantSaatStatusPending() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.PENDING);

        User registrant = createInitialRegistrantUser(company);
        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN)).thenReturn(List.of(registrant));

        SecurityException ex = assertThrows(SecurityException.class, () -> service.updateCompanyProfile(
                registrant,
                "PT Pending",
                "Teknologi",
                "Bandung",
                null,
                "info@pending.com",
                "Deskripsi"
        ));

        assertEquals("Hanya COMPANY_ADMIN yang berhak mengubah profil perusahaan", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakUpdateCompanyProfileJikaPermissionNullTetapiBukanInitialRegistrant() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        User actualRegistrant = createInitialRegistrantUser(company);
        User fakeRegistrant = createInitialRegistrantUser(company);
        fakeRegistrant.setId(99L);
        fakeRegistrant.setEmail("fake@mandiri.com");

        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN)).thenReturn(List.of(actualRegistrant));

        SecurityException ex = assertThrows(SecurityException.class, () -> service.updateCompanyProfile(
                fakeRegistrant,
                "PT Fake",
                "Teknologi",
                "Bandung",
                null,
                "info@fake.com",
                "Deskripsi"
        ));

        assertEquals("Hanya COMPANY_ADMIN yang berhak mengubah profil perusahaan", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusBisaResubmitCompanyVerificationOlehInitialRegistrantSaatStatusRejected() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        company.setRejectionReason("Legalitas belum lengkap");

        User registrant = createInitialRegistrantUser(company);
        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN)).thenReturn(List.of(registrant));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company resubmitted = service.resubmitCompanyVerification(registrant);

        assertNotNull(resubmitted);
        assertEquals(CompanyVerificationStatus.PENDING, resubmitted.getVerificationStatus());
        assertNull(resubmitted.getRejectionReason()); // rejectionReason dibersihkan saat resubmit
        verify(companyRepository).save(company);
    }

    @Test
    void harusBisaResubmitCompanyVerificationOlehCompanyAdminSaatStatusRejected() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        company.setRejectionReason("Kontak perusahaan tidak aktif");

        User admin = createEmployerAdminUser(company);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company resubmitted = service.resubmitCompanyVerification(admin);

        assertNotNull(resubmitted);
        assertEquals(CompanyVerificationStatus.PENDING, resubmitted.getVerificationStatus());
        assertNull(resubmitted.getRejectionReason());
        verify(companyRepository).save(company);
    }

    @Test
    void harusMenolakResubmitOlehMember() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        User member = createEmployerMemberUser(company);

        SecurityException ex = assertThrows(SecurityException.class, () -> service.resubmitCompanyVerification(member));
        assertEquals("Hanya COMPANY_ADMIN atau pendaftar awal yang berhak mengajukan ulang verifikasi perusahaan", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakResubmitOlehUserPermissionNullTetapiBukanInitialRegistrant() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        User actualRegistrant = createInitialRegistrantUser(company);
        User unauthorizedUser = createInitialRegistrantUser(company);
        unauthorizedUser.setId(99L);
        unauthorizedUser.setEmail("unauthorized@mandiri.com");

        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN)).thenReturn(List.of(actualRegistrant));

        SecurityException ex = assertThrows(SecurityException.class, () -> service.resubmitCompanyVerification(unauthorizedUser));
        assertEquals("Hanya COMPANY_ADMIN atau pendaftar awal yang berhak mengajukan ulang verifikasi perusahaan", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakResubmitSaatStatusCompanyPending() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.PENDING);

        User admin = createEmployerAdminUser(company);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.resubmitCompanyVerification(admin));
        assertEquals("Hanya perusahaan dengan status REJECTED yang dapat diajukan ulang verifikasinya", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakResubmitSaatStatusCompanyVerified() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        User admin = createEmployerAdminUser(company);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.resubmitCompanyVerification(admin));
        assertEquals("Hanya perusahaan dengan status REJECTED yang dapat diajukan ulang verifikasinya", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakResubmitJikaCompanyNull() {
        User adminWithoutCompany = createEmployerAdminUser(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.resubmitCompanyVerification(adminWithoutCompany));
        assertEquals("Akun employer belum terhubung dengan perusahaan", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMenolakResubmitOlehNonEmployer() {
        User applicant = createApplicantUser();

        SecurityException ex = assertThrows(SecurityException.class, () -> service.resubmitCompanyVerification(applicant));
        assertEquals("Hanya employer yang dapat mengakses layanan ini", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMemastikanCompanyTargetResubmitBerasalDariCurrentUserCompany() {
        Company companyA = createVerifiedCompany();
        companyA.setId(10L);
        companyA.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        User adminA = createEmployerAdminUser(companyA);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company result = service.resubmitCompanyVerification(adminA);

        assertEquals(10L, result.getId());
        assertEquals(CompanyVerificationStatus.PENDING, result.getVerificationStatus());
        verify(companyRepository).save(companyA);
    }

    @Test
    void harusMempertahankanStatusRejectedDanRejectionReasonSaatSaveCompanyProfile() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        company.setRejectionReason("Dokumen pendukung buram");

        User admin = createEmployerAdminUser(company);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company updated = service.updateCompanyProfile(
                admin,
                "PT Diperbaiki Admin",
                "Industri",
                "Lokasi",
                null,
                "kontak",
                "deskripsi"
        );

        assertEquals(CompanyVerificationStatus.REJECTED, updated.getVerificationStatus());
        assertEquals("Dokumen pendukung buram", updated.getRejectionReason());
        verify(companyRepository).save(company);
    }

    @Test
    void harusMenolakResubmitJikaAdaLebihDariSatuEmployerDanPermissionNull() {
        Company company = createVerifiedCompany();
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        User user1 = createInitialRegistrantUser(company);
        User user2 = createInitialRegistrantUser(company);
        user2.setId(5L);

        when(userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN)).thenReturn(List.of(user1, user2));

        SecurityException ex = assertThrows(SecurityException.class, () -> service.resubmitCompanyVerification(user1));
        assertEquals("Hanya COMPANY_ADMIN atau pendaftar awal yang berhak mengajukan ulang verifikasi perusahaan", ex.getMessage());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void harusMemastikanCapabilityCanEditDanCanResubmitSesuaiStatusDanPermission() {
        Company verifiedCompany = createVerifiedCompany();
        User admin = createEmployerAdminUser(verifiedCompany);
        User member = createEmployerMemberUser(verifiedCompany);

        // 1. COMPANY_ADMIN pada VERIFIED: canEdit = true, canResubmit = false
        assertTrue(service.canEditCompanyProfile(admin));
        assertFalse(service.canResubmitVerification(admin));

        // 2. MEMBER pada VERIFIED: canEdit = false, canResubmit = false
        assertFalse(service.canEditCompanyProfile(member));
        assertFalse(service.canResubmitVerification(member));

        // 3. COMPANY_ADMIN pada REJECTED: canEdit = true, canResubmit = true
        Company rejectedCompany = createVerifiedCompany();
        rejectedCompany.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        User adminRejected = createEmployerAdminUser(rejectedCompany);
        assertTrue(service.canEditCompanyProfile(adminRejected));
        assertTrue(service.canResubmitVerification(adminRejected));

        // 4. INITIAL REGISTRANT pada REJECTED: canEdit = true, canResubmit = true
        User registrantRejected = createInitialRegistrantUser(rejectedCompany);
        when(userRepository.findByCompanyAndRole(rejectedCompany, Role.PEMBERI_LAMARAN))
                .thenReturn(List.of(registrantRejected));
        assertTrue(service.canEditCompanyProfile(registrantRejected));
        assertTrue(service.canResubmitVerification(registrantRejected));

        // 5. INITIAL REGISTRANT pada PENDING: canEdit = false, canResubmit = false
        Company pendingCompany = createVerifiedCompany();
        pendingCompany.setVerificationStatus(CompanyVerificationStatus.PENDING);
        User registrantPending = createInitialRegistrantUser(pendingCompany);
        when(userRepository.findByCompanyAndRole(pendingCompany, Role.PEMBERI_LAMARAN))
                .thenReturn(List.of(registrantPending));
        assertFalse(service.canEditCompanyProfile(registrantPending));
        assertFalse(service.canResubmitVerification(registrantPending));

        // 6. MEMBER pada REJECTED: canEdit = false, canResubmit = false
        User memberRejected = createEmployerMemberUser(rejectedCompany);
        assertFalse(service.canEditCompanyProfile(memberRejected));
        assertFalse(service.canResubmitVerification(memberRejected));

        // 7. Null checks
        assertFalse(service.canEditCompanyProfile(null));
        assertFalse(service.canResubmitVerification(null));
        assertFalse(service.canEditCompanyProfile(createApplicantUser()));
        assertFalse(service.canResubmitVerification(createApplicantUser()));
    }
}
