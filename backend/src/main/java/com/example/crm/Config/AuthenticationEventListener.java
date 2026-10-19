package com.example.crm.Config;

import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import com.example.crm.Service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

// 将来infrastructure/securityに分類
// 認証イベントを受け取り、ログ記録と連続ログイン失敗カウントの更新を行う。
// ProviderManager が発行するイベントなので、HTTP Basic / フォームログイン / JWT の
// どの認証方式でも発火する。AuthenticationFailureHandler より適用範囲が広い。
@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventListener.class);

    private final UserService userService;

    // AbstractAuthenticationFailureEvent ではなく BadCredentials に限定する。
    // 前者はロック済みアカウント再試行時の LockedException も拾うため、
    // ロック後もカウントが増え続けてしまう。
    @EventListener
    public void handleFailure(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getName();
        logger.warn("認証失敗イベント発生：{}", email);
        userService.recordLoginFailure(email);
    }

    @EventListener
    public void handleSuccess(AuthenticationSuccessEvent event) {
        String email = event.getAuthentication().getName();
        logger.info("認証成功：{}", email);
        userService.resetLoginFailures(email);
    }
}
