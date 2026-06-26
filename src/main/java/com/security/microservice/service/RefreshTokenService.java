package com.security.microservice.service;

import com.security.microservice.entity.RefreshToken;
import com.security.microservice.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

}