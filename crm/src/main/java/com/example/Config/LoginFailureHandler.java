package com.example.Config;

import org.springframework.context.annotation.Bean;

import com.example.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Bean
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) {
        String email = request.getParameter("username");
        userRepository.findByEmail(email).ifPresent(users -> {
            int count = users.getFailedLoginCount() + 1;  // 失敗回数を+1
            users.setFailedLoginCount(count);
            if (count >= 3) {
                users.setLocked(true);   // ←3回でロック！
            }
            userRepository.save(users);  // DBに保存
        });
        response.sendRedirect("/login?error");
    }
}
