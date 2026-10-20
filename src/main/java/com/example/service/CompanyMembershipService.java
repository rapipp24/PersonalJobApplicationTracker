package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyMembershipService {

    private final UserRepository userRepository;

    public CompanyMembershipService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getPendingMembers(User companyAdmin) {
        Company adminCompany = validateCompanyAdmin(companyAdmin);

        return userRepository.findByCompanyAndMembershipStatus(
                adminCompany,
                CompanyMembershipStatus.PENDING
        );
    }

    @Transactional
    public User approveMember(Long candidateUserId, User companyAdmin) {
        Company adminCompany = validateCompanyAdmin(companyAdmin);
        User candidate = validateCandidateForApproval(candidateUserId, adminCompany);

        candidate.setMembershipStatus(CompanyMembershipStatus.ACTIVE);

        return userRepository.save(candidate);
    }

    @Transactional
    public User rejectMember(Long candidateUserId, User companyAdmin) {
        Company adminCompany = validateCompanyAdmin(companyAdmin);
        User candidate = validateCandidateForApproval(candidateUserId, adminCompany);

        candidate.setMembershipStatus(CompanyMembershipStatus.REJECTED);

        return userRepository.save(candidate);
    }

    private Company validateCompanyAdmin(User companyAdmin) {
        if (companyAdmin == null) {
            throw new IllegalArgumentException("Company admin tidak boleh null");
        }

        if (companyAdmin.getRole() != Role.PEMBERI_LAMARAN) {
            throw new SecurityException("User bukan employer");
        }

        Company adminCompany = companyAdmin.getCompany();
        if (adminCompany == null) {
            throw new IllegalStateException("Akun employer belum terhubung dengan perusahaan");
        }

        if (adminCompany.getVerificationStatus() != CompanyVerificationStatus.VERIFIED) {
            throw new IllegalStateException("Perusahaan belum terverifikasi");
        }

        if (companyAdmin.getMembershipStatus() != CompanyMembershipStatus.ACTIVE) {
            throw new IllegalStateException("Status membership admin belum aktif");
        }

        if (companyAdmin.getCompanyPermission() != CompanyPermission.COMPANY_ADMIN) {
            throw new SecurityException("Hanya COMPANY_ADMIN yang berhak mengakses fitur ini");
        }

        return adminCompany;
    }

    private User validateCandidateForApproval(Long candidateUserId, Company adminCompany) {
        if (candidateUserId == null) {
            throw new IllegalArgumentException("ID calon anggota tidak boleh null");
        }

        User candidate = userRepository.findById(candidateUserId)
                .orElseThrow(() -> new IllegalArgumentException("Calon anggota tidak ditemukan dengan ID: " + candidateUserId));

        if (candidate.getRole() != Role.PEMBERI_LAMARAN) {
            throw new IllegalStateException("Calon anggota bukan merupakan employer");
        }

        Company candidateCompany = candidate.getCompany();
        if (candidateCompany == null) {
            throw new IllegalStateException("Calon anggota belum terhubung dengan perusahaan");
        }

        if (adminCompany.getId() == null || candidateCompany.getId() == null
                || !adminCompany.getId().equals(candidateCompany.getId())) {
            throw new SecurityException("Anda hanya dapat mengelola keanggotaan perusahaan Anda sendiri");
        }

        if (candidate.getMembershipStatus() != CompanyMembershipStatus.PENDING) {
            throw new IllegalStateException("Hanya calon anggota berstatus PENDING yang dapat diproses");
        }

        if (candidate.getCompanyPermission() != CompanyPermission.MEMBER) {
            throw new IllegalStateException("Calon anggota harus memiliki permission MEMBER");
        }

        return candidate;
    }
}
