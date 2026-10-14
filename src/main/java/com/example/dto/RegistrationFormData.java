package com.example.dto;

import com.example.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegistrationFormData {

    @NotBlank(message = "Full Name is required.")
    @Size(min = 2, max = 100, message = "Full Name must contain between 2 and 100 characters.")
    private String fullName = "";

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 254, message = "Email must not exceed 254 characters.")
    private String email = "";

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must contain at least 8 characters.")
    private String password = "";

    @NotBlank(message = "Password confirmation is required.")
    private String confirmPassword = "";

    @NotNull(message = "Please select an account type.")
    private Role accountType;

    // Nama perusahaan — hanya wajib untuk PEMBERI_LAMARAN.
    // Tidak dipasang @NotBlank di sini supaya PELAMAR tidak gagal validasi.
    // Validasi conditional dilakukan di RegisterView sebelum memanggil service.
    private String companyName = "";

    private CompanyRegistrationType companyRegistrationType = CompanyRegistrationType.REGISTER_NEW_COMPANY;
    private String industry = "";
    private String location = "";
    private String website = "";
    private String officialContact = "";
    private String description = "";
    private Long selectedCompanyId;

    public RegistrationFormData() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Role getAccountType() {
        return accountType;
    }

    public void setAccountType(Role accountType) {
        this.accountType = accountType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public CompanyRegistrationType getCompanyRegistrationType() {
        return companyRegistrationType;
    }

    public void setCompanyRegistrationType(CompanyRegistrationType companyRegistrationType) {
        this.companyRegistrationType = companyRegistrationType;
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

    public Long getSelectedCompanyId() {
        return selectedCompanyId;
    }

    public void setSelectedCompanyId(Long selectedCompanyId) {
        this.selectedCompanyId = selectedCompanyId;
    }
}
