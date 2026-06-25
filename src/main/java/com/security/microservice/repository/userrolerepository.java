package com.security.microservice.repository;

import com.security.microservice.entity.user;
import com.security.microservice.entity.userrole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface userrolerepository extends JpaRepository<userrole, Long> {

    List<userrole> findByUser(user user);

}