package com.example.Auth.repository;


import com.example.Auth.model.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Integer> {
    Optional<Verification> findByUserId(Long userId);

}
