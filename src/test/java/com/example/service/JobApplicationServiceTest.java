package com.example.service;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.JobApplicationRepository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void harusBisaMengambilPelamarBerdasarkanJobPostingMilikEmployer() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = new User();
        employer.setId(1L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employer);

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
    void harusMenolakMengambilPelamarJikaEmployerBukanPemilik() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerPemilik = new User();
        employerPemilik.setId(1L);

        User employerLain = new User();
        employerLain.setId(2L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employerPemilik);

        assertThrows(
                SecurityException.class,
                () -> service.findByJobPosting(lowongan, employerLain)
        );

        verify(repository, never()).findByJobPostingOrderByApplicationDateDesc(any());
    }

    @Test
    void harusMengembalikanListKosongJikaPostingBelumPunyaPelamar() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employer = new User();
        employer.setId(1L);

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

        User employer = new User();
        employer.setId(1L);

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
    void testUpdateStatusDitolakUntukEmployerLain() {
        JobApplicationRepository repository = Mockito.mock(JobApplicationRepository.class);
        JobApplicationService service = new JobApplicationService(repository);

        User employerPemilik = new User();
        employerPemilik.setId(1L);

        User employerLain = new User();
        employerLain.setId(2L);

        JobPosting lowongan = new JobPosting();
        lowongan.setId(10L);
        lowongan.setEmployer(employerPemilik);

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

        User employer = new User();
        employer.setId(1L);

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

        User employer = new User();
        employer.setId(1L);

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

        User employer = new User();
        employer.setId(1L);

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

        User employer = new User();
        employer.setId(1L);

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

        User employerA = new User();
        employerA.setId(1L);

        User employerB = new User();
        employerB.setId(2L);

        JobPosting postingB = new JobPosting();
        postingB.setId(20L);
        postingB.setEmployer(employerB);

        JobPosting postingA = new JobPosting();
        postingA.setId(10L);
        postingA.setEmployer(employerA);

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

        User employer = new User();
        employer.setId(1L);

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
}