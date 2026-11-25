package com.example.bookstore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull(message = "书籍 ID 不能为空") Long bookId,
        @NotNull(message = "评分不能为空") @Min(1) @Max(5) Integer rating,
        @Size(max = 2000, message = "评论内容过长") String content
) {
}