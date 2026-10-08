package com.example.service;

import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.JobApplicationRepository;
import com.example.repository.JobPostingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobPostingService {

    private final JobPostingRepository repository;
    private final JobApplicationRepository jobApplicationRepository;

    public JobPostingService(
            JobPostingRepository repository,
            JobApplicationRepository jobApplicationRepository) {
        this.repository = repository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public List<JobPosting> findAll() {
        return repository.findAll();
    }

    public Optional<JobPosting> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    public JobPosting simpanLowongan(JobPosting jobPosting) {
        return repository.save(jobPosting);
    }

    public void hapusLowongan(JobPosting jobPosting) {
        repository.delete(jobPosting);
    }

    public void hapusLowongan(JobPosting jobPosting, User employer) {
        if (jobPosting == null || jobPosting.getId() == null) {
            throw new IllegalArgumentException("Lowongan tidak valid untuk dihapus");
        }
        if (jobPosting.getEmployer() == null || employer == null || !jobPosting.getEmployer().getId().equals(employer.getId())) {
            throw new SecurityException("Anda tidak memiliki hak untuk menghapus lowongan ini");
        }
        if (hasApplications(jobPosting)) {
            throw new IllegalStateException("Lowongan tidak dapat dihapus karena sudah memiliki lamaran");
        }
        repository.delete(jobPosting);
    }

    public boolean hasApplications(JobPosting jobPosting) {
        if (jobPosting == null || jobPosting.getId() == null) {
            return false;
        }
        return jobApplicationRepository.existsByJobPosting(jobPosting);
    }

    public List<JobPosting> findByEmployer(User employer) {
        if (employer == null || employer.getId() == null) {
            return List.of();
        }
        return repository.findByEmployer(employer);
    }

    public List<JobPosting> findActiveJobPostings() {
        return repository.findByActiveTrue();
    }
}