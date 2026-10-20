package com.example.service;

import com.example.dto.RegistrationFormData;
import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<Company> findVerifiedCompanies(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return companyRepository.findByVerificationStatus(CompanyVerificationStatus.VERIFIED);
        }
        return companyRepository.findByNameContainingIgnoreCaseAndVerificationStatus(
                keyword.trim(),
                CompanyVerificationStatus.VERIFIED
        );
    }

    @Transactional
    public User register(
            String name,
            String email,
            String rawPassword,
            Role role,
            String companyName) {

        if (role == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Admin cannot register through public registration."
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "Email is already registered."
            );
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(role);

        if (role == Role.PEMBERI_LAMARAN) {
            Company company = new Company();
            company.setName(companyName);
            companyRepository.save(company);

            user.setCompany(company);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User registerEmployerWithNewCompany(RegistrationFormData formData) {
        if (formData == null) {
            throw new IllegalArgumentException("Registration data cannot be null.");
        }

        String email = formData.getEmail() != null ? formData.getEmail().trim() : "";
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email is already registered.");
        }

        String companyName = formData.getCompanyName() != null ? formData.getCompanyName().trim() : "";
        if (companyName.isEmpty()) {
            throw new IllegalArgumentException("Company Name is required.");
        }

        String industry = formData.getIndustry() != null ? formData.getIndustry().trim() : "";
        if (industry.isEmpty()) {
            throw new IllegalArgumentException("Industry is required.");
        }

        String location = formData.getLocation() != null ? formData.getLocation().trim() : "";
        if (location.isEmpty()) {
            throw new IllegalArgumentException("Company Location is required.");
        }

        String officialContact = formData.getOfficialContact() != null ? formData.getOfficialContact().trim() : "";
        if (officialContact.isEmpty()) {
            throw new IllegalArgumentException("Official Company Email / Contact is required.");
        }

        String description = formData.getDescription() != null ? formData.getDescription().trim() : "";
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Company Description is required.");
        }

        String website = formData.getWebsite() != null ? formData.getWebsite().trim() : "";

        // Validasi Duplicate Company (Case A, B, C)
        checkDuplicateCompany(formData);

        // 1. Simpan Company baru dengan status PENDING
        Company company = new Company();
        company.setName(companyName);
        company.setIndustry(industry);
        company.setLocation(location);
        company.setWebsite(website);
        company.setOfficialContact(officialContact);
        company.setDescription(description);
        company.setVerificationStatus(CompanyVerificationStatus.PENDING);
        company.setRejectionReason(null);

        Company savedCompany = companyRepository.save(company);

        // 2. Simpan User Employer baru
        String rawPassword = formData.getPassword() != null ? formData.getPassword() : "";
        if (rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);

        String fullName = formData.getFullName() != null ? formData.getFullName().trim() : "";
        if (fullName.isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }

        User user = new User();
        user.setName(fullName);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(Role.PEMBERI_LAMARAN);
        user.setCompany(savedCompany);
        user.setCompanyPermission(null);
        user.setMembershipStatus(CompanyMembershipStatus.PENDING);

        return userRepository.save(user);
    }

    @Transactional
    public User registerEmployerWithExistingCompany(RegistrationFormData formData) {
        if (formData == null) {
            throw new IllegalArgumentException("Registration data cannot be null.");
        }

        String email = formData.getEmail() != null ? formData.getEmail().trim() : "";
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email is already registered.");
        }

        Long companyId = formData.getSelectedCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("Company must be selected.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Selected company not found."));

        if (company.getVerificationStatus() != CompanyVerificationStatus.VERIFIED) {
            throw new IllegalStateException("Only VERIFIED companies can be joined.");
        }

        String rawPassword = formData.getPassword() != null ? formData.getPassword() : "";
        if (rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);

        String fullName = formData.getFullName() != null ? formData.getFullName().trim() : "";
        if (fullName.isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }

        User user = new User();
        user.setName(fullName);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(Role.PEMBERI_LAMARAN);
        user.setCompany(company);
        user.setMembershipStatus(CompanyMembershipStatus.PENDING);
        user.setCompanyPermission(CompanyPermission.MEMBER);
        user.setMembershipRequestedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    private static final List<String> PUBLIC_EMAIL_DOMAINS = List.of(
            "gmail.com",
            "yahoo.com",
            "outlook.com",
            "hotmail.com"
    );

    private void checkDuplicateCompany(RegistrationFormData formData) {
        String companyName = formData.getCompanyName() != null ? formData.getCompanyName().trim() : "";
        List<Company> candidates = companyRepository.findByNameIgnoreCase(companyName);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        String newWebsiteDomain = extractWebsiteDomain(formData.getWebsite());
        String newCorporateEmailDomain = extractCorporateEmailDomain(formData.getOfficialContact());
        String newIndustry = formData.getIndustry() != null ? formData.getIndustry().trim() : "";
        String newLocation = formData.getLocation() != null ? formData.getLocation().trim() : "";

        for (Company existing : candidates) {
            // Kita hanya memeriksa status VERIFIED dan PENDING
            if (existing.getVerificationStatus() != CompanyVerificationStatus.VERIFIED
                    && existing.getVerificationStatus() != CompanyVerificationStatus.PENDING) {
                continue;
            }

            boolean isDuplicate = false;

            String existingWebsiteDomain = extractWebsiteDomain(existing.getWebsite());
            String existingCorporateEmailDomain = extractCorporateEmailDomain(existing.getOfficialContact());

            // CASE A: Company Name sama + Website Domain sama
            if (newWebsiteDomain != null && existingWebsiteDomain != null) {
                if (newWebsiteDomain.equalsIgnoreCase(existingWebsiteDomain)) {
                    isDuplicate = true;
                }
            }

            // CASE B: Company Name sama + Corporate Email Domain sama
            if (!isDuplicate && newCorporateEmailDomain != null && existingCorporateEmailDomain != null) {
                if (newCorporateEmailDomain.equalsIgnoreCase(existingCorporateEmailDomain)) {
                    isDuplicate = true;
                }
            }

            // CASE C: Fallback ketika Website tidak tersedia dan corporate email domain juga tidak tersedia
            if (!isDuplicate) {
                boolean websiteConflict = (newWebsiteDomain != null && existingWebsiteDomain != null
                        && !newWebsiteDomain.equalsIgnoreCase(existingWebsiteDomain));
                boolean emailConflict = (newCorporateEmailDomain != null && existingCorporateEmailDomain != null
                        && !newCorporateEmailDomain.equalsIgnoreCase(existingCorporateEmailDomain));

                if (!websiteConflict && !emailConflict) {
                    boolean websiteUnavailable = (newWebsiteDomain == null || existingWebsiteDomain == null);
                    boolean emailUnavailable = (newCorporateEmailDomain == null || existingCorporateEmailDomain == null);

                    if (websiteUnavailable && emailUnavailable) {
                        String existingIndustry = existing.getIndustry() != null ? existing.getIndustry().trim() : "";
                        String existingLocation = existing.getLocation() != null ? existing.getLocation().trim() : "";

                        if (!newIndustry.isEmpty() && newIndustry.equalsIgnoreCase(existingIndustry)
                                && !newLocation.isEmpty() && newLocation.equalsIgnoreCase(existingLocation)) {
                            isDuplicate = true;
                        }
                    }
                }
            }

            if (isDuplicate) {
                if (existing.getVerificationStatus() == CompanyVerificationStatus.VERIFIED) {
                    throw new IllegalStateException("Company sudah terdaftar. Silakan gunakan Join Existing Company.");
                } else if (existing.getVerificationStatus() == CompanyVerificationStatus.PENDING) {
                    throw new IllegalStateException("Company tersebut sedang dalam proses verifikasi.");
                }
            }
        }
    }

    private String extractWebsiteDomain(String website) {
        if (website == null || website.trim().isEmpty()) {
            return null;
        }

        String cleaned = website.trim().toLowerCase();

        if (cleaned.startsWith("https://")) {
            cleaned = cleaned.substring("https://".length());
        } else if (cleaned.startsWith("http://")) {
            cleaned = cleaned.substring("http://".length());
        }

        if (cleaned.startsWith("www.")) {
            cleaned = cleaned.substring("www.".length());
        }

        int slashIndex = cleaned.indexOf('/');
        if (slashIndex != -1) {
            cleaned = cleaned.substring(0, slashIndex);
        }

        int colonIndex = cleaned.indexOf(':');
        if (colonIndex != -1) {
            cleaned = cleaned.substring(0, colonIndex);
        }

        cleaned = cleaned.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String extractCorporateEmailDomain(String officialContact) {
        if (officialContact == null || officialContact.trim().isEmpty()) {
            return null;
        }

        String cleaned = officialContact.trim().toLowerCase();

        int atIndex = cleaned.lastIndexOf('@');
        if (atIndex == -1 || atIndex == 0 || atIndex == cleaned.length() - 1) {
            return null;
        }

        String domain = cleaned.substring(atIndex + 1).trim();

        if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
            return null;
        }

        for (String publicDomain : PUBLIC_EMAIL_DOMAINS) {
            if (domain.equalsIgnoreCase(publicDomain)) {
                return null;
            }
        }

        return domain;
    }
}
