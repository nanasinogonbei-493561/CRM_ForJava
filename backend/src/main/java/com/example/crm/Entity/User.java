package com.example.crm.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import com.example.crm.Enum.Role;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.validation.constraints.Size;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

// 将来domain層に移動。
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
// ユーザーテーブルのモノ(データ、状態)を定義。
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
        
    @Size(min=10, max=50)
    @Column(nullable = false)
    private String name;

    @Size(max=255)
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
        
    @Column(nullable = false)
    private boolean is_deleted = Boolean.FALSE;
        
    @Column(nullable = false)
    private String passwordHash;
        
    @Column(nullable = false)
    private boolean locked = Boolean.FALSE;

    @Column(nullable = false)
    private int failed_login_count;
        
    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(nullable = false, name = "created_at", updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private LocalDateTime created_at;

    protected User() {} // JPA専用。業務コードからは使わない。

    public User(String name, String email, Role role, String passwordHash) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return String.format(
            "User[id=%d, name='%s', email='%s', role='%s']",
        id, name, email, role);
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isDeleted() {
        return this.is_deleted;
    }

    public void setDeleted(boolean deleted) {
        this.is_deleted = deleted;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getFailedLoginCount() {
        return this.failed_login_count;
    }

    public void setFailedLoginCount(int failedLoginCount) {
        this.failed_login_count = failedLoginCount;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    public LocalDateTime getCreatedAt() {
        return this.created_at;
    }
        
    @Version private Long version;
    /** ログイン失敗がこの回数に達したらアカウントをロックする */
    private static final int MAX_FAILED_ATTEMPTS = 3;

    // ...既存のフィールド...
    public void recordLoginFailure() {
        this.failed_login_count++;
        if (this.failed_login_count >= MAX_FAILED_ATTEMPTS) {
            this.locked = true;
            this.lockTime = LocalDateTime.now();  // ロック時刻も状態として記録
        }
    }

    // User.java（ドメイン層）── 自分の状態「だけ」を変更する
    public void unlock() {
        this.locked = false;           // ← 元コードはtrueで「解除できない」バグ
        this.failed_login_count = 0;
        this.lockTime = null;
    }

        
}
