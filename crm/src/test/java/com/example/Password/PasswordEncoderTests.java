package com.example.Password;

public class PasswordEncoderTests {
    String result = encoder.encode("");
    assertTrue(encoder.matches("", result));
}
