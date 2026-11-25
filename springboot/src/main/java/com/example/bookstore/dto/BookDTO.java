package com.example.bookstore.dto;

import java.math.BigDecimal;

public class BookDTO {

    private Long id;
    private String name;
    private String author;
    private String publisher;
    private Integer pages;
    private BigDecimal price;
    private String isbn;
    private String summary;
    private String catalog;
    private Long categoryId;
    private String categoryName;
    private Integer stock;
    private String imageUrl;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Integer salesCount;

    public BookDTO() {}

    public BookDTO(Long id, String name, String author, String publisher, Integer pages,
                  BigDecimal price, String isbn, String summary, String catalog, Long categoryId,
                  String categoryName, Integer stock, String imageUrl) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.publisher = publisher;
        this.pages = pages;
        this.price = price;
        this.isbn = isbn;
        this.summary = summary;
        this.catalog = catalog;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.avgRating = BigDecimal.ZERO;
        this.reviewCount = 0;
        this.salesCount = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getCatalog() { return catalog; }
    public void setCatalog(String catalog) { this.catalog = catalog; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public Integer getSalesCount() { return salesCount; }
    public void setSalesCount(Integer salesCount) { this.salesCount = salesCount; }
}