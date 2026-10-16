package com.example.service;

import com.example.dto.CompanyRegistrationType;
import com.example.dto.RegistrationFormData;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrationServiceTest {

    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private PasswordEncoder passwordEncoder;
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        companyRepository = Mockito.mock(CompanyRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        registrationService = new RegistrationService(userRepository, companyRepository, passwordEncoder);
    }

    @Test
    void testRegisterApplicantBerhasil() {
        String name = "Test Applicant";
        String email = "applicant@example.com";
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPasswordHash";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = registrationService.register(name, email, rawPassword, Role.PELAMAR, null);

        assertNotNull(registeredUser);
        assertEquals(name, registeredUser.getName());
        assertEquals(email, registeredUser.getEmail());
        assertEquals(encodedPassword, registeredUser.getPassword());
        assertEquals(Role.PELAMAR, registeredUser.getRole());
        assertNull(registeredUser.getCompany());

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void testRegisterEmployerBerhasil() {
        String name = "Test Employer";
        String email = "employer@example.com";
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPasswordHash";
        String companyName = "PT Maju Jaya";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = registrationService.register(name, email, rawPassword, Role.PEMBERI_LAMARAN, companyName);

        assertNotNull(registeredUser);
        assertEquals(name, registeredUser.getName());
        assertEquals(email, registeredUser.getEmail());
        assertEquals(encodedPassword, registeredUser.getPassword());
        assertEquals(Role.PEMBERI_LAMARAN, registeredUser.getRole());
        assertNotNull(registeredUser.getCompany());
        assertEquals(companyName, registeredUser.getCompany().getName());

        verify(passwordEncoder).encode(rawPassword);
        verify(companyRepository).save(any(Company.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmailDitolak() {
        String email = "duplicate@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> registrationService.register("User Baru", email, "password123", Role.PELAMAR, null)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void testRegisterAdminDitolak() {
        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.register("Admin Baru", "admin2@example.com", "password123", Role.ADMIN, null)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void testFindVerifiedCompaniesDenganKeywordKosongMengambilSemuaVerified() {
        Company comp = new Company();
        comp.setName("PT Terverifikasi");
        comp.setVerificationStatus(com.example.entity.CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findByVerificationStatus(com.example.entity.CompanyVerificationStatus.VERIFIED))
                .thenReturn(java.util.List.of(comp));

        java.util.List<Company> resultEmpty = registrationService.findVerifiedCompanies("");
        assertEquals(1, resultEmpty.size());
        assertEquals("PT Terverifikasi", resultEmpty.get(0).getName());

        java.util.List<Company> resultNull = registrationService.findVerifiedCompanies(null);
        assertEquals(1, resultNull.size());

        verify(companyRepository, Mockito.times(2))
                .findByVerificationStatus(com.example.entity.CompanyVerificationStatus.VERIFIED);
        verify(companyRepository, never())
                .findByNameContainingIgnoreCaseAndVerificationStatus(any(), any());
    }

    @Test
    void testFindVerifiedCompaniesDenganKeywordMemanggilSearchVerifiedSaja() {
        Company comp = new Company();
        comp.setName("PT Maju Makmur");
        comp.setVerificationStatus(com.example.entity.CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findByNameContainingIgnoreCaseAndVerificationStatus(
                "Maju", com.example.entity.CompanyVerificationStatus.VERIFIED))
                .thenReturn(java.util.List.of(comp));

        java.util.List<Company> result = registrationService.findVerifiedCompanies("  Maju  ");
        assertEquals(1, result.size());
        assertEquals("PT Maju Makmur", result.get(0).getName());

        verify(companyRepository).findByNameContainingIgnoreCaseAndVerificationStatus(
                "Maju", com.example.entity.CompanyVerificationStatus.VERIFIED);
        verify(companyRepository, never()).findByVerificationStatus(any());
    }

    @Test
    void testRegisterEmployerWithNewCompanyBerhasil() {
        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Baru");
        formData.setEmail("newemployer@example.com");
        formData.setPassword("secret123");
        formData.setConfirmPassword("secret123");
        formData.setAccountType(Role.PEMBERI_LAMARAN);
        formData.setCompanyRegistrationType(CompanyRegistrationType.REGISTER_NEW_COMPANY);
        formData.setCompanyName("PT Inovasi Teknologi");
        formData.setIndustry("Teknologi Informasi");
        formData.setLocation("Bandung");
        formData.setWebsite("https://inovasi.co.id");
        formData.setOfficialContact("contact@inovasi.co.id");
        formData.setDescription("Perusahaan software development");

        String encodedPassword = "$2a$10$encodedPasswordHash";

        when(userRepository.existsByEmail("newemployer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn(encodedPassword);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = registrationService.registerEmployerWithNewCompany(formData);

        assertNotNull(result);
        assertEquals("Employer Baru", result.getName());
        assertEquals("newemployer@example.com", result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(Role.PEMBERI_LAMARAN, result.getRole());
        assertNull(result.getCompanyPermission());
        assertEquals(CompanyMembershipStatus.PENDING, result.getMembershipStatus());

        Company savedComp = result.getCompany();
        assertNotNull(savedComp);
        assertEquals("PT Inovasi Teknologi", savedComp.getName());
        assertEquals("Teknologi Informasi", savedComp.getIndustry());
        assertEquals("Bandung", savedComp.getLocation());
        assertEquals("https://inovasi.co.id", savedComp.getWebsite());
        assertEquals("contact@inovasi.co.id", savedComp.getOfficialContact());
        assertEquals("Perusahaan software development", savedComp.getDescription());
        assertEquals(CompanyVerificationStatus.PENDING, savedComp.getVerificationStatus());
        assertNull(savedComp.getRejectionReason());

        verify(passwordEncoder).encode("secret123");
        verify(companyRepository).save(any(Company.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithNewCompanyFieldWajibKosongDitolak() {
        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Baru");
        formData.setEmail("newemployer@example.com");
        formData.setPassword("secret123");
        formData.setCompanyName("");

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerEmployerWithNewCompany(formData)
        );

        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithNewCompanyDuplicateEmailDitolak() {
        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Baru");
        formData.setEmail("existing@example.com");
        formData.setPassword("secret123");
        formData.setCompanyName("PT Inovasi");
        formData.setIndustry("Teknologi Informasi");
        formData.setLocation("Jakarta");
        formData.setOfficialContact("hr@inovasi.com");
        formData.setDescription("Deskripsi perusahaan");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithNewCompany(formData)
        );

        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDuplicateWebsiteDomainDitolak() {
        Company existing = new Company();
        existing.setName("PT Muara Buana");
        existing.setWebsite("https://www.muarabuana.co.id/");
        existing.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findByNameIgnoreCase("pt muara buana")).thenReturn(List.of(existing));

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Baru");
        formData.setEmail("new@example.com");
        formData.setPassword("secret123");
        formData.setCompanyName("pt muara buana");
        formData.setWebsite("muarabuana.co.id");
        formData.setIndustry("Teknologi Informasi");
        formData.setLocation("Jakarta");
        formData.setOfficialContact("0812345678");
        formData.setDescription("Deskripsi perusahaan");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithNewCompany(formData)
        );

        assertEquals("Company sudah terdaftar. Silakan gunakan Join Existing Company.", ex.getMessage());
        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDuplicateCorporateEmailDomainDitolak() {
        Company existing = new Company();
        existing.setName("PT Muara Buana");
        existing.setOfficialContact("contact@muarabuana.co.id");
        existing.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findByNameIgnoreCase("PT Muara Buana")).thenReturn(List.of(existing));

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Baru");
        formData.setEmail("new@example.com");
        formData.setPassword("secret123");
        formData.setCompanyName("PT Muara Buana");
        formData.setOfficialContact("hr@muarabuana.co.id");
        formData.setIndustry("Teknologi Informasi");
        formData.setLocation("Bandung");
        formData.setDescription("Deskripsi perusahaan");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithNewCompany(formData)
        );

        assertEquals("Company sudah terdaftar. Silakan gunakan Join Existing Company.", ex.getMessage());
        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testPublicEmailDomainBukanCorporateIndicatorTidakDuplicate() {
        Company existing = new Company();
        existing.setName("PT Solusi A");
        existing.setOfficialContact("companyA@gmail.com");
        existing.setIndustry("Teknologi Informasi");
        existing.setLocation("Jakarta");
        existing.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findByNameIgnoreCase("PT Solusi A")).thenReturn(List.of(existing));
        when(userRepository.existsByEmail("solusi-b@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(companyRepository.save(any(Company.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Solusi");
        formData.setEmail("solusi-b@example.com");
        formData.setPassword("password123");
        formData.setCompanyName("PT Solusi A");
        formData.setOfficialContact("companyB@gmail.com");
        formData.setIndustry("Pendidikan");
        formData.setLocation("Surabaya");
        formData.setDescription("Deskripsi perusahaan");

        User registeredUser = registrationService.registerEmployerWithNewCompany(formData);

        assertNotNull(registeredUser);
        verify(companyRepository).save(any(Company.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDuplicateFallbackNameIndustryLocationDitolak() {
        Company existing = new Company();
        existing.setName("PT Makmur Sejahtera");
        existing.setIndustry("Manufaktur");
        existing.setLocation("Semarang");
        existing.setOfficialContact("024-123456");
        existing.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findByNameIgnoreCase("pt makmur sejahtera")).thenReturn(List.of(existing));

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Baru");
        formData.setEmail("new@example.com");
        formData.setPassword("secret123");
        formData.setCompanyName("pt makmur sejahtera");
        formData.setIndustry("manufaktur");
        formData.setLocation("semarang");
        formData.setOfficialContact("0812345678");
        formData.setDescription("Deskripsi perusahaan");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithNewCompany(formData)
        );

        assertEquals("Company sudah terdaftar. Silakan gunakan Join Existing Company.", ex.getMessage());
        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testNameSamaTetapiIndikatorLainBerbedaBukanDuplicate() {
        Company existing = new Company();
        existing.setName("PT Cemerlang");
        existing.setWebsite("https://cemerlang-abadi.com");
        existing.setOfficialContact("info@cemerlang-abadi.com");
        existing.setIndustry("Keuangan");
        existing.setLocation("Jakarta");
        existing.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findByNameIgnoreCase("PT Cemerlang")).thenReturn(List.of(existing));
        when(userRepository.existsByEmail("cemerlang2@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(companyRepository.save(any(Company.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Cemerlang");
        formData.setEmail("cemerlang2@example.com");
        formData.setPassword("password123");
        formData.setCompanyName("PT Cemerlang");
        formData.setWebsite("https://cemerlang-kreatif.com");
        formData.setOfficialContact("info@cemerlang-kreatif.com");
        formData.setIndustry("Media & Kreatif");
        formData.setLocation("Bandung");
        formData.setDescription("Deskripsi perusahaan");

        User registeredUser = registrationService.registerEmployerWithNewCompany(formData);

        assertNotNull(registeredUser);
        verify(companyRepository).save(any(Company.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDuplicateVerifiedMenghasilkanPesanJoinExisting() {
        Company existing = new Company();
        existing.setName("PT Sukses Bersama");
        existing.setWebsite("www.suksesbersama.com");
        existing.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        when(companyRepository.findByNameIgnoreCase("PT Sukses Bersama")).thenReturn(List.of(existing));

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Sukses");
        formData.setEmail("sukses@example.com");
        formData.setPassword("password123");
        formData.setCompanyName("PT Sukses Bersama");
        formData.setWebsite("https://suksesbersama.com");
        formData.setIndustry("Teknologi Informasi");
        formData.setLocation("Jakarta");
        formData.setOfficialContact("contact@suksesbersama.com");
        formData.setDescription("Deskripsi perusahaan");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithNewCompany(formData)
        );

        assertEquals("Company sudah terdaftar. Silakan gunakan Join Existing Company.", ex.getMessage());
        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDuplicatePendingMenghasilkanPesanProsesVerifikasi() {
        Company existing = new Company();
        existing.setName("PT Muara Buana");
        existing.setWebsite("https://www.muarabuana.co.id/");
        existing.setVerificationStatus(CompanyVerificationStatus.PENDING);

        when(companyRepository.findByNameIgnoreCase("pt muara buana")).thenReturn(List.of(existing));

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Employer Baru");
        formData.setEmail("new@example.com");
        formData.setPassword("secret123");
        formData.setCompanyName("pt muara buana");
        formData.setWebsite("muarabuana.co.id");
        formData.setIndustry("Teknologi Informasi");
        formData.setLocation("Jakarta");
        formData.setOfficialContact("hr@muarabuana.co.id");
        formData.setDescription("Deskripsi perusahaan");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithNewCompany(formData)
        );

        assertEquals("Company tersebut sedang dalam proses verifikasi.", ex.getMessage());
        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithExistingCompanyBerhasil() {
        Long companyId = 55L;
        Company verifiedCompany = new Company();
        verifiedCompany.setId(companyId);
        verifiedCompany.setName("PT Mitra Sejahtera");
        verifiedCompany.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Karyawan Baru");
        formData.setEmail("karyawan@example.com");
        formData.setPassword("password123");
        formData.setConfirmPassword("password123");
        formData.setAccountType(Role.PEMBERI_LAMARAN);
        formData.setCompanyRegistrationType(CompanyRegistrationType.JOIN_EXISTING_COMPANY);
        formData.setSelectedCompanyId(companyId);
        formData.setCompanyName("PT Mitra Sejahtera");

        String encodedPassword = "$2a$10$encodedPasswordHashJoin";

        when(userRepository.existsByEmail("karyawan@example.com")).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(verifiedCompany));
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = registrationService.registerEmployerWithExistingCompany(formData);

        assertNotNull(result);
        assertEquals("Karyawan Baru", result.getName());
        assertEquals("karyawan@example.com", result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(Role.PEMBERI_LAMARAN, result.getRole());
        assertEquals(verifiedCompany, result.getCompany());
        assertEquals(CompanyPermission.MEMBER, result.getCompanyPermission());
        assertEquals(CompanyMembershipStatus.PENDING, result.getMembershipStatus());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void testRegisterEmployerWithExistingCompanyIdNullDitolak() {
        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Karyawan Baru");
        formData.setEmail("karyawan@example.com");
        formData.setPassword("password123");
        formData.setSelectedCompanyId(null);

        when(userRepository.existsByEmail("karyawan@example.com")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerEmployerWithExistingCompany(formData)
        );

        assertEquals("Company must be selected.", ex.getMessage());
        verify(companyRepository, never()).findById(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithExistingCompanyTidakDitemukanDitolak() {
        Long companyId = 999L;
        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Karyawan Baru");
        formData.setEmail("karyawan@example.com");
        formData.setPassword("password123");
        formData.setSelectedCompanyId(companyId);

        when(userRepository.existsByEmail("karyawan@example.com")).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerEmployerWithExistingCompany(formData)
        );

        assertEquals("Selected company not found.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithExistingCompanyStatusPendingDitolak() {
        Long companyId = 77L;
        Company pendingCompany = new Company();
        pendingCompany.setId(companyId);
        pendingCompany.setName("PT Belum Verified");
        pendingCompany.setVerificationStatus(CompanyVerificationStatus.PENDING);

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Karyawan Baru");
        formData.setEmail("karyawan@example.com");
        formData.setPassword("password123");
        formData.setSelectedCompanyId(companyId);

        when(userRepository.existsByEmail("karyawan@example.com")).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(pendingCompany));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithExistingCompany(formData)
        );

        assertEquals("Only VERIFIED companies can be joined.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithExistingCompanyStatusRejectedDitolak() {
        Long companyId = 88L;
        Company rejectedCompany = new Company();
        rejectedCompany.setId(companyId);
        rejectedCompany.setName("PT Ditolak");
        rejectedCompany.setVerificationStatus(CompanyVerificationStatus.REJECTED);

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Karyawan Baru");
        formData.setEmail("karyawan@example.com");
        formData.setPassword("password123");
        formData.setSelectedCompanyId(companyId);

        when(userRepository.existsByEmail("karyawan@example.com")).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(rejectedCompany));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithExistingCompany(formData)
        );

        assertEquals("Only VERIFIED companies can be joined.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithExistingCompanyStatusNullDitolak() {
        Long companyId = 89L;
        Company nullStatusCompany = new Company();
        nullStatusCompany.setId(companyId);
        nullStatusCompany.setName("PT Status Null");
        nullStatusCompany.setVerificationStatus(null);

        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Karyawan Baru");
        formData.setEmail("karyawan@example.com");
        formData.setPassword("password123");
        formData.setSelectedCompanyId(companyId);

        when(userRepository.existsByEmail("karyawan@example.com")).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(nullStatusCompany));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithExistingCompany(formData)
        );

        assertEquals("Only VERIFIED companies can be joined.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithExistingCompanyDuplicateEmailDitolak() {
        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Karyawan Baru");
        formData.setEmail("duplikat@example.com");
        formData.setPassword("password123");
        formData.setSelectedCompanyId(55L);

        when(userRepository.existsByEmail("duplikat@example.com")).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registrationService.registerEmployerWithExistingCompany(formData)
        );

        assertEquals("Email is already registered.", ex.getMessage());
        verify(companyRepository, never()).findById(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmployerWithExistingCompanyFieldWajibKosongDitolak() {
        // formData null
        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerEmployerWithExistingCompany(null)
        );

        // email null / kosong
        RegistrationFormData noEmail = new RegistrationFormData();
        noEmail.setFullName("Karyawan");
        noEmail.setPassword("password123");
        noEmail.setEmail("");
        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerEmployerWithExistingCompany(noEmail)
        );

        // password kosong
        RegistrationFormData noPassword = new RegistrationFormData();
        noPassword.setFullName("Karyawan");
        noPassword.setEmail("karyawan@example.com");
        noPassword.setPassword("");
        noPassword.setSelectedCompanyId(1L);
        Company comp = new Company();
        comp.setVerificationStatus(CompanyVerificationStatus.VERIFIED);
        when(userRepository.existsByEmail("karyawan@example.com")).thenReturn(false);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(comp));

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerEmployerWithExistingCompany(noPassword)
        );

        // fullName kosong
        RegistrationFormData noName = new RegistrationFormData();
        noName.setFullName("");
        noName.setEmail("karyawan@example.com");
        noName.setPassword("password123");
        noName.setSelectedCompanyId(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerEmployerWithExistingCompany(noName)
        );
    }
}
