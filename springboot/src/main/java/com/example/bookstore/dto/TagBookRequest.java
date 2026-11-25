package com.example.bookstore.dto;

import jakarta.validation.constraints.NotNull;

public record TagBookRequest(
        @NotNull(message = "标签 ID 不能为空") Long tagId
) {
}