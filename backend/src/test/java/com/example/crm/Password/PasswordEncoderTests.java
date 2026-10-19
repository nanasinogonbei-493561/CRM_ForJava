package com.example.crm.Password;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderTests {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void エンコードした値は元のパスワードと一致する() {
        String result = encoder.encode("password123");

        assertTrue(encoder.matches("password123", result));
        assertFalse(encoder.matches("wrongpass", result));
    }

    @Test
    void 同じパスワードでもハッシュ値は毎回変わる() {
        String hash1 = encoder.encode("password123");
        String hash2 = encoder.encode("password123");

        assertFalse(hash1.equals(hash2));               // ソルトが毎回異なるため
        assertTrue(encoder.matches("password123", hash1)); // それでも照合はどちらも成功する
    }
}