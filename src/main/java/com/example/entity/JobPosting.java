package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Posisi harus diisi")
    private String position;

    @NotBlank(message = "Perusahaan harus diisi")
    private String companyName;

    @NotBlank(message = "Lokasi harus diisi")
    private String location;

    @NotNull(message = "Tipe kerja harus dipilih")
    @Enumerated(EnumType.STRING)
    private ApplicationTipe tipeKerja;

    @NotNull(message = "Jenis pekerjaan harus dipilih")
    @Enumerated(EnumType.STRING)
    private JobTipe jobTipe;

    private Long salaryMin;

    private Long salaryMax;

    @NotBlank(message = "Deskripsi pekerjaan harus diisi")
    @Column(length = 3000)
    private String description;

    @Column(length = 3000)
    private String requirements;

    private LocalDate postedDate = LocalDate.now();

    private boolean active = true;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer; //user yang membuat job posting (employe)

    public JobPosting() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ApplicationTipe getTipeKerja() {
        return tipeKerja;
    }

    public void setTipeKerja(ApplicationTipe tipeKerja) {
        this.tipeKerja = tipeKerja;
    }

    public JobTipe getJobTipe() {
        return jobTipe;
    }

    public void setJobTipe(JobTipe jobTipe) {
        this.jobTipe = jobTipe;
    }

    public Long getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(Long salaryMin) {
        this.salaryMin = salaryMin;
    }

    public Long getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(Long salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public LocalDate getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(LocalDate postedDate) {
        this.postedDate = postedDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getEmployer() {
        return employer;
    }

    public void setEmployer(User employer) {
        this.employer = employer;
    }
}