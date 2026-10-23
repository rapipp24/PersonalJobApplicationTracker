package com.example.repository;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.Role;
import com.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByCompanyAndRole(Company company, Role role);

    List<User> findByCompanyAndMembershipStatus(Company company, CompanyMembershipStatus membershipStatus);

    @Query("""
        SELECT u FROM User u
        LEFT JOIN u.company c
        WHERE u.role <> :adminRole
          AND (:role IS NULL OR u.role = :role)
          AND (
                :keyword = ''
                OR LOWER(u.name) LIKE CONCAT('%', :keyword, '%')
                OR LOWER(u.email) LIKE CONCAT('%', :keyword, '%')
              )
          AND (
                :companyId IS NULL
                OR c.id = :companyId
              )
          AND (
                (
                    :membershipStatus IS NULL
                    AND (
                        u.role = :applicantRole
                        OR (
                            u.role = :employerRole
                            AND (
                                u.membershipStatus = :activeStatus
                                OR u.membershipStatus = :pendingStatus
                            )
                        )
                    )
                )
                OR
                (
                    :membershipStatus IS NOT NULL
                    AND u.membershipStatus = :membershipStatus
                )
              )
        """)
    Page<User> findForAdminMonitoring(
            @Param("adminRole") Role adminRole,
            @Param("role") Role role,
            @Param("keyword") String keyword,
            @Param("companyId") Long companyId,
            @Param("membershipStatus") CompanyMembershipStatus membershipStatus,
            @Param("applicantRole") Role applicantRole,
            @Param("employerRole") Role employerRole,
            @Param("activeStatus") CompanyMembershipStatus activeStatus,
            @Param("pendingStatus") CompanyMembershipStatus pendingStatus,
            Pageable pageable);

    default Page<User> findForAdminMonitoring(
            Role role,
            String keyword,
            Long companyId,
            CompanyMembershipStatus membershipStatus,
            Pageable pageable) {
        String safeKeyword = keyword == null ? "" : keyword.trim().toLowerCase(java.util.Locale.ROOT);
        return findForAdminMonitoring(
                Role.ADMIN,
                role,
                safeKeyword,
                companyId,
                membershipStatus,
                Role.PELAMAR,
                Role.PEMBERI_LAMARAN,
                CompanyMembershipStatus.ACTIVE,
                CompanyMembershipStatus.PENDING,
                pageable
        );
    }
}
