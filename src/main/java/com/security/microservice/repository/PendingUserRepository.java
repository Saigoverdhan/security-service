package com.security.microservice.repository;

import com.security.microservice.entity.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PendingUserRepository extends JpaRepository<PendingUser, Long> {

    Optional<PendingUser> findByEmail(String email);

    Optional<PendingUser> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void deleteByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PendingUser p WHERE p.otpExpiresAt < :now")
    int deleteExpiredPendingUsers(@Param("now") LocalDateTime now);

}
