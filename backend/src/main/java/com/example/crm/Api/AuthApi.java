package com.example.crm.Api;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.crm.Config.JwtUtil;
import com.example.crm.Web.DTO.LoginRequest;
import com.example.crm.Web.DTO.LoginResponse;

import jakarta.validation.Valid;

/**
 * トークン発行エンドポイント。
 *
 * 発行処理を Service 層に置かず Api 層に置いているのは、JwtUtil が Config
 * パッケージにあり、Config → Service の依存が既に存在するため。Service から
 * Config を参照すると相互依存になり ArchitectureTests の
 * packagesShouldBeFreeOfCycles が落ちる。
 */
@RestController
@RequestMapping("/auth")
public class AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    AuthApi(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * email とパスワードを検証し、成功したら JWT を返す。
     *
     * 自前でハッシュ照合せず AuthenticationManager に委ねているのは、
     * パスワード照合・ロック判定・認証イベント発行(=連続失敗カウント)を
     * すべて既存の仕組みに乗せるため。
     *
     * 認証失敗時は AuthenticationException が送出され、Spring Security の
     * ExceptionTranslationFilter が捕まえて 401 に変換する。
     */
    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(token, "Bearer", jwtUtil.extractExpiration(token).toInstant());
    }
}
