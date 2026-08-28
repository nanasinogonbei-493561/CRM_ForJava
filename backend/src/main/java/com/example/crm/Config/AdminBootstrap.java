package com.example.crm.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.crm.Entity.UserEntity;
import com.example.crm.Enum.Role;
import com.example.crm.Repository.UserRepository;
import com.example.crm.Service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

// 将来infrastructure層に分類。
// 初期管理者の作成。POST /users を ADMIN 限定にしたため、
// 最初の1人だけはAPI以外の経路で用意する必要がある（鶏と卵問題）。
// ApplicationRunner は起動完了直後に一度だけ run() が呼ばれる。
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final UserService userService;

    // 未設定なら空文字。パスワードは絶対にログへ出さないこと。
    @Value("${crm.bootstrap.admin-username:}")
    private String adminUsername;

    @Value("${crm.bootstrap.admin-email:}")
    private String adminEmail;

    @Value("${crm.bootstrap.admin-password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        // ① 設定が未指定の環境では何もしない。
        // うっかり既定パスワードの管理者が生まれるのを防ぐ安全弁。
        if (adminUsername.isBlank() || adminEmail.isBlank() || adminPassword.isBlank()) {
            logger.info("初期管理者の設定が未指定のためスキップします。");
            return;
        }

        // ② ADMIN が1人もいないときだけ作る。
        // ApplicationRunner は起動のたびに走るため、このガードが無いと
        // 2回目の起動で email の unique 制約に衝突する。
        if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
            // ③ 生パスワードのハッシュ化は UserService.register が担当する。
            // ここで userRepository.save() を直接呼ぶと平文が保存されてしまう。
            UserEntity admin = userService.register(
                    adminUsername, adminEmail, Role.ADMIN, adminPassword);
            logger.info("初期管理者を作成しました。id={}, email={}", admin.getId(), admin.getEmail());
        } else {
            logger.info("ADMIN が既に存在するため初期管理者の作成をスキップします。");
        }
    }
}
