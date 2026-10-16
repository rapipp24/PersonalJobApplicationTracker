package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyVerificationService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyVerificationService(
            CompanyRepository companyRepository,
            UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public List<Company> getPendingCompanies() {
        return companyRepository.findByVerificationStatus(CompanyVerificationStatus.PENDING);
    }

    public User getCompanyRegistrant(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Company tidak boleh null");
        }

        List<User> employers = userRepository.findByCompanyAndRole(company, Role.PEMBERI_LAMARAN);

        if (employers.isEmpty()) {
            throw new IllegalStateException("Tidak ada akun employer yang terhubung dengan perusahaan ini");
        }

        if (employers.size() > 1) {
            throw new IllegalStateException("Terdapat lebih dari satu akun employer yang terhubung dengan perusahaan ini");
        }

        return employers.get(0);
    }

    @Transactional
    public void approveCompany(Long companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID tidak boleh null");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Perusahaan tidak ditemukan dengan ID: " + companyId));

        if (company.getVerificationStatus() != CompanyVerificationStatus.PENDING) {
            throw new IllegalStateException("Hanya perusahaan dengan status PENDING yang dapat disetujui");
        }

        User employer = getCompanyRegistrant(company);

        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);

        employer.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);
        employer.setMembershipStatus(CompanyMembershipStatus.ACTIVE);

        companyRepository.save(company);
        userRepository.save(employer);
    }

    @Transactional
    public void rejectCompany(Long companyId, String rejectionReason) {
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID tidak boleh null");
        }

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Alasan penolakan wajib diisi");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Perusahaan tidak ditemukan dengan ID: " + companyId));

        if (company.getVerificationStatus() != CompanyVerificationStatus.PENDING) {
            throw new IllegalStateException("Hanya perusahaan dengan status PENDING yang dapat ditolak");
        }

        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        company.setRejectionReason(rejectionReason.trim());

        companyRepository.save(company);
    }
}
