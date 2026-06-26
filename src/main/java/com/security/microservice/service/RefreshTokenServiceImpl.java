package com.security.microservice.service;

import com.security.microservice.entity.RefreshToken;
import com.security.microservice.entity.User;
import com.security.microservice.repository.RefreshTokenRepository;
import com.security.microservice.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Override
    public RefreshToken createRefreshToken(User user) {

        String token = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryTime(LocalDateTime.now().plusDays(7))
                .build();

        return refreshTokenRepository.save(refreshToken);

    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Refresh Token"));

        if (refreshToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh Token Expired");
        }

        return refreshToken;

    }

}