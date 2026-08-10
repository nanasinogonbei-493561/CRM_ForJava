package com.example.crm.Api;

import org.springframework.web.bind.annotation.RestController;

import com.example.crm.Repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class UserApi {
    private final UserRepository repository;

    UserApi(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/users")
    
    
}
