package com.example.crm.Config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.example.crm.Repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// 将来infrastructure/securityに分類
// ログイン失敗時の例外処理実装。
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String email = request.getParameter("username");
        userRepository.findByEmail(email).ifPresent(user -> {
            user.recordLoginFailure();   // ルールはEntityが知っている
            userRepository.save(user);
        });
        response.sendRedirect("/login?error");
    }
}