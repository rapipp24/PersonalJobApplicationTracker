package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyPermission;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployerSettingsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployerSettingsService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Company getCompanyProfile(User currentUser) {
        validateEmployer(currentUser);

        Company company = currentUser.getCompany();
        if (company == null) {
            throw new IllegalStateException("Akun employer belum terhubung dengan perusahaan");
        }

        return company;
    }

    @Transactional
    public Company updateCompanyProfile(
            User currentUser,
            String companyName,
            String industry,
            String location,
            String website,
            String officialContact,
            String description) {

        validateEmployer(currentUser);

        Company company = currentUser.getCompany();
        if (company == null) {
            throw new IllegalStateException("Akun employer belum terhubung dengan perusahaan");
        }

        if (currentUser.getCompanyPermission() != CompanyPermission.COMPANY_ADMIN) {
            throw new SecurityException("Hanya COMPANY_ADMIN yang berhak mengubah profil perusahaan");
        }

        String trimmedName = validateRequired(companyName, 255, "Nama perusahaan");
        String trimmedIndustry = validateRequired(industry, 100, "Industri");
        String trimmedLocation = validateRequired(location, 255, "Lokasi perusahaan");
        String normalizedWebsite = normalizeOptional(website, 255, "Website");
        String trimmedContact = validateRequired(officialContact, 255, "Kontak resmi perusahaan");
        String trimmedDesc = validateRequired(description, 2000, "Deskripsi perusahaan");

        company.setName(trimmedName);
        company.setIndustry(trimmedIndustry);
        company.setLocation(trimmedLocation);
        company.setWebsite(normalizedWebsite);
        company.setOfficialContact(trimmedContact);
        company.setDescription(trimmedDesc);

        return companyRepository.save(company);
    }

    @Transactional
    public User updateFullName(User currentUser, String fullName) {
        validateEmployer(currentUser);

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama lengkap tidak boleh kosong");
        }

        currentUser.setName(fullName.trim());
        return userRepository.save(currentUser);
    }

    @Transactional
    public void changePassword(
            User currentUser,
            String currentPassword,
            String newPassword,
            String confirmPassword) {

        validateEmployer(currentUser);

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("Password saat ini tidak boleh kosong");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Password baru tidak boleh kosong");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("Konfirmasi password tidak boleh kosong");
        }

        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new IllegalArgumentException("Password saat ini salah");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Konfirmasi password tidak cocok");
        }

        String encoded = passwordEncoder.encode(newPassword);
        currentUser.setPassword(encoded);
        userRepository.save(currentUser);
    }

    private void validateEmployer(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User tidak boleh null");
        }
        if (user.getRole() != Role.PEMBERI_LAMARAN) {
            throw new SecurityException("Hanya employer yang dapat mengakses layanan ini");
        }
    }

    private String validateRequired(String value, int maxLength, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " tidak boleh kosong");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " tidak boleh melebihi " + maxLength + " karakter");
        }
        return trimmed;
    }

    private String normalizeOptional(String value, int maxLength, String fieldName) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " tidak boleh melebihi " + maxLength + " karakter");
        }
        return trimmed;
    }
}
