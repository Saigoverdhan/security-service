package com.security.microservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class sample_test {

    @GetMapping("test")
    public String sai(){
        return "app is working";
    }
}
