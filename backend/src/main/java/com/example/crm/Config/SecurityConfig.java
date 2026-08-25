package com.example.crm.Config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 将来infrastructure/securityに分類。Spring Security使用。
// パスワードハッシュの設定。道具を1個だけ用意
@Configuration
@EnableMethodSecurity   // これが無いと@PreAuthorizeはServiceに置いても動かない
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // ←strength省略=10が標準
    }

    // URL単位の認可設定。
    // @PreAuthorize がControllerに「入ってから」判定するのに対し、こちらは
    // フィルタ段階＝Controllerに到達する前に弾く。Web APIの認可はこちらが本命。
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // JSON APIはブラウザのフォーム送信ではなくCSRFトークンを持たないため無効化。
            // Cookieセッションでブラウザから直接叩く用途が出たら見直すこと。
            .csrf(csrf -> csrf.disable())
            // curlやRESTクライアントから Authorization ヘッダで認証できるようにする
            .httpBasic(withDefaults())
            .authorizeHttpRequests(auth -> auth
                //   ルールは上から順に評価され、最初に一致した1件だけが適用される。
                //   そのため .anyRequest() は必ず最後に置くこと。
                //   HttpMethod と hasRole(...) はimport済み。
                .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            .build();
    }
}
