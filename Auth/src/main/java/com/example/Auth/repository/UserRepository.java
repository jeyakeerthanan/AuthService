package com.example.Auth.repository;

import com.example.Auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserName(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String verifier);

    Optional<User> findByPhoneNumber(String verifier);
    // Add custom query methods if needed, e.g., findByUsername(), findByEmail(), etc.
}
