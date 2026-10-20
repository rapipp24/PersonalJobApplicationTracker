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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nama tidak boleh kosong")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password tidak boleh kosong")
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull(message = "Role tidak boleh null")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_permission")
    private CompanyPermission companyPermission;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status")
    private CompanyMembershipStatus membershipStatus;

    @Column(name = "membership_requested_at")
    private LocalDateTime membershipRequestedAt;

    public User() {
    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CompanyPermission getCompanyPermission() {
        return companyPermission;
    }

    public void setCompanyPermission(CompanyPermission companyPermission) {
        this.companyPermission = companyPermission;
    }

    public CompanyMembershipStatus getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(CompanyMembershipStatus membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public LocalDateTime getMembershipRequestedAt() {
        return membershipRequestedAt;
    }

    public void setMembershipRequestedAt(LocalDateTime membershipRequestedAt) {
        this.membershipRequestedAt = membershipRequestedAt;
    }
}
