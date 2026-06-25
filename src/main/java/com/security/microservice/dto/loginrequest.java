package com.security.microservice.dto;

import lombok.Data;

@Data
public class loginrequest {

    private String email;
    private String password;
}