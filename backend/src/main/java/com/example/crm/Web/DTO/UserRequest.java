package com.example.crm.Web.DTO;

import com.example.crm.Enum.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank
    @Size(min=10, max=50)
    String username,

    @NotBlank(message = "Emailは必須です。")
    String email,

    @NotNull
    Role role,

    @NotBlank(message = "パスワードが未入力です。")
    @Size(min = 8)
    String rawPassword
) {
}
