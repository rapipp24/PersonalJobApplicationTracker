package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public boolean canEditCompanyProfile(User currentUser) {
        if (currentUser == null || currentUser.getRole() != Role.PEMBERI_LAMARAN) {
            return false;
        }
        Company company = currentUser.getCompany();
        if (company == null) {
            return false;
        }
        if (currentUser.getCompanyPermission() == CompanyPermission.COMPANY_ADMIN) {
            return true;
        }
        return company.getVerificationStatus() == CompanyVerificationStatus.REJECTED
                && isInitialCompanyRegistrant(currentUser, company);
    }

    @Transactional(readOnly = true)
    public boolean canResubmitVerification(User currentUser) {
        if (currentUser == null || currentUser.getRole() != Role.PEMBERI_LAMARAN) {
            return false;
        }
        Company company = currentUser.getCompany();
        if (company == null || company.getVerificationStatus() != CompanyVerificationStatus.REJECTED) {
            return false;
        }
        return currentUser.getCompanyPermission() == CompanyPermission.COMPANY_ADMIN
                || isInitialCompanyRegistrant(currentUser, company);
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

        boolean isCompanyAdmin = currentUser.getCompanyPermission() == CompanyPermission.COMPANY_ADMIN;
        boolean isAllowedRegistrant = company.getVerificationStatus() == CompanyVerificationStatus.REJECTED
                && isInitialCompanyRegistrant(currentUser, company);

        if (!isCompanyAdmin && !isAllowedRegistrant) {
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
    public Company resubmitCompanyVerification(User currentUser) {
        validateEmployer(currentUser);

        Company company = currentUser.getCompany();
        if (company == null) {
            throw new IllegalStateException("Akun employer belum terhubung dengan perusahaan");
        }

        boolean isCompanyAdmin = currentUser.getCompanyPermission() == CompanyPermission.COMPANY_ADMIN;
        boolean isRegistrant = isInitialCompanyRegistrant(currentUser, company);

        if (!isCompanyAdmin && !isRegistrant) {
            throw new SecurityException("Hanya COMPANY_ADMIN atau pendaftar awal yang berhak mengajukan ulang verifikasi perusahaan");
        }

        if (company.getVerificationStatus() != CompanyVerificationStatus.REJECTED) {
            throw new IllegalStateException("Hanya perusahaan dengan status REJECTED yang dapat diajukan ulang verifikasinya");
        }

        company.setVerificationStatus(CompanyVerificationStatus.PENDING);
        company.setRejectionReason(null);

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

    private boolean isInitialCompanyRegistrant(User user, Company company) {
        if (user == null || company == null) {
            return false;
        }
        if (user.getRole() != Role.PEMBERI_LAMARAN) {
            return false;
        }
        if (user.getCompanyPermission() != null) {
            return false;
        }
        List<User> employers = userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN);
        if (employers.size() != 1) {
            return false;
        }
        User registrant = employers.get(0);
        if (user.getId() != null && registrant.getId() != null) {
            return user.getId().equals(registrant.getId());
        }
        if (user.getEmail() != null && registrant.getEmail() != null) {
            return user.getEmail().equalsIgnoreCase(registrant.getEmail());
        }
        return user == registrant;
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
