package com.example.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.validation.constraints.Size;

// ユーザーテーブルのモノ(データ、状態)を定義。
public class UserEntity {
    @Entity
    @Table(name = USERS)
    @SQLDelete(sql = "UPDATE users SET deleted = true WHERE id=?")
    @Where(clause = "deleted=false")
    public class users {
        @Id
        @GeneratedValue(strategy = GeneratedValue.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;
        
        @Size(min=10, max=50)
        @Column(nullable = false)
        private String name;

        @Size(min=20, max=255)
        @Column(unique = true)
        private String email;

        @Enumerated(EnumType.STRING)
        @Size(min=10, max=20)
        @Column(name = "role", nullable = false)
        private Role role;

        private boolean deleted = Boolean.FALSE;

        private String passwordHash;
    }
}
