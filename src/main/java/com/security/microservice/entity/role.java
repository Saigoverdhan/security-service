package com.security.microservice.entity;

import com.security.microservice.enums.rolename;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "role",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private rolename name;

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