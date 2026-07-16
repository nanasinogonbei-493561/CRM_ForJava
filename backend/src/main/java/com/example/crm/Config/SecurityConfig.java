package com.example.crm.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// パスワードハッシュの設定。道具を1個だけ用意
@Configuration
@EnableMethodSecurity   // これが無いと@PreAuthorizeはServiceに置いても動かない
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // ←strength省略=10が標準
    }
}
