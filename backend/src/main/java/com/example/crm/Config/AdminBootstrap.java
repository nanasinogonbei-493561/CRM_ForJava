package com.example.crm.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
        // TODO(human): 初期管理者を作る条件と手順をここに書く
        
    }
}
