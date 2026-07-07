package com.example.crm.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

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

    @Test
    void アカウントロックが解除される() {
        // 1. テスト対象のemailの準備(アカウントロック状態にする)
        String email = "lock@example.com";
        User user = newUser(email);

        user.setLocked(true);
        user.setLockTime(Timestamp.valueOf(LocalDateTime.now().minusHours(2)));
        userRepository.save(user);  // ★ ロック状態をDBに反映

        // 2. アカウントロック解除メソッドの実行
        userService.unlockUser(email);

        // 3. アサーション(DBから再取得して検証)
        User updated = userRepository.findByEmail(email).orElseThrow();
        assertFalse(updated.isLocked(), "アカウントロックが解除されていること");
        assertNull(updated.getLockTime(), "ロック時間がリセットされていること");
    }
}