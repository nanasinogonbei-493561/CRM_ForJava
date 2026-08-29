package com.example.crm.Service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.crm.Entity.*;
import com.example.crm.Enum.Role;
import com.example.crm.Exception.UserEntityNotFoundException;
import com.example.crm.Repository.UserRepository;

import java.util.List;
import java.util.Optional;

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

    public UserEntity register(String name, String email, Role role, String rawPassword) {
        String passwordHash = passwordEncoder.encode(rawPassword);  // 先に作る
        UserEntity user = new UserEntity(name, email, role, passwordHash);  // 全部渡して一発で完成。
        return userRepository.save(user);
    }

    /**
     * ログイン成功時に連続失敗カウントを 0 に戻す。
     * 「連続」3回を数えたいので、1度でも成功したらリセットする必要がある。
     */
    @Transactional
    public void resetLoginFailures(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getFailedLoginCount() > 0) {
                user.setFailedLoginCount(0);
                // @Transactional内の管理状態EntityなのでdirtyCheckingでUPDATEされる
            }
        });
    }

    /**
     * ログイン失敗時に連続失敗カウントを進め、規定回数に達したらロックする。
     * ルール本体は UserEntity.recordLoginFailure() が持っている。
     */
    @Transactional
    public void recordLoginFailure(String email) {
        // 3回失敗したらロックする。
        // メソッド参照(UserEntity::recordLoginFailure)ではなくラムダを使う。
        // 前者だとConsumerの引数がそのままレシーバ(this)になり、JDTのnull解析が
        // 「JDKのConsumerは@NonNull宣言を持たないのでnon-nullを保証できない」と警告するため。
        userRepository.findByEmail(email).ifPresent(user -> user.recordLoginFailure());
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

    /**
     * 指定ロールの利用者が既に存在するか。
     * 初期管理者を作ってよいかの判定に使う。判定を Service に置くことで、
     * 呼び出し側(Config)が Repository を直接持たずに済む。
     */
    public boolean existsByRole(Role role) {
        return userRepository.existsByRole(role);
    }

    /** 一覧取得。参照のみなので副作用は無い。 */
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    /** 1件取得。存在しない場合の扱いは呼び出し側に委ねるため Optional のまま返す。 */
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 削除。存在確認と削除をひとつのトランザクションにまとめることで、
     * 「存在する」と判定した直後に他のリクエストが消す競合を防ぐ。
     */
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserEntityNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    /**
     * 氏名とメールアドレスを更新する。
     * 書き換える対象そのものが必要なので、existsById ではなく findById で取り出す。
     */
    @Transactional
    public UserEntity update(Long id, String username, String email) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserEntityNotFoundException(id));
        user.setUsername(username);
        user.setEmail(email);
        // unlockUser() と同じく、管理状態の Entity は
        // dirty checking でコミット時に UPDATE されるため save() は不要
        return user;
    }
}
