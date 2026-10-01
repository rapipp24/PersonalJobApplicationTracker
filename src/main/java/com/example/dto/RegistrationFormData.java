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
}
