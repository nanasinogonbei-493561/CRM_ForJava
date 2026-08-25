package com.example.crm.Web.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank
    @Size(min = 10, max = 50)
    String username,

    @NotBlank
    @Email
    String email
) {
} // role も password も含めない
