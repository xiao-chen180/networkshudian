package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "分类名称不能为空") @Size(max = 50) String name,
        @Size(max = 200) String description
) {
}