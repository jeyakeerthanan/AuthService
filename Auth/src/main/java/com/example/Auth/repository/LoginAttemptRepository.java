package com.example.Auth.repository;

import com.example.Auth.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository  extends JpaRepository<LoginAttempt, Integer> {
}
