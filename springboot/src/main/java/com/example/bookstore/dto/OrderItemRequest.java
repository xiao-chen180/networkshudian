package com.example.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "书籍 ID 不能为空") Long bookId,
        @NotNull(message = "数量不能为空") @Min(value = 1, message = "数量必须大于 0") Integer quantity
) {
}