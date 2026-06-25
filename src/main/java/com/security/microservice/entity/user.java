package com.security.microservice.entity;

import com.security.microservice.enums.authtype;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "user",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstname;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private authtype provider;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailverified;

    @Column(name = "last_login")
    private LocalDateTime lastlogin;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdat;

    @Column(nullable = false)
    private LocalDateTime updatedat;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<userrole> userroles = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        this.createdat = LocalDateTime.now();
        this.updatedat = LocalDateTime.now();

        if (enabled == null)
            enabled = false;

        if (emailverified == null)
            emailverified = false;

        if (deleted == null)
            deleted = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedat = LocalDateTime.now();
    }

}