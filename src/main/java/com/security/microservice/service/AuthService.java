package com.security.microservice.service;

import com.security.microservice.dto.request.*;
import com.security.microservice.dto.response.*;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    ApiResponse verifyOtp(VerifyOtpRequest request);

    ApiResponse forgotPassword(ForgotPasswordRequest request);

    ApiResponse resetPassword(ResetPasswordRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

}
