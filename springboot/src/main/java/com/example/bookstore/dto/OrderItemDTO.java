package com.example.bookstore.dto;

import java.math.BigDecimal;

public class OrderItemDTO {

    private Long bookId;
    private String bookName;
    private String author;
    private Integer quantity;
    private BigDecimal price;
    private String imageUrl;

    public OrderItemDTO() {}

    public OrderItemDTO(Long bookId, String bookName, String author, Integer quantity, BigDecimal price) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}