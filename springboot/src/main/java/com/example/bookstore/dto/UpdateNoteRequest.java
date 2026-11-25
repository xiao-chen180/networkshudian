package com.example.bookstore.dto;

import jakarta.validation.constraints.Size;

public record UpdateNoteRequest(
        @Size(max = 200) String title,
        @Size(max = 10000, message = "笔记内容过长") String content
) {
}