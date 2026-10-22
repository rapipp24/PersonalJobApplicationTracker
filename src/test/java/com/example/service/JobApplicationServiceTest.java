package com.example.service;

import com.example.entity.ApplicantProfile;
import com.example.entity.ApplicationStatus;
import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.JobApplicationRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobApplicationServiceTest {

    @Test
    void harusBisaMenyimpanLamaran() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        JobApplication jobApplication =
                new JobApplication();

        service.simpanLamaran(jobApplication);

        verify(repository).save(jobApplication);
    }


    @Test
    void harusBisaMenghapusLamaran() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        JobApplication jobApplication =
                new JobApplication();

        service.hapusLamaran(jobApplication);

        verify(repository).delete(jobApplication);
    }


    @Test
    void harusBisaMengambilSemuaLamaran() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        JobApplication lamaran1 = new JobApplication();
        JobApplication lamaran2 = new JobApplication();

        List<JobApplication> data =
                List.of(lamaran1, lamaran2);

        when(repository.findAll()).thenReturn(data);

        List<JobApplication> hasil =
                service.findAll();

        assertEquals(2, hasil.size());

        verify(repository).findAll();
    }


    @Test
    void harusBisaFilterBerdasarkanKeywordDanStatus() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        JobApplication lamaran1 = new JobApplication();
        lamaran1.setCompanyName("Winosa");
        lamaran1.setPosition("Java Developer");
        lamaran1.setStatus(ApplicationStatus.INTERVIEW);
        lamaran1.setNotes("Interview tahap akhir");

        JobApplication lamaran2 = new JobApplication();
        lamaran2.setCompanyName("Xdemia");
        lamaran2.setPosition("QA Engineer");
        lamaran2.setStatus(ApplicationStatus.APPLIED);
        lamaran2.setNotes("Menunggu screening");

        List<JobApplication> data =
                List.of(lamaran1, lamaran2);

        when(repository.findAll()).thenReturn(data);

        List<JobApplication> hasil =
                service.findAll(
                        "winosa",
                        ApplicationStatus.INTERVIEW
                );

        assertEquals(1, hasil.size());

        assertEquals(
                "Winosa",
                hasil.get(0).getCompanyName()
        );

        verify(repository).findAll();
    }

    @Test
    void harusBisaMengecekApakahPelamarSudahMelamar() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        User pelamar = new User();
        JobPosting lowongan = new JobPosting();

        when(repository.existsByApplicantAndJobPosting(pelamar, lowongan))
                .thenReturn(true);

        boolean sudah = service.hasApplied(pelamar, lowongan);

        assertTrue(sudah);
        verify(repository).existsByApplicantAndJobPosting(pelamar, lowongan);
    }

    private User createValidActiveEmployer(Long id) {
        return createValidActiveEmployer(id, 1L, "PT Winosa Digital");
    }

    private User createValidActiveEmployer(Long id, Long companyId, String companyName) {
        Company company = new Company();
        company.setId(companyId);
        company.setName(companyName);
        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        User employer = new User();
        employer.setId(id);
        employer.setCompany(company);
        employer.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        return employer;
    }

    @Test
    void harusBisaMengambilPelamarBerdasarkanJobPostingMilikEmployer() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);
        lowongan.setCompany(employer.getCompany());

        JobApplication app1 = new JobApplication();
        app1.setPosition("Backend Dev");
        app1.setJobPosting(lowongan);

        JobApplication app2 = new JobApplication();
        app2.setPosition("Backend Dev");
        app2.setJobPosting(lowongan);

        when(repository.findByJobPostingOrderByApplicationDateDesc(lowongan))
                .thenReturn(List.of(app1, app2));

        List<JobApplication> hasil = service.findByJobPosting(lowongan, employer);

        assertEquals(2, hasil.size());
        verify(repository).findByJobPostingOrderByApplicationDateDesc(lowongan);
    }

    @Test
    void harusBisaMengambilPelamarOlehEmployerSatuCompany() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerPemilik = createValidActiveEmployer(1L, 1L, "PT Winosa Digital");
        User employerSatuCompany = createValidActiveEmployer(2L, 1L, "PT Winosa Digital");

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employerPemilik);
        lowongan.setCompany(employerPemilik.getCompany());

        JobApplication app1 = new JobApplication();
        app1.setPosition("Backend Dev");
        app1.setJobPosting(lowongan);

        when(repository.findByJobPostingOrderByApplicationDateDesc(lowongan))
                .thenReturn(List.of(app1));

        List<JobApplication> hasil = service.findByJobPosting(lowongan, employerSatuCompany);

        assertEquals(1, hasil.size());
        verify(repository).findByJobPostingOrderByApplicationDateDesc(lowongan);
    }

    @Test
    void harusMenolakMengambilPelamarJikaEmployerBedaCompany() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerPemilik = createValidActiveEmployer(1L, 1L, "PT Winosa Digital");
        User employerBedaCompany = createValidActiveEmployer(2L, 2L, "PT Lain");

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employerPemilik);
        lowongan.setCompany(employerPemilik.getCompany());

        assertThrows(
                SecurityException.class,
                () -> service.findByJobPosting(lowongan, employerBedaCompany)
        );

        verify(repository, never()).findByJobPostingOrderByApplicationDateDesc(any());
    }

    @Test
    void harusMengembalikanListKosongJikaPostingBelumPunyaPelamar() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        when(repository.findByJobPostingOrderByApplicationDateDesc(lowongan))
                .thenReturn(List.of());

        List<JobApplication> hasil = service.findByJobPosting(lowongan, employer);

        assertTrue(hasil.isEmpty());
        verify(repository).findByJobPostingOrderByApplicationDateDesc(lowongan);
    }

    @Test
    void testUpdateStatusBerhasilOlehEmployerPemilik() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = service.updateStatus(jobApplication, employer, ApplicationStatus.INTERVIEW);

        assertEquals(ApplicationStatus.INTERVIEW, result.getStatus());
        verify(repository).save(jobApplication);
    }

    @Test
    void testUpdateStatusBerhasilOlehEmployerSatuCompany() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerPemilik = createValidActiveEmployer(1L, 1L, "PT Winosa");
        User colleague = createValidActiveEmployer(2L, 1L, "PT Winosa");

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employerPemilik);
        lowongan.setCompany(employerPemilik.getCompany());

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = service.updateStatus(jobApplication, colleague, ApplicationStatus.INTERVIEW);

        assertEquals(ApplicationStatus.INTERVIEW, result.getStatus());
        verify(repository).save(jobApplication);
    }

    @Test
    void testUpdateStatusDitolakUntukEmployerBedaCompany() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerPemilik = createValidActiveEmployer(1L, 1L, "PT Winosa");
        User employerLain = createValidActiveEmployer(2L, 2L, "PT Lain");

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employerPemilik);
        lowongan.setCompany(employerPemilik.getCompany());

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));

        assertThrows(
                SecurityException.class,
                () -> service.updateStatus(jobApplication, employerLain, ApplicationStatus.INTERVIEW)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusDitolakJikaJobPostingNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobApplication legacyApplication = new JobApplication();
        legacyApplication.setId(100L);
        legacyApplication.setJobPosting(null);
        legacyApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(legacyApplication));

        assertThrows(
                IllegalStateException.class,
                () -> service.updateStatus(legacyApplication, employer, ApplicationStatus.INTERVIEW)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusDitolakJikaNewStatusNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateStatus(jobApplication, employer, null)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusMenjadiOffered() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.INTERVIEW);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = service.updateStatus(jobApplication, employer, ApplicationStatus.OFFERED);

        assertEquals(ApplicationStatus.OFFERED, result.getStatus());
        verify(repository).save(jobApplication);
    }

    @Test
    void testUpdateStatusMenjadiRejected() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.SCREENING);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = service.updateStatus(jobApplication, employer, ApplicationStatus.REJECTED);

        assertEquals(ApplicationStatus.REJECTED, result.getStatus());
        verify(repository).save(jobApplication);
    }

    @Test
    void testUpdateStatusDitolakJikaObjectCallerDimanipulasi() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerA = createValidActiveEmployer(1L, 1L, "PT A");
        User employerB = createValidActiveEmployer(2L, 2L, "PT B");

        JobPosting postingB = new JobPosting();
        postingB.setId(20L);
        postingB.setEmployer(employerB);
        postingB.setCompany(employerB.getCompany());

        JobPosting postingA = new JobPosting();
        postingA.setId(10L);
        postingA.setEmployer(employerA);
        postingA.setCompany(employerA.getCompany());

        // Record asli di database terikat ke lowongan milik Employer B
        JobApplication existingApplication = new JobApplication();
        existingApplication.setId(100L);
        existingApplication.setJobPosting(postingB);
        existingApplication.setStatus(ApplicationStatus.APPLIED);

        // Caller memanipulasi object seolah-olah terikat ke Employer A
        JobApplication callerApplication = new JobApplication();
        callerApplication.setId(100L);
        callerApplication.setJobPosting(postingA);
        callerApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(existingApplication));

        // Employer A mencoba update status lamaran milik Employer B
        assertThrows(
                SecurityException.class,
                () -> service.updateStatus(callerApplication, employerA, ApplicationStatus.INTERVIEW)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusDitolakJikaIdNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = new User();
        employer.setId(1L);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateStatus(jobApplication, employer, ApplicationStatus.INTERVIEW)
        );

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusDitolakJikaIdTidakDitemukan() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = new User();
        employer.setId(1L);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(999L);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateStatus(jobApplication, employer, ApplicationStatus.INTERVIEW)
        );

        verify(repository).findById(999L);
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusMenyimpanRecordAsliDariDatabaseBukanCaller() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        // Objek yang dikirim oleh caller
        JobApplication callerApplication = new JobApplication();
        callerApplication.setId(100L);
        callerApplication.setStatus(ApplicationStatus.APPLIED);

        // Objek asli yang diambil dari database
        JobApplication existingApplication = new JobApplication();
        existingApplication.setId(100L);
        existingApplication.setJobPosting(lowongan);
        existingApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(existingApplication));
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = service.updateStatus(callerApplication, employer, ApplicationStatus.INTERVIEW);

        assertEquals(ApplicationStatus.INTERVIEW, existingApplication.getStatus());
        assertEquals(ApplicationStatus.INTERVIEW, result.getStatus());

        verify(repository).save(existingApplication);
        verify(repository, never()).save(callerApplication);
    }

    @Test
    void testFindByEmployerHanyaMengambilLamaranMilikEmployerTerkait() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerA = new User();
        employerA.setId(1L);

        JobPosting lowonganA = new JobPosting();
        lowonganA.setId(10L);
        lowonganA.setEmployer(employerA);

        JobApplication appA = new JobApplication();
        appA.setId(101L);
        appA.setJobPosting(lowonganA);

        when(repository.findByJobPostingEmployerOrderByApplicationDateDesc(employerA))
                .thenReturn(List.of(appA));

        List<JobApplication> hasil = service.findByEmployer(employerA);

        assertEquals(1, hasil.size());
        assertEquals(101L, hasil.get(0).getId());
        verify(repository).findByJobPostingEmployerOrderByApplicationDateDesc(employerA);
    }

    @Test
    void testFindByEmployerMengembalikanListKosongJikaEmployerTanpaLowongan() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = new User();
        employer.setId(1L);

        when(repository.findByJobPostingEmployerOrderByApplicationDateDesc(employer))
                .thenReturn(List.of());

        List<JobApplication> hasil = service.findByEmployer(employer);

        assertTrue(hasil.isEmpty());
        verify(repository).findByJobPostingEmployerOrderByApplicationDateDesc(employer);
    }

    @Test
    void testFindByEmployerAmanJikaEmployerNullAtauIdNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        List<JobApplication> hasilNull = service.findByEmployer(null);
        assertTrue(hasilNull.isEmpty());

        User employerTanpaId = new User();
        employerTanpaId.setId(null);

        List<JobApplication> hasilTanpaId = service.findByEmployer(employerTanpaId);
        assertTrue(hasilTanpaId.isEmpty());

        verify(repository, never()).findByJobPostingEmployerOrderByApplicationDateDesc(any());
    }

    @Test
    void testFindByEmployerMengembalikanLamaranDariBeberapaLowonganMilikEmployer() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = new User();
        employer.setId(1L);

        JobPosting lowongan1 = new JobPosting();
        lowongan1.setId(10L);
        lowongan1.setEmployer(employer);

        JobPosting lowongan2 = new JobPosting();
        lowongan2.setId(20L);
        lowongan2.setEmployer(employer);

        JobApplication app1 = new JobApplication();
        app1.setId(101L);
        app1.setJobPosting(lowongan1);

        JobApplication app2 = new JobApplication();
        app2.setId(102L);
        app2.setJobPosting(lowongan2);

        when(repository.findByJobPostingEmployerOrderByApplicationDateDesc(employer))
                .thenReturn(List.of(app1, app2));

        List<JobApplication> hasil = service.findByEmployer(employer);

        assertEquals(2, hasil.size());
        verify(repository).findByJobPostingEmployerOrderByApplicationDateDesc(employer);
    }

    @Test
    void testUpdateStatusAllowedJikaCompanyVerifiedDanMembershipActive() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = service.updateStatus(jobApplication, employer, ApplicationStatus.OFFERED);

        assertEquals(ApplicationStatus.OFFERED, result.getStatus());
        verify(repository).save(jobApplication);
    }

    @Test
    void testUpdateStatusDeniedJikaCompanyPending() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.getCompany().setVerificationStatus(CompanyVerificationStatus.PENDING);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.updateStatus(jobApplication, employer, ApplicationStatus.OFFERED)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk mengubah status pelamar", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusDeniedJikaCompanyRejected() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.getCompany().setVerificationStatus(CompanyVerificationStatus.REJECTED);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.updateStatus(jobApplication, employer, ApplicationStatus.OFFERED)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk mengubah status pelamar", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusDeniedJikaMembershipPending() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.setMembershipStatus(CompanyMembershipStatus.PENDING);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.updateStatus(jobApplication, employer, ApplicationStatus.OFFERED)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusDeniedJikaMembershipRejected() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.setMembershipStatus(CompanyMembershipStatus.REJECTED);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.updateStatus(jobApplication, employer, ApplicationStatus.OFFERED)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatusDeniedJikaMembershipNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.setMembershipStatus(null);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(100L);
        jobApplication.setJobPosting(lowongan);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(100L)).thenReturn(Optional.of(jobApplication));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.updateStatus(jobApplication, employer, ApplicationStatus.OFFERED)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void harusBisaMengambilPelamarJikaCompanyVerifiedDanMembershipActiveDanOwner() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        JobApplication app1 = new JobApplication();
        app1.setPosition("Backend Dev");
        app1.setJobPosting(lowongan);

        when(repository.findByJobPostingOrderByApplicationDateDesc(lowongan))
                .thenReturn(List.of(app1));

        List<JobApplication> hasil = service.findByJobPosting(lowongan, employer);

        assertEquals(1, hasil.size());
        verify(repository).findByJobPostingOrderByApplicationDateDesc(lowongan);
    }

    @Test
    void harusMenolakMengambilPelamarJikaCompanyVerifiedTapiMembershipPending() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.setMembershipStatus(CompanyMembershipStatus.PENDING);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.findByJobPosting(lowongan, employer)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(repository, never()).findByJobPostingOrderByApplicationDateDesc(any());
    }

    @Test
    void harusMenolakMengambilPelamarJikaCompanyVerifiedTapiMembershipRejected() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.setMembershipStatus(CompanyMembershipStatus.REJECTED);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.findByJobPosting(lowongan, employer)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(repository, never()).findByJobPostingOrderByApplicationDateDesc(any());
    }

    @Test
    void harusMenolakMengambilPelamarJikaCompanyVerifiedTapiMembershipNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.setMembershipStatus(null);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.findByJobPosting(lowongan, employer)
        );

        assertEquals("Status membership employer belum aktif", ex.getMessage());
        verify(repository, never()).findByJobPostingOrderByApplicationDateDesc(any());
    }

    @Test
    void harusMenolakMengambilPelamarJikaCompanyPending() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.getCompany().setVerificationStatus(CompanyVerificationStatus.PENDING);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.findByJobPosting(lowongan, employer)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk melihat data pelamar", ex.getMessage());
        verify(repository, never()).findByJobPostingOrderByApplicationDateDesc(any());
    }

    @Test
    void harusMenolakMengambilPelamarJikaCompanyRejected() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.getCompany().setVerificationStatus(CompanyVerificationStatus.REJECTED);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.findByJobPosting(lowongan, employer)
        );

        assertEquals("Perusahaan harus berstatus VERIFIED untuk melihat data pelamar", ex.getMessage());
        verify(repository, never()).findByJobPostingOrderByApplicationDateDesc(any());
    }

    @Test
    void harusMenolakMengambilPelamarBedaCompanyWalaupunVerifiedDanActive() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerPemilik = createValidActiveEmployer(1L, 1L, "PT A");
        User employerLain = createValidActiveEmployer(2L, 2L, "PT B");

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employerPemilik);
        lowongan.setCompany(employerPemilik.getCompany());

        assertThrows(
                SecurityException.class,
                () -> service.findByJobPosting(lowongan, employerLain)
        );

        verify(repository, never()).findByJobPostingOrderByApplicationDateDesc(any());
    }

    @Test
    void testFindByCompanyMengambilSemuaLamaranSatuCompany() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        Company company = new Company();
        company.setId(1L);

        JobApplication app = new JobApplication();
        app.setId(10L);

        when(repository.findByJobPostingCompanyOrderByApplicationDateDesc(company))
                .thenReturn(List.of(app));

        List<JobApplication> hasil = service.findByCompany(company);

        assertEquals(1, hasil.size());
        verify(repository).findByJobPostingCompanyOrderByApplicationDateDesc(company);
    }

    @Test
    void harusBisaMenyimpanLamaranDenganSnapshotKontak() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setPhone("08123456789");
        jobApplication.setLinkedIn("https://linkedin.com/in/budi");
        jobApplication.setPortfolio("https://budi.dev");

        when(repository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        JobApplication saved = service.simpanLamaran(jobApplication);

        assertEquals("08123456789", saved.getPhone());
        assertEquals("https://linkedin.com/in/budi", saved.getLinkedIn());
        assertEquals("https://budi.dev", saved.getPortfolio());
        verify(repository).save(jobApplication);
    }

    @Test
    void harusBisaMenyimpanLamaranDenganSnapshotKontakOpsionalNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setPhone("08123456789");
        jobApplication.setLinkedIn(null);
        jobApplication.setPortfolio(null);

        when(repository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        JobApplication saved = service.simpanLamaran(jobApplication);

        assertEquals("08123456789", saved.getPhone());
        assertNull(saved.getLinkedIn());
        assertNull(saved.getPortfolio());
        verify(repository).save(jobApplication);
    }

    @Test
    void harusMemastikanSnapshotKontakJobApplicationIndependenDariPerubahanApplicantProfile() {
        User applicant = new User();
        applicant.setId(10L);
        applicant.setName("Pelamar Budi");

        ApplicantProfile profile = new ApplicantProfile(applicant);
        profile.setPhone("0811111111");
        profile.setLinkedIn("https://linkedin.com/in/profile-lama");
        profile.setPortfolio("https://profile-lama.dev");

        // Lamaran dibuat dengan nilai form custom (snapshot)
        JobApplication jobApplication = new JobApplication();
        jobApplication.setApplicant(applicant);
        jobApplication.setPhone("0822222222"); // custom edit di form
        jobApplication.setLinkedIn("https://linkedin.com/in/form-custom");
        jobApplication.setPortfolio("https://form-custom.dev");

        // Setelah submit, pelamar mengubah profilnya di kemudian hari
        profile.setPhone("0899999999");
        profile.setLinkedIn("https://linkedin.com/in/profile-baru");
        profile.setPortfolio("https://profile-baru.dev");

        // Snapshot pada JobApplication harus tetap menyimpan nilai saat apply
        assertEquals("0822222222", jobApplication.getPhone());
        assertEquals("https://linkedin.com/in/form-custom", jobApplication.getLinkedIn());
        assertEquals("https://form-custom.dev", jobApplication.getPortfolio());

        // Dan profil terpisah menyimpan nilainya sendiri
        assertEquals("0899999999", profile.getPhone());
        assertEquals("https://linkedin.com/in/profile-baru", profile.getLinkedIn());
        assertEquals("https://profile-baru.dev", profile.getPortfolio());
    }

    // ==========================================
    // PENGUJIAN PENYIMPANAN CV PERMANEN
    // ==========================================

    @Test
    void harusBisaMenyimpanLamaranDenganCvDanMetadata() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);
        app.setPhone("08123456789");

        InputStream stream = new ByteArrayInputStream("PDF content".getBytes());
        ResumeStorageResult mockResult = new ResumeStorageResult(
                "resumes/uuid-123.pdf",
                "my_cv.pdf",
                "application/pdf",
                2048L
        );

        when(storageService.store(any(InputStream.class), any(), any())).thenReturn(mockResult);
        when(repository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        JobApplication saved = service.simpanLamaranDenganCv(app, stream, "my_cv.pdf", "application/pdf");

        assertNotNull(saved);
        assertEquals("resumes/uuid-123.pdf", saved.getResumeStorageKey());
        assertEquals("my_cv.pdf", saved.getResumeOriginalFilename());
        assertEquals("application/pdf", saved.getResumeContentType());
        assertEquals(2048L, saved.getResumeFileSize());
        assertEquals("08123456789", saved.getPhone());

        verify(storageService).store(stream, "my_cv.pdf", "application/pdf");
        verify(repository).save(app);
    }

    @Test
    void harusMembatalkanPenyimpananLamaranJikaStorageGagal() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        InputStream stream = new ByteArrayInputStream("PDF content".getBytes());
        when(storageService.store(any(), any(), any())).thenThrow(new IllegalStateException("Disk I/O error"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.simpanLamaranDenganCv(app, stream, "cv.pdf", "application/pdf")
        );

        assertEquals("Disk I/O error", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void harusMenghapusCvJikaDatabaseSaveGagal() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        InputStream stream = new ByteArrayInputStream("PDF content".getBytes());
        ResumeStorageResult mockResult = new ResumeStorageResult(
                "resumes/uuid-123.pdf",
                "my_cv.pdf",
                "application/pdf",
                2048L
        );

        when(storageService.store(any(), any(), any())).thenReturn(mockResult);
        when(repository.save(any(JobApplication.class))).thenThrow(new RuntimeException("Database connection timeout"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.simpanLamaranDenganCv(app, stream, "my_cv.pdf", "application/pdf")
        );

        assertEquals("Database connection timeout", ex.getMessage());
        // Memastikan compensating delete dipanggil untuk membersihkan orphan file
        verify(storageService).delete("resumes/uuid-123.pdf");
    }

    @Test
    void harusMenolakLamaranTanpaCv() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.simpanLamaranDenganCv(app, null, "cv.pdf", "application/pdf")
        );

        assertEquals("File CV wajib diunggah", ex.getMessage());
        verify(storageService, never()).store(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void harusTidakMenyimpanCvJikaLowonganTidakAktif() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting inactivePosting = new JobPosting();
        inactivePosting.setId(10L);
        inactivePosting.setActive(false);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(inactivePosting);

        InputStream stream = new ByteArrayInputStream("PDF".getBytes());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.simpanLamaranDenganCv(app, stream, "cv.pdf", "application/pdf")
        );

        assertEquals("Lowongan ini sudah tidak aktif", ex.getMessage());
        verify(storageService, never()).store(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void harusTidakMenyimpanCvJikaPelamarSudahPernahMelamar() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        when(repository.existsByApplicantAndJobPosting(applicant, posting)).thenReturn(true);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        InputStream stream = new ByteArrayInputStream("PDF".getBytes());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.simpanLamaranDenganCv(app, stream, "cv.pdf", "application/pdf")
        );

        assertEquals("Anda sudah melamar lowongan ini", ex.getMessage());
        verify(storageService, never()).store(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void harusMenerjemahkanDbUniqueConstraintMenjadiErrorRamahSaatSimpanDenganCv() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        when(repository.existsByApplicantAndJobPosting(applicant, posting)).thenReturn(false);

        ResumeStorageResult mockResult = new ResumeStorageResult(
                "resumes/uuid-race.pdf",
                "cv.pdf",
                "application/pdf",
                2048L
        );
        when(storageService.store(any(InputStream.class), any(), any())).thenReturn(mockResult);

        ConstraintViolationException cve = new ConstraintViolationException(
                "duplicate key value violates unique constraint",
                new SQLException("ERROR: duplicate key value", "23505"),
                "uk_job_application_applicant_job_posting"
        );
        DataIntegrityViolationException dive = new DataIntegrityViolationException(
                "could not execute statement [uk_job_application_applicant_job_posting]",
                cve
        );
        when(repository.save(any(JobApplication.class))).thenThrow(dive);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        InputStream stream = new ByteArrayInputStream("PDF".getBytes());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.simpanLamaranDenganCv(app, stream, "cv.pdf", "application/pdf")
        );

        assertEquals("Anda sudah melamar lowongan ini", ex.getMessage());
        verify(storageService).delete("resumes/uuid-race.pdf");
    }

    @Test
    void harusTetapMelemparOriginalExceptionDanHapusCvJikaDbErrorBukanDuplicate() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        when(repository.existsByApplicantAndJobPosting(applicant, posting)).thenReturn(false);

        ResumeStorageResult mockResult = new ResumeStorageResult(
                "resumes/uuid-other.pdf",
                "cv.pdf",
                "application/pdf",
                2048L
        );
        when(storageService.store(any(InputStream.class), any(), any())).thenReturn(mockResult);

        ConstraintViolationException cve = new ConstraintViolationException(
                "foreign key violation",
                new SQLException("ERROR: fk violation", "23503"),
                "fk_some_other_table"
        );
        DataIntegrityViolationException dive = new DataIntegrityViolationException(
                "could not execute statement [fk_some_other_table]",
                cve
        );
        when(repository.save(any(JobApplication.class))).thenThrow(dive);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        InputStream stream = new ByteArrayInputStream("PDF".getBytes());

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> service.simpanLamaranDenganCv(app, stream, "cv.pdf", "application/pdf")
        );

        assertEquals(dive, ex);
        verify(storageService).delete("resumes/uuid-other.pdf");
    }

    @Test
    void harusMenolakSimpanLamaranJikaPelamarDanJobPostingSudahMelamar() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);

        when(repository.existsByApplicantAndJobPosting(applicant, posting)).thenReturn(true);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.simpanLamaran(app)
        );

        assertEquals("Anda sudah melamar lowongan ini", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void harusMenerjemahkanDbUniqueConstraintMenjadiErrorRamahSaatSimpanLamaranBiasa() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);

        when(repository.existsByApplicantAndJobPosting(applicant, posting)).thenReturn(false);

        ConstraintViolationException cve = new ConstraintViolationException(
                "duplicate key value",
                new SQLException("ERROR: duplicate key", "23505"),
                "uk_job_application_applicant_job_posting"
        );
        DataIntegrityViolationException dive = new DataIntegrityViolationException("duplicate", cve);
        when(repository.save(any(JobApplication.class))).thenThrow(dive);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.simpanLamaran(app)
        );

        assertEquals("Anda sudah melamar lowongan ini", ex.getMessage());
    }

    @Test
    void harusTetapBisaMenyimpanLamaranManualDenganJobPostingNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User applicant = new User();
        applicant.setId(1L);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(null);
        app.setCompanyName("PT External Manual");
        app.setPosition("Backend Engineer");

        when(repository.save(app)).thenReturn(app);

        JobApplication saved = service.simpanLamaran(app);

        assertNotNull(saved);
        verify(repository).save(app);
        verify(repository, never()).existsByApplicantAndJobPosting(any(), any());
    }

    @Test
    void harusMempertahankanSnapshotKontakExistingSaatSimpanDenganCv() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);
        app.setPhone("0811223344");
        app.setLinkedIn("https://linkedin.com/in/test");
        app.setPortfolio("https://test.dev");

        InputStream stream = new ByteArrayInputStream("PDF".getBytes());
        ResumeStorageResult mockResult = new ResumeStorageResult(
                "resumes/uuid-999.pdf",
                "cv.pdf",
                "application/pdf",
                1000L
        );

        when(storageService.store(any(), any(), any())).thenReturn(mockResult);
        when(repository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        JobApplication saved = service.simpanLamaranDenganCv(app, stream, "cv.pdf", "application/pdf");

        assertEquals("0811223344", saved.getPhone());
        assertEquals("https://linkedin.com/in/test", saved.getLinkedIn());
        assertEquals("https://test.dev", saved.getPortfolio());
        assertEquals("resumes/uuid-999.pdf", saved.getResumeStorageKey());
    }

    @Test
    void harusAmanMenghadapiDataHistorisLamaranDenganCvNull() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        JobApplication legacyApp = new JobApplication();
        legacyApp.setId(5L);
        legacyApp.setCompanyName("PT Lama");
        legacyApp.setPosition("Java Dev");
        legacyApp.setResumeStorageKey(null);
        legacyApp.setResumeOriginalFilename(null);
        legacyApp.setResumeContentType(null);
        legacyApp.setResumeFileSize(null);

        when(repository.findAll()).thenReturn(List.of(legacyApp));

        List<JobApplication> results = service.findAll();
        assertEquals(1, results.size());
        assertNull(results.get(0).getResumeStorageKey());
    }

    @Test
    void harusTetapMelemparErrorDatabaseMeskipunCompensatingDeleteGagal() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User applicant = new User();
        applicant.setId(1L);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setActive(true);

        JobApplication app = new JobApplication();
        app.setApplicant(applicant);
        app.setJobPosting(posting);

        InputStream stream = new ByteArrayInputStream("PDF".getBytes());
        ResumeStorageResult mockResult = new ResumeStorageResult(
                "resumes/uuid-fail.pdf",
                "cv.pdf",
                "application/pdf",
                1000L
        );

        when(storageService.store(any(), any(), any())).thenReturn(mockResult);
        when(repository.save(any(JobApplication.class))).thenThrow(new RuntimeException("Fatal DB Error"));
        doThrow(new RuntimeException("Disk Permission Error saat delete")).when(storageService).delete("resumes/uuid-fail.pdf");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.simpanLamaranDenganCv(app, stream, "cv.pdf", "application/pdf")
        );

        assertEquals("Fatal DB Error", ex.getMessage());
        verify(storageService).delete("resumes/uuid-fail.pdf");
    }

    @Test
    void harusBisaMengambilDetailLamaranUntukEmployerSatuCompany() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L, 1L, "PT Solusi");

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setEmployer(employer);
        posting.setCompany(employer.getCompany());

        JobApplication app = new JobApplication();
        app.setId(100L);
        app.setJobPosting(posting);
        app.setPhone("081234567890");

        when(repository.findById(100L)).thenReturn(Optional.of(app));

        JobApplication result = service.findApplicationForEmployer(100L, employer);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("081234567890", result.getPhone());
        verify(repository).findById(100L);
    }

    @Test
    void harusMenolakDetailLamaranDariCompanyLain() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerA = createValidActiveEmployer(1L, 1L, "PT A");
        User employerB = createValidActiveEmployer(2L, 2L, "PT B");

        JobPosting postingA = new JobPosting();
        postingA.setId(10L);
        postingA.setEmployer(employerA);
        postingA.setCompany(employerA.getCompany());

        JobApplication app = new JobApplication();
        app.setId(100L);
        app.setJobPosting(postingA);

        when(repository.findById(100L)).thenReturn(Optional.of(app));

        assertThrows(
                SecurityException.class,
                () -> service.findApplicationForEmployer(100L, employerB)
        );
    }

    @Test
    void harusMenolakDetailJikaCompanyEmployerBelumVerified() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.getCompany().setVerificationStatus(CompanyVerificationStatus.PENDING);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setEmployer(employer);
        posting.setCompany(employer.getCompany());

        JobApplication app = new JobApplication();
        app.setId(100L);
        app.setJobPosting(posting);

        when(repository.findById(100L)).thenReturn(Optional.of(app));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.findApplicationForEmployer(100L, employer)
        );
        assertTrue(ex.getMessage().contains("VERIFIED"));
    }

    @Test
    void harusMenolakDetailJikaMembershipEmployerTidakActive() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);
        employer.setMembershipStatus(CompanyMembershipStatus.PENDING);

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setEmployer(employer);
        posting.setCompany(employer.getCompany());

        JobApplication app = new JobApplication();
        app.setId(100L);
        app.setJobPosting(posting);

        when(repository.findById(100L)).thenReturn(Optional.of(app));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.findApplicationForEmployer(100L, employer)
        );
        assertTrue(ex.getMessage().contains("membership"));
    }

    @Test
    void harusMenanganiApplicationTidakDitemukanDenganAman() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = createValidActiveEmployer(1L);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.findApplicationForEmployer(999L, employer)
        );
        assertTrue(ex.getMessage().contains("tidak ditemukan"));
    }

    @Test
    void harusBisaMembukaStreamCvUntukEmployerSatuCompany() throws Exception {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User employer = createValidActiveEmployer(1L, 1L, "PT Sukses");

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setEmployer(employer);
        posting.setCompany(employer.getCompany());

        JobApplication app = new JobApplication();
        app.setId(100L);
        app.setJobPosting(posting);
        app.setResumeStorageKey("resumes/real-uuid.pdf");
        app.setResumeOriginalFilename("CV_Pelamar.pdf");

        when(repository.findById(100L)).thenReturn(Optional.of(app));
        when(storageService.load("resumes/real-uuid.pdf"))
                .thenReturn(new ByteArrayInputStream("konten cv asli".getBytes()));

        try (InputStream stream = service.loadResumeStreamForEmployer(100L, employer)) {
            assertNotNull(stream);
            byte[] bytes = stream.readAllBytes();
            assertEquals("konten cv asli", new String(bytes));
        }

        verify(storageService).load("resumes/real-uuid.pdf");
    }

    @Test
    void harusMenolakDownloadCvDariCompanyLain() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User employerA = createValidActiveEmployer(1L, 1L, "PT A");
        User employerB = createValidActiveEmployer(2L, 2L, "PT B");

        JobPosting postingA = new JobPosting();
        postingA.setId(10L);
        postingA.setEmployer(employerA);
        postingA.setCompany(employerA.getCompany());

        JobApplication app = new JobApplication();
        app.setId(100L);
        app.setJobPosting(postingA);
        app.setResumeStorageKey("resumes/secret.pdf");

        when(repository.findById(100L)).thenReturn(Optional.of(app));

        assertThrows(
                SecurityException.class,
                () -> service.loadResumeStreamForEmployer(100L, employerB)
        );

        verify(storageService, never()).load(any());
    }

    @Test
    void harusMenolakDownloadJikaCvTidakTersedia() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        ResumeStorageService storageService = Mockito.mock(ResumeStorageService.class);
        JobApplicationService service = new JobApplicationService(repository, storageService);

        User employer = createValidActiveEmployer(1L, 1L, "PT A");

        JobPosting posting = new JobPosting();
        posting.setId(10L);
        posting.setEmployer(employer);
        posting.setCompany(employer.getCompany());

        JobApplication app = new JobApplication();
        app.setId(100L);
        app.setJobPosting(posting);
        app.setResumeStorageKey(null); // tidak ada CV

        when(repository.findById(100L)).thenReturn(Optional.of(app));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.loadResumeStreamForEmployer(100L, employer)
        );

        assertTrue(ex.getMessage().contains("tidak tersedia"));
        verify(storageService, never()).load(any());
    }
}