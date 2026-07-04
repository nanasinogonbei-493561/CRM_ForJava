package com.example.crm.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.crm.Entity.User;
import com.example.crm.Enum.Role;
import com.example.crm.Repository.UserRepository;
import com.example.crm.Service.UserService;

@ExtendWith(MockitoExtension.class)   // ← 自分自身ではなくMockitoの拡張を指定
class UserServiceTests {

    @Mock
    PasswordEncoder passwordEncoder;  // ← これが無いと@InjectMocksがnullを注入してしまう

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;          // 変数名は小文字始まりが慣例です

    @Test
    void registerは平文ではなくハッシュを保存する() {
        when(passwordEncoder.encode("rawPass123")).thenReturn("HASHED");

        userService.register("テスト用ユーザー山田太郎", "taro@example.com",
                             Role.SALES, "rawPass123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASHED"); // 平文が保存されていないこと
    }
}