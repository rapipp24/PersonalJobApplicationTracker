package com.example.service;

import java.util.List;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.JobApplicationRepository;
import com.example.entity.ApplicationStatus;
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
    
    public List<JobApplication> findAll(
        String keyword,
        ApplicationStatus status
    ) {

        List<JobApplication> data = repository.findAll();

        String kataKunci;

        if (keyword == null) {
            kataKunci = "";
        } else {
            kataKunci = keyword.toLowerCase();
        }

        List <JobApplication> hasilFilter = data.stream()
         .filter(jobApplication -> {

            boolean cocokKeyword = 
            jobApplication.getCompanyName().toLowerCase().contains(kataKunci)
            ||
            jobApplication.getPosition().toLowerCase().contains(kataKunci)
            ||
            (
                jobApplication.getNotes() != null && jobApplication.getNotes().toLowerCase().contains(kataKunci)
            );

            boolean cocokStatus = 
            status == null
            ||
            jobApplication.getStatus() == status;

            return cocokKeyword && cocokStatus;

            })
        
        .toList();

        return hasilFilter;
    }

    public List<JobApplication> findByApplicant(User applicant) {
        return repository.findByApplicant(applicant);
    }

    public List<JobApplication> findByApplicant(
            User applicant,
            String keyword,
            ApplicationStatus status
    ) {
        List<JobApplication> data = repository.findByApplicant(applicant);

        String kataKunci;

        if (keyword == null) {
            kataKunci = "";
        } else {
            kataKunci = keyword.toLowerCase();
        }

        List<JobApplication> hasilFilter = data.stream()
                .filter(jobApplication -> {
                    boolean cocokKeyword =
                            jobApplication.getCompanyName().toLowerCase().contains(kataKunci)
                            || jobApplication.getPosition().toLowerCase().contains(kataKunci)
                            || (jobApplication.getNotes() != null && jobApplication.getNotes().toLowerCase().contains(kataKunci));

                    boolean cocokStatus =
                            status == null
                            || jobApplication.getStatus() == status;

                    return cocokKeyword && cocokStatus;
                })
                .toList();

        return hasilFilter;
    }

    public JobApplication simpanLamaran(JobApplication jobApplication) {
        return repository.save(jobApplication);
    }

    public void hapusLamaran(JobApplication jobApplication) {
        repository.delete(jobApplication);
    }

    public boolean hasApplied(User applicant, JobPosting jobPosting) {
        return repository.existsByApplicantAndJobPosting(applicant, jobPosting);
    }
}