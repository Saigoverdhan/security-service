package com.security.microservice.service;

import com.security.microservice.dto.request.*;
import com.security.microservice.dto.response.*;
import com.security.microservice.entity.Otp;
import com.security.microservice.entity.RefreshToken;
import com.security.microservice.entity.User;
import com.security.microservice.enums.AuthProvider;
import com.security.microservice.exception.InvalidOtpException;
import com.security.microservice.exception.RefreshTokenException;
import com.security.microservice.exception.ResourceNotFoundException;
import com.security.microservice.exception.UserAlreadyExistsException;
import com.security.microservice.repository.OtpRepository;
import com.security.microservice.repository.RefreshTokenRepository;
import com.security.microservice.repository.UserRepository;
import com.security.microservice.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // ==============================
    // Repositories
    // ==============================

    private final UserRepository userRepository;

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

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .provider(AuthProvider.LOCAL)
                .enabled(false)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        String otp = otpService.generateOtp();

        otpService.saveOtp(savedUser, otp);

        emailService.sendOtp(savedUser.getEmail(), otp);

        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();

    }

    // ==============================
    // Login
    // ==============================

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

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
    public ApiResponse verifyOtp(VerifyOtpRequest request) {

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

        user.setEnabled(true);
        user.setEmailVerified(true);

        userRepository.save(user);

        otpRepository.delete(otp);

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

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        String otp = otpService.generateOtp();

        otpService.saveOtp(user, otp);

        emailService.sendOtp(user.getEmail(), otp);

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

        RefreshToken refreshToken = refreshTokenService
                .verifyRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        String accessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message("Access Token Generated Successfully")
                .build();

    }

}