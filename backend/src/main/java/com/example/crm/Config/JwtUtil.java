package com.example.crm.Config;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.util.function.Function;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 秘密鍵を生成（application.propertiesの値をUTF-8バイト列としてHMAC-SHA512鍵に変換）
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // トークン生成
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ユーザー名の抽出
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }

    // 有効期限の抽出
    public Date extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration());
    }

    // 汎用クレーム抽出
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // 署名検証を行いながらClaimsを取得する。不正なトークンはここで例外になる
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 有効期限チェック
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // トークン検証（署名検証 + 有効期限チェック + ユーザー名一致確認）
    // 注意: extractAllClaims内でExpiredJwtException等がスローされる場合があります。
    //       呼び出し元のJwtAuthenticationFilterでtry-catchしてください。
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}