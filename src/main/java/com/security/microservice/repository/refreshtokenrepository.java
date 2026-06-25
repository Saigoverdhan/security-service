package com.security.microservice.repository;

import com.security.microservice.entity.refreshtoken;
import com.security.microservice.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface refreshtokenrepository extends JpaRepository<refreshtoken, Long> {

    Optional<refreshtoken> findByToken(String token);

    List<refreshtoken> findByUser(user user);

}