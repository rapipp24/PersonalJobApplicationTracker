package com.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class JobApplication{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String companyName;
    private String position;
    private Long expectedSalary;
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String notes;

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
}




