package com.example.repository;

import com.example.entity.JobApplication;
import org.springframework.data.repository.CrudRepository;

public interface JobApplicationRepository extends CrudRepository<JobApplication, Long> {
    
}