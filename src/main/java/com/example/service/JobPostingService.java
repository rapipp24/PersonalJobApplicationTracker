package com.example.service;

import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.JobPostingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobPostingService {

    private final JobPostingRepository repository;

    public JobPostingService(JobPostingRepository repository) {
        this.repository = repository;
    }

    public List<JobPosting> findAll() {
        return repository.findAll();
    }

    public JobPosting simpanLowongan(JobPosting jobPosting) {
        return repository.save(jobPosting);
    }

    public void hapusLowongan(JobPosting jobPosting) {
        repository.delete(jobPosting);
    }

    public List<JobPosting> findByEmployer(User employer) {
        return repository.findByEmployer(employer);
    }

    public List<JobPosting> findActiveJobPostings() {
        return repository.findByActiveTrue();
    }
}