package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
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

    public JobPosting simpanLowongan(JobPosting jobPosting, User currentUser) {
        if (jobPosting == null) {
            throw new IllegalArgumentException("Lowongan pekerjaan tidak boleh null");
        }

        Company employerCompany = validateActiveEmployer(currentUser);

        if (jobPosting.getId() == null) {
            jobPosting.setEmployer(currentUser);
            jobPosting.setCompany(employerCompany);
            if (jobPosting.getCompanyName() == null || jobPosting.getCompanyName().isBlank()) {
                jobPosting.setCompanyName(employerCompany.getName());
            }
            return repository.save(jobPosting);
        }

        JobPosting existing = repository.findById(jobPosting.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lowongan tidak ditemukan"));

        validateSameCompany(existing, employerCompany);
        validateJobPostingManagementAccess(existing, currentUser);

        jobPosting.setEmployer(existing.getEmployer());
        jobPosting.setCompany(existing.getCompany() != null ? existing.getCompany() : employerCompany);
        if (jobPosting.getCompanyName() == null || jobPosting.getCompanyName().isBlank()) {
            jobPosting.setCompanyName(existing.getCompanyName());
        }

        return repository.save(jobPosting);
    }

    public void hapusLowongan(JobPosting jobPosting, User employer) {
        if (jobPosting == null || jobPosting.getId() == null) {
            throw new IllegalArgumentException("Lowongan tidak valid untuk dihapus");
        }

        Company employerCompany = validateActiveEmployer(employer);

        JobPosting existing = repository.findById(jobPosting.getId())
                .orElse(jobPosting);

        validateSameCompany(existing, employerCompany);
        validateJobPostingManagementAccess(existing, employer);

        if (hasApplications(existing)) {
            throw new IllegalStateException("Lowongan tidak dapat dihapus karena sudah memiliki lamaran");
        }
        repository.delete(existing);
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

    public List<JobPosting> findByCompany(Company company) {
        if (company == null || company.getId() == null) {
            return List.of();
        }
        return repository.findByCompany(company);
    }

    public List<JobPosting> findActiveJobPostings() {
        return repository.findByActiveTrue();
    }

    Company getJobPostingCompany(JobPosting jobPosting) {
        if (jobPosting == null) {
            return null;
        }
        Company company = jobPosting.getCompany();
        if (company != null) {
            return company;
        }
        if (jobPosting.getEmployer() != null) {
            return jobPosting.getEmployer().getCompany();
        }
        return null;
    }

    private Company validateActiveEmployer(User employer) {
        if (employer == null) {
            throw new IllegalStateException("Employer tidak boleh null untuk menyimpan lowongan");
        }

        Company company = employer.getCompany();
        if (company == null) {
            throw new IllegalStateException("Akun employer belum terhubung dengan data perusahaan");
        }

        CompanyVerificationStatus verificationStatus = company.getVerificationStatus();
        if (verificationStatus != CompanyVerificationStatus.VERIFIED) {
            throw new IllegalStateException("Perusahaan harus berstatus VERIFIED untuk menyimpan lowongan pekerjaan");
        }

        CompanyMembershipStatus membershipStatus = employer.getMembershipStatus();
        if (membershipStatus != CompanyMembershipStatus.ACTIVE) {
            throw new IllegalStateException("Status membership employer belum aktif");
        }

        return company;
    }

    private void validateSameCompany(JobPosting jobPosting, Company employerCompany) {
        Company postingCompany = getJobPostingCompany(jobPosting);
        if (postingCompany == null || postingCompany.getId() == null) {
            throw new IllegalStateException("Lowongan tidak terhubung dengan data perusahaan yang valid");
        }
        if (employerCompany == null || employerCompany.getId() == null) {
            throw new IllegalStateException("Perusahaan employer tidak valid");
        }
        if (!postingCompany.getId().equals(employerCompany.getId())) {
            throw new SecurityException("Anda tidak memiliki hak untuk mengakses lowongan ini");
        }
    }

    private void validateJobPostingManagementAccess(JobPosting jobPosting, User currentUser) {
        if (jobPosting == null || jobPosting.getEmployer() == null || jobPosting.getEmployer().getId() == null) {
            throw new IllegalStateException("Pembuat lowongan tidak valid");
        }
        if (currentUser == null || currentUser.getId() == null) {
            throw new SecurityException("Anda tidak memiliki hak untuk mengubah lowongan ini");
        }
        if (jobPosting.getEmployer().getId().equals(currentUser.getId())) {
            return;
        }
        if (currentUser.getCompanyPermission() == CompanyPermission.COMPANY_ADMIN) {
            return;
        }
        throw new SecurityException("Anda tidak memiliki hak untuk mengubah lowongan ini");
    }
}