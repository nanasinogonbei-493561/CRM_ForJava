package com.example.crm.Web.DTO;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
    @NotBlank(message = "Emailは必須です。")
    String email,

    @NotBlank(message = "パスワードが未入力です。")
    String passwordHash
) {
}
