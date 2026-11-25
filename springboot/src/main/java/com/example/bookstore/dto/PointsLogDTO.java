package com.example.bookstore.dto;

import java.time.LocalDateTime;

public class PointsLogDTO {

    private Long id;
    private Integer amount;
    private String type;
    private String description;
    private LocalDateTime createdAt;

    public PointsLogDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
