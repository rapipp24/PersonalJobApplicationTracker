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

    public List<JobApplication> findByJobPosting(JobPosting jobPosting) {
        if (jobPosting == null || jobPosting.getId() == null) {
            return List.of();
        }
        return repository.findByJobPostingOrderByApplicationDateDesc(jobPosting);
    }

    public List<JobApplication> findByJobPosting(JobPosting jobPosting, User employer) {
        if (jobPosting == null || jobPosting.getId() == null) {
            return List.of();
        }
        if (jobPosting.getEmployer() == null || employer == null || !jobPosting.getEmployer().getId().equals(employer.getId())) {
            throw new SecurityException("Anda tidak memiliki hak untuk melihat pelamar lowongan ini");
        }
        return repository.findByJobPostingOrderByApplicationDateDesc(jobPosting);
    }

    public JobApplication updateStatus(
            JobApplication jobApplication,
            User employer,
            ApplicationStatus newStatus
    ) {
        if (jobApplication == null) {
            throw new IllegalArgumentException("Lamaran tidak boleh null");
        }

        if (jobApplication.getId() == null) {
            throw new IllegalArgumentException("ID lamaran tidak boleh null");
        }

        JobApplication existingApplication = repository.findById(jobApplication.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lamaran tidak ditemukan"));

        if (existingApplication.getJobPosting() == null) {
            throw new IllegalStateException("Lamaran tidak terhubung dengan lowongan kerja");
        }

        if (existingApplication.getJobPosting().getEmployer() == null) {
            throw new IllegalStateException("Lowongan tidak memiliki pemberi lamaran yang valid");
        }

        if (employer == null || employer.getId() == null) {
            throw new IllegalArgumentException("Employer tidak valid");
        }

        if (existingApplication.getJobPosting().getEmployer().getId() == null
                || !existingApplication.getJobPosting().getEmployer().getId().equals(employer.getId())) {
            throw new SecurityException("Anda tidak memiliki hak untuk mengubah status lamaran ini");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("Status baru tidak boleh kosong");
        }

        existingApplication.setStatus(newStatus);
        return repository.save(existingApplication);
    }

    public List<JobApplication> findByEmployer(User employer) {
        if (employer == null || employer.getId() == null) {
            return List.of();
        }
        return repository.findByJobPostingEmployerOrderByApplicationDateDesc(employer);
    }
}