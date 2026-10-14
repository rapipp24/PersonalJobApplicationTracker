package com.example.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CompanyStructureTest {

    @Test
    void testCompanyDapatMenyimpanDanMembacaSeluruhFieldProfile() {
        Company company = new Company();
        company.setName("PT Solusi Teknologi");
        company.setIndustry("Teknologi Informasi");
        company.setLocation("Jakarta Selatan");
        company.setWebsite("https://solusiteknologi.co.id");
        company.setOfficialContact("hrd@solusiteknologi.co.id");
        company.setDescription("Perusahaan pengembang perangkat lunak dan solusi digital.");
        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);
        company.setRejectionReason(null);

        assertEquals("PT Solusi Teknologi", company.getName());
        assertEquals("Teknologi Informasi", company.getIndustry());
        assertEquals("Jakarta Selatan", company.getLocation());
        assertEquals("https://solusiteknologi.co.id", company.getWebsite());
        assertEquals("hrd@solusiteknologi.co.id", company.getOfficialContact());
        assertEquals("Perusahaan pengembang perangkat lunak dan solusi digital.", company.getDescription());
        assertEquals(CompanyVerificationStatus.VERIFIED, company.getVerificationStatus());
        assertNull(company.getRejectionReason());

        // Uji jika status REJECTED dengan alasan penolakan
        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        company.setRejectionReason("Dokumen legalitas perusahaan belum lengkap");
        assertEquals(CompanyVerificationStatus.REJECTED, company.getVerificationStatus());
        assertEquals("Dokumen legalitas perusahaan belum lengkap", company.getRejectionReason());
    }

    @Test
    void testCompanyBaruMemilikiDefaultVerificationStatusPending() {
        Company company = new Company();
        assertNotNull(company.getVerificationStatus());
        assertEquals(CompanyVerificationStatus.PENDING, company.getVerificationStatus());
    }

    @Test
    void testUserDapatMenyimpanCompanyDanMembershipFields() {
        Company company = new Company();
        company.setName("PT Maju Bersama");

        User user = new User();
        user.setName("Budi Santoso");
        user.setEmail("budi@majubersama.com");
        user.setRole(Role.PEMBERI_LAMARAN);
        user.setCompany(company);
        user.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);
        user.setMembershipStatus(CompanyMembershipStatus.ACTIVE);

        assertEquals(company, user.getCompany());
        assertEquals("PT Maju Bersama", user.getCompany().getName());
        assertEquals(CompanyPermission.COMPANY_ADMIN, user.getCompanyPermission());
        assertEquals(CompanyMembershipStatus.ACTIVE, user.getMembershipStatus());
    }

    @Test
    void testDuaEmployerDapatMereferensikanCompanyYangSama() {
        Company company = new Company();
        company.setName("PT Inovasi Mandiri");

        // Employer 1: Admin Company
        User employer1 = new User();
        employer1.setName("Alice");
        employer1.setEmail("alice@inovasi.com");
        employer1.setRole(Role.PEMBERI_LAMARAN);
        employer1.setCompany(company);
        employer1.setCompanyPermission(CompanyPermission.COMPANY_ADMIN);
        employer1.setMembershipStatus(CompanyMembershipStatus.ACTIVE);

        // Employer 2: Member biasa yang baru bergabung
        User employer2 = new User();
        employer2.setName("Bob");
        employer2.setEmail("bob@inovasi.com");
        employer2.setRole(Role.PEMBERI_LAMARAN);
        employer2.setCompany(company);
        employer2.setCompanyPermission(CompanyPermission.MEMBER);
        employer2.setMembershipStatus(CompanyMembershipStatus.PENDING);

        // Keduanya menunjuk ke satu instance Company yang sama
        assertSame(employer1.getCompany(), employer2.getCompany());
        assertEquals("PT Inovasi Mandiri", employer1.getCompany().getName());
        assertEquals("PT Inovasi Mandiri", employer2.getCompany().getName());

        // Status dan Permission masing-masing user independen
        assertEquals(CompanyPermission.COMPANY_ADMIN, employer1.getCompanyPermission());
        assertEquals(CompanyMembershipStatus.ACTIVE, employer1.getMembershipStatus());

        assertEquals(CompanyPermission.MEMBER, employer2.getCompanyPermission());
        assertEquals(CompanyMembershipStatus.PENDING, employer2.getMembershipStatus());
    }
}
