package com.security.microservice.service;

public interface EmailService {

    void sendOtp(String email, String otp);

}