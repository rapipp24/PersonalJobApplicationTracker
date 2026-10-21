package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
public class JobApplication{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Nama perusahaan harus diisi")
    private String companyName;

    @NotBlank(message = "Posisi harus diisi")
    private String position;

    private Long expectedSalary;
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private ApplicationTipe tipeKerja;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @ManyToOne
    @JoinColumn(name = "job_posting_id")
    private JobPosting jobPosting;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String linkedIn;

    @Column(length = 255)
    private String portfolio;

    @Column(name = "resume_storage_key", length = 255)
    private String resumeStorageKey;

    @Column(name = "resume_original_filename", length = 255)
    private String resumeOriginalFilename;

    @Column(name = "resume_content_type", length = 100)
    private String resumeContentType;

    @Column(name = "resume_file_size")
    private Long resumeFileSize;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
    this.companyName = companyName;
}
    
    public String getPosition() {
    return position;
    }

    public void setPosition(String position) {
    this.position = position;
    }

    public Long getExpectedSalary() {
    return expectedSalary;
    }

    public void setExpectedSalary(Long expectedSalary) {
    this.expectedSalary = expectedSalary;
    }

    public LocalDate getApplicationDate() {
    return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
    this.applicationDate = applicationDate;
    }

    public ApplicationStatus getStatus() {
    return status;
    }

    public void setStatus(ApplicationStatus status) {
    this.status = status;   
    }

    public String getNotes() {
    return notes;
    }

    public void setNotes(String notes) {
    this.notes = notes;
    }

    public ApplicationTipe gettipeKerja() {
    return tipeKerja;
    }

    public void settipeKerja(ApplicationTipe tipeKerja) {
        this.tipeKerja = tipeKerja;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public JobPosting getJobPosting() {
        return jobPosting;
    }

    public void setJobPosting(JobPosting jobPosting) {
        this.jobPosting = jobPosting;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLinkedIn() {
        return linkedIn;
    }

    public void setLinkedIn(String linkedIn) {
        this.linkedIn = linkedIn;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public String getResumeStorageKey() {
        return resumeStorageKey;
    }

    public void setResumeStorageKey(String resumeStorageKey) {
        this.resumeStorageKey = resumeStorageKey;
    }

    public String getResumeOriginalFilename() {
        return resumeOriginalFilename;
    }

    public void setResumeOriginalFilename(String resumeOriginalFilename) {
        this.resumeOriginalFilename = resumeOriginalFilename;
    }

    public String getResumeContentType() {
        return resumeContentType;
    }

    public void setResumeContentType(String resumeContentType) {
        this.resumeContentType = resumeContentType;
    }

    public Long getResumeFileSize() {
        return resumeFileSize;
    }

    public void setResumeFileSize(Long resumeFileSize) {
        this.resumeFileSize = resumeFileSize;
    }
}




