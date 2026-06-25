package com.security.microservice.dto;

import lombok.Data;

@Data
public class registerrequest {

    private String name;
    private String email;
    private String mobile;
    private String password;
    private String role;
}