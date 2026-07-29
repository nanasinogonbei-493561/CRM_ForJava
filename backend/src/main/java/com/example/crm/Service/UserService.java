package com.example.crm.Service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.crm.Entity.*;
import com.example.crm.Enum.Role;
import com.example.crm.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// 将来application層に移動。
// ユーザーテーブルのコト(処理、ロジック)を書く。
// ここで実際にencode()して保存する
@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void register(String name, String email, Role role, String rawPassword) {
        String passwordHash = passwordEncoder.encode(rawPassword);  // 先に作る
        UserEntity user = new UserEntity(name, email, role, passwordHash);  // 全部渡して一発で完成。
        userRepository.save(user);
    }

    // UserService.java（アプリケーション層）
    @PreAuthorize("hasRole('ADMIN')")   // Spring管理BeanであるServiceでのみ有効
    @Transactional
    public void unlockUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: id=" + userId));
        user.unlock();   // ドメインロジックはEntityに委譲
        // @Transactional内で取得した管理状態のEntityは、コミット時に
        // dirty checkingで自動的にUPDATEされるため save() の明示は不要
    }
}
