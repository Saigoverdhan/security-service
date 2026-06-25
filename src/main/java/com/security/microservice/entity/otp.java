package com.security.microservice.entity;

import com.security.microservice.enums.otppurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "otp",
        indexes = {
                @Index(name = "idx_otp_email", columnList = "email"),
                @Index(name = "idx_otp_expiry", columnList = "expiry_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String otphash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private otppurpose purpose;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expirytime;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private boolean expired;

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