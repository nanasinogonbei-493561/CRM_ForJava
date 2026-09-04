package com.example.crm.Config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.crm.Service.CustomUserDetailsService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 将来infrastructure/securityに分類。
 *
 * Authorization ヘッダの Bearer トークンを検証し、成功したら SecurityContext に
 * 認証情報を載せるフィルタ。
 *
 * 【@Component を付けていない理由】
 * Spring Boot は Filter 型の Bean を「サーブレットフィルタ」として自動登録する。
 * @Component を付けると、Spring Security のフィルタチェーン内と、その外側の
 * サーブレットチェーンとで二重に実行されてしまう。そのため Bean にはせず、
 * SecurityConfig が new してチェーンに差し込む形にしている。
 *
 * 【AuthenticationManager を通さない理由】
 * 通すと ProviderManager が毎リクエスト AuthenticationSuccessEvent を発行し、
 * AuthenticationEventListener 経由で resetLoginFailures() の DB 更新が走る。
 * ログイン時に一度確認済みの資格情報を毎回照合し直す意味も無いので、
 * ここでは UserDetails を読んで SecurityContext を直接組み立てる。
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    // SecurityConfig の permitAll と同じパスを、同じマッチングエンジンで判定する。
    private static final RequestMatcher LOGIN = 
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/auth/login");

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    // ロック済み・無効化済みアカウントを弾く。AuthenticationManager を通さない以上、
    // この確認は自分でやる必要がある（3回失敗ロックは locked 列に立つ）。
    private final AccountStatusUserDetailsChecker statusChecker = new AccountStatusUserDetailsChecker();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return LOGIN.matches(request); // 片方だけ書き換えて挙動がズレる事故を防ぎたいから
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER);

        // Bearer トークンが無いリクエストはこのフィルタの担当外。
        // httpBasic など他の認証方式に処理を渡すため、何もせず素通しする。
        // 既に認証済みの場合も、後から上書きしない。
        if (header == null || !header.startsWith(PREFIX)
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length());

        try {
            // 署名不正・期限切れ・構造不正はここで JwtException になる
            String email = jwtUtil.extractUsername(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            statusChecker.check(userDetails);   // ロック済みなら LockedException

            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | AuthenticationException e) {
            // トークンが提示されたが検証に失敗した場合の扱い。

            log.debug("JWT検証に失敗したため401を返します: {} - {}", e.getClass().getSimpleName(), e.getMessage());


            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
