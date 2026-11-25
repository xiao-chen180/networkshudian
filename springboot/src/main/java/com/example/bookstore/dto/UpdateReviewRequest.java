package com.example.bookstore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
        @Min(1) @Max(5) Integer rating,
        @Size(max = 2000, message = "评论内容过长") String content
) {
}