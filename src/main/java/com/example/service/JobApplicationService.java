package com.example.service;

import java.util.List;
import com.example.entity.JobApplication;
import com.example.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationService {
    
    private final JobApplicationRepository repository;
    
    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public List<JobApplication> findAll() {
        return repository.findAll();
    }

    public JobApplication simpanLamaran(JobApplication jobApplication) {
        return repository.save(jobApplication);
    }

    public void hapusLamaran(JobApplication jobApplication) {
        repository.delete(jobApplication);
    }
}