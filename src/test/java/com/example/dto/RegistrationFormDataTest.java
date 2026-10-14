package com.example.dto;

import com.example.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RegistrationFormDataTest {

    @Test
    void testDefaultCompanyRegistrationTypeAdalahRegisterNewCompany() {
        RegistrationFormData formData = new RegistrationFormData();
        assertEquals(CompanyRegistrationType.REGISTER_NEW_COMPANY, formData.getCompanyRegistrationType());
        assertNull(formData.getSelectedCompanyId());
    }

    @Test
    void testSimpanDataRegisterNewCompany() {
        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Budi Santoso");
        formData.setEmail("budi@company.com");
        formData.setPassword("password123");
        formData.setConfirmPassword("password123");
        formData.setAccountType(Role.PEMBERI_LAMARAN);
        formData.setCompanyRegistrationType(CompanyRegistrationType.REGISTER_NEW_COMPANY);
        formData.setCompanyName("PT Maju Teknologi");
        formData.setIndustry("Teknologi Informasi");
        formData.setLocation("Jakarta Selatan");
        formData.setWebsite("https://majutek.id");
        formData.setOfficialContact("hrd@majutek.id");
        formData.setDescription("Perusahaan software development terkemuka");

        assertEquals(CompanyRegistrationType.REGISTER_NEW_COMPANY, formData.getCompanyRegistrationType());
        assertEquals("PT Maju Teknologi", formData.getCompanyName());
        assertEquals("Teknologi Informasi", formData.getIndustry());
        assertEquals("Jakarta Selatan", formData.getLocation());
        assertEquals("https://majutek.id", formData.getWebsite());
        assertEquals("hrd@majutek.id", formData.getOfficialContact());
        assertEquals("Perusahaan software development terkemuka", formData.getDescription());
        assertNull(formData.getSelectedCompanyId());
    }

    @Test
    void testSimpanDataJoinExistingCompanyDenganSelectedCompanyId() {
        RegistrationFormData formData = new RegistrationFormData();
        formData.setFullName("Siti Aminah");
        formData.setEmail("siti@mitra.com");
        formData.setPassword("password123");
        formData.setConfirmPassword("password123");
        formData.setAccountType(Role.PEMBERI_LAMARAN);
        formData.setCompanyRegistrationType(CompanyRegistrationType.JOIN_EXISTING_COMPANY);
        formData.setSelectedCompanyId(42L);
        formData.setCompanyName("PT Toko Sebelah");

        assertEquals(CompanyRegistrationType.JOIN_EXISTING_COMPANY, formData.getCompanyRegistrationType());
        assertEquals(42L, formData.getSelectedCompanyId());
        assertEquals("PT Toko Sebelah", formData.getCompanyName());
    }
}
