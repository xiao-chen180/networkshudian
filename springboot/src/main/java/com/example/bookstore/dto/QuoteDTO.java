package com.example.bookstore.dto;

import java.time.LocalDateTime;

public record QuoteDTO(
        Long id,
        Long bookId,
        String content,
        String authorName,
        LocalDateTime createdAt
) {
}