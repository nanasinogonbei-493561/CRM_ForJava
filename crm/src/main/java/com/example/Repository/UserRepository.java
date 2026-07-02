package com.example.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

// ユーザーテーブルのやりとりを操作。
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByStatus(Role role);
}
