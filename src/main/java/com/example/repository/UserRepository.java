package com.example.repository;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.Role;
import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByCompanyAndRole(Company company, Role role);

    List<User> findByCompanyAndMembershipStatus(Company company, CompanyMembershipStatus membershipStatus);
}
