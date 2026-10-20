package com.example.service;

import com.example.entity.ApplicantProfile;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.ApplicantProfileRepository;
import com.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicantProfileService {

    private final ApplicantProfileRepository applicantProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApplicantProfileService(
            ApplicantProfileRepository applicantProfileRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.applicantProfileRepository = applicantProfileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ApplicantProfile getOrCreateProfile(User currentUser) {
        validateApplicant(currentUser);

        return applicantProfileRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    ApplicantProfile newProfile = new ApplicantProfile(currentUser);
                    return applicantProfileRepository.save(newProfile);
                });
    }

    @Transactional
    public ApplicantProfile updateProfile(
            User currentUser,
            String phone,
            String linkedIn,
            String portfolio) {

        validateApplicant(currentUser);

        ApplicantProfile profile = getOrCreateProfile(currentUser);

        profile.setPhone(normalizeOptionalField(phone, 30, "Nomor telepon"));
        profile.setLinkedIn(normalizeOptionalField(linkedIn, 255, "LinkedIn"));
        profile.setPortfolio(normalizeOptionalField(portfolio, 255, "Portfolio"));

        return applicantProfileRepository.save(profile);
    }

    @Transactional
    public User updateFullName(User currentUser, String fullName) {
        validateApplicant(currentUser);

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

        validateApplicant(currentUser);

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

    private void validateApplicant(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User tidak boleh null");
        }
        if (user.getRole() != Role.PELAMAR) {
            throw new SecurityException("Hanya pelamar yang dapat mengakses profil ini");
        }
    }

    private String normalizeOptionalField(String value, int maxLength, String fieldName) {
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
