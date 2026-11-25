package com.example.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "min_amount", precision = 10, scale = 2)
    private BigDecimal minAmount = BigDecimal.ZERO;

    @Column(name = "total_num")
    private Integer totalNum = 100;

    @Column(name = "used_num")
    private Integer usedNum = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Coupon() {}

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

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public Integer getTotalNum() { return totalNum; }
    public void setTotalNum(Integer totalNum) { this.totalNum = totalNum; }

    public Integer getUsedNum() { return usedNum; }
    public void setUsedNum(Integer usedNum) { this.usedNum = usedNum; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
