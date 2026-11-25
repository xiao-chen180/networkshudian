package com.example.bookstore.dto;

import java.time.LocalDateTime;

public record BannerDTO(
        Long id,
        String title,
        String subtitle,
        Long bookId,
        String imageUrl,
        Integer sortOrder,
        String status,
        LocalDateTime createdAt
) {
}