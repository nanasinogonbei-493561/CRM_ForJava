package com.example.crm.Repository;

import com.example.crm.Entity.User;
import com.example.crm.Enum.Role;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// 将来、datasource層に移動。
// ユーザーテーブルのやりとりを操作。
public interface UserRepository extends JpaRepository<User, Long> {
        List<User> findByRole(Role role);
        Optional<User> findByEmail(String email);
}
    
