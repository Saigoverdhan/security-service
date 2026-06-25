package com.security.microservice.repository;

import com.security.microservice.entity.role;
import com.security.microservice.enums.rolename;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface rolerepository extends JpaRepository<role, Long> {

    Optional<role> findByName(rolename name);

}