package com.security.microservice.service;

import com.security.microservice.entity.Otp;
import com.security.microservice.entity.User;
import com.security.microservice.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    @Override
    public String generateOtp() {
        //logging
        log.info("OTP generated.");

        return String.valueOf(100000 + new Random().nextInt(900000));

    }

    @Override
    public void saveOtp(User user, String otp) {

        //logging
        log.info("OTP saved for {}", user.getEmail());

        otpRepository.deleteByUser(user);

        Otp newOtp = Otp.builder()
                .user(user)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        otpRepository.save(newOtp);

    }

    @Override
    public boolean verifyOtp(User user, String otp) {

        Otp savedOtp = otpRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("OTP Not Found"));

        //logging
        log.info("OTP deleted.");

        return savedOtp.getOtp().equals(otp)
                && savedOtp.getExpiryTime().isAfter(LocalDateTime.now());

    }

}