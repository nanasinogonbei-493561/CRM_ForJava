package com.example.Repository;

import com.example.Entity.User;
import com.example.Enum.Role;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


// ユーザーテーブルのやりとりを操作。
public interface UserRepository extends JpaRepository<User, Long> {
        List<User> findByRole(Role role);
        Optional<User> findByEmail(String email);
}
    
