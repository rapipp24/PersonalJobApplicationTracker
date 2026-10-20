package com.example.service;

import com.example.entity.Company;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
