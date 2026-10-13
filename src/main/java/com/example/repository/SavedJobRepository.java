package com.example.repository;

import com.example.entity.JobPosting;
import com.example.entity.SavedJob;
import com.example.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    @EntityGraph(attributePaths = "jobPosting")
    List<SavedJob> findByApplicantOrderBySavedAtDesc(User applicant);

    boolean existsByApplicantAndJobPosting(User applicant, JobPosting jobPosting);

    Optional<SavedJob> findByApplicantAndJobPosting(User applicant, JobPosting jobPosting);

    void deleteByApplicantAndJobPosting(User applicant, JobPosting jobPosting);
}
