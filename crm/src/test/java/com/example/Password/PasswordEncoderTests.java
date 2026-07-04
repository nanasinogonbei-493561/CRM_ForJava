package com.example.Password;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.example.Repository.UserRepository;
import com.example.Service.UserService;

@ExtendWith(PasswordEncoderTests.class)
class PasswordEncoderTests {
    @InjectMocks
    UserService Service;

    @Mock
    UserRepository repository;

    @Test
    void testPasswordEncoder() {
        String result = encoder.encode("");
        assertTrue(encoder.matches("", result));
    }
}
