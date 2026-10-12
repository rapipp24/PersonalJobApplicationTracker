package com.example.service;

import com.example.entity.JobPosting;
import com.example.entity.SavedJob;
import com.example.entity.User;
import com.example.repository.SavedJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SavedJobService {

    private final SavedJobRepository repository;

    public SavedJobService(SavedJobRepository repository) {
        this.repository = repository;
    }

    public boolean isJobSaved(User applicant, JobPosting jobPosting) {
        if (applicant == null || jobPosting == null) {
            return false;
        }
        return repository.existsByApplicantAndJobPosting(applicant, jobPosting);
    }

    @Transactional
    public SavedJob saveJob(User applicant, JobPosting jobPosting) {
        if (applicant == null) {
            throw new IllegalArgumentException("Applicant tidak boleh null");
        }
        if (jobPosting == null) {
            throw new IllegalArgumentException("Job posting tidak boleh null");
        }
        if (!jobPosting.isActive()) {
            throw new IllegalStateException("Lowongan pekerjaan sudah tidak aktif");
        }
        if (repository.existsByApplicantAndJobPosting(applicant, jobPosting)) {
            throw new IllegalStateException("Lowongan pekerjaan sudah disimpan sebelumnya");
        }

        SavedJob savedJob = new SavedJob(applicant, jobPosting);
        return repository.save(savedJob);
    }

    @Transactional
    public void unsaveJob(User applicant, JobPosting jobPosting) {
        if (applicant == null || jobPosting == null) {
            return;
        }
        repository.findByApplicantAndJobPosting(applicant, jobPosting)
                .ifPresent(repository::delete);
    }

    @Transactional
    public boolean toggleSave(User applicant, JobPosting jobPosting) {
        if (applicant == null) {
            throw new IllegalArgumentException("Applicant tidak boleh null");
        }
        if (jobPosting == null) {
            throw new IllegalArgumentException("Job posting tidak boleh null");
        }

        if (repository.existsByApplicantAndJobPosting(applicant, jobPosting)) {
            unsaveJob(applicant, jobPosting);
            return false;
        } else {
            saveJob(applicant, jobPosting);
            return true;
        }
    }

    public List<SavedJob> getSavedJobs(User applicant) {
        if (applicant == null) {
            return List.of();
        }
        return repository.findByApplicantOrderBySavedAtDesc(applicant);
    }
}
