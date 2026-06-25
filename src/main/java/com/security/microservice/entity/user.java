package com.security.microservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 20)
    private String mobile;

    @Column(nullable = false)
    private Boolean enabled = false;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(nullable = false)
    private Boolean mobileVerified = false;

    @Column(nullable = false)
    private Boolean accountLocked = false;

    @Column(nullable = false)
    private Boolean accountExpired = false;

    @Column(nullable = false)
    private Boolean credentialsExpired = false;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(length = 30)
    private String provider;

    @Column(length = 150)
    private String providerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}