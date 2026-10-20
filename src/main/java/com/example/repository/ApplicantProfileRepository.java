package com.example.repository;

import com.example.entity.ApplicantProfile;
import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicantProfileRepository extends JpaRepository<ApplicantProfile, Long> {

    Optional<ApplicantProfile> findByUser(User user);
}
