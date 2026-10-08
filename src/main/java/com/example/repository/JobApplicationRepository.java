package com.example.repository;

import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByApplicant(User applicant);

    boolean existsByApplicantAndJobPosting(User applicant, JobPosting jobPosting);

    boolean existsByJobPosting(JobPosting jobPosting);

    List<JobApplication> findByJobPosting(JobPosting jobPosting);

    List<JobApplication> findByJobPostingOrderByApplicationDateDesc(JobPosting jobPosting);

    List<JobApplication> findByJobPostingEmployerOrderByApplicationDateDesc(User employer);
}