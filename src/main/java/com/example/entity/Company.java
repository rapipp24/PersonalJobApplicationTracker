package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nama perusahaan tidak boleh kosong")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "official_contact", length = 255)
    private String officialContact;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private CompanyVerificationStatus verificationStatus = CompanyVerificationStatus.PENDING;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    public Company() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getOfficialContact() {
        return officialContact;
    }

    public void setOfficialContact(String officialContact) {
        this.officialContact = officialContact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CompanyVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(CompanyVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
