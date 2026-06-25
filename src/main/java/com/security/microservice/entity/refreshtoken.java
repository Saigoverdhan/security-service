package com.security.microservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_token", columnList = "token"),
                @Index(name = "idx_refresh_expiry", columnList = "expiry_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class refreshtoken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private user user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expirydate;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdat;

    @Column(nullable = false)
    private LocalDateTime updatedat;

    @PrePersist
    public void prePersist() {
        this.createdat = LocalDateTime.now();
        this.updatedat = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedat = LocalDateTime.now();
    }
}