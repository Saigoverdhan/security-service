package com.security.microservice.repository;

import com.security.microservice.entity.otp;
import com.security.microservice.enums.otppurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface otprepository extends JpaRepository<otp, Long> {

    Optional<otp> findTopByEmailAndPurposeOrderByCreatedatDesc(
            String email,
            otppurpose purpose
    );

}