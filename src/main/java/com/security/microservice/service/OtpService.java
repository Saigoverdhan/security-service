package com.security.microservice.service;

import com.security.microservice.entity.User;

public interface OtpService {

    String generateOtp();

    void saveOtp(User user, String otp);

    boolean verifyOtp(User user, String otp);

}