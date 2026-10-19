package com.example.crm.Exception;

public class UserEntityNotFoundException extends RuntimeException {
    public UserEntityNotFoundException(Long id) {
        super("Could not find User " + id);
    }
}
