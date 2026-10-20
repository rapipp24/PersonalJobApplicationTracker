package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.JobApplicationRepository;
import com.example.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobPostingServiceTest {

    private JobPostingRepository jobPostingRepository;
    private JobApplicationRepository jobApplicationRepository;
    private JobPostingService jobPostingService;

    @BeforeEach
    void setUp() {
        jobPostingRepository = Mockito.mock(JobPostingRepository.class);
        jobApplicationRepository = Mockito.mock(JobApplicationRepository.class);
        jobPostingService = new JobPostingService(jobPostingRepository, jobApplicationRepository);
    }

    private User createEmployerWithCompanyStatus(CompanyVerificationStatus status) {
        Company company = new Company();
        company.setId(1L);
        company.setName("PT Test Company");
        company.setVerificationStatus(status);

        User employer = new User();
        employer.setId(1L);
        employer.setName("Test Employer");
        employer.setCompany(company);
        if (status == CompanyVerificationStatus.VERIFIED) {
            employer.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
            employer.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);
        }

        return employer;
    }

    private Company createCompany(Long id, String name, CompanyVerificationStatus status) {
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        company.setVerificationStatus(status);
        return company;
    }

    private User createEmployer(Long id, String name, Company company, CompanyPermission permission, CompanyMembershipStatus membershipStatus) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setCompany(company);
        user.setCompanyPermission(permission);
        user.setMembershipStatus(membershipStatus);
        return user;
    }

    @Test
    void harusBisaHapusLowonganTanpaLamaran() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(100L);
        jobPosting.setPosition("Java Developer");
        jobPosting.setEmployer(employer);
        jobPosting.setCompany(employer.getCompany());

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobPosting));
        when(jobApplicationRepository.existsByJobPosting(jobPosting)).thenReturn(false);

        jobPostingService.hapusLowongan(jobPosting, employer);

        verify(jobPostingRepository).delete(jobPosting);
    }

    @Test
    void harusMenolakHapusLowonganYangSudahMemilikiLamaran() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(100L);
        jobPosting.setPosition("Java Developer");
        jobPosting.setEmployer(employer);
        jobPosting.setCompany(employer.getCompany());

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobPosting));
        when(jobApplicationRepository.existsByJobPosting(jobPosting)).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.hapusLowongan(jobPosting, employer)
        );

        verify(jobPostingRepository, never()).delete(any());
    }

    @Test
    void harusBisaHapusLowonganOlehCreator() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("Java Developer");
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));
        when(jobApplicationRepository.existsByJobPosting(jobX)).thenReturn(false);

        jobPostingService.hapusLowongan(jobX, wanda);

        verify(jobPostingRepository).delete(jobX);
    }

    @Test
    void harusBisaHapusLowonganOlehCompanyAdminSatuCompany() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);
        User adminA = createEmployer(1L, "Admin A", companyA, CompanyPermission.COMPANY_ADMIN, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("Java Developer");
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));
        when(jobApplicationRepository.existsByJobPosting(jobX)).thenReturn(false);

        jobPostingService.hapusLowongan(jobX, adminA);

        verify(jobPostingRepository).delete(jobX);
    }

    @Test
    void harusMenolakHapusLowonganOlehMemberNonCreator() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);
        User memberB = createEmployer(2L, "Member B", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("Java Developer");
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));
        when(jobApplicationRepository.existsByJobPosting(jobX)).thenReturn(false);

        assertThrows(
                SecurityException.class,
                () -> jobPostingService.hapusLowongan(jobX, memberB)
        );

        verify(jobPostingRepository, never()).delete(any());
    }

    @Test
    void harusMenolakHapusLowonganOlehCompanyAdminBedaCompany() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        Company companyB = createCompany(2L, "Company B", CompanyVerificationStatus.VERIFIED);
        User adminCompanyB = createEmployer(4L, "Admin B", companyB, CompanyPermission.COMPANY_ADMIN, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("Java Developer");
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));

        assertThrows(
                SecurityException.class,
                () -> jobPostingService.hapusLowongan(jobX, adminCompanyB)
        );

        verify(jobPostingRepository, never()).delete(any());
    }

    @Test
    void harusBisaMengecekApakahLowonganMemilikiLamaran() {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(100L);

        when(jobApplicationRepository.existsByJobPosting(jobPosting)).thenReturn(true);

        boolean hasApp = jobPostingService.hasApplications(jobPosting);

        assertTrue(hasApp);
        verify(jobApplicationRepository).existsByJobPosting(jobPosting);

        when(jobApplicationRepository.existsByJobPosting(jobPosting)).thenReturn(false);
        assertFalse(jobPostingService.hasApplications(jobPosting));
    }

    @Test
    void harusBisaSimpanDanAmbilLowongan() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Frontend Developer");

        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));

        JobPosting saved = jobPostingService.simpanLowongan(jobPosting, employer);

        assertNotNull(saved);
        assertEquals("Frontend Developer", saved.getPosition());
        assertEquals(employer, saved.getEmployer());
        assertEquals(employer.getCompany(), saved.getCompany());
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void harusBisaAmbilLowonganAktif() {
        JobPosting job1 = new JobPosting();
        job1.setActive(true);

        when(jobPostingRepository.findByActiveTrue()).thenReturn(List.of(job1));

        List<JobPosting> active = jobPostingService.findActiveJobPostings();

        assertEquals(1, active.size());
        verify(jobPostingRepository).findByActiveTrue();
    }

    @Test
    void harusBisaMencariLowonganBerdasarkanId() {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(50L);
        jobPosting.setPosition("DevOps Engineer");

        when(jobPostingRepository.findById(50L)).thenReturn(Optional.of(jobPosting));

        Optional<JobPosting> found = jobPostingService.findById(50L);

        assertTrue(found.isPresent());
        assertEquals("DevOps Engineer", found.get().getPosition());
        verify(jobPostingRepository).findById(50L);
    }

    @Test
    void harusBisaAmbilLowonganBerdasarkanEmployer() {
        User employer = new User();
        employer.setId(1L);

        JobPosting job1 = new JobPosting();
        job1.setId(10L);
        job1.setEmployer(employer);

        when(jobPostingRepository.findByEmployer(employer)).thenReturn(List.of(job1));

        List<JobPosting> hasil = jobPostingService.findByEmployer(employer);

        assertEquals(1, hasil.size());
        verify(jobPostingRepository).findByEmployer(employer);
    }

    @Test
    void harusMengembalikanListKosongJikaEmployerNullSaatAmbilLowongan() {
        List<JobPosting> hasilNull = jobPostingService.findByEmployer(null);
        assertTrue(hasilNull.isEmpty());

        User employerTanpaId = new User();
        employerTanpaId.setId(null);

        List<JobPosting> hasilTanpaId = jobPostingService.findByEmployer(employerTanpaId);
        assertTrue(hasilTanpaId.isEmpty());

        verify(jobPostingRepository, never()).findByEmployer(any());
    }

    @Test
    void harusBisaSimpanLowonganJikaCompanyVerified() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Backend Developer");

        when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(jobPosting);

        JobPosting saved = jobPostingService.simpanLowongan(jobPosting, employer);

        assertNotNull(saved);
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void harusMenolakSimpanLowonganJikaCompanyPending() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.PENDING);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Backend Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk menyimpan lowongan pekerjaan", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakSimpanLowonganJikaCompanyRejected() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.REJECTED);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Backend Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk menyimpan lowongan pekerjaan", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakSimpanLowonganJikaCompanyNull() {
        User employer = new User();
        employer.setId(1L);
        employer.setCompany(null);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Backend Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Akun employer belum terhubung dengan data perusahaan", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakSimpanLowonganJikaVerificationStatusNull() {
        User employer = createEmployerWithCompanyStatus(null);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Backend Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk menyimpan lowongan pekerjaan", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakSimpanLowonganJikaEmployerNull() {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Backend Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, null)
        );

        assertEquals("Employer tidak boleh null untuk menyimpan lowongan", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakSimpanLowonganJikaJobPostingNull() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> jobPostingService.simpanLowongan(null, employer)
        );

        assertEquals("Lowongan pekerjaan tidak boleh null", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusBisaAktivasiLowonganJikaCompanyVerified() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);

        JobPosting existing = new JobPosting();
        existing.setId(10L);
        existing.setPosition("QA Engineer");
        existing.setActive(false);
        existing.setEmployer(employer);
        existing.setCompany(employer.getCompany());

        when(jobPostingRepository.findById(10L)).thenReturn(Optional.of(existing));

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(10L);
        jobPosting.setPosition("QA Engineer");
        jobPosting.setActive(true);

        when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(jobPosting);

        JobPosting saved = jobPostingService.simpanLowongan(jobPosting, employer);

        assertNotNull(saved);
        assertTrue(saved.isActive());
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void harusMenolakAktivasiLowonganJikaCompanyPending() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.PENDING);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(10L);
        jobPosting.setPosition("QA Engineer");
        jobPosting.setActive(false);
        jobPosting.setEmployer(employer);

        jobPosting.setActive(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk menyimpan lowongan pekerjaan", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusBisaSimpanLowonganJikaCompanyVerifiedDanMembershipActive() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);
        employer.setMembershipStatus(CompanyMembershipStatus.ACTIVE);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Fullstack Developer");

        when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(jobPosting);

        JobPosting saved = jobPostingService.simpanLowongan(jobPosting, employer);

        assertNotNull(saved);
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void harusMenolakSimpanLowonganJikaCompanyVerifiedTapiMembershipPending() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);
        employer.setMembershipStatus(CompanyMembershipStatus.PENDING);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Fullstack Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakSimpanLowonganJikaCompanyVerifiedTapiMembershipRejected() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);
        employer.setMembershipStatus(CompanyMembershipStatus.REJECTED);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Fullstack Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakSimpanLowonganJikaCompanyVerifiedTapiMembershipNull() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);
        employer.setMembershipStatus(null);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Fullstack Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakSimpanLowonganJikaCompanyPendingDanMembershipPending() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.PENDING);
        employer.setMembershipStatus(CompanyMembershipStatus.PENDING);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Fullstack Developer");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.simpanLowongan(jobPosting, employer)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk menyimpan lowongan pekerjaan", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenyimpanCompanyDanEmployerSaatCreateLowongan() {
        User employer = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Data Scientist");

        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));

        JobPosting saved = jobPostingService.simpanLowongan(jobPosting, employer);

        assertNotNull(saved);
        assertEquals(employer, saved.getEmployer());
        assertEquals(employer.getCompany(), saved.getCompany());
        assertEquals("PT Test Company", saved.getCompanyName());
    }

    @Test
    void harusBisaCreateOlehMemberBukanAdmin() {
        User member = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);
        member.setCompanyPermission(CompanyPermission.MEMBER);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Mobile Developer");

        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));

        JobPosting saved = jobPostingService.simpanLowongan(jobPosting, member);

        assertNotNull(saved);
        assertEquals(member, saved.getEmployer());
        assertEquals(member.getCompany(), saved.getCompany());
    }

    @Test
    void harusBisaAmbilLowonganSatuCompanyOlehEmployerLain() {
        Company company = new Company();
        company.setId(1L);
        company.setName("PT Satu Visi");

        JobPosting jobA = new JobPosting();
        jobA.setId(10L);
        jobA.setPosition("Product Manager");
        jobA.setCompany(company);

        when(jobPostingRepository.findByCompany(company)).thenReturn(List.of(jobA));

        List<JobPosting> hasil = jobPostingService.findByCompany(company);

        assertEquals(1, hasil.size());
        assertEquals("Product Manager", hasil.get(0).getPosition());
        verify(jobPostingRepository).findByCompany(company);
    }

    @Test
    void harusTidakMenampilkanLowonganCompanyLain() {
        Company companyA = new Company();
        companyA.setId(1L);

        Company companyB = new Company();
        companyB.setId(2L);

        JobPosting jobB = new JobPosting();
        jobB.setId(20L);
        jobB.setPosition("HR Specialist");
        jobB.setCompany(companyB);

        when(jobPostingRepository.findByCompany(companyA)).thenReturn(List.of());
        when(jobPostingRepository.findByCompany(companyB)).thenReturn(List.of(jobB));

        List<JobPosting> listA = jobPostingService.findByCompany(companyA);
        List<JobPosting> listB = jobPostingService.findByCompany(companyB);

        assertTrue(listA.isEmpty());
        assertEquals(1, listB.size());
    }

    @Test
    void harusBisaEditLowonganOlehCreator() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("Old Position");
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));

        JobPosting updateData = new JobPosting();
        updateData.setId(100L);
        updateData.setPosition("New Position");

        JobPosting saved = jobPostingService.simpanLowongan(updateData, wanda);

        assertEquals("New Position", saved.getPosition());
        assertEquals(wanda, saved.getEmployer());
        assertEquals(companyA, saved.getCompany());
        verify(jobPostingRepository).save(updateData);
    }

    @Test
    void harusBisaEditLowonganOlehCompanyAdminSatuCompany() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);
        User adminA = createEmployer(1L, "Admin A", companyA, CompanyPermission.COMPANY_ADMIN, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("Old Position");
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));

        JobPosting updateData = new JobPosting();
        updateData.setId(100L);
        updateData.setPosition("Admin Updated Position");

        JobPosting saved = jobPostingService.simpanLowongan(updateData, adminA);

        assertEquals("Admin Updated Position", saved.getPosition());
        assertEquals(wanda, saved.getEmployer());
        assertEquals(companyA, saved.getCompany());
        verify(jobPostingRepository).save(updateData);
    }

    @Test
    void harusMenolakEditLowonganOlehMemberNonCreator() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);
        User memberB = createEmployer(2L, "Member B", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("Developer");
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));

        JobPosting updateData = new JobPosting();
        updateData.setId(100L);
        updateData.setPosition("Hacked Position");

        assertThrows(
                SecurityException.class,
                () -> jobPostingService.simpanLowongan(updateData, memberB)
        );

        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakEditLowonganOlehCompanyAdminBedaCompany() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        Company companyB = createCompany(2L, "Company B", CompanyVerificationStatus.VERIFIED);
        User adminCompanyB = createEmployer(4L, "Admin B", companyB, CompanyPermission.COMPANY_ADMIN, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("Developer");
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));

        JobPosting updateData = new JobPosting();
        updateData.setId(100L);
        updateData.setPosition("Cross Company Edit");

        assertThrows(
                SecurityException.class,
                () -> jobPostingService.simpanLowongan(updateData, adminCompanyB)
        );

        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusBisaToggleOlehCreator() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("QA Engineer");
        jobX.setActive(true);
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));

        JobPosting toggleData = new JobPosting();
        toggleData.setId(100L);
        toggleData.setActive(false);

        JobPosting saved = jobPostingService.simpanLowongan(toggleData, wanda);

        assertFalse(saved.isActive());
        verify(jobPostingRepository).save(toggleData);
    }

    @Test
    void harusBisaToggleOlehCompanyAdminSatuCompany() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);
        User adminA = createEmployer(1L, "Admin A", companyA, CompanyPermission.COMPANY_ADMIN, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("QA Engineer");
        jobX.setActive(true);
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));

        JobPosting toggleData = new JobPosting();
        toggleData.setId(100L);
        toggleData.setActive(false);

        JobPosting saved = jobPostingService.simpanLowongan(toggleData, adminA);

        assertFalse(saved.isActive());
        verify(jobPostingRepository).save(toggleData);
    }

    @Test
    void harusMenolakToggleOlehMemberNonCreator() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);
        User memberB = createEmployer(2L, "Member B", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("QA Engineer");
        jobX.setActive(true);
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));

        JobPosting toggleData = new JobPosting();
        toggleData.setId(100L);
        toggleData.setActive(false);

        assertThrows(
                SecurityException.class,
                () -> jobPostingService.simpanLowongan(toggleData, memberB)
        );

        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMenolakToggleOlehCompanyAdminBedaCompany() {
        Company companyA = createCompany(1L, "Company A", CompanyVerificationStatus.VERIFIED);
        User wanda = createEmployer(3L, "Wanda", companyA, CompanyPermission.MEMBER, CompanyMembershipStatus.ACTIVE);

        Company companyB = createCompany(2L, "Company B", CompanyVerificationStatus.VERIFIED);
        User adminCompanyB = createEmployer(4L, "Admin B", companyB, CompanyPermission.COMPANY_ADMIN, CompanyMembershipStatus.ACTIVE);

        JobPosting jobX = new JobPosting();
        jobX.setId(100L);
        jobX.setPosition("QA Engineer");
        jobX.setActive(true);
        jobX.setEmployer(wanda);
        jobX.setCompany(companyA);

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(jobX));

        JobPosting toggleData = new JobPosting();
        toggleData.setId(100L);
        toggleData.setActive(false);

        assertThrows(
                SecurityException.class,
                () -> jobPostingService.simpanLowongan(toggleData, adminCompanyB)
        );

        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void harusMengenaliCompanyPostingLegacyDariEmployer() {
        User creator = createEmployerWithCompanyStatus(CompanyVerificationStatus.VERIFIED);

        JobPosting legacyPosting = new JobPosting();
        legacyPosting.setId(99L);
        legacyPosting.setCompany(null);
        legacyPosting.setEmployer(creator);

        Company detected = jobPostingService.getJobPostingCompany(legacyPosting);

        assertNotNull(detected);
        assertEquals(creator.getCompany().getId(), detected.getId());
        assertEquals("PT Test Company", detected.getName());
    }
}
