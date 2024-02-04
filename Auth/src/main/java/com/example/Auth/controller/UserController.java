package com.example.Auth.controller;

import com.example.Auth.model.dto.AuthenticationResponse;
import com.example.Auth.model.dto.RegisterResponse;
import com.example.Auth.model.User;
import com.example.Auth.model.dto.VerificationResponse;
import com.example.Auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody User user) {
        try {
            var savedUser= userService.registerUser(user);
            var response = RegisterResponse.builder()
                    .email(savedUser.getEmail())
                    .phoneNumber(savedUser.getPhoneNumber())
                    .username(savedUser.getUsername())
                    .build();
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RegisterResponse.builder()
                    .error("Registration Failed").build());
        }

    }

//    @PostMapping("/login")
//    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody User user) {
//      //  return userService.registerUser(user);
//
//    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/verify/{verifier}")
    public ResponseEntity<VerificationResponse> getVerified(@PathVariable String verifier) {

        try {
            return ResponseEntity.ok().body(VerificationResponse
                    .builder().verificationLink(userService.verify(verifier)).build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(VerificationResponse.builder()
                    .error("Verification Failed").build());
        }
    }

    // Add more endpoints for updating user profile, deactivating/deleting account, etc.
}

