package com.example.crm.Web.DTO;

import jakarta.validation.constraints.NotBlank;

// ログイン要求。認証は email + 生パスワードで行う（CustomUserDetailsService が email で引くため）。
public record LoginRequest(
    @NotBlank(message = "Emailは必須です。")
    String email,

    @NotBlank(message = "パスワードが未入力です。")
    String password
) {
}
