package com.example.Auth.controller;

import com.example.Auth.model.dto.AuthenticationResponse;
import com.example.Auth.model.dto.RegisterResponse;
import com.example.Auth.model.User;
import com.example.Auth.model.dto.ResponseDto;
import com.example.Auth.model.dto.VerificationRequest;
import com.example.Auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("auth/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody User user) {
        try {
            var savedUser = userService.registerUser(user);
            var response = RegisterResponse.builder()
                    .email(savedUser.getEmail())
                    .phoneNumber(savedUser.getPhoneNumber())
                    .username(savedUser.getUsername())
                    .build();
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RegisterResponse.builder()
                    .error(e.getMessage()).build());
        }

    }

    @GetMapping("get-user/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping("auth/verify/{verifier}")
    public ResponseEntity<ResponseDto> getVerifiedToken(@PathVariable String verifier) {

        try {
            userService.getVerifiedToken(verifier);
            return ResponseEntity.ok().body(ResponseDto.builder().status("success").statusCode(200).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseDto.builder().error(e.getMessage()).statusCode(400).build());
        }
    }

    @PostMapping("auth/verifying/{verifier}")
    public ResponseEntity<AuthenticationResponse> getVerified(@RequestBody VerificationRequest verificationRequest) {

        try {
            var response = userService.verify(verificationRequest);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            throw new RuntimeException("Verification Confirmation failed: " + e.getMessage());
        }
    }

}

