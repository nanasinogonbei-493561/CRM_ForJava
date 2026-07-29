package com.example.crm.Repository;

import com.example.crm.Entity.UserEntity;
import com.example.crm.Enum.Role;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// 将来、datasource層に移動。
// ユーザーテーブルのやりとりを操作。
public interface UserRepository extends JpaRepository<UserEntity, Long> {
        List<UserEntity> findByRole(Role role);
        Optional<UserEntity> findByEmail(String email);
        Optional<UserEntity> findByUsername(String username);
}
    
