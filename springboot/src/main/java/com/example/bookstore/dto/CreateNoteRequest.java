package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNoteRequest(
        @NotNull(message = "书籍 ID 不能为空") Long bookId,
        @NotBlank(message = "标题不能为空") @Size(max = 200) String title,
        @Size(max = 10000, message = "笔记内容过长") String content,
        Boolean isPublic
) {
}