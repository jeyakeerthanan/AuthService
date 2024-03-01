package com.example.Auth.service;

import com.example.Auth.config.JwtService;
import com.example.Auth.model.*;
import com.example.Auth.model.dto.AuthenticationResponse;
import com.example.Auth.model.dto.LoginRequest;
import com.example.Auth.model.dto.VerificationRequest;
import com.example.Auth.repository.LoginAttemptRepository;
import com.example.Auth.repository.TokenRepository;
import com.example.Auth.repository.UserRepository;
import com.example.Auth.repository.VerificationRepository;
import com.example.Auth.util.Validator;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private VerificationRepository verificationRepository;


    @Autowired
    private VerificationService verificationService;

    @Value("${verification.expiration.time}")
    private long expirationTime;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    public User registerUser(User user) {
        Set<Role> roleSet= new HashSet<>();
        roleSet.add(Role.builder().roleName("User").build());
        validateUser(user);
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        user.setPhoneVerified(false);
        user.setEmailVerified(false);
        user.setRoles(roleSet);
        return userRepository.save(user);
    }

    private void validateUser(User user) {
        if (StringUtils.isEmpty(user.getEmail()) || StringUtils.isEmpty(user.getPhoneNumber())
                || StringUtils.isEmpty(user.getPassword()) || StringUtils.isEmpty(user.getUsername())) {
            throw new RuntimeException("Invalid User Details");
        }
        if (userRepository.existsByUserName(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }
    }

    public Optional<User> getUserById(UUID userId) {
        return  userRepository.findByUserUniversal(userId);
    }

    public void getVerifiedToken(String verifier) {
        if (Validator.isValidEmail(verifier)) {
            var user = userRepository.findByEmail(verifier);
            if (user.isPresent() && !user.get().getRoles().contains("ADMIN")) {
                sendVerificationMail(user.get());
            } else {
                throw new RuntimeException("email Invalid ");
            }
        } else if (Validator.isValidPhoneNumber(verifier)) {
            var user = userRepository.findByPhoneNumber(verifier);
            if (user.isPresent() && !user.get().getRoles().contains("ADMIN")) {
                sendVerificationSMS(user.get());
            } else {
                throw new RuntimeException("phone number Invalid ");
            }
        } else {
            throw new RuntimeException("Verifier Invalid");
        }
    }

    private void sendVerificationSMS(User user) {
        var otp = generateOTP(6);
        saveTheVerification(otp, user);
        verificationService.sendSMS(user.getPhoneNumber(), otp);
    }

    private void sendVerificationMail(User user) {
        var verification = hashWithSaltAndUUID(user.getEmail());
        saveTheVerification(verification, user);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verification");
        message.setText(verification);
        javaMailSender.send(message);
    }

    private void saveTheVerification(String verification, User user) {
        verificationRepository.save(Verification.builder()
                .token(verification)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .user(user)
                .build());
    }

    public static String hashWithSaltAndUUID(String valueToHash) {
        UUID uuid = UUID.randomUUID();
        String salt = uuid.toString();
        String valueWithSalt = salt + valueToHash;
        String hashedValue = hashSHA256(valueWithSalt);
        return hashedValue;
    }


    private static String hashSHA256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateOTP(int length) {
        String numbers = "0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        return sb.toString();
    }

    public AuthenticationResponse verify(VerificationRequest verifier) {
        if (StringUtils.isEmpty(verifier.getVerifier()) || StringUtils.isEmpty(verifier.getVerificationCode())) {
            throw new RuntimeException("Verification data Invalid");
        }
        if (Validator.isValidEmail(verifier.getVerifier())) {
            var user = userRepository.findByEmail(verifier.getVerifier());
            if (user.isPresent()) {
                var verified = checkTheVerification(user.get().getId(), verifier.getVerificationCode());
                if (verified) {
                    return createToken(user.get(),verifier);
                } else {
                    throw new RuntimeException("verification failed");
                }
            } else {
                throw new RuntimeException("user not Exist for this Email:" + verifier.getVerifier());
            }
        } else if (Validator.isValidPhoneNumber(verifier.getVerifier())) {
            var user = userRepository.findByPhoneNumber(verifier.getVerifier());
            if (user.isPresent()) {
                sendVerificationSMS(user.get());
            } else {
                throw new RuntimeException("user not Exist for this Phone number:" + verifier.getVerifier());
            }
        } else {
            throw new RuntimeException("Verifier invalid Not a valid phone number or Email");
        }
        return null;
    }

    private AuthenticationResponse createToken(User user, VerificationRequest verifier) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, refreshToken, verifier);
        return buildAuthResponse(user, refreshToken, jwtToken);
    }

    private AuthenticationResponse buildAuthResponse(User user, String refreshToken, String jwtToken) {
        return AuthenticationResponse.builder()
                .user(user)
                .token(refreshToken)
                .accessToken(jwtToken)
                .build();
    }

    private void saveUserToken(User user, String refreshToken, VerificationRequest verifier) {
        user.setLastLogin(Timestamp.valueOf(LocalDateTime.now()));
        if (Validator.isValidPhoneNumber(verifier.getVerifier())) {
            user.setPhoneVerified(true);
        } else if (Validator.isValidEmail(verifier.getVerifier())) {
            user.setEmailVerified(true);
        }
        recordTokenAndAttempt(refreshToken, user);
        userRepository.save(user);
    }

    private void recordTokenAndAttempt(String refreshToken, User user) {
        var token = Token.builder()
                .token(refreshToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        var loginAttempt = LoginAttempt.builder()
                .success(true)
                .ipAddress(request.getRemoteAddr())
                .user(user)
                .timestamp(LocalDateTime.now()).build();

        loginAttemptRepository.save(loginAttempt);
        tokenRepository.save(token);
    }


    private boolean checkTheVerification(Long userId, String token) {
        var verificationData = verificationRepository.findByUserId(userId);
        if (verificationData.isPresent()) {
            if (checkTimeStamp(verificationData)) {
                if (verificationData.get().getToken().equals(token)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                throw new RuntimeException("verification time Expired");
            }
        } else {
            log.error("user not found in the verification DB: {}", userId);
            throw new RuntimeException("Verification Details not found");
        }
    }

    private boolean checkTimeStamp(Optional<Verification> verificationData) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime createdAtDateTime = verificationData.get().getCreatedAt().toLocalDateTime();
        long minutesDifference = ChronoUnit.MINUTES.between(createdAtDateTime, currentDateTime);
        if (minutesDifference <= expirationTime) {
            return true;
        } else {
            return false;
        }
    }


    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail());
               if (user.isPresent() && (user.get().isEmailVerified() || user.get().isPhoneVerified()) ) {
                   var jwtToken = jwtService.generateToken(user.get());
                   var refreshToken = jwtService.generateRefreshToken(user.get());
                   revokeAllUserTokens(user.get());
                   saveUserToken(user.get(), jwtToken);
                   saveAttempt(user.get());
                   return AuthenticationResponse.builder()
                           .accessToken(jwtToken)
                           .token(refreshToken)
                           .build();
               } else {
                   throw new RuntimeException("User not found or not Verified");
               }

    }

    private void saveAttempt(User user) {
        var loginAttempt = LoginAttempt.builder()
                .success(true)
                .ipAddress(request.getRemoteAddr())
                .user(user)
                .timestamp(LocalDateTime.now()).build();

        loginAttemptRepository.save(loginAttempt);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

}

