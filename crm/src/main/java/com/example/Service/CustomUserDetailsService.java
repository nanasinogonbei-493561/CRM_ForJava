package com.example.Service;

import org.springframework.stereotype.Service;

import com.example.Repository.UserRepository;
import com.example.Entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        // emailでDBからユーザーを探す
        User user = userRepository.findByEmail(email)
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
