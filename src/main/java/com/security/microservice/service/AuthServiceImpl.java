package com.security.microservice.service;

import com.security.microservice.dto.request.*;
import com.security.microservice.dto.response.*;
import com.security.microservice.entity.Otp;
import com.security.microservice.entity.PendingUser;
import com.security.microservice.entity.RefreshToken;
import com.security.microservice.entity.User;
import com.security.microservice.enums.AuthProvider;
import com.security.microservice.enums.Role;
import com.security.microservice.exception.InvalidOtpException;
import com.security.microservice.exception.RefreshTokenException;
import com.security.microservice.exception.ResourceNotFoundException;
import com.security.microservice.exception.UserAlreadyExistsException;
import com.security.microservice.repository.OtpRepository;
import com.security.microservice.repository.PendingUserRepository;
import com.security.microservice.repository.RefreshTokenRepository;
import com.security.microservice.repository.UserRepository;
import com.security.microservice.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // ==============================
    // Repositories
    // ==============================

    private final UserRepository userRepository;

    private final PendingUserRepository pendingUserRepository;

    private final OtpRepository otpRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    // ==============================
    // Security
    // ==============================

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    // ==============================
    // Services
    // ==============================

    private final EmailService emailService;

    private final OtpService otpService;

    private final RefreshTokenService refreshTokenService;

    // ==============================
    // Register
    // ==============================

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        //logging
        log.info("Registration request received for email: {}", request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            //for logging
            log.warn("Username already exists: {}", request.getUsername());

            throw new UserAlreadyExistsException("Username already exists.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            //logging
            log.warn("Email already exists: {}", request.getEmail());

            throw new UserAlreadyExistsException("Email already exists.");
        }

        String otp = otpService.generateOtp();
        Role role = request.getRole() != null ? request.getRole() : Role.LEARNER;

        Optional<PendingUser> existingPending = pendingUserRepository.findByEmail(request.getEmail());
        PendingUser pendingUser;

        if (existingPending.isPresent()) {
            pendingUser = existingPending.get();
            pendingUser.setUsername(request.getUsername());
            pendingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            pendingUser.setRole(role);
            pendingUser.setProvider(AuthProvider.LOCAL);
            pendingUser.setOtp(otp);
            pendingUser.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
            pendingUser.setCreatedAt(LocalDateTime.now());
        } else {
            pendingUser = PendingUser.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(role)
                    .provider(AuthProvider.LOCAL)
                    .otp(otp)
                    .otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        PendingUser savedPendingUser = pendingUserRepository.save(pendingUser);

        //logging
        log.info("Pending registration saved for email: {}", savedPendingUser.getEmail());
        log.info("OTP generated for {}", savedPendingUser.getEmail());

        emailService.sendOtp(savedPendingUser.getEmail(), otp);

        return UserResponse.builder()
                .id(savedPendingUser.getId())
                .username(savedPendingUser.getUsername())
                .email(savedPendingUser.getEmail())
                .role(savedPendingUser.getRole())
                .build();

    }

    // ==============================
    // Login
    // ==============================

    @Override
    public AuthResponse login(LoginRequest request) {

        //logging
        log.info("Login request received for username: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            //logging
            log.warn("Password login attempted for Google account: {}", user.getEmail());
            throw new RuntimeException("Please login using Google.");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            //logging
            log.warn("Email not verified: {}", user.getEmail());

            throw new RuntimeException("Please verify your email before logging in.");
        }

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        //logging
        log.info("Login successful for {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message("Login Successful")
                .build();

    }


    // ==============================
    // Verify OTP
    // ==============================

    @Override
    @Transactional
    public ApiResponse verifyOtp(VerifyOtpRequest request) {

        //logging
        log.info("OTP verification started for {}", request.getEmail());

        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pending registration not found for email: " + request.getEmail()));

        if (!pendingUser.getOtp().equals(request.getOtp())) {
            //logging
            log.warn("Invalid OTP entered for {}", request.getEmail());

            throw new InvalidOtpException("Invalid OTP");
        }

        if (pendingUser.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            //logging
            log.warn("OTP expired for {}", request.getEmail());

            throw new InvalidOtpException("OTP Expired");
        }

        // Re-check that the email does not already exist in UserRepository
        if (userRepository.existsByEmail(pendingUser.getEmail())) {
            log.warn("Email already exists in main table: {}", pendingUser.getEmail());
            throw new UserAlreadyExistsException("Email already exists.");
        }

        User user = User.builder()
                .username(pendingUser.getUsername())
                .email(pendingUser.getEmail())
                .password(pendingUser.getPassword())
                .role(pendingUser.getRole())
                .provider(pendingUser.getProvider() != null ? pendingUser.getProvider() : AuthProvider.LOCAL)
                .enabled(true)
                .emailVerified(true)
                .build();

        userRepository.save(user);

        pendingUserRepository.delete(pendingUser);

        //logging
        log.info("OTP verified successfully and user created for {}", request.getEmail());

        return ApiResponse.builder()
                .success(true)
                .message("OTP Verified Successfully")
                .build();

    }

    // ==============================
    // Forgot Password
    // ==============================

    @Override
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {

        //logging
        log.info("Forgot password requested for {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        String otp = otpService.generateOtp();

        otpService.saveOtp(user, otp);

        emailService.sendOtp(user.getEmail(), otp);

        //logging
        log.info("Reset OTP sent to {}", request.getEmail());

        return ApiResponse.builder()
                .success(true)
                .message("OTP Sent Successfully")
                .build();

    }

    // ==============================
    // Reset Password
    // ==============================

    @Override
    public ApiResponse resetPassword(ResetPasswordRequest request) {

        //logging
        log.info("Password reset started for {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Otp otp = otpRepository.findByUser(user)
                .orElseThrow(() ->
                        new InvalidOtpException("OTP not found"));

        if (!otp.getOtp().equals(request.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP Expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        otpRepository.delete(otp);

        //logging
        log.info("Password reset successful for {}", request.getEmail());

        return ApiResponse.builder()
                .success(true)
                .message("Password Reset Successfully")
                .build();

    }

    // ==============================
    // Refresh Token
    // ==============================

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        //logging
        log.info("Refresh token request received.");

        RefreshToken refreshToken = refreshTokenService
                .verifyRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        String accessToken = jwtService.generateAccessToken(user);

        //logging
        log.info("New access token generated.");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message("Access Token Generated Successfully")
                .build();

    }

}