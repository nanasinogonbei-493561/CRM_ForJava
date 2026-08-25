package com.example.crm.Web.DTO;

import java.time.LocalDateTime;

import com.example.crm.Entity.UserEntity;
import com.example.crm.Enum.Role;

public record UserResponse(
    Long id,
    String username,
    String email,
    Role role,
    boolean locked,
    LocalDateTime createdAt
) {
    public static UserResponse from(UserEntity u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.isLocked(), u.getCreatedAt());
    }
}
