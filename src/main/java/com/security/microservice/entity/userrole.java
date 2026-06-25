package com.security.microservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_role",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "role_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class userrole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private user user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdat;

    @PrePersist
    public void prePersist() {
        this.createdat = LocalDateTime.now();
    }

}