package com.security.microservice.dto.request;

import com.security.microservice.enums.Role;
import lombok.*;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class RegisterRequest {

        private String username;
        private String email;
        private String password;
        private Role role;

    }

