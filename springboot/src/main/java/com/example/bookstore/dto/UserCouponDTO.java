package com.example.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserCouponDTO {

    private Long id;
    private Long couponId;
    private String couponName;
    private String type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;

    public UserCouponDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }

    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
