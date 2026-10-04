package com.security.microservice;

import com.security.microservice.dto.request.ForgotPasswordRequest;
import com.security.microservice.dto.request.LoginRequest;
import com.security.microservice.dto.request.RegisterRequest;
import com.security.microservice.dto.request.VerifyOtpRequest;
import com.security.microservice.dto.response.ApiResponse;
import com.security.microservice.dto.response.AuthResponse;
import com.security.microservice.dto.response.UserResponse;
import com.security.microservice.entity.PendingUser;
import com.security.microservice.entity.RefreshToken;
import com.security.microservice.entity.User;
import com.security.microservice.enums.AuthProvider;
import com.security.microservice.enums.Role;
import com.security.microservice.exception.InvalidOtpException;
import com.security.microservice.exception.ResourceNotFoundException;
import com.security.microservice.exception.UserAlreadyExistsException;
import com.security.microservice.repository.OtpRepository;
import com.security.microservice.repository.PendingUserRepository;
import com.security.microservice.repository.RefreshTokenRepository;
import com.security.microservice.repository.UserRepository;
import com.security.microservice.scheduler.PendingUserCleanupScheduler;
import com.security.microservice.security.jwt.JwtService;
import com.security.microservice.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthRegistrationFlowTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PendingUserRepository pendingUserRepository;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpService otpService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private PendingUserCleanupScheduler cleanupScheduler;

    @BeforeEach
    void setUp() {
        cleanupScheduler = new PendingUserCleanupScheduler(pendingUserRepository);
    }

    @Test
    @DisplayName("Test case 1: New email registration - PendingUser created, User NOT created, OTP sent")
    void testCase1_newEmailRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Password123")
                .role(Role.LEARNER)
                .build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(otpService.generateOtp()).thenReturn("123456");
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword123");
        when(pendingUserRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        PendingUser savedPending = PendingUser.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .role(Role.LEARNER)
                .provider(AuthProvider.LOCAL)
                .otp("123456")
                .otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .build();

        when(pendingUserRepository.save(any(PendingUser.class))).thenReturn(savedPending);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john_doe", response.getUsername());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.LEARNER, response.getRole());

        // Verify PendingUser was saved with correct properties
        ArgumentCaptor<PendingUser> pendingCaptor = ArgumentCaptor.forClass(PendingUser.class);
        verify(pendingUserRepository).save(pendingCaptor.capture());
        PendingUser captured = pendingCaptor.getValue();
        assertEquals("john_doe", captured.getUsername());
        assertEquals("john@example.com", captured.getEmail());
        assertEquals("encodedPassword123", captured.getPassword());
        assertEquals("123456", captured.getOtp());
        assertTrue(captured.getOtpExpiresAt().isAfter(LocalDateTime.now().plusMinutes(9)));
        assertTrue(captured.getOtpExpiresAt().isBefore(LocalDateTime.now().plusMinutes(11)));

        // Verify User was NOT created in main users table
        verify(userRepository, never()).save(any(User.class));

        // Verify OTP was sent to email
        verify(emailService).sendOtp("john@example.com", "123456");
    }

    @Test
    @DisplayName("Test case 2: Correct OTP - PendingUser found, OTP accepted, User created in users with enabled=true and emailVerified=true, PendingUser deleted")
    void testCase2_correctOtpVerification() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("john@example.com")
                .otp("123456")
                .build();

        PendingUser pendingUser = PendingUser.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .role(Role.LEARNER)
                .provider(AuthProvider.LOCAL)
                .otp("123456")
                .otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .build();

        when(pendingUserRepository.findByEmail("john@example.com")).thenReturn(Optional.of(pendingUser));
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        ApiResponse response = authService.verifyOtp(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("OTP Verified Successfully", response.getMessage());

        // Verify User created in main users table with enabled=true and emailVerified=true
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("john_doe", savedUser.getUsername());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("encodedPassword123", savedUser.getPassword());
        assertEquals(Role.LEARNER, savedUser.getRole());
        assertEquals(AuthProvider.LOCAL, savedUser.getProvider());
        assertTrue(savedUser.getEnabled());
        assertTrue(savedUser.getEmailVerified());

        // Verify PendingUser deleted
        verify(pendingUserRepository).delete(pendingUser);
    }

    @Test
    @DisplayName("Test case 3: Wrong OTP - throws InvalidOtpException, no User created, PendingUser remains")
    void testCase3_wrongOtp() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("john@example.com")
                .otp("000000")
                .build();

        PendingUser pendingUser = PendingUser.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .role(Role.LEARNER)
                .provider(AuthProvider.LOCAL)
                .otp("123456")
                .otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .build();

        when(pendingUserRepository.findByEmail("john@example.com")).thenReturn(Optional.of(pendingUser));

        InvalidOtpException ex = assertThrows(InvalidOtpException.class, () -> authService.verifyOtp(request));
        assertEquals("Invalid OTP", ex.getMessage());

        // Verify no user created and pending user not deleted
        verify(userRepository, never()).save(any(User.class));
        verify(pendingUserRepository, never()).delete(any(PendingUser.class));
    }

    @Test
    @DisplayName("Test case 4: Expired OTP - throws InvalidOtpException, no User created, PendingUser remains")
    void testCase4_expiredOtp() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("john@example.com")
                .otp("123456")
                .build();

        PendingUser pendingUser = PendingUser.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .role(Role.LEARNER)
                .provider(AuthProvider.LOCAL)
                .otp("123456")
                .otpExpiresAt(LocalDateTime.now().minusMinutes(1)) // Expired
                .createdAt(LocalDateTime.now().minusMinutes(15))
                .build();

        when(pendingUserRepository.findByEmail("john@example.com")).thenReturn(Optional.of(pendingUser));

        InvalidOtpException ex = assertThrows(InvalidOtpException.class, () -> authService.verifyOtp(request));
        assertEquals("OTP Expired", ex.getMessage());

        // Verify no user created and pending user not deleted
        verify(userRepository, never()).save(any(User.class));
        verify(pendingUserRepository, never()).delete(any(PendingUser.class));
    }

    @Test
    @DisplayName("Test case 5: Scheduled cleanup - deletes expired PendingUsers")
    void testCase5_scheduledCleanup() {
        when(pendingUserRepository.deleteExpiredPendingUsers(any(LocalDateTime.class))).thenReturn(3);

        cleanupScheduler.cleanupExpiredPendingUsers();

        verify(pendingUserRepository).deleteExpiredPendingUsers(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Test case 6: Existing user registers again - throws UserAlreadyExistsException")
    void testCase6_existingUserRegistersAgain() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existing_user")
                .email("existing@example.com")
                .password("Password123")
                .role(Role.LEARNER)
                .build();

        when(userRepository.existsByUsername("existing_user")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        assertEquals("Email already exists.", ex.getMessage());

        verify(pendingUserRepository, never()).save(any(PendingUser.class));
        verify(emailService, never()).sendOtp(anyString(), anyString());
    }

    @Test
    @DisplayName("Test case 7: Existing pending email registers again - updates existing pending registration, fresh OTP generated, no duplicate records")
    void testCase7_existingPendingEmailRegistersAgain() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_updated")
                .email("john@example.com")
                .password("NewPassword123")
                .role(Role.INSTRUCTOR)
                .build();

        when(userRepository.existsByUsername("john_updated")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(otpService.generateOtp()).thenReturn("654321");
        when(passwordEncoder.encode("NewPassword123")).thenReturn("newEncodedPassword");

        PendingUser existingPending = PendingUser.builder()
                .id(1L)
                .username("john_old")
                .email("john@example.com")
                .password("oldEncoded")
                .role(Role.LEARNER)
                .provider(AuthProvider.LOCAL)
                .otp("111111")
                .otpExpiresAt(LocalDateTime.now().minusMinutes(5))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        when(pendingUserRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingPending));
        when(pendingUserRepository.save(existingPending)).thenReturn(existingPending);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john_updated", response.getUsername());
        assertEquals(Role.INSTRUCTOR, response.getRole());

        // Verify the existing pending record was updated
        assertEquals("john_updated", existingPending.getUsername());
        assertEquals("newEncodedPassword", existingPending.getPassword());
        assertEquals(Role.INSTRUCTOR, existingPending.getRole());
        assertEquals("654321", existingPending.getOtp());
        assertTrue(existingPending.getOtpExpiresAt().isAfter(LocalDateTime.now().plusMinutes(9)));

        verify(pendingUserRepository).save(existingPending);
        verify(userRepository, never()).save(any(User.class));
        verify(emailService).sendOtp("john@example.com", "654321");
    }

    @Test
    @DisplayName("Test case 8: Forgot password - existing flow works with real User and existing Otp mechanism")
    void testCase8_forgotPasswordFlow() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("john@example.com")
                .build();

        User user = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.LEARNER)
                .enabled(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateOtp()).thenReturn("987654");

        ApiResponse response = authService.forgotPassword(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("OTP Sent Successfully", response.getMessage());

        // Verify existing OtpService.saveOtp(User, String) was called
        verify(otpService).saveOtp(user, "987654");
        verify(emailService).sendOtp("john@example.com", "987654");
    }

    @Test
    @DisplayName("Test case 9: Login after successful registration - newly verified user can login normally")
    void testCase9_loginAfterVerification() {
        LoginRequest request = LoginRequest.builder()
                .identifier("john_doe")
                .password("Password123")
                .build();

        User verifiedUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.LEARNER)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(verifiedUser));
        when(jwtService.generateAccessToken(verifiedUser)).thenReturn("access_token_xyz");
        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh_token_xyz")
                .user(verifiedUser)
                .expiryTime(LocalDateTime.now().plusDays(7))
                .build();
        when(refreshTokenService.createRefreshToken(verifiedUser)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token_xyz", response.getAccessToken());
        assertEquals("refresh_token_xyz", response.getRefreshToken());
        assertEquals("Login Successful", response.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Test case 10: Login before verification - impossible because there is no real User in users table yet")
    void testCase10_loginBeforeVerification() {
        LoginRequest request = LoginRequest.builder()
                .identifier("john_unverified")
                .password("Password123")
                .build();

        // User exists only in pending_users, NOT in users table!
        when(userRepository.findByUsername("john_unverified")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
        assertEquals("User not found", ex.getMessage());

        verify(jwtService, never()).generateAccessToken(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(any(User.class));
    }

}
