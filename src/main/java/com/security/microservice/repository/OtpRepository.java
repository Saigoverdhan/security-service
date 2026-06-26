package com.security.microservice.repository;

import com.security.microservice.entity.Otp;
import com.security.microservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByOtp(String otp);

    Optional<Otp> findByUser(User user);

    void deleteByUser(User user);

}