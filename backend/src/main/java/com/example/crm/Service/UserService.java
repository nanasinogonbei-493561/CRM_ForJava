package com.example.crm.Service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.crm.Entity.*;
import com.example.crm.Enum.Role;
import com.example.crm.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

// ユーザーテーブルのコト(処理、ロジック)を書く。
// ここで実際にencode()して保存する
@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void register(String name, String email, Role role, String rawPassword) {
        String passwordHash = passwordEncoder.encode(rawPassword);  // 先に作る
        User user = new User(name, email, role, passwordHash);  // 全部渡して一発で完成。
        userRepository.save(user);
    }
}
