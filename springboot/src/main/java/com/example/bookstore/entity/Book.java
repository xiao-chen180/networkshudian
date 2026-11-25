package com.example.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 100)
    private String publisher;

    private Integer pages;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String catalog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private Integer stock = 0;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "avg_rating", precision = 3, scale = 1)
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "sales_count")
    private Integer salesCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Book() {}

    public Book(Long id, String name, String author, String publisher, Integer pages, 
                BigDecimal price, String isbn, String summary, String catalog, Category category, 
                Integer stock, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.publisher = publisher;
        this.pages = pages;
        this.price = price;
        this.isbn = isbn;
        this.summary = summary;
        this.catalog = catalog;
        this.category = category;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}