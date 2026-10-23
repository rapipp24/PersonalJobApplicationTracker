package com.example.repository;

import com.example.entity.Company;
import com.example.entity.CompanyVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    List<Company> findByVerificationStatus(CompanyVerificationStatus verificationStatus);

    List<Company> findByNameContainingIgnoreCaseAndVerificationStatus(
            String name,
            CompanyVerificationStatus verificationStatus);

    List<Company> findByNameIgnoreCase(String name);

    List<Company> findByNameContainingIgnoreCase(String name);
}

