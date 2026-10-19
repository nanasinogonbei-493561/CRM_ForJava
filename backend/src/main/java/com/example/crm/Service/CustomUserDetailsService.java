package com.example.crm.Service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.crm.Entity.UserEntity;
import com.example.crm.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

// 将来infrastructure/securityに分類。Spring Security使用。
// ユーザー見つからなかった時の例外処理を実装。
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        // emailでDBからユーザーを探す
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));

        // Springが理解できる UserDetails 型に変換して返す
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPasswordHash())   // ←DBのハッシュをそのまま渡す
            .roles(user.getRole().name())              // 営業/サポート/管理者
            .accountLocked(user.isLocked())     // ←locked列がここで効く！
            .build();
    }
}
