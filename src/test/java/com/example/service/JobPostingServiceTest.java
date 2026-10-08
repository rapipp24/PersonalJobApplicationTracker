package com.example.service;

import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.JobApplicationRepository;
import com.example.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

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

    @Test
    void harusBisaHapusLowonganTanpaLamaran() {
        User employer = new User();
        employer.setId(1L);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(100L);
        jobPosting.setPosition("Java Developer");
        jobPosting.setEmployer(employer);

        when(jobApplicationRepository.existsByJobPosting(jobPosting)).thenReturn(false);

        jobPostingService.hapusLowongan(jobPosting, employer);

        verify(jobPostingRepository).delete(jobPosting);
    }

    @Test
    void harusMenolakHapusLowonganYangSudahMemilikiLamaran() {
        User employer = new User();
        employer.setId(1L);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(100L);
        jobPosting.setPosition("Java Developer");
        jobPosting.setEmployer(employer);

        when(jobApplicationRepository.existsByJobPosting(jobPosting)).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> jobPostingService.hapusLowongan(jobPosting, employer)
        );

        verify(jobPostingRepository, never()).delete(any());
    }

    @Test
    void harusMenolakHapusLowonganMilikEmployerLain() {
        User employerOwner = new User();
        employerOwner.setId(1L);

        User employerLain = new User();
        employerLain.setId(2L);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(100L);
        jobPosting.setPosition("Java Developer");
        jobPosting.setEmployer(employerOwner);

        when(jobApplicationRepository.existsByJobPosting(jobPosting)).thenReturn(false);

        assertThrows(
                SecurityException.class,
                () -> jobPostingService.hapusLowongan(jobPosting, employerLain)
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
        JobPosting jobPosting = new JobPosting();
        jobPosting.setPosition("Frontend Developer");

        when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(jobPosting);

        JobPosting saved = jobPostingService.simpanLowongan(jobPosting);

        assertNotNull(saved);
        assertEquals("Frontend Developer", saved.getPosition());
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

        when(jobPostingRepository.findById(50L)).thenReturn(java.util.Optional.of(jobPosting));

        java.util.Optional<JobPosting> found = jobPostingService.findById(50L);

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
}
