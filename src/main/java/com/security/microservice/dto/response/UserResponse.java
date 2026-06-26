package com.security.microservice.dto.response;



import lombok.*;

import javax.management.relation.Role;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;

}