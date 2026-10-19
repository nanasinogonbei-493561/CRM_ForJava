package com.example.crm.Web.DTO;

import java.time.Instant;

// ログイン応答。
// tokenType を返すのは、クライアントが Authorization ヘッダを
// 「<tokenType> <token>」で組み立てられるようにするため（RFC 6750）。
// expiresAt はトークン内の exp と同じ値。クライアントが JWT をデコードせずに
// 失効時刻を知れるよう、あえて外側にも出している。
public record LoginResponse(
    String token,
    String tokenType,
    Instant expiresAt
) {
}
