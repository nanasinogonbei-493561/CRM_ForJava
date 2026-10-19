package com.example.crm.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.crm.Service.CustomUserDetailsService;

// 将来infrastructure/securityに分類。Spring Security使用。
// パスワードハッシュの設定。道具を1個だけ用意
@Configuration
@EnableMethodSecurity   // これが無いと@PreAuthorizeはServiceに置いても動かない
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // ←strength省略=10が標準
    }

    /**
     * AuthenticationManager を Bean として公開する。
     * AuthApi がログイン時のパスワード照合に使う。Spring Security が内部で
     * 組み立てたものを取り出しているので、CustomUserDetailsService と
     * PasswordEncoder の組み合わせがそのまま使われる。
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // URL単位の認可設定。
    // @PreAuthorize がControllerに「入ってから」判定するのに対し、こちらは
    // フィルタ段階＝Controllerに到達する前に弾く。Web APIの認可はこちらが本命。
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    JwtUtil jwtUtil,
                                    CustomUserDetailsService userDetailsService) throws Exception {
        // JwtAuthenticationFilter は Bean にせずここで生成する。
        // Filter 型の Bean は Boot がサーブレットフィルタとしても自動登録するため、
        // Bean にすると Security チェーンの内と外で二重に走ってしまう。
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        return http
            // JSON APIはブラウザのフォーム送信ではなくCSRFトークンを持たないため無効化。
            // Cookieセッションでブラウザから直接叩く用途が出たら見直すこと。
            .csrf(csrf -> csrf.disable())
            // JWT は毎リクエストで自己完結するのでサーバ側にセッションを持たない。
            // STATELESS にすると JSESSIONID が発行されず、SecurityContext も
            // リクエスト間で引き継がれない＝毎回トークンの提示が必要になる。
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // httpBasic は外した。残しておくと Basic ヘッダを送るだけで認証が通ってしまい、
            // JWT が壊れていてもリクエストが成功する＝テストが素通りする。
            // 認証経路は Bearer トークン一本に絞る。
            //
            // 既定の未認証応答は Http403ForbiddenEntryPoint による 403 だが、
            // 「認証されていない」は 401、「認証済みだが権限が足りない」は 403 という
            // RFC 7235 の区別に合わせて 401 を返す。
            //       Bearer 運用なら `WWW-Authenticate: Bearer` を返すべきだが、
            //       HttpStatusEntryPoint は付けない。必要になったら独自実装に差し替える。
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                //   ルールは上から順に評価され、最初に一致した1件だけが適用される。
                //   そのため .anyRequest() は必ず最後に置くこと。
                //   HttpMethod と hasRole(...) はimport済み。

                // トークンを取りに来る入口なので、ここだけは未認証で通す必要がある。
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            // UsernamePasswordAuthenticationFilter の前に差し込む。
            // 「認証を確立するフィルタ群」より前に SecurityContext を埋めておくことで、
            // 後段の認可フィルタが認証済みとして扱えるようになる。
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
