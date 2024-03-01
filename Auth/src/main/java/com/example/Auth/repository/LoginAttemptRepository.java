package com.example.Auth.repository;

import com.example.Auth.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAttemptRepository  extends JpaRepository<LoginAttempt, Long> {
}
