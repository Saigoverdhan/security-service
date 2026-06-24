package com.security.microservice.repository;

import com.security.microservice.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface userrepository extends JpaRepository<user, Long> {

    Optional<user> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);
}