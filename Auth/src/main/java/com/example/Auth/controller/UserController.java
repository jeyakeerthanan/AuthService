package com.example.Auth.controller;

import com.example.Auth.model.Status;

import com.example.Auth.model.dto.LoginRequest;
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

import java.util.UUID;

import static com.example.Auth.util.Constants.REGISTRATION_FAILED;
import static com.example.Auth.util.Constants.REGISTRATION_SUCCESS;

@RestController
@RequestMapping("/api/v1/")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("auth/register")
    public ResponseEntity<ResponseDto> registerUser(@Valid @RequestBody User user) {
        try {
            var savedUser = userService.registerUser(user);
            var response = RegisterResponse.builder()
                    .email(savedUser.getEmail())
                    .phoneNumber(savedUser.getPhoneNumber())
                    .username(savedUser.getUsername())
                    .build();
            return ResponseEntity.ok().body(ResponseDto.builder()
                    .status(Status.Success).data(response)
                    .message(REGISTRATION_SUCCESS).build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDto.builder()
                    .status(Status.Failed).error(e.getMessage())
                    .message(REGISTRATION_FAILED).build());
        }
    }

    @GetMapping("get-user/{userId}")
    public ResponseEntity<ResponseDto> getUserById(@PathVariable UUID userId) {
        var response = userService.getUserById(userId);
        if (response.isPresent()) {
            return ResponseEntity.ok().body(ResponseDto.builder()
                    .status(Status.Success).message("User details get Successfully").data(response).build());
        } else {
            return ResponseEntity.ok().body(ResponseDto.builder()
                    .status(Status.Failed).message("User not Found").build());
        }
    }

    @PostMapping("auth/verify/{verifier}")
    public ResponseEntity<ResponseDto> getVerifiedToken(@PathVariable String verifier) {

        try {
            userService.getVerifiedToken(verifier);
            return ResponseEntity.ok().body(ResponseDto.builder()
                    .status(Status.Success).message("Verified Token has been sent").build());
        } catch (Exception e) {
            return ResponseEntity.ok().body(ResponseDto.builder().
                    error(e.getMessage()).status(Status.Failed).build());
        }
    }

    @PostMapping("auth/verifying/{verifier}")
    public ResponseEntity<ResponseDto> getVerified(@RequestBody VerificationRequest verificationRequest) {

        try {
            var response = userService.verify(verificationRequest);
            return ResponseEntity.ok().body(ResponseDto.builder().data(response)
                    .message("User verified").status(Status.Success).build());
        } catch (Exception e) {
            return ResponseEntity.ok().body(ResponseDto.builder()
                    .message("Verification Confirmation failed").status(Status.Failed).build());

        }
    }

    @PostMapping("auth/login")
    public ResponseEntity<ResponseDto> getLogin(@RequestBody LoginRequest loginRequest) {

        try {
            var response = userService.login(loginRequest);
            return ResponseEntity.ok().body(ResponseDto.builder().data(response)
                    .message("Login Successfully").status(Status.Success).build());
        } catch (Exception e) {
            return ResponseEntity.ok().body(ResponseDto.builder()
                    .message("Login failed").status(Status.Failed).build());

        }
    }

}

