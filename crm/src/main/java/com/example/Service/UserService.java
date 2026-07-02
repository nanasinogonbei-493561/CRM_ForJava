package com.example.Service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Repository.UserRepository;

// ユーザーテーブルのコト(処理、ロジック)を書く。
// ここで実際にencode()して保存する
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void register(String name, String rawPassword) {
        Users users = new Users();
        users.setName(name);
        users.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(users);
    }
}
