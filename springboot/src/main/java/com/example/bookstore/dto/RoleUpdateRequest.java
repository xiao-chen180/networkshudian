package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequest(
        @NotBlank(message = "角色不能为空") String role
) {
}