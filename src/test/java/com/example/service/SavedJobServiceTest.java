package com.example.service;

import com.example.entity.JobPosting;
import com.example.entity.Role;
import com.example.entity.SavedJob;
import com.example.entity.User;
import com.example.repository.SavedJobRepository;
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

public class SavedJobServiceTest {

    private SavedJobRepository repository;
    private SavedJobService service;

    private User applicant;
    private JobPosting activeJobPosting;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SavedJobRepository.class);
        service = new SavedJobService(repository);

        applicant = new User("Pelamar Test", "pelamar@example.com", "password", Role.PELAMAR);

        activeJobPosting = new JobPosting();
        activeJobPosting.setPosition("Software Engineer");
        activeJobPosting.setCompanyName("PT Maju Mundur");
        activeJobPosting.setActive(true);
    }

    @Test
    void harusBisaMenyimpanJobPostingBaru() {
        when(repository.existsByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(false);
        when(repository.save(any(SavedJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SavedJob savedJob = service.saveJob(applicant, activeJobPosting);

        assertNotNull(savedJob);
        assertEquals(applicant, savedJob.getApplicant());
        assertEquals(activeJobPosting, savedJob.getJobPosting());
        assertNotNull(savedJob.getSavedAt());
        verify(repository).save(any(SavedJob.class));
    }

    @Test
    void harusMencegahDuplikasiPenyimpanan() {
        when(repository.existsByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.saveJob(applicant, activeJobPosting);
        });

        assertTrue(exception.getMessage().contains("sudah disimpan"));
        verify(repository, never()).save(any(SavedJob.class));
    }

    @Test
    void isJobSavedMengembalikanHasilYangBenar() {
        when(repository.existsByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(true);
        assertTrue(service.isJobSaved(applicant, activeJobPosting));

        when(repository.existsByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(false);
        assertFalse(service.isJobSaved(applicant, activeJobPosting));

        assertFalse(service.isJobSaved(null, activeJobPosting));
        assertFalse(service.isJobSaved(applicant, null));
    }

    @Test
    void getSavedJobsMengembalikanHasilDariRepository() {
        SavedJob savedJob = new SavedJob(applicant, activeJobPosting);
        when(repository.findByApplicantOrderBySavedAtDesc(applicant)).thenReturn(List.of(savedJob));

        List<SavedJob> result = service.getSavedJobs(applicant);

        assertEquals(1, result.size());
        assertEquals(savedJob, result.get(0));
        assertEquals(activeJobPosting, result.get(0).getJobPosting());

        List<SavedJob> emptyResult = service.getSavedJobs(null);
        assertTrue(emptyResult.isEmpty());
    }

    @Test
    void unsaveJobMenghapusRelasiSavedJob() {
        SavedJob savedJob = new SavedJob(applicant, activeJobPosting);
        when(repository.findByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(Optional.of(savedJob));

        service.unsaveJob(applicant, activeJobPosting);

        verify(repository).delete(savedJob);
    }

    @Test
    void unsaveJobTidakErrorJikaDataTidakAda() {
        when(repository.findByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(Optional.empty());

        service.unsaveJob(applicant, activeJobPosting);

        verify(repository, never()).delete(any());
    }

    @Test
    void harusMenolakInputNullPadaSave() {
        assertThrows(IllegalArgumentException.class, () -> service.saveJob(null, activeJobPosting));
        assertThrows(IllegalArgumentException.class, () -> service.saveJob(applicant, null));
        verify(repository, never()).save(any());
    }

    @Test
    void harusMenolakPenyimpananJobPostingYangTidakAktif() {
        JobPosting inactiveJob = new JobPosting();
        inactiveJob.setPosition("Frontend Dev");
        inactiveJob.setActive(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.saveJob(applicant, inactiveJob);
        });

        assertTrue(exception.getMessage().contains("tidak aktif"));
        verify(repository, never()).save(any());
    }

    @Test
    void toggleSaveMenyimpanJikaBelumAdaDanMenghapusJikaSudahAda() {
        // Case 1: Belum disimpan -> Simpan dan return true
        when(repository.existsByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(false);
        when(repository.save(any(SavedJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean nowSaved = service.toggleSave(applicant, activeJobPosting);

        assertTrue(nowSaved);
        verify(repository).save(any(SavedJob.class));

        // Case 2: Sudah disimpan -> Unsave dan return false
        SavedJob existing = new SavedJob(applicant, activeJobPosting);
        when(repository.existsByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(true);
        when(repository.findByApplicantAndJobPosting(applicant, activeJobPosting)).thenReturn(Optional.of(existing));

        boolean nowUnsaved = service.toggleSave(applicant, activeJobPosting);

        assertFalse(nowUnsaved);
        verify(repository).delete(existing);
    }
}
