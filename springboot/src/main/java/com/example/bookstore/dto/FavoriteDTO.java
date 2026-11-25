package com.example.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FavoriteDTO {

    private Long id;
    private Long userId;
    private Long bookId;
    private String bookName;
    private String bookAuthor;
    private BigDecimal bookPrice;
    private String bookImageUrl;
    private String categoryName;
    private LocalDateTime createdAt;

    public FavoriteDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public BigDecimal getBookPrice() { return bookPrice; }
    public void setBookPrice(BigDecimal bookPrice) { this.bookPrice = bookPrice; }

    public String getBookImageUrl() { return bookImageUrl; }
    public void setBookImageUrl(String bookImageUrl) { this.bookImageUrl = bookImageUrl; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
