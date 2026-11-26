package com.example.bookstore.dto;

import java.math.BigDecimal;

public class CartItemDTO {

    private Long id;
    private Long bookId;
    private String bookName;
    private String author;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
    private String imageUrl;

    public CartItemDTO() {}

    public CartItemDTO(Long id, Long bookId, String bookName, String author, 
                      BigDecimal price, Integer quantity, Integer stock) {
        this.id = id;
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}