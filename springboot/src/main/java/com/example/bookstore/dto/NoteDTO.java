package com.example.bookstore.dto;

import java.time.LocalDateTime;

public class NoteDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookName;
    private String title;
    private String content;
    private Boolean isPublic;
    private Integer likeCount;
    private LocalDateTime createdAt;

    public NoteDTO() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
